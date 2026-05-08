package org.rsmod.api.net.rsprot

import net.rsprot.protocol.api.Session
import net.rsprot.protocol.game.outgoing.info.Infos
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarExtendedInfo
import net.rsprot.protocol.game.outgoing.info.util.getOrThrow
import net.rsprot.protocol.game.outgoing.info.util.isEmpty
import net.rsprot.protocol.game.outgoing.info.util.safeReleaseOrThrow
import net.rsprot.protocol.game.outgoing.map.RebuildLoginV2
import net.rsprot.protocol.game.outgoing.map.RebuildNormalV2
import net.rsprot.protocol.game.outgoing.map.RebuildRegionV2
import net.rsprot.protocol.game.outgoing.map.util.RebuildRegionZone
import org.rsmod.api.config.refs.baseanimsets
import org.rsmod.api.config.refs.params
import org.rsmod.api.player.righthand
import org.rsmod.api.registry.region.RegionRegistry
import org.rsmod.api.utils.map.BuildAreaUtils
import org.rsmod.game.client.ClientCycle
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.util.EntityFaceAngle
import org.rsmod.game.entity.util.EntityFaceTarget
import org.rsmod.game.headbar.Headbar
import org.rsmod.game.hit.Hitmark
import org.rsmod.game.movement.MoveSpeed
import org.rsmod.game.region.Region
import org.rsmod.game.region.zone.RegionZoneCopy
import org.rsmod.game.seq.EntitySeq
import org.rsmod.game.spot.EntitySpotanim
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid
import org.rsmod.map.zone.ZoneKey

