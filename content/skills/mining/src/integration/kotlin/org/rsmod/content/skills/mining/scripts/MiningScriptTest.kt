package org.rsmod.content.skills.mining.scripts

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.righthand
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.mining.configs.mining_objs
import org.rsmod.content.skills.mining.scripts.Mining.Companion.oreDepletedLoc
import org.rsmod.content.skills.mining.scripts.Mining.Companion.oreLevelReq
import org.rsmod.content.skills.mining.scripts.Mining.Companion.oreProduct
import org.rsmod.content.skills.mining.scripts.Mining.Companion.oreRespawnTime
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

class MiningScriptTest {
    @Test
    fun GameTestState.`validate pickaxe requirement`() =
        runGameTest(Mining::class) {
            val type = findLocType(content.ore) { it.oreLevelReq == 1 }
            val ore = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), type)
            player.teleport(ore.coords.translateX(-1))
            player.clearInv()

            player.righthand = InvObj(objs.rune_pickaxe)
            player.stats[stats.mining] = 1
            player.opLoc1(ore)
            advance(ticks = 2)
            assertMessagesSent(
                "You need a pickaxe to mine this rock.",
                "You do not have a pickaxe which you have the Mining level to use.",
            )

            player.actionDelay = -1
            player.stats[stats.mining] = 100
            player.opLoc1(ore)
            advance(ticks = 2)
            assertMessageSent("You swing your pickaxe at the rock.")
        }

    @Test
    fun GameTestState.`mine copper or tin ore`() =
        runGameTest(Mining::class) {
            val type = findLocType(content.ore) { it.oreProduct == mining_objs.copper_ore }
            val ore = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), type)
            val product = type.oreProduct
            player.teleport(ore.coords.translateX(-1))

            player.righthand = InvObj(objs.bronze_pickaxe)
            player.stats[stats.mining] = type.oreLevelReq
            player.opLoc1(ore)
            advance(ticks = 2)
            assertMessageSent("You swing your pickaxe at the rock.")
            assertMessageNotSent("You manage to mine some copper.")
            assertDoesNotContain(player.inv, product)

            random.next = 0
            advance(ticks = 2)
            assertMessageSent("You manage to mine some copper.")
            assertContains(player.inv, product)
            assertDoesNotExist(ore)
            assertExists(ore.coords, type.oreDepletedLoc)

            advance(ticks = type.oreRespawnTime - 1)
            assertExists(ore)
            assertDoesNotExist(ore.coords, type.oreDepletedLoc)
        }
}
