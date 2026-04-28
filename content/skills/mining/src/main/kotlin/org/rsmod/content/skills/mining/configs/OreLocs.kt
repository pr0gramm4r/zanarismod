package org.rsmod.content.skills.mining.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjType

private typealias ores = OreLocs
typealias mining_objs = MiningObjs
private typealias depleted = DepletedOreLocs

internal object MiningOres : LocEditor() {
    init {
        copper(ores.copperrock1, depleted.rocks1)
        copper(ores.copperrock2, depleted.rocks2)
        copper(ores.newbiecopperrock, depleted.newbierocks1)
        copper(ores.tut2_copperrock, depleted.newbierocks1)
        copper(ores.lotr_mine_wall_copper_mid, depleted.lotr_mine_wall_depleted_mid)
        copper(ores.lotr_mine_wall_copper_l, depleted.lotr_mine_wall_depleted_l)
        copper(ores.lotr_mine_wall_copper_r, depleted.lotr_mine_wall_depleted_r)
        copper(ores.lotr_mine_wall_copper_single, depleted.lotr_mine_wall_depleted_single)

        tin(ores.tinrock1, depleted.rocks1)
        tin(ores.tinrock2, depleted.rocks2)
        tin(ores.newbietinrock, depleted.newbierocks1)
        tin(ores.tut2_tinrock, depleted.newbierocks1)
        tin(ores.lotr_mine_wall_tin_mid, depleted.lotr_mine_wall_depleted_mid)
        tin(ores.lotr_mine_wall_tin_l, depleted.lotr_mine_wall_depleted_l)
        tin(ores.lotr_mine_wall_tin_r, depleted.lotr_mine_wall_depleted_r)
        tin(ores.lotr_mine_wall_tin_single, depleted.lotr_mine_wall_depleted_single)
    }

    private fun copper(type: LocType, depleted: LocType) {
        ore(type, depleted, mining_objs.copper_ore)
    }

    private fun tin(type: LocType, depleted: LocType) {
        ore(type, depleted, mining_objs.tin_ore)
    }

    private fun ore(type: LocType, depleted: LocType, product: ObjType) {
        edit(type) {
            contentGroup = content.ore
            param[params.levelrequire] = 1
            param[params.skill_xp] = PlayerStatMap.toFineXP(17.5).toInt()
            param[params.skill_productitem] = product
            param[params.next_loc_stage] = depleted
            param[params.respawn_time] = 4
        }
    }
}

object MiningObjs : ObjReferences() {
    val copper_ore = find("copper_ore")
    val tin_ore = find("tin_ore")
}

internal object OreLocs : LocReferences() {
    val copperrock1 = find("copperrock1")
    val copperrock2 = find("copperrock2")
    val newbiecopperrock = find("newbiecopperrock")
    val tut2_copperrock = find("tut2_copperrock")
    val lotr_mine_wall_copper_mid = find("lotr_mine_wall_copper_mid")
    val lotr_mine_wall_copper_l = find("lotr_mine_wall_copper_l")
    val lotr_mine_wall_copper_r = find("lotr_mine_wall_copper_r")
    val lotr_mine_wall_copper_single = find("lotr_mine_wall_copper_single")

    val tinrock1 = find("tinrock1")
    val tinrock2 = find("tinrock2")
    val newbietinrock = find("newbietinrock")
    val tut2_tinrock = find("tut2_tinrock")
    val lotr_mine_wall_tin_mid = find("lotr_mine_wall_tin_mid")
    val lotr_mine_wall_tin_l = find("lotr_mine_wall_tin_l")
    val lotr_mine_wall_tin_r = find("lotr_mine_wall_tin_r")
    val lotr_mine_wall_tin_single = find("lotr_mine_wall_tin_single")
}

internal object DepletedOreLocs : LocReferences() {
    val rocks1 = find("rocks1")
    val rocks2 = find("rocks2")
    val newbierocks1 = find("newbierocks1")
    val lotr_mine_wall_depleted_mid = find("lotr_mine_wall_depleted_mid")
    val lotr_mine_wall_depleted_l = find("lotr_mine_wall_depleted_l")
    val lotr_mine_wall_depleted_r = find("lotr_mine_wall_depleted_r")
    val lotr_mine_wall_depleted_single = find("lotr_mine_wall_depleted_single")
}
