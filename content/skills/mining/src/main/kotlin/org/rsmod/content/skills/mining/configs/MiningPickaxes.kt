package org.rsmod.content.skills.mining.configs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.type.editors.obj.ObjEditor
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.seq.SeqType

private typealias mining_seqs = MiningSeqs

object MiningPickaxes : ObjEditor() {
    init {
        pickaxe(objs.bronze_pickaxe, level = 1, mining_seqs.human_mining_bronze_pickaxe)
        pickaxe(objs.iron_pickaxe, level = 1, mining_seqs.human_mining_iron_pickaxe)
        pickaxe(objs.steel_pickaxe, level = 6, mining_seqs.human_mining_steel_pickaxe)
        pickaxe(objs.black_pickaxe, level = 11, mining_seqs.human_mining_black_pickaxe)
        pickaxe(objs.mithril_pickaxe, level = 21, mining_seqs.human_mining_mithril_pickaxe)
        pickaxe(objs.adamant_pickaxe, level = 31, mining_seqs.human_mining_adamant_pickaxe)
        pickaxe(objs.rune_pickaxe, level = 41, mining_seqs.human_mining_rune_pickaxe)
        pickaxe(objs.dragon_pickaxe, level = 61, mining_seqs.human_mining_dragon_pickaxe)
        pickaxe(objs.dragon_pickaxe_upgraded, level = 61, mining_seqs.human_mining_dragon_pickaxe_pretty)
        pickaxe(
            objs.dragon_pickaxe_or_trailblazer,
            level = 61,
            mining_seqs.human_mining_trailblazer_pickaxe_no_infernal,
        )
        pickaxe(objs.dragon_pickaxe_or_zalcano, level = 61, mining_seqs.human_mining_zalcano_pickaxe)
        pickaxe(objs.infernal_pickaxe, level = 61, mining_seqs.human_mining_infernal_pickaxe)
        pickaxe(objs.infernal_pickaxe_uncharged, level = 61, mining_seqs.human_mining_infernal_pickaxe)
        pickaxe(objs.infernal_pickaxe_or, level = 61, mining_seqs.human_mining_trailblazer_pickaxe)
        pickaxe(
            objs.infernal_pickaxe_or_uncharged,
            level = 61,
            mining_seqs.human_mining_trailblazer_pickaxe,
        )
        pickaxe(objs.third_age_pickaxe, level = 61, mining_seqs.human_mining_3a_pickaxe)
        pickaxe(objs.crystal_pickaxe, level = 71, mining_seqs.human_mining_crystal_pickaxe)
    }

    private fun pickaxe(type: ObjType, level: Int, anim: SeqType) {
        edit(type) {
            param[params.levelrequire] = level
            param[params.skill_anim] = anim
        }
    }
}

object MiningPickaxeRefs {
    val pickaxes: List<ObjType> =
        listOf(
            objs.bronze_pickaxe,
            objs.iron_pickaxe,
            objs.steel_pickaxe,
            objs.black_pickaxe,
            objs.mithril_pickaxe,
            objs.adamant_pickaxe,
            objs.rune_pickaxe,
            objs.dragon_pickaxe,
            objs.dragon_pickaxe_upgraded,
            objs.dragon_pickaxe_or_trailblazer,
            objs.dragon_pickaxe_or_zalcano,
            objs.infernal_pickaxe,
            objs.infernal_pickaxe_uncharged,
            objs.infernal_pickaxe_or,
            objs.infernal_pickaxe_or_uncharged,
            objs.third_age_pickaxe,
            objs.crystal_pickaxe,
        )
}

internal object MiningSeqs : SeqReferences() {
    val human_mining_bronze_pickaxe = find("human_mining_bronze_pickaxe")
    val human_mining_iron_pickaxe = find("human_mining_iron_pickaxe")
    val human_mining_steel_pickaxe = find("human_mining_steel_pickaxe")
    val human_mining_black_pickaxe = find("human_mining_black_pickaxe")
    val human_mining_mithril_pickaxe = find("human_mining_mithril_pickaxe")
    val human_mining_adamant_pickaxe = find("human_mining_adamant_pickaxe")
    val human_mining_rune_pickaxe = find("human_mining_rune_pickaxe")
    val human_mining_dragon_pickaxe = find("human_mining_dragon_pickaxe")
    val human_mining_dragon_pickaxe_pretty = find("human_mining_dragon_pickaxe_pretty")
    val human_mining_trailblazer_pickaxe_no_infernal =
        find("human_mining_trailblazer_pickaxe_no_infernal")
    val human_mining_zalcano_pickaxe = find("human_mining_zalcano_pickaxe")
    val human_mining_infernal_pickaxe = find("human_mining_infernal_pickaxe")
    val human_mining_trailblazer_pickaxe = find("human_mining_trailblazer_pickaxe")
    val human_mining_3a_pickaxe = find("human_mining_3a_pickaxe")
    val human_mining_crystal_pickaxe = find("human_mining_crystal_pickaxe")
}