class RspCycle(
    private val session: Session<Player>,
    private val infos: Infos,
    private val objTypes: ObjTypeList,
    private val regions: RegionRegistry,
) : ClientCycle {
    private var knownCoords: CoordGrid = CoordGrid.ZERO

    private var knownBuildArea: CoordGrid = CoordGrid.NULL

    private var knownCachedSpeed: MoveSpeed = MoveSpeed.Stationary

    private var knownFaceEntity: Int? = -1

    private var knownRegionUid: Int? = null

    private var cachedRegionZoneProvider: RebuildRegionV2.RebuildRegionZoneProvider? = null

    private val playerExtendedInfo: PlayerAvatarExtendedInfo
        get() = infos.playerInfo.avatar.extendedInfo

    private val worldId: Int
        get() = 0

    fun init(player: Player) {
        player.updateCoords()
        infos.updateRootBuildAreaCenteredOnPlayer(player.x, player.z)
        player.queueRebuildLogin()
    }

    private fun Player.queueRebuildLogin() {
        val rebuild = RebuildLoginV2(x shr 3, z shr 3, worldId, infos.playerInfo)
        session.queue(rebuild)
    }

    override fun update(player: Player) {
        val teleporting = player.pendingTeleport || player.pendingTelejump || player.isTeleportMovement
        infos.npcInfo.setZoneSearchRadius(if (teleporting) NO_NPC_SEARCH_RADIUS else DEFAULT_NPC_SEARCH_RADIUS)
        player.updateMoveSpeed()
        player.updateCoords()
        player.rebuildArea()
        player.applyExactMove()
        player.applyPublicMessage()
        player.applyFacePathingEntity()
        player.applyFaceAngle()
        player.applyAnim()
        player.applySpotanims()
        player.applySay()
        player.applyHeadbars()
        player.applyHitmarks()
        player.syncAppearance(objTypes)
    }

    override fun flush(player: Player) {
        val packets = infos.getPackets()
        val root = packets.rootWorldInfoPackets
        session.queue(root.activeWorld)
        session.queue(root.worldEntityInfo.getOrThrow())
        session.queue(root.playerInfo.getOrThrow())
        if (!root.npcInfo.isEmpty()) {
            session.queue(root.npcUpdateOrigin)
            session.queue(root.npcInfo.getOrThrow())
        } else {
            root.npcInfo.safeReleaseOrThrow()
        }

        for (world in packets.activeWorlds) {
            session.queue(world.activeWorld)
            if (!world.npcInfo.isEmpty()) {
                session.queue(world.npcUpdateOrigin)
                session.queue(world.npcInfo.getOrThrow())
            } else {
                world.npcInfo.safeReleaseOrThrow()
            }
        }
        session.queue(root.activeWorld)
    }

    override fun release() {
        val packets = infos.getPackets()
        val root = packets.rootWorldInfoPackets
        root.worldEntityInfo.getOrNull()?.safeRelease()
        root.playerInfo.getOrNull()?.safeRelease()
        root.npcInfo.getOrNull()?.safeRelease()
        for (world in packets.activeWorlds) {
            world.npcInfo.getOrNull()?.safeRelease()
        }
    }

    private fun Player.updateMoveSpeed() {
        if (knownCachedSpeed != cachedMoveSpeed) {
            val extendedInfo = infos.playerInfo.avatar.extendedInfo
            extendedInfo.setMoveSpeed(cachedMoveSpeed.protocolMoveSpeed)
            knownCachedSpeed = cachedMoveSpeed
        }
        val moveSpeed = resolvePendingMoveSpeed()
        if (coords != knownCoords && (moveSpeed != cachedMoveSpeed || moveSpeed == MoveSpeed.Stationary)) {
            val extendedInfo = infos.playerInfo.avatar.extendedInfo
            extendedInfo.setTempMoveSpeed(moveSpeed.protocolTempMoveSpeed)
        }
    }

    private fun Player.resolvePendingMoveSpeed(): MoveSpeed =
        when {
            pendingTelejump -> MoveSpeed.Stationary
            pendingTeleport -> MoveSpeed.Stationary
            isTeleportMovement -> MoveSpeed.Stationary
            pendingStepCount == 1 -> MoveSpeed.Walk
            pendingStepCount == 2 -> MoveSpeed.Run
            else -> moveSpeed
        }

    private val Player.isTeleportMovement: Boolean
        get() {
            if (coords == knownCoords) {
                return false
            }
            val deltaX = kotlin.math.abs(x - knownCoords.x)
            val deltaZ = kotlin.math.abs(z - knownCoords.z)
            return level != knownCoords.level || deltaX > 2 || deltaZ > 2
        }

    private val MoveSpeed.protocolMoveSpeed: Int
        get() =
            when (this) {
                MoveSpeed.Stationary -> -1
                else -> steps
            }

    private val MoveSpeed.protocolTempMoveSpeed: Int
        get() =
            when (this) {
                MoveSpeed.Stationary -> 127
                else -> steps
            }

    private fun Player.updateCoords() {
        infos.updateRootCoord(level, x, z)
        knownCoords = coords
    }

    private fun Player.rebuildArea() {
        refreshBuildArea()
        val recalcBuildArea = knownBuildArea != buildArea && buildArea != CoordGrid.NULL
        if (recalcBuildArea) {
            val zone = ZoneKey.from(buildArea)
            infos.updateRootBuildArea(zone.x, zone.z)
        }

        if (!recalcBuildArea) {
            return
        }

        // Skip log-in rebuild as RebuildLogin is already sent.
        if (knownBuildArea == CoordGrid.NULL) {
            knownBuildArea = buildArea
            return
        }

        if (regionUid == null) {
            val rebuild = RebuildNormalV2(x shr 3, z shr 3, worldId)
            knownBuildArea = buildArea
            knownRegionUid = null
            cachedRegionZoneProvider = null
            markMapBuildPending()
            session.queue(rebuild)
            return
        }

        val region = regions[coords]

        // The player's region uid should be reassigned every cycle before calling this function,
        // as such we should expect the region to always be valid at this point.
        checkNotNull(region) { "Unexpected invalid region: uid=$regionUid, coords=$coords" }

        // TODO: When implementing `net` module properly, figure out what the best way would be to
        //  "invalidate" the `cachedRebuildRegion` if the region is somehow altered. This can
        //  happen in regions such as the Gauntlet. (If we decide to keep this as a cached value
        //  as opposed to reconstructing it every time)
        if (regionUid != knownRegionUid) {
            cachedRegionZoneProvider = createRegionZoneProvider(region)
            knownRegionUid = regionUid
        }

        val zoneProvider = cachedRegionZoneProvider ?: createRegionZoneProvider(region)
        val rebuild = RebuildRegionV2(x shr 3, z shr 3, true, zoneProvider)
        knownBuildArea = buildArea
        cachedRegionZoneProvider = zoneProvider
        markMapBuildPending()
        session.queue(rebuild)
    }

    private fun Player.refreshBuildArea() {
        if (buildArea == CoordGrid.NULL || BuildAreaUtils.requiresNewBuildArea(this)) {
            buildArea = BuildAreaUtils.calculateBuildArea(ZoneKey.from(coords))
        }
    }

    private fun Player.markMapBuildPending() {
        lastMapBuildComplete = Int.MIN_VALUE
        lastProcessedZone = ZoneKey.from(coords)
        visibleZoneKeys.clear()
    }

    private fun createRegionZoneProvider(region: Region): RebuildRegionV2.RebuildRegionZoneProvider {
        val regionZones = region.toZoneList()
        val rebuildZones =
            regionZones.associateWith { zone ->
                val copyZone = regions[zone]
                if (copyZone == RegionZoneCopy.NULL) {
                    return@associateWith null
                }
                RebuildRegionZone(
                    copyZone.normalX,
                    copyZone.normalZ,
                    copyZone.normalLevel,
                    copyZone.rotation,
                )
            }
        val zoneProvider =
            object : RebuildRegionV2.RebuildRegionZoneProvider {
                override fun provide(zoneX: Int, zoneZ: Int, level: Int): RebuildRegionZone? {
                    val zoneKey = ZoneKey(zoneX, zoneZ, level)
                    return rebuildZones[zoneKey]
                }
            }
        return zoneProvider
    }

    private fun Player.applyPublicMessage() {
        val message = publicMessage ?: return
        playerExtendedInfo.setChat(
            colour = message.colour,
            effects = message.effect,
            modicon = message.modIcon,
            autotyper = message.autoTyper,
            text = message.text,
            pattern = message.pattern,
        )
        publicMessage = null
    }

    private fun Player.applyFacePathingEntity() {
        if (knownFaceEntity != faceEntity.entitySlot) {
            playerExtendedInfo.setFaceTarget(faceEntity)
            knownFaceEntity = faceEntity.entitySlot
        }
    }

    private fun Player.applyFaceAngle() {
        if (pendingFaceAngle != EntityFaceAngle.NULL) {
            playerExtendedInfo.setFaceAngle(pendingFaceAngle.intValue)
        }
    }

    private fun Player.applyAnim() {
        when (pendingSequence) {
            EntitySeq.NULL -> return
            EntitySeq.ZERO -> playerExtendedInfo.setSequence(-1, 0)
            else -> playerExtendedInfo.setSequence(pendingSequence.id, pendingSequence.delay)
        }
    }

    private fun Player.applySpotanims() {
        if (pendingSpotanims.isEmpty) {
            return
        }
        for (packed in pendingSpotanims.longIterator()) {
            val (id, delay, height, slot) = EntitySpotanim(packed)
            playerExtendedInfo.setSpotAnim(slot, id, delay, height)
        }
    }

    private fun Player.applySay() {
        val text = pendingSay ?: return
        playerExtendedInfo.setSay(text)
    }

    private fun Player.applyExactMove() {
        val move = pendingExactMove ?: return
        playerExtendedInfo.setExactMove(
            deltaX1 = move.deltaX1,
            deltaZ1 = move.deltaZ1,
            delay1 = move.clientDelay1,
            deltaX2 = move.deltaX2,
            deltaZ2 = move.deltaZ2,
            delay2 = move.clientDelay2,
            angle = move.direction,
        )
    }

    private fun Player.applyHeadbars() {
        for (packedHeadbar in activeHeadbars.longIterator()) {
            val headbar = Headbar(packedHeadbar)
            playerExtendedInfo.addHeadBar(
                sourceIndex = if (headbar.isNoSource) -1 else headbar.sourceSlot,
                selfType = headbar.self,
                otherType = if (headbar.isPrivate) -1 else headbar.public,
                startFill = headbar.startFill,
                endFill = headbar.endFill,
                startTime = headbar.startTime,
                endTime = headbar.endTime,
            )
        }
    }

    private fun Player.applyHitmarks() {
        for (packedHitmark in activeHitmarks.longIterator()) {
            val hitmark = Hitmark(packedHitmark)
            playerExtendedInfo.addHitMark(
                sourceIndex = if (hitmark.isNoSource) -1 else hitmark.sourceSlot,
                selfType = hitmark.self,
                sourceType = hitmark.source,
                otherType = if (hitmark.isPrivate) -1 else hitmark.public,
                value = hitmark.damage,
                delay = hitmark.delay,
            )
        }
    }

    private fun Player.syncAppearance(objTypes: ObjTypeList) {
        if (!appearance.rebuild) {
            return
        }
        val info = playerExtendedInfo

        val colours = appearance.coloursSnapshot()
        for (i in colours.indices) {
            info.setColour(i, colours[i].toInt())
        }

        val identKit = appearance.identKitSnapshot()
        for (i in identKit.indices) {
            info.setIdentKit(i, identKit[i].toInt())
        }

        info.setName(displayName)
        info.setOverheadIcon(overheadIcon ?: -1)
        info.setSkullIcon(skullIcon ?: -1)
        info.setCombatLevel(combatLevel)
        info.setBodyType(appearance.bodyType)
        info.setPronoun(appearance.pronoun)
        info.setHidden(appearance.softHidden)

        info.setNameExtras(
            beforeName = appearance.namePrefix ?: "",
            afterName = appearance.nameSuffix ?: "",
            afterCombatLevel = appearance.combatLvlSuffix ?: "",
        )

        val bas = this.appearance.bas
        val weapon = this.righthand
        val transmog = this.transmog

        val readyAnim: Int
        val turnOnSpotAnim: Int
        val walkForwardAnim: Int
        val walkBackAnim: Int
        val walkLeftAnim: Int
        val walkRightAnim: Int
        val runningAnim: Int

        if (bas != null) {
            readyAnim = bas.readyAnim.id
            turnOnSpotAnim = bas.turnOnSpot.id
            walkForwardAnim = bas.walkForward.id
            walkBackAnim = bas.walkBack.id
            walkLeftAnim = bas.walkLeft.id
            walkRightAnim = bas.walkRight.id
            runningAnim = bas.running.id
        } else if (transmog != null) {
            readyAnim = transmog.readyAnim
            turnOnSpotAnim = transmog.turnBackAnim
            walkForwardAnim = transmog.walkAnim
            walkBackAnim = transmog.walkAnim
            walkLeftAnim = transmog.turnLeftAnim
            walkRightAnim = transmog.turnRightAnim
            runningAnim = transmog.runAnim
        } else if (weapon != null) {
            val type = objTypes[weapon]
            readyAnim = type.param(params.bas_readyanim).id
            turnOnSpotAnim = type.param(params.bas_turnonspot).id
            walkForwardAnim = type.param(params.bas_walk_f).id
            walkBackAnim = type.param(params.bas_walk_b).id
            walkLeftAnim = type.param(params.bas_walk_l).id
            walkRightAnim = type.param(params.bas_walk_r).id
            runningAnim = type.param(params.bas_running).id
        } else {
            val default = baseanimsets.human_default
            readyAnim = default.readyAnim.id
            turnOnSpotAnim = default.turnOnSpot.id
            walkForwardAnim = default.walkForward.id
            walkBackAnim = default.walkBack.id
            walkLeftAnim = default.walkLeft.id
            walkRightAnim = default.walkRight.id
            runningAnim = default.running.id
        }

        info.setTransmogrification(transmog?.id ?: -1)
        info.setBaseAnimationSet(
            readyAnim = readyAnim,
            turnAnim = turnOnSpotAnim,
            walkAnim = walkForwardAnim,
            walkAnimBack = walkBackAnim,
            walkAnimLeft = walkLeftAnim,
            walkAnimRight = walkRightAnim,
            runAnim = runningAnim,
        )

        for (wearpos in Wearpos.visibleWearpos) {
            val obj = worn[wearpos.slot]
            if (obj == null) {
                info.setWornObj(wearpos.slot, -1, -1, -1)
                continue
            }
            val objType = objTypes[obj]
            info.setWornObj(wearpos.slot, obj.id, objType.wearpos2, objType.wearpos3)
        }
    }

    private companion object {
        private const val DEFAULT_NPC_SEARCH_RADIUS = 3
        private const val NO_NPC_SEARCH_RADIUS = -1
    }
}

private fun PlayerAvatarExtendedInfo.setFaceTarget(target: EntityFaceTarget) {
    when {
        target.entitySlot == -1 -> resetFacing()
        target.isNpc -> setFaceNpc(target.npcSlot, instant = false, walkMode = 0, entityFallbackAngle = 0)
        target.isPlayer ->
            setFacePlayer(target.playerSlot, instant = false, walkMode = 0, entityFallbackAngle = 0)
    }
}
