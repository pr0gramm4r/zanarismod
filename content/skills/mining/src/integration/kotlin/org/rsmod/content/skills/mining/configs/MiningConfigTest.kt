package org.rsmod.content.skills.mining.configs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.assertions.assertNotNullContract
import org.rsmod.content.skills.mining.scripts.Mining.Companion.mineSuccessRates
import org.rsmod.game.inv.InvObj

class MiningConfigTest {
    @Test
    fun GameTestState.`ensure all pickaxes have required params`() = runBasicGameTest {
        for (pickaxe in MiningPickaxeRefs.pickaxes) {
            val type = cacheTypes.objs[pickaxe]
            val params = type.paramMap
            assertNotNullContract(params)
            assertTrue(org.rsmod.api.config.refs.params.levelrequire in params)
            assertTrue(org.rsmod.api.config.refs.params.skill_anim in params)
        }
    }

    @Test
    fun GameTestState.`ensure all ore locs have required params`() = runBasicGameTest {
        val ores = cacheTypes.locs.values.filter { it.isContentType(content.ore) }
        assertEquals(16, ores.size)

        for (ore in ores) {
            val oreParams = ore.paramMap
            assertNotNullContract(oreParams)
            assertTrue(params.levelrequire in oreParams)
            assertTrue(params.skill_productitem in oreParams)
            assertTrue(params.skill_xp in oreParams)
            assertTrue(params.next_loc_stage in oreParams)
            assertTrue(params.respawn_time in oreParams)

            for (pickaxe in MiningPickaxeRefs.pickaxes) {
                val (lowRate, highRate) = mineSuccessRates(InvObj(pickaxe), cacheTypes.objs)
                assertTrue(lowRate > 0)
                assertTrue(highRate > 0)
            }
        }
    }
}
