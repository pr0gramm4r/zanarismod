package org.rsmod.content.interfaces.skill.guides.configs

import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences

typealias guide_interfaces = SkillGuideInterfaces

typealias guide_components = SkillGuideComponents

object SkillGuideInterfaces : InterfaceReferences() {
    val skill_guide_v2 = find("skill_guide_v2")
}

object SkillGuideComponents : ComponentReferences() {
    val attack = find("stats:attack", 4184249122120322083)
    val strength = find("stats:strength", 3279942110565409805)
    val defence = find("stats:defence", 1342833944159457821)
    val ranged = find("stats:ranged", 8814217576824147803)
    val prayer = find("stats:prayer", 1749022814565288062)
    val magic = find("stats:magic", 2910318112712455196)
    val runecraft = find("stats:runecraft", 3002893077606291851)
    val construction = find("stats:construction", 2640482247835721101)
    val hitpoints = find("stats:hitpoints", 9005298535819123910)
    val agility = find("stats:agility", 2972904928411303875)
    val herblore = find("stats:herblore", 2068597916856391597)
    val thieving = find("stats:thieving", 3379093798817134116)
    val crafting = find("stats:crafting", 2474786787262221838)
    val fletching = find("stats:fletching", 2567361752156058493)
    val slayer = find("stats:slayer", 4242018096908957632)
    val hunter = find("stats:hunter", 7312331077720947543)
    val mining = find("stats:mining", 3570174757812110223)
    val smithing = find("stats:smithing", 2665867746257197945)
    val fishing = find("stats:fishing", 8770129878041406678)
    val cooking = find("stats:cooking", 3072056616663028186)
    val firemaking = find("stats:firemaking", 9176318748447236919)
    val woodcutting = find("stats:woodcutting", 8272011736892324641)
    val farming = find("stats:farming", 4120100676970717860)
    val sailing = find("stats:sailing")

    val subsection_1 = find("skill_guide:00", 5617794397242844921)
    val subsection_2 = find("skill_guide:01", 4321920680269798703)
    val subsection_3 = find("skill_guide:02", 3026046963296752485)
    val subsection_4 = find("skill_guide:03", 1730173246323706267)
    val subsection_5 = find("skill_guide:04", 434299529350660049)
    val subsection_6 = find("skill_guide:05", 8361797849232389639)
    val subsection_7 = find("skill_guide:06", 7065924132259343421)
    val subsection_8 = find("skill_guide:07", 5770050415286297203)
    val subsection_9 = find("skill_guide:08", 4474176698313250985)
    val subsection_10 = find("skill_guide:09", 3178302981340204767)
    val subsection_11 = find("skill_guide:10", 1882429264367158549)
    val subsection_12 = find("skill_guide:11", 586555547394112331)
    val subsection_13 = find("skill_guide:12", 8514053867275841921)
    val subsection_14 = find("skill_guide:13", 7218180150302795703)
    val subsection_entry_list = find("skill_guide:icons", 1898164220621361302)
    val close_button = find("skill_guide_v2:close")
}
