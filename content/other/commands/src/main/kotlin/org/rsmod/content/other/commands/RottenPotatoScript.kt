package org.rsmod.content.other.commands

import jakarta.inject.Inject
import kotlin.math.max
import org.rsmod.annotations.InternalApi
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.modlevels
import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.forceDisconnect
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.PlayerSkillXP
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.player.stat.statSub
import org.rsmod.api.player.ui.PlayerInterfaceUpdates
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeld3
import org.rsmod.api.script.onOpHeld4
import org.rsmod.api.type.builders.npc.NpcBuilder
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.content.interfaces.bank.BankTab
import org.rsmod.content.interfaces.bank.selectedTab
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.hit.HitType
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.stat.PlayerSkillXPTable
import org.rsmod.game.type.stat.StatType
import org.rsmod.game.type.stat.StatTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

internal typealias rotten_potato_npcs = RottenPotatoNpcs

internal object RottenPotatoNpcs : NpcReferences() {
    val inferno_jad = find("inferno_jad")
    val dwarf_normal = find("dwarf_normal")
    val elvarg = find("elvarg")
    val black_knight_titan = find("black_knight_titan")
    val king_dragon = find("king_dragon")
    val wise_old_man = find("wise_old_man")
    val dwarf_city_drunken_dwarf = find("dwarf_city_drunken_dwarf")
    val dorgesh_zanik = find("dorgesh_zanik")
    val barbassault_pen_healer_lv1 = find("barbassault_pen_healer_lv1")
    val halloween_death = find("halloween_death")
    val obj_pumpkin = find("rotten_potato_obj_pumpkin")
    val obj_halloweenmask_red = find("rotten_potato_obj_halloweenmask_red")
    val obj_halloweenmask_blue = find("rotten_potato_obj_halloweenmask_blue")
    val obj_halloweenmask_green = find("rotten_potato_obj_halloweenmask_green")
}

internal object RottenPotatoObjectTransmogNpcs : NpcBuilder() {
    init {
        objectTransmog("rotten_potato_obj_pumpkin", "Pumpkin", model = 2591)
        objectTransmog("rotten_potato_obj_halloweenmask_red", "Halloween mask - red", model = 2438)
        objectTransmog(
            "rotten_potato_obj_halloweenmask_blue",
            "Halloween mask - blue",
            model = 2438,
            recolS = 926,
            recolD = -21602,
        )
        objectTransmog(
            "rotten_potato_obj_halloweenmask_green",
            "Halloween mask - green",
            model = 2438,
            recolS = 926,
            recolD = 22420,
        )
    }

    private fun objectTransmog(
        internal: String,
        name: String,
        model: Int,
        recolS: Int? = null,
        recolD: Int? = null,
    ) {
        build(internal) {
            this.name = name
            models[0] = model
            minimap = false
            active = false
            if (recolS != null && recolD != null) {
                this.recolS[0] = recolS
                this.recolD[0] = recolD
            }
        }
    }
}

