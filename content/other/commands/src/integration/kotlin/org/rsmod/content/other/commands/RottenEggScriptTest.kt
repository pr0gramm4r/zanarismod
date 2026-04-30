package org.rsmod.content.other.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.events.interact.HeldObjEvents
import org.rsmod.api.player.input.ResumePauseButtonInput
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.mod.UnpackedModLevelType
import org.rsmod.map.CoordGrid

class RottenEggScriptTest {
    @Test
    fun GameTestState.`scramble opens teleport category menu`() =
        runGameTest(RottenEggScript::class) {
            player.modLevel = adminModLevel()
            scrambleRottenEgg()

            assertModalOpen(interfaces.menu)
        }

    @Test
    fun GameTestState.`scramble teleports through nested location menus`() =
        runGameTest(RottenEggScript::class) {
            player.modLevel = adminModLevel()
            val falador = CoordGrid(3000, 3360)
            allocZoneCollision(falador)
            scrambleRottenEgg()

            selectMenuOption(1) // Towns F-M
            selectMenuOption(0) // Falador

            assertEquals(falador, player.coords)
            assertModalNotOpen(interfaces.menu)
        }

    @Test
    fun GameTestState.`reward token menu closes without implementation`() =
        runGameTest(RottenEggScript::class) {
            player.modLevel = adminModLevel()
            scrambleRottenEgg()

            selectMenuOption(7) // Minigames: Reward Tokens/Variables

            assertModalNotOpen(interfaces.menu)
        }

    @Test
    fun GameTestState.`scramble is admin only`() =
        runGameTest(RottenEggScript::class) {
            player.modLevel = playerModLevel()
            scrambleRottenEgg()

            assertModalNotOpen(interfaces.menu)
        }

    @Test
    fun `teleport list stays within menu bounds`() {
        val categories = RottenEggTeleports.categories
        val categoryLabels = categories.map { it.label }
        val sections = categories.flatMap { it.sections }

        assertEquals(
            listOf(
                "Towns A-E",
                "Towns F-M",
                "Towns N-S",
                "Towns T-Z",
                "Bosses",
                "Dungeons",
                "Minigames",
                "Minigames: Reward Tokens/Variables",
                "Mining",
                "Fishing",
                "Agility",
                "Runecrafting",
                "Woodcutting",
                "Farming",
                "Hunter",
            ),
            categoryLabels,
        )
        assertEquals(562, sections.sumOf { it.locations.size })
        assertTrue(categories.size < 128)
        assertTrue(sections.all { it.locations.size < 128 })
        assertTrue(categories.all { it.closeOnly || it.sections.isNotEmpty() })
    }

    private fun GameTestScope.scrambleRottenEgg(slot: Int = 0) {
        if (player.inv[slot] == null) {
            player.inv[slot] = InvObj(objs.rotten_egg)
        }
        player.withProtectedAccess {
            publish(
                HeldObjEvents.Op1(
                    slot = slot,
                    obj = player.inv.getValue(slot),
                    type = objTypes[objs.rotten_egg],
                    inventory = player.inv,
                )
            )
        }
        advance()
    }

    private fun GameTestScope.selectMenuOption(option: Int) {
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

    private fun playerModLevel(): UnpackedModLevelType =
        UnpackedModLevelType(
            clientCode = 0,
            accessFlags = 0,
            internalId = 0,
            internalName = "player",
        )
}
