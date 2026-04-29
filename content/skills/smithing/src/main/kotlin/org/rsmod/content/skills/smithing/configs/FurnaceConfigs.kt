package org.rsmod.content.skills.smithing.configs

import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.varp.VarpReferences

typealias furnace_components = FurnaceComponents

typealias furnace_interfaces = FurnaceInterfaces

typealias furnace_locs = FurnaceLocs

typealias furnace_objs = FurnaceObjs

typealias furnace_seqs = FurnaceSeqs

typealias furnace_varps = FurnaceVarps

object FurnaceInterfaces : InterfaceReferences() {
    val skillmulti = find("skillmulti")
    val crafting_gold = find("crafting_gold")
    val silver_crafting = find("silver_crafting")
}

object FurnaceComponents : ComponentReferences() {
    val skillmulti_title = find("skillmulti:title")
    val skillmulti_text = find("skillmulti:text")
    val skillmulti_instructions = find("skillmulti:instructions")
    val skillmulti_a = find("skillmulti:a")
    val skillmulti_b = find("skillmulti:b")
    val skillmulti_c = find("skillmulti:c")
    val skillmulti_d = find("skillmulti:d")
    val skillmulti_e = find("skillmulti:e")
    val skillmulti_f = find("skillmulti:f")
    val skillmulti_g = find("skillmulti:g")
    val skillmulti_h = find("skillmulti:h")
    val skillmulti_i = find("skillmulti:i")
    val skillmulti_j = find("skillmulti:j")

    val gold_ring = find("crafting_gold:gold_ring")
    val sapphire_ring = find("crafting_gold:sapphire_ring")
    val emerald_ring = find("crafting_gold:emerald_ring")
    val ruby_ring = find("crafting_gold:ruby_ring")
    val diamond_ring = find("crafting_gold:diamond_ring")
    val dragonstone_ring = find("crafting_gold:dragon_ring")
    val onyx_ring = find("crafting_gold:onyx_ring")
    val zenyte_ring = find("crafting_gold:zenyte_ring")

    val gold_necklace = find("crafting_gold:gold_necklace")
    val sapphire_necklace = find("crafting_gold:sapphire_necklace")
    val emerald_necklace = find("crafting_gold:emerald_necklace")
    val ruby_necklace = find("crafting_gold:ruby_necklace")
    val diamond_necklace = find("crafting_gold:diamond_necklace")
    val dragonstone_necklace = find("crafting_gold:dragon_necklace")
    val onyx_necklace = find("crafting_gold:onyx_necklace")
    val zenyte_necklace = find("crafting_gold:zenyte_necklace")

    val gold_amulet = find("crafting_gold:gold_amulet")
    val sapphire_amulet = find("crafting_gold:sapphire_amulet")
    val emerald_amulet = find("crafting_gold:emerald_amulet")
    val ruby_amulet = find("crafting_gold:ruby_amulet")
    val diamond_amulet = find("crafting_gold:diamond_amulet")
    val dragonstone_amulet = find("crafting_gold:dragon_amulet")
    val onyx_amulet = find("crafting_gold:onyx_amulet")
    val zenyte_amulet = find("crafting_gold:zenyte_amulet")

    val gold_bracelet = find("crafting_gold:gold_bracelet")
    val sapphire_bracelet = find("crafting_gold:sapphire_bracelet")
    val emerald_bracelet = find("crafting_gold:emerald_bracelet")
    val ruby_bracelet = find("crafting_gold:ruby_bracelet")
    val diamond_bracelet = find("crafting_gold:diamond_bracelet")
    val dragonstone_bracelet = find("crafting_gold:dragon_bracelet")
    val onyx_bracelet = find("crafting_gold:onyx_bracelet")
    val zenyte_bracelet = find("crafting_gold:zenyte_bracelet")

    val crafting_gold_make_1 = find("crafting_gold:make_1")
    val crafting_gold_make_5 = find("crafting_gold:make_5")
    val crafting_gold_make_10 = find("crafting_gold:make_10")
    val crafting_gold_make_x = find("crafting_gold:make_x")
    val crafting_gold_make_all = find("crafting_gold:make_all")

