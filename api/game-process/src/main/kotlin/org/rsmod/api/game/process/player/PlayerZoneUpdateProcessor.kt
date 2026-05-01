package org.rsmod.api.game.process.player

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import jakarta.inject.Inject
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.zone.header.UpdateZoneFullFollows
import net.rsprot.protocol.game.outgoing.zone.header.UpdateZonePartialEnclosed
import net.rsprot.protocol.message.ZoneProt
import org.rsmod.api.registry.loc.LocRegistry
import org.rsmod.api.registry.obj.ObjRegistry
import org.rsmod.api.registry.zone.ZoneUpdateMap
import org.rsmod.api.registry.zone.ZoneUpdateTransformer
import org.rsmod.api.utils.map.BuildAreaUtils
import org.rsmod.api.utils.zone.SharedZoneEnclosedBuffers
import org.rsmod.game.entity.Player
import org.rsmod.game.loc.LocInfo
import org.rsmod.game.obj.Obj
import org.rsmod.map.CoordGrid
import org.rsmod.map.zone.ZoneKey

public class PlayerZoneUpdateProcessor
@Inject
constructor(
    private val updates: ZoneUpdateMap,
    private val locReg: LocRegistry,
    private val objReg: ObjRegistry,
    private val enclosedBuffers: SharedZoneEnclosedBuffers,
) {
    public fun computeEnclosedBuffers() {
        enclosedBuffers.computeSharedBuffers()
    }

    public fun process(player: Player) {
        player.processZoneUpdates()
    }

    public fun clearEnclosedBuffers() {
        enclosedBuffers.clear()
    }

    public fun clearPendingZoneUpdates() {
        updates.clear()
    }

    private fun Player.processZoneUpdates() {
        val currZone = ZoneKey.from(coords)
        val visibleZones = visibleZoneKeys
        val prevZone = lastProcessedZone
        val buildArea = buildArea

        if (currZone != prevZone) {
            // Compute neighbouring zones based on the player's current zone.
            val currZones =
                currZone.computeVisibleNeighbouringZones().filterWithinBuildArea(buildArea)

            // Determine the newly visible zones that were not previously visible.
            // These are zones that need to be reset and have persistent updates/entities sent.
            val newZones = IntArrayList(currZones).apply { removeAll(visibleZones) }
            processNewVisibleZones(buildArea, newZones)

            // Update the player's cached visible zone keys to reflect the current visible zones.
            refreshVisibleZoneKeys(currZones)

            // Identify zones that have been visible for more than one cycle (or one call to this
            // processor). These zones will have their transient updates sent. This prevents a newly
            // visible zone from immediately sending a transient update (e.g., an `ObjAdd` update)
            // right after a persistent entity update, which could occur if an obj is spawned on the
            // ground the same cycle the zone becomes visible to the player.
            val oldZones = IntArrayList(currZones).apply { removeAll(newZones) }
            processVisibleZoneUpdates(buildArea, oldZones)
        } else {
            // If the player hasn't moved to a new zone, process updates for currently visible
            // zones.
            processVisibleZoneUpdates(buildArea, visibleZones)
        }

        lastProcessedZone = currZone
    }

    private fun Player.processNewVisibleZones(buildArea: CoordGrid, zones: IntList) {
        for (zone in zones.intIterator()) {
            val key = ZoneKey(zone)
            val zoneBase = key.toCoords()
            sendZoneResetUpdate(buildArea, zoneBase)
            sendZonePersistentUpdates(buildArea, key, zoneBase)
        }
    }

    private fun Player.sendZoneResetUpdate(buildArea: CoordGrid, zoneBase: CoordGrid) {
        val deltaX = zoneBase.x - buildArea.x
        val deltaZ = zoneBase.z - buildArea.z
        val message = UpdateZoneFullFollows(deltaX, deltaZ, zoneBase.level)
        client.write(message)
    }

    private fun Player.sendZonePersistentUpdates(
        buildArea: CoordGrid,
        zone: ZoneKey,
        zoneBase: CoordGrid,
    ) {
        val spawnedLocs = locReg.findAllSpawned(zone)
        sendPersistentLocs(spawnedLocs)

        val spawnedObjs = objReg.findAll(zone)
        sendPersistentObjs(buildArea, zoneBase, spawnedObjs, observerUUID)
    }

    private fun Player.sendPersistentLocs(locs: Sequence<LocInfo>) {
        for (loc in locs) {
            val prot = ZoneUpdateTransformer.toPersistentLocChange(loc)
            client.write(prot)
        }
    }

    private fun Player.sendPersistentObjs(
        buildArea: CoordGrid,
        zoneBase: CoordGrid,
        objs: Sequence<Obj>,
        observerId: Long?,
    ) {
        val prots =
            objs.mapNotNull { obj -> ZoneUpdateTransformer.toPersistentObjAdd(obj, observerId) }
                .toList()
        if (prots.isNotEmpty()) {
            sendZoneEnclosedUpdates(buildArea, zoneBase, prots)
        }
    }

    private fun Player.refreshVisibleZoneKeys(zones: IntList) {
        visibleZoneKeys.clear()
        visibleZoneKeys.addAll(zones)
    }

    private fun Player.processVisibleZoneUpdates(buildArea: CoordGrid, currZones: List<Int>) {
        for (zone in currZones) {
            val zoneKey = ZoneKey(zone)
            val zoneBase = zoneKey.toCoords()
            sendPlayerSpecificEnclosedUpdates(buildArea, zoneKey, zoneBase)
            sendZoneSharedEnclosedUpdates(buildArea, zoneKey, zoneBase)
        }
    }

    private fun Player.sendPlayerSpecificEnclosedUpdates(
        buildArea: CoordGrid,
        zone: ZoneKey,
        zoneBase: CoordGrid,
    ) {
        val updates = updates[zone] ?: return
        check(updates.isNotEmpty) { "`updates` for zone should not be empty: $zone" }

        // These updates are specific to the observer, so they cannot use the shared cached buffer.
        // As of revision 237, obj zone payloads must be wrapped in partial-enclosed packets.
        val filtered =
            updates.filterIsInstance<ZoneUpdateTransformer.PartialFollowsZoneProt>().filterNot {
                it.isHidden(observerUUID)
            }
        if (filtered.isEmpty()) {
            return
        }
        val backing = filtered.map { it.backing }
        sendZoneEnclosedUpdates(buildArea, zoneBase, backing)
    }

    private fun Player.sendZoneSharedEnclosedUpdates(
        buildArea: CoordGrid,
        zone: ZoneKey,
        zoneBase: CoordGrid,
    ) {
        val enclosed = enclosedBuffers[zone] ?: return
        val buffer = enclosed[OldSchoolClientType.DESKTOP] ?: return
        val deltaX = zoneBase.x - buildArea.x
        val deltaZ = zoneBase.z - buildArea.z
        val prot = UpdateZonePartialEnclosed(deltaX, deltaZ, zoneBase.level, buffer)
        client.write(prot)
    }

    private fun Player.sendZoneEnclosedUpdates(
        buildArea: CoordGrid,
        zoneBase: CoordGrid,
        protList: Collection<ZoneProt>,
    ) {
        val buffer = enclosedBuffers.computeClientBuffer(OldSchoolClientType.DESKTOP, protList)
        val deltaX = zoneBase.x - buildArea.x
        val deltaZ = zoneBase.z - buildArea.z
        val prot = UpdateZonePartialEnclosed(deltaX, deltaZ, zoneBase.level, buffer)
        client.write(prot)
    }

    private fun ZoneProt.isHidden(observerId: Long?): Boolean =
        this is ZoneUpdateTransformer.ObjPrivateZoneProt && !isVisibleTo(observerId) ||
            this is ZoneUpdateTransformer.ObjReveal && observerId == obj.receiverId

    private fun ZoneKey.computeVisibleNeighbouringZones(): IntList {
        val zones = IntArrayList(ZONE_VIEW_TOTAL_COUNT)
        for (x in -ZONE_VIEW_RADIUS..ZONE_VIEW_RADIUS) {
            for (z in -ZONE_VIEW_RADIUS..ZONE_VIEW_RADIUS) {
                val zone = translate(x, z)
                zones.add(zone.packed)
            }
        }
        return zones
    }

    private fun IntList.filterWithinBuildArea(buildArea: CoordGrid): IntList {
        val zones = IntArrayList(size)
        forEach { zone ->
            val zoneBase = ZoneKey(zone).toCoords()
            val deltaX = zoneBase.x - buildArea.x
            val deltaZ = zoneBase.z - buildArea.z
            val viewable = deltaX in BUILD_AREA_BOUNDS && deltaZ in BUILD_AREA_BOUNDS
            if (viewable) {
                zones.add(zone)
            }
        }
        return zones
    }

    public companion object {
        public const val ZONE_VIEW_RADIUS: Int = 3
        public const val ZONE_VIEW_TOTAL_COUNT: Int =
            (2 * ZONE_VIEW_RADIUS + 1) * (2 * ZONE_VIEW_RADIUS + 1)

        public val BUILD_AREA_BOUNDS: IntRange = 0 until BuildAreaUtils.SIZE
    }
}
