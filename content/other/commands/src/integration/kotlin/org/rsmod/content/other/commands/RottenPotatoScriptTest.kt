package org.rsmod.content.other.commands

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.invs
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.events.interact.HeldObjEvents
import org.rsmod.api.player.input.ResumePStringDialogInput
import org.rsmod.api.player.input.ResumePauseButtonInput
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.interfaces.bank.BankTab
import org.rsmod.content.interfaces.bank.selectedTab
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.mod.UnpackedModLevelType
import org.rsmod.map.CoordGrid

class RottenPotatoScriptTest {
    @Test
    fun GameTestState.`eat opens chatbox options`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            openRottenPotato()

            assertModalOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`set all stats from level input`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            player.stats[stats.attack] = 1
            player.stats[stats.hitpoints] = 10
            openRottenPotato()

            selectRottenPotatoOption(1)
            player.resumeCountDialog(42)
            advance()

            assertEquals(42, player.statBase(stats.attack))
            assertEquals(42, player.statBase(stats.hitpoints))
        }

    @Test
    fun GameTestState.`wipe inventory sets potato in first slot and empties the rest`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            player.inv[4] = InvObj(objs.rotten_potato)
            player.inv[6] = InvObj(objs.coins, count = 100)
            player.inv[14] = InvObj(objs.beer)
            openRottenPotato(slot = 4)

            selectRottenPotatoOption(2)

            assertEquals(InvObj(objs.rotten_potato), player.inv[0])
            for (slot in 1 until player.inv.size) {
                assertNull(player.inv[slot])
            }
            assertModalNotOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`setup poh closes the interface`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            openRottenPotato()

            selectRottenPotatoOption(3)

            assertModalNotOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`teleport to player moves to same-world target`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            val targetCoords = CoordGrid(0, 50, 51, 12, 34)
            registerPlayer(targetCoords, Player().apply { displayName = "m3rx" })
            openRottenPotato()

            selectRottenPotatoOption(4)
            player.resumeActiveCoroutine(ResumePStringDialogInput("m3rx"))
            advance()

            assertEquals(targetCoords, player.coords)
        }

    @Test
    fun GameTestState.`spawn aggressive npc closes the interface`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            openRottenPotato()

            selectRottenPotatoOption(5)

            assertModalNotOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`peel opens chatbox options`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            peelRottenPotato()

            assertModalOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`peel bank menu opens nested chatbox options`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            peelRottenPotato()

            selectRottenPotatoOption(1)

            assertModalOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`peel bank menu open bank opens bank interfaces`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            peelRottenPotato()

            selectRottenPotatoOption(1)
            selectRottenPotatoOption(1)

            assertModalNotOpen(interfaces.chatmenu)
            assertTrue(player.ui.contains(interfaces.bank_main))
            assertTrue(player.ui.contains(interfaces.bank_side))
        }

    @Test
    fun GameTestState.`peel bank menu wipe bank empties bank and resets tabs`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            val bank = player.invMap.getOrPut(cacheTypes.invs[invs.bank])
            bank[0] = InvObj(objs.coins, count = 100)
            bank[1] = InvObj(objs.beer)
            player.withProtectedAccess {
                vars[BankTab.Tab1.sizeVarBit] = 1
                vars[BankTab.Main.sizeVarBit] = 1
                selectedTab = BankTab.Tab1
            }
            peelRottenPotato()

            selectRottenPotatoOption(1)
            selectRottenPotatoOption(3)

            for (slot in bank.indices) {
                assertNull(bank[slot])
            }
            for (tab in BankTab.entries) {
                assertEquals(0, player.vars[tab.sizeVarBit])
            }
            assertModalNotOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`peel skipped options close the interface`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            peelRottenPotato()

            selectRottenPotatoOption(2)

            assertModalNotOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`peel bank menu set pin closes the interface`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            peelRottenPotato()

            selectRottenPotatoOption(1)
            selectRottenPotatoOption(2)

            assertModalNotOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`mash opens chatbox options`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            mashRottenPotato()

            assertModalOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`mash keep me logged in closes the interface`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            mashRottenPotato()

            selectRottenPotatoOption(1)

            assertModalNotOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`mash kick me out forces disconnect`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            mashRottenPotato()

            player.resumeActiveCoroutine(ResumePauseButtonInput(components.chatmenu_pbutton, 2))

            assertTrue(player.forceDisconnect)
            assertModalNotOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`mash kill me takes lethal damage`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            player.stats[stats.hitpoints] = 10
            mashRottenPotato()

            selectRottenPotatoOption(3)

            assertEquals(0, player.hitpoints)
            assertModalNotOpen(interfaces.chatmenu)
        }

    @Test
    fun GameTestState.`mash transmogrify opens transmog menu`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            mashRottenPotato()

            selectRottenPotatoOption(4)

            assertModalOpen(interfaces.menu)
        }

    @Test
    fun GameTestState.`transmogrify selection sets player model`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            mashRottenPotato()

            selectRottenPotatoOption(4)
            selectTransmogOption(1)

            assertEquals("inferno_jad", player.transmog?.internalName)
            assertModalNotOpen(interfaces.menu)
        }

    @Test
    fun GameTestState.`transmogrify object selection uses unanimated object model`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            mashRottenPotato()

            selectRottenPotatoOption(4)
            selectTransmogOption(12)

            val transmog = checkNotNull(player.transmog)
            assertEquals("rotten_potato_obj_halloweenmask_red", transmog.internalName)
            assertArrayEquals(intArrayOf(2438), transmog.models)
            assertEquals(-1, transmog.readyAnim)
            assertEquals(-1, transmog.walkAnim)
            assertEquals(-1, transmog.runAnim)
            assertModalNotOpen(interfaces.menu)
        }

    @Test
    fun GameTestState.`transmogrify myself resets player model`() =
        runGameTest(RottenPotatoScript::class) {
            player.modLevel = adminModLevel()
            player.withProtectedAccess {
                player.transmog = npcTypes.values.first { it.internalName == "dwarf_normal" }
                rebuildAppearance()
            }
            mashRottenPotato()

            selectRottenPotatoOption(4)
            selectTransmogOption(0)

            assertNull(player.transmog)
            assertModalNotOpen(interfaces.menu)
        }

    private fun GameTestScope.openRottenPotato(slot: Int = 0) {
        if (player.inv[slot] == null) {
            player.inv[slot] = InvObj(objs.rotten_potato)
        }
        player.withProtectedAccess {
            publish(
                HeldObjEvents.Op1(
                    slot = slot,
                    obj = player.inv.getValue(slot),
                    type = objTypes[objs.rotten_potato],
                    inventory = player.inv,
                )
            )
        }
        advance()
    }

    private fun GameTestScope.peelRottenPotato(slot: Int = 0) {
        if (player.inv[slot] == null) {
            player.inv[slot] = InvObj(objs.rotten_potato)
        }
        player.withProtectedAccess {
            publish(
                HeldObjEvents.Op3(
                    slot = slot,
                    obj = player.inv.getValue(slot),
                    type = objTypes[objs.rotten_potato],
                    inventory = player.inv,
                )
            )
        }
        advance()
    }

    private fun GameTestScope.mashRottenPotato(slot: Int = 0) {
        if (player.inv[slot] == null) {
            player.inv[slot] = InvObj(objs.rotten_potato)
        }
        player.withProtectedAccess {
            publish(
                HeldObjEvents.Op4(
                    slot = slot,
                    obj = player.inv.getValue(slot),
                    type = objTypes[objs.rotten_potato],
                    inventory = player.inv,
                )
            )
        }
        advance()
    }

    private fun GameTestScope.selectRottenPotatoOption(option: Int) {
        player.resumeActiveCoroutine(ResumePauseButtonInput(components.chatmenu_pbutton, option))
        advance()
    }

    private fun GameTestScope.selectTransmogOption(option: Int) {
        player.resumeActiveCoroutine(ResumePauseButtonInput(components.menu_list, option))
        advance()
    }

    private fun adminModLevel(): UnpackedModLevelType =
        UnpackedModLevelType(
            clientCode = 2,
            accessFlags = 0,
            internalId = 2,
            internalName = "admin",
        )
}
