package org.rsmod.content.skills.mining.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.locParam
import org.rsmod.api.config.locXpParam
import org.rsmod.api.config.objParam
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.synths
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.miningLvl
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc3
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.stats.levelmod.InvisibleLevels
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.events.UnboundEvent
import org.rsmod.game.MapClock
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.loc.UnpackedLocType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.game.type.obj.WeaponCategory
import org.rsmod.game.type.seq.SeqType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Mining
@Inject
constructor(
    private val objTypes: ObjTypeList,
    private val locRepo: LocRepository,
    private val xpMods: XpModifiers,
    private val invisibleLvls: InvisibleLevels,
    private val mapClock: MapClock,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(content.ore) { attempt(it.loc, it.type) }
        onOpLoc3(content.ore) { mine(it.loc, it.type) }
        onOpLocU(content.ore) { mine(it.loc, it.type) }
    }

    private fun ProtectedAccess.attempt(ore: BoundLocInfo, type: UnpackedLocType) {
        if (!canMine(type)) {
            return
        }

        if (actionDelay < mapClock) {
            actionDelay = mapClock + 3
            skillAnimDelay = mapClock + 3
            opLoc1(ore)
        } else {
            val pickaxe = findPickaxe(player, objTypes)
            if (pickaxe == null) {
                mes("You need a pickaxe to mine this rock.")
                mes("You do not have a pickaxe which you have the Mining level to use.")
                return
            }
            anim(objTypes[pickaxe].pickaxeMiningAnim)
            spam("You swing your pickaxe at the rock.")
            mine(ore, type)
        }
    }

    private fun ProtectedAccess.mine(ore: BoundLocInfo, type: UnpackedLocType) {
        val pickaxe = findPickaxe(player, objTypes)
        if (pickaxe == null) {
            mes("You need a pickaxe to mine this rock.")
            mes("You do not have a pickaxe which you have the Mining level to use.")
            return
        }

        if (!canMine(type)) {
            return
        }

        if (skillAnimDelay <= mapClock) {
            skillAnimDelay = mapClock + 4
            anim(objTypes[pickaxe].pickaxeMiningAnim)
        }

        var minedOre = false

        if (actionDelay < mapClock) {
            actionDelay = mapClock + 3
        } else if (actionDelay == mapClock) {
            val (low, high) = mineSuccessRates(pickaxe, objTypes)
            minedOre = statRandom(stats.mining, low, high, invisibleLvls)
        }

        if (minedOre) {
            val product = objTypes[type.oreProduct]
            val xp = type.oreXp * xpMods.get(player, stats.mining)
            spam("You manage to mine some ${product.minedName()}.")
            statAdvance(stats.mining, xp)
            invAdd(inv, product)
            locRepo.change(ore, type.oreDepletedLoc, type.oreRespawnTime)
            resetAnim()
            publish(MinedOre(player, ore, product))
            return
        }

        opLoc3(ore)
    }

    private fun ProtectedAccess.canMine(type: UnpackedLocType): Boolean {
        if (player.miningLvl < type.oreLevelReq) {
            mes("You need a Mining level of ${type.oreLevelReq} to mine this rock.")
            return false
        }

        if (inv.isFull()) {
            val product = objTypes[type.oreProduct]
            mes("Your inventory is too full to hold any more ${product.name.lowercase()}.")
            soundSynth(synths.pillory_wrong)
            return false
        }

        return true
    }

    data class MinedOre(val player: Player, val ore: BoundLocInfo, val product: ObjType) :
        UnboundEvent

    companion object {
        val UnpackedObjType.pickaxeMiningReq: Int by objParam(params.levelrequire)
        val UnpackedObjType.pickaxeMiningAnim: SeqType by objParam(params.skill_anim)

        val UnpackedLocType.oreLevelReq: Int by locParam(params.levelrequire)
        val UnpackedLocType.oreProduct: ObjType by locParam(params.skill_productitem)
        val UnpackedLocType.oreXp: Double by locXpParam(params.skill_xp)
        val UnpackedLocType.oreDepletedLoc: LocType by locParam(params.next_loc_stage)
        val UnpackedLocType.oreRespawnTime: Int by locParam(params.respawn_time)

        fun findPickaxe(player: Player, objTypes: ObjTypeList): InvObj? {
            val worn = player.wornPickaxe(objTypes)
            val carried = player.carriedPickaxe(objTypes)
            if (worn != null && carried != null) {
                if (objTypes[worn].pickaxeMiningReq >= objTypes[carried].pickaxeMiningReq) {
                    return worn
                }
                return carried
            }
            return worn ?: carried
        }

        private fun Player.wornPickaxe(objTypes: ObjTypeList): InvObj? {
            val righthand = righthand ?: return null
            return righthand.takeIf { objTypes[it].isUsablePickaxe(miningLvl) }
        }

        private fun Player.carriedPickaxe(objTypes: ObjTypeList): InvObj? {
            return inv.filterNotNull { objTypes[it].isUsablePickaxe(miningLvl) }
                .maxByOrNull { objTypes[it].pickaxeMiningReq }
        }

        private fun UnpackedObjType.isUsablePickaxe(miningLevel: Int): Boolean =
            hasParam(params.skill_anim) &&
                weaponCategory == WeaponCategory.Pickaxe.id &&
                miningLevel >= pickaxeMiningReq

        private fun UnpackedObjType.minedName(): String {
            return name.lowercase().removeSuffix(" ore")
        }

        fun mineSuccessRates(pickaxe: InvObj, objTypes: ObjTypeList): Pair<Int, Int> {
            return when (objTypes[pickaxe].internalName) {
                "bronze_pickaxe" -> 64 to 200
                "iron_pickaxe" -> 96 to 300
                "steel_pickaxe" -> 128 to 400
                "black_pickaxe" -> 144 to 450
                "mithril_pickaxe" -> 160 to 500
                "adamant_pickaxe" -> 192 to 600
                "rune_pickaxe" -> 224 to 700
                "crystal_pickaxe" -> 250 to 800
                else -> 240 to 750
            }
        }
    }
}
