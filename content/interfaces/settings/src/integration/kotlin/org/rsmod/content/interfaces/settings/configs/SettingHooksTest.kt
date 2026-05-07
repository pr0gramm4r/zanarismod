package org.rsmod.content.interfaces.settings.configs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.testing.GameTestState

class SettingHooksTest {
    @Test
    fun GameTestState.`toggle server-backed setting`() = runBasicGameTest {
        withPlayer {
            val initial = vars[varbits.option_hide_rooftops]

            assertTrue(setting_hooks.toggle(this, settingId = 7))

            assertEquals(1 - initial, vars[varbits.option_hide_rooftops])
        }
    }

    @Test
    fun GameTestState.`set clamped number setting`() = runBasicGameTest {
        withPlayer {
            assertTrue(setting_hooks.setNumber(this, settingId = 280, value = 1))

            assertEquals(10, vars[varbits.settings_hitsplat_threshold])
        }
    }

    @Test
    fun GameTestState.`set dropdown-backed setting`() = runBasicGameTest {
        withPlayer {
            assertTrue(setting_hooks.setDropdown(this, settingId = 477, value = 1))

            assertEquals(1, vars[varps.musicplay])
        }
    }

    @Test
    fun GameTestState.`number setting can enable paired toggle`() = runBasicGameTest {
        withPlayer {
            assertTrue(setting_hooks.setToggle(this, settingId = 42, enabled = false))
            assertTrue(setting_hooks.setNumber(this, settingId = 43, value = 32))

            assertEquals(1, vars[varbits.option_dropwarning_on])
            assertEquals(32, vars[varbits.option_dropwarning_value])
        }
    }

    @Test
    fun `resolve rsprox settings click mapping`() {
        assertEquals(7, setting_hooks.findSettingId(category = 4, sub = 4))
        assertEquals(43, setting_hooks.findSettingId(category = 7, sub = 26))
        assertEquals(298, setting_hooks.findSettingId(category = 7, sub = 60))
    }

    @Test
    fun GameTestState.`reject unmapped setting`() = runBasicGameTest {
        withPlayer {
            assertFalse(setting_hooks.toggle(this, settingId = -1))
            assertFalse(setting_hooks.setNumber(this, settingId = -1, value = 1))
            assertFalse(setting_hooks.setDropdown(this, settingId = -1, value = 1))
        }
    }
}