class RottenPotatoScript
@Inject
constructor(private val playerList: PlayerList, private val statTypes: StatTypeList) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onOpHeld1(objs.rotten_potato) { eat(it.inventory) }
        onOpHeld3(objs.rotten_potato) { peel() }
        onOpHeld4(objs.rotten_potato) { mash() }
    }

    private suspend fun ProtectedAccess.eat(inventory: Inventory) {
        if (!player.modLevel.hasAccessTo(modlevels.admin)) {
            player.mes("Nothing interesting happens.")
            return
        }
        when (
            choice5(
                "Set all stats.",
                RottenPotatoAction.SetAllStats,
                "Wipe inventory.",
                RottenPotatoAction.WipeInventory,
                "Setup POH",
                RottenPotatoAction.SetupPoh,
                "Teleport to player.",
                RottenPotatoAction.TeleportToPlayer,
                "Spawn aggressive NPC.",
                RottenPotatoAction.SpawnAggressiveNpc,
                title = "Op1",
            )
        ) {
            RottenPotatoAction.SetAllStats -> setAllStats()
            RottenPotatoAction.WipeInventory -> wipeInventory(inventory)
            RottenPotatoAction.SetupPoh -> ifClose()
            RottenPotatoAction.TeleportToPlayer -> teleportToPlayer()
            RottenPotatoAction.SpawnAggressiveNpc -> ifClose()
        }
    }

    private suspend fun ProtectedAccess.setAllStats() {
        val level = countDialog("Enter level for all stats:")
        if (level !in STAT_LEVEL_RANGE) {
            mes("Please enter a level from 1 to 99.")
            return
        }
        player.setStatLevels(level)
        mes("All stats have been set to $level.")
    }

    private fun ProtectedAccess.wipeInventory(inventory: Inventory) {
        for (slot in inventory.indices) {
            inventory[slot] = if (slot == 0) InvObj(objs.rotten_potato) else null
        }
        ifClose()
    }

    private suspend fun ProtectedAccess.teleportToPlayer() {
        val name = stringDialog("Enter player's display name:").trim()
        if (name.isEmpty()) {
            mes("Player not found.")
            return
        }
        val target = playerList.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
        if (target == null) {
            mes("Player not found.")
            return
        }
        telejump(target.coords)
    }

    private suspend fun ProtectedAccess.peel() {
        if (!player.modLevel.hasAccessTo(modlevels.admin)) {
            player.mes("Nothing interesting happens.")
            return
        }
        when (
            choice4(
                "Bank menu",
                RottenPotatoPeelAction.BankMenu,
                "AMEs for all",
                RottenPotatoPeelAction.AmesForAll,
                "Teleport to RARE!",
                RottenPotatoPeelAction.TeleportToRare,
                "Spawn RARE!",
                RottenPotatoPeelAction.SpawnRare,
                title = "Op3",
            )
        ) {
            RottenPotatoPeelAction.BankMenu -> bankMenu()
            RottenPotatoPeelAction.AmesForAll -> ifClose()
            RottenPotatoPeelAction.TeleportToRare -> ifClose()
            RottenPotatoPeelAction.SpawnRare -> ifClose()
        }
    }

    private suspend fun ProtectedAccess.mash() {
        if (!player.modLevel.hasAccessTo(modlevels.admin)) {
            player.mes("Nothing interesting happens.")
            return
        }
        when (
            choice4(
                "Keep me logged in.",
                RottenPotatoMashAction.KeepLoggedIn,
                "Kick me out.",
                RottenPotatoMashAction.KickMeOut,
                "Kill me.",
                RottenPotatoMashAction.KillMe,
                "Transmogrify me...",
                RottenPotatoMashAction.Transmogrify,
                title = "Op4",
            )
        ) {
            RottenPotatoMashAction.KeepLoggedIn -> ifClose()
            RottenPotatoMashAction.KickMeOut -> kickOut()
            RottenPotatoMashAction.KillMe -> killPlayer()
            RottenPotatoMashAction.Transmogrify -> transmogrify()
        }
    }

    private fun ProtectedAccess.kickOut() {
        ifClose()
        player.forceDisconnect()
    }

    private fun ProtectedAccess.killPlayer() {
        ifClose()
        if (player.hitpoints <= 0) {
            queueDeath()
            return
        }
        takeInstantHit(HitType.Typeless, player.hitpoints)
    }

    private suspend fun ProtectedAccess.transmogrify() {
        val selected =
            menu(
                "Transmogrify me into...",
                hotkeys = false,
                choices = TRANSMOGRIFY_OPTIONS.map { it.label },
            )
        val option = TRANSMOGRIFY_OPTIONS.getOrNull(selected) ?: return
        if (option.npc == null) {
            resetTransmog()
        } else {
            transmog(option.npc)
        }
        rebuildAppearance()
        ifClose()
    }

    private suspend fun ProtectedAccess.bankMenu() {
        when (
            choice3(
                "Open bank.",
                RottenPotatoBankAction.OpenBank,
                "Set PIN to 2468.",
                RottenPotatoBankAction.SetPin,
                "Wipe bank.",
                RottenPotatoBankAction.WipeBank,
                title = "Op3",
            )
        ) {
            RottenPotatoBankAction.OpenBank -> openBank()
            RottenPotatoBankAction.SetPin -> ifClose()
            RottenPotatoBankAction.WipeBank -> wipeBank()
        }
    }

    private fun ProtectedAccess.openBank() {
        ifClose()
        ifOpenMainSidePair(main = interfaces.bank_main, side = interfaces.bank_side)
    }

    private fun ProtectedAccess.wipeBank() {
        for (slot in bank.indices) {
            bank[slot] = null
        }
        for (tab in BankTab.entries) {
            vars[tab.sizeVarBit] = 0
        }
        selectedTab = BankTab.Main
        ifClose()
    }

    @OptIn(InternalApi::class)
    private fun Player.setStatLevels(level: Int) {
        val xp = PlayerSkillXPTable.getXPFromLevel(level)
        for (stat in statTypes.values) {
            val baseLevel = statMap.getBaseLevel(stat)
            val targetLevel = max(stat.minLevel, level)
            if (baseLevel > targetLevel) {
                statRevert(stat, targetLevel, xp)
                continue
            }
            val xpDelta = xp - statMap.getXP(stat)
            statMap.setCurrentLevel(stat, targetLevel.toByte())
            statAdvance(stat, xpDelta.toDouble(), rate = 1.0)
        }
    }

    @OptIn(InternalApi::class)
    private fun Player.statRevert(stat: StatType, targetLevel: Int, targetXp: Int) {
        statMap.setCurrentLevel(stat, statMap.getBaseLevel(stat))
        val levelDelta = stat(stat) - targetLevel
        require(levelDelta > 0) { "This function can only be used to reduce stat levels." }
        statMap.setXP(stat, targetXp)
        statMap.setBaseLevel(stat, targetLevel.toByte())
        statSub(stat, constant = levelDelta, percent = 0)
        appearance.combatLevel = PlayerSkillXP.calculateCombatLevel(this)
        PlayerInterfaceUpdates.updateCombatLevel(this)
    }

    private enum class RottenPotatoAction {
        SetAllStats,
        WipeInventory,
        SetupPoh,
        TeleportToPlayer,
        SpawnAggressiveNpc,
    }

    private enum class RottenPotatoPeelAction {
        BankMenu,
        AmesForAll,
        TeleportToRare,
        SpawnRare,
    }

    private enum class RottenPotatoBankAction {
        OpenBank,
        SetPin,
        WipeBank,
    }

    private enum class RottenPotatoMashAction {
        KeepLoggedIn,
        KickMeOut,
        KillMe,
        Transmogrify,
    }

    private data class RottenPotatoTransmogOption(val label: String, val npc: NpcType?)

    private companion object {
        private val STAT_LEVEL_RANGE = 1..99
        private val TRANSMOGRIFY_OPTIONS =
            listOf(
                RottenPotatoTransmogOption("~ Myself ~", null),
                RottenPotatoTransmogOption("TzTok-Jad", rotten_potato_npcs.inferno_jad),
                RottenPotatoTransmogOption("Dwarf", rotten_potato_npcs.dwarf_normal),
                RottenPotatoTransmogOption("Elvarg", rotten_potato_npcs.elvarg),
                RottenPotatoTransmogOption(
                    "Black Knight Titan",
                    rotten_potato_npcs.black_knight_titan,
                ),
                RottenPotatoTransmogOption("King Black Dragon", rotten_potato_npcs.king_dragon),
                RottenPotatoTransmogOption("Wise Old Man", rotten_potato_npcs.wise_old_man),
                RottenPotatoTransmogOption(
                    "Drunken Dwarf",
                    rotten_potato_npcs.dwarf_city_drunken_dwarf,
                ),
                RottenPotatoTransmogOption("Zanik", rotten_potato_npcs.dorgesh_zanik),
                RottenPotatoTransmogOption(
                    "Penance healer",
                    rotten_potato_npcs.barbassault_pen_healer_lv1,
                ),
                RottenPotatoTransmogOption("Scythe", rotten_potato_npcs.halloween_death),
                RottenPotatoTransmogOption("Pumpkin", rotten_potato_npcs.obj_pumpkin),
                RottenPotatoTransmogOption(
                    "Halloween mask - red",
                    rotten_potato_npcs.obj_halloweenmask_red,
                ),
                RottenPotatoTransmogOption(
                    "Halloween mask - blue",
                    rotten_potato_npcs.obj_halloweenmask_blue,
                ),
                RottenPotatoTransmogOption(
                    "Halloween mask - green",
                    rotten_potato_npcs.obj_halloweenmask_green,
                ),
            )
    }
}