    val silver_opal_ring = find("silver_crafting:opal_ring")
    val silver_jade_ring = find("silver_crafting:jade_ring")
    val silver_topaz_ring = find("silver_crafting:topaz_ring")
    val silver_opal_necklace = find("silver_crafting:opal_necklace")
    val silver_jade_necklace = find("silver_crafting:jade_necklace")
    val silver_topaz_necklace = find("silver_crafting:topaz_necklace")
    val silver_opal_amulet = find("silver_crafting:opal_amulet")
    val silver_jade_amulet = find("silver_crafting:jade_amulet")
    val silver_topaz_amulet = find("silver_crafting:topaz_amulet")
    val silver_opal_bracelet = find("silver_crafting:opal_bracelet")
    val silver_jade_bracelet = find("silver_crafting:jade_bracelet")
    val silver_topaz_bracelet = find("silver_crafting:topaz_bracelet")
    val holy_symbol = find("silver_crafting:holy_symbol")
    val unholy_symbol = find("silver_crafting:unholy_symbol")
    val silver_sickle = find("silver_crafting:sickle")
    val silver_crossbow_bolt = find("silver_crafting:crossbow_bolt")
    val tiara = find("silver_crafting:tiara")
    val agrith_sigil = find("silver_crafting:agrith_sigil")

    val silver_crafting_make_1 = find("silver_crafting:make_1")
    val silver_crafting_make_5 = find("silver_crafting:make_5")
    val silver_crafting_make_10 = find("silver_crafting:make_10")
    val silver_crafting_make_x = find("silver_crafting:make_x")
    val silver_crafting_make_all = find("silver_crafting:make_all")
}

object FurnaceLocs : LocReferences() {
    val furnace = find("furnace")
    val furnace2 = find("furnace2")
    val furnace3 = find("furnace3")
    val newbiefurnace = find("newbiefurnace")
    val tut2_furnace = find("tut2_furnace")
    val varrock_diary_furnace = find("varrock_diary_furnace")
    val fai_falador_furnace = find("fai_falador_furnace")
    val fai_varrock_bank_gold_bars = find("fai_varrock_bank_gold_bars")
    val fairy_furnace = find("fairy_furnace")
    val dwarf_keldagrim_furnace = find("dwarf_keldagrim_furnace")
    val wilderness_resource_furnace = find("wilderness_resource_furnace")
    val prif_furnace = find("prif_furnace")
    val cam_torum_furnace = find("cam_torum_furnace")
    val brimstone_furnace = find("brimstone_furnace")
    val br_furnace = find("br_furnace")
    val gim_furnace = find("gim_furnace")
    val bcs_furnace = find("bcs_furnace")
    val viking_furnace = find("viking_furnace")
    val viking_furnace2 = find("viking_furnace2")

    val all =
        listOf(
            furnace,
            furnace2,
            furnace3,
            newbiefurnace,
            tut2_furnace,
            varrock_diary_furnace,
            fai_falador_furnace,
            fai_varrock_bank_gold_bars,
            fairy_furnace,
            dwarf_keldagrim_furnace,
            wilderness_resource_furnace,
            prif_furnace,
            cam_torum_furnace,
            brimstone_furnace,
            br_furnace,
            gim_furnace,
            bcs_furnace,
            viking_furnace,
            viking_furnace2,
        )
}

object FurnaceSeqs : SeqReferences() {
    val human_furnace = find("human_furnace")
}

object FurnaceVarps : VarpReferences() {
    val skillmulti_previousselection = find("skillmulti_previousselection")
    val makexcrafting = find("makexcrafting")
}

object FurnaceObjs : ObjReferences() {
    val copper_ore = find("copper_ore")
    val tin_ore = find("tin_ore")
    val iron_ore = find("iron_ore")
    val blurite_ore = find("blurite_ore")
    val silver_ore = find("silver_ore")
    val coal = find("coal")
    val gold_ore = find("gold_ore")
    val mithril_ore = find("mithril_ore")
    val adamantite_ore = find("adamantite_ore")
    val runite_ore = find("runite_ore")

