package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.baseMiningLvl
import org.rsmod.api.script.advanced.onUnimplementedOpNpc1
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class MiningTutor : PluginScript() {
    override fun ScriptContext.startup() {
        onUnimplementedOpNpc1(lumbridge_npcs.mining_tutor) { startDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc) {
        startDialogue(npc) { miningTutorDialogue() }
    }

    private suspend fun Dialogue.miningTutorDialogue() {
        when {
            player.baseMiningLvl in 25..38 -> intermediateLevelMenu()
            player.baseMiningLvl >= 39 -> highLevelMenu()
            else -> lowLevelMenu()
        }
    }

    private suspend fun Dialogue.lowLevelMenu() {
        val choice =
            choice3(
                "Can you teach me the basics of mining please?",
                1,
                "Are there any mining related quests?",
                2,
                "Tell me about different rocks and picks.",
                3,
            )
        when (choice) {
            1 -> noviceBasics()
            2 -> miningQuests()
            3 -> rocksAndPicksInquiry()
        }
    }

    private suspend fun Dialogue.intermediateLevelMenu() {
        val choice =
            choice4(
                "I already know about the basics of mining, got any tips?",
                1,
                "Are there any mining related quests?",
                2,
                "Tell me about different rocks and picks.",
                3,
                "Goodbye.",
                4,
            )
        when (choice) {
            1 -> intermediateAdvice()
            2 -> miningQuests()
            3 -> rocksAndPicksInquiry()
            4 -> goodbye()
        }
    }

    private suspend fun Dialogue.highLevelMenu() {
        val choice =
            choice4(
                "Any advice for an advanced miner?",
                1,
                "Are there any mining related quests?",
                2,
                "Tell me about different rocks and picks.",
                3,
                "Goodbye.",
                4,
            )
        when (choice) {
            1 -> advancedAdvice()
            2 -> miningQuests()
            3 -> rocksAndPicksInquiry()
            4 -> goodbye()
        }
    }

    private suspend fun Dialogue.noviceBasics() {
        chatPlayer(quiz, "Can you teach me the basics of mining please?")
        objbox(objs.mining_icon, "Look for this icon on your minimap to find mining rocks.")
        pickaxeReminder()
        chatNpc(
            neutral,
            "Rocks have different colours and shapes, so you can " +
                "usually tell what ore you will get before you swing.",
        )
        chatNpc(
            happy,
            "To mine a rock, click it while you have a pickaxe with " +
                "you. Keep mining until your inventory is full.",
        )
        chatNpc(
            happy,
            "Then take the ore to a bank. The nearest one is on the " +
                "roof of Lumbridge Castle.",
        )
        objbox(
            objs.bank_icon,
            "To find a bank, look for this symbol on your minimap " +
                "after climbing to the top of Lumbridge Castle.",
        )
        lowLevelMenu()
    }

    private suspend fun Dialogue.intermediateAdvice() {
        chatPlayer(quiz, "I already know about the basics of mining, got any tips?")
        pickaxeReminder()
        chatNpc(
            happy,
            "Different mines contain different rocks, so it is worth " +
                "exploring once copper and tin feel too familiar.",
        )
        chatNpc(
            neutral,
            "A good next stop is Al Kharid mine. Follow the path " +
                "north, pass through the gate, then continue north-east.",
        )
        chatNpc(shifty, "Keep an eye on the scorpions there. They can be a nasty surprise.")
        intermediateLevelMenu()
    }

    private suspend fun Dialogue.advancedAdvice() {
        chatPlayer(quiz, "Any advice for an advanced miner?")
        pickaxeReminder()
        chatNpc(
            happy,
            "If you can enter the Mining Guild, you should. The " +
                "entrance is down the stairs in eastern Falador.",
        )
        if (player.members) {
            chatNpc(
                happy,
                "Members may also find the coal trucks west of Seers' " +
                    "Village useful when gathering a lot of coal.",
            )
        }
        chatPlayer(happy, "Thank you, I'll remember that.")
        highLevelMenu()
    }

    private suspend fun Dialogue.miningQuests() {
        chatNpc(
            happy,
            "Doric, north of Falador, often has work for miners. " +
                "You can find him near the anvils by the road.",
        )
        if (player.members) {
            chatNpc(
                quiz,
                "You could also investigate the Dig Site east of " +
                    "Varrock. There is plenty there for a curious miner.",
            )
        }
        miningTutorDialogue()
    }

    private suspend fun Dialogue.rocksAndPicksInquiry() {
        chatPlayer(quiz, "Tell me about different rocks and picks.")
        chatNpc(happy, "Of course. What would you like to hear about?")
        rocksAndPicksMenu()
    }

    private suspend fun Dialogue.rocksAndPicksMenu() {
        val choice =
            choice3(
                "Rocks and ores.",
                1,
                "Picks.",
                2,
                "Teach me about mining.",
                3,
                title = "Mining",
            )
        when (choice) {
            1 -> rocksMenu()
            2 -> picksExplanation()
            3 -> miningTutorDialogue()
        }
    }

    private suspend fun Dialogue.rocksMenu() {
        when (
            choice4(
                "Copper, tin and iron.",
                1,
                "Clay, silver and gold.",
                2,
                "Mithril, adamantite and runite.",
                3,
                "Go back.",
                4,
                title = "Rocks",
            )
        ) {
            1 -> commonRocks()
            2 -> preciousRocks()
            3 -> rareRocks()
            4 -> rocksAndPicksMenu()
        }
    }

    private suspend fun Dialogue.commonRocks() {
        chatNpc(
            happy,
            "Copper and tin are perfect for new miners. Smelt one " +
                "of each together and you can make bronze bars.",
        )
        chatNpc(
            happy,
            "Iron is the next big step. You can mine it at places " +
                "like Al Kharid, Varrock and the Dwarven Mine.",
        )
        rocksMenu()
    }

    private suspend fun Dialogue.preciousRocks() {
        chatNpc(
            happy,
            "Clay is useful for Crafting and can be mined in many " +
                "places, including south-west of Varrock.",
        )
        chatNpc(
            neutral,
            "Silver helps with Crafting, while gold takes more Mining " +
                "experience but is valuable for Smithing and jewellery.",
        )
        rocksMenu()
    }

    private suspend fun Dialogue.rareRocks() {
        chatNpc(
            neutral,
            "Mithril and adamantite rocks are uncommon and need a " +
                "skilled miner, so you will usually find them deeper in mines.",
        )
        chatNpc(
            shifty,
            "Runite is rarer still. By the time you can mine it, you " +
                "will know your way around the world well enough to hunt it down.",
        )
        rocksMenu()
    }

    private suspend fun Dialogue.picksExplanation() {
        objbox(
            objs.bronze_pickaxe,
            "Bronze picks are easy to get. If you lose yours, talk to " +
                "me and I can replace it as long as you have space.",
        )
        doubleobjbox(
            objs.iron_pickaxe,
            objs.steel_pickaxe,
            "Better pickaxes let experienced miners work faster. " +
                "Iron and steel picks are common early upgrades.",
        )
        doubleobjbox(
            objs.mithril_pickaxe,
            objs.rune_pickaxe,
            "Mithril, adamant and rune picks require higher Mining " +
                "levels to use, but they are well worth the effort.",
        )
        rocksAndPicksMenu()
    }

    private suspend fun Dialogue.pickaxeReminder() {
        if (player.hasPickaxe()) {
            chatNpc(happy, "You already have a pickaxe, so you can mine the rocks around me.")
            return
        }
        val add = player.invAdd(player.inv, objs.bronze_pickaxe)
        if (add.success) {
            chatNpc(happy, "Here, take this bronze pickaxe so you can mine the rocks nearby.")
        } else {
            chatNpc(
                sad,
                "I'd give you a pickaxe to mine the rocks around me, " +
                    "but you don't have room in your inventory.",
            )
        }
    }

    private fun org.rsmod.game.entity.Player.hasPickaxe(): Boolean {
        val pickaxes = pickaxes()
        return pickaxes.any { obj -> inv.any { it.isType(obj) } || righthand.isType(obj) }
    }

    private fun pickaxes(): List<ObjType> =
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

    private suspend fun Dialogue.goodbye() {
        chatPlayer(neutral, "Goodbye.")
    }
}
