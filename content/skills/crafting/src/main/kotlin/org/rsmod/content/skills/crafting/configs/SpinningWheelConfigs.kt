package org.rsmod.content.skills.crafting.configs

import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.varp.VarpReferences

typealias spinning_components = SpinningComponents

typealias spinning_interfaces = SpinningInterfaces

typealias spinning_locs = SpinningLocs

typealias spinning_objs = SpinningObjs

typealias spinning_seqs = SpinningSeqs

typealias spinning_varps = SpinningVarps

object SpinningInterfaces : InterfaceReferences() {
    val skillmulti = find("skillmulti")
}

object SpinningComponents : ComponentReferences() {
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
}

object SpinningLocs : LocReferences() {
    val viking_spinningwheel = find("viking_spinningwheel")
    val elf_village_spinning_wheel = find("elf_village_spinning_wheel")
    val spinningwheel = find("spinningwheel")
    val contact_spinning_wheel = find("contact_spinning_wheel")
    val iznot_spinning_wheel = find("iznot_spinning_wheel")
    val kr_spinningwheel = find("kr_spinningwheel")
    val murder_qip_spinning_wheel = find("murder_qip_spinning_wheel")
    val fossil_spinning_wheel_built = find("fossil_spinning_wheel_built")
    val fossil_spinning_wheel = find("fossil_spinning_wheel")
    val sw_spinningwheel_fixed = find("sw_spinningwheel_fixed")
    val sw_spinningwheel = find("sw_spinningwheel")
    val spinningwheel_quetzacali = find("spinningwheel_quetzacali")
    val spinningwheel_2 = find("spinningwheel_2")

    val all =
        listOf(
            viking_spinningwheel,
            elf_village_spinning_wheel,
            spinningwheel,
            contact_spinning_wheel,
            iznot_spinning_wheel,
            kr_spinningwheel,
            murder_qip_spinning_wheel,
            fossil_spinning_wheel_built,
            fossil_spinning_wheel,
            sw_spinningwheel_fixed,
            sw_spinningwheel,
            spinningwheel_quetzacali,
            spinningwheel_2,
        )
}

object SpinningSeqs : SeqReferences() {
    val human_spinningwheel = find("human_spinningwheel")
}

object SpinningVarps : VarpReferences() {
    val skillmulti_previousselection = find("skillmulti_previousselection")
}

object SpinningObjs : ObjReferences() {
    val wool = find("wool")
    val ball_of_wool = find("ball_of_wool")
    val viking_golden_fleece = find("viking_golden_fleece")
    val viking_golden_wool = find("viking_golden_wool")
    val flax = find("flax")
    val bow_string = find("bow_string")
    val xbows_sinew = find("xbows_sinew")
    val xbows_crossbow_string = find("xbows_crossbow_string")
    val oak_roots = find("oak_roots")
    val willow_roots = find("willow_roots")
    val maple_roots = find("maple_roots")
    val yew_roots = find("yew_roots")
    val magic_roots = find("magic_roots")
    val magic_string = find("magic_string")
    val yak_hair = find("yak_hair")
    val rope = find("rope")
}