    val bronze_bar = find("bronze_bar")
    val iron_bar = find("iron_bar")
    val blurite_bar = find("blurite_bar")
    val silver_bar = find("silver_bar")
    val steel_bar = find("steel_bar")
    val gold_bar = find("gold_bar")
    val mithril_bar = find("mithril_bar")
    val adamantite_bar = find("adamantite_bar")
    val runite_bar = find("runite_bar")

    val ring_mould = find("ring_mould")
    val necklace_mould = find("necklace_mould")
    val amulet_mould = find("amulet_mould")
    val bracelet_mould = find("jewl_bracelet_mould")
    val holy_symbol_mould = find("holy_symbol_mould")
    val unholy_symbol_mould = find("unholy_symbol_mould")
    val sickle_mould = find("sickle_mould")
    val tiara_mould = find("tiara_mould")
    val silver_bolt_mould = find("xbows_silver_bolt_mould")
    val agrith_sigil_mould = find("agrith_sigil_mould")

    val opal = find("opal")
    val jade = find("jade")
    val red_topaz = find("red_topaz")
    val sapphire = find("sapphire")
    val emerald = find("emerald")
    val ruby = find("ruby")
    val diamond = find("diamond")
    val dragonstone = find("dragonstone")
    val onyx = find("onyx")
    val zenyte = find("zenyte")

    val gold_ring = find("gold_ring")
    val sapphire_ring = find("sapphire_ring")
    val emerald_ring = find("emerald_ring")
    val ruby_ring = find("ruby_ring")
    val diamond_ring = find("diamond_ring")
    val dragonstone_ring = find("dragonstone_ring")
    val onyx_ring = find("onyx_ring")
    val zenyte_ring = find("zenyte_ring")

    val gold_necklace = find("gold_necklace")
    val sapphire_necklace = find("sapphire_necklace")
    val emerald_necklace = find("emerald_necklace")
    val ruby_necklace = find("ruby_necklace")
    val diamond_necklace = find("diamond_necklace")
    val dragonstone_necklace = find("dragonstone_necklace")
    val onyx_necklace = find("onyx_necklace")
    val zenyte_necklace = find("zenyte_necklace")

    val gold_amulet = find("unstrung_gold_amulet")
    val sapphire_amulet = find("unstrung_sapphire_amulet")
    val emerald_amulet = find("unstrung_emerald_amulet")
    val ruby_amulet = find("unstrung_ruby_amulet")
    val diamond_amulet = find("unstrung_diamond_amulet")
    val dragonstone_amulet = find("unstrung_dragonstone_amulet")
    val onyx_amulet = find("unstrung_onyx_amulet")
    val zenyte_amulet = find("unstrung_zenyte_amulet")

    val gold_bracelet = find("jewl_gold_bracelet")
    val sapphire_bracelet = find("jewl_sapphire_bracelet")
    val emerald_bracelet = find("jewl_emerald_bracelet")
    val ruby_bracelet = find("jewl_ruby_bracelet")
    val diamond_bracelet = find("jewl_diamond_bracelet")
    val dragonstone_bracelet = find("jewl_dragonstone_bracelet")
    val onyx_bracelet = find("jewl_onyx_bracelet")
    val zenyte_bracelet = find("zenyte_bracelet")

    val opal_ring = find("opal_ring")
    val jade_ring = find("jade_ring")
    val topaz_ring = find("topaz_ring")
    val opal_necklace = find("opal_necklace")
    val jade_necklace = find("jade_necklace")
    val topaz_necklace = find("topaz_necklace")
    val opal_amulet = find("unstrung_opal_amulet")
    val jade_amulet = find("unstrung_jade_amulet")
    val topaz_amulet = find("unstrung_topaz_amulet")
    val opal_bracelet = find("opal_bracelet")
    val jade_bracelet = find("jade_bracelet")
    val topaz_bracelet = find("topaz_bracelet")
    val holy_symbol = find("ics_little_holy_symbol")
    val unholy_symbol = find("ics_little_unholy_symbol")
    val silver_sickle = find("silver_sickle")
    val silver_crossbow_bolts = find("xbows_crossbow_bolts_silver")
    val tiara = find("tiara")
    val agrith_sigil = find("agrith_sigil")
}
