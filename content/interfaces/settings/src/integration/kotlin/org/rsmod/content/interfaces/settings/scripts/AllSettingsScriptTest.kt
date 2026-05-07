package org.rsmod.content.interfaces.settings.scripts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.input.ResumePStringDialogInput
import org.rsmod.api.player.input.ResumePauseButtonInput
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.interfaces.settings.configs.setting_components
import org.rsmod.content.interfaces.settings.configs.setting_hooks

class AllSettingsScriptTest {
    @Test
    fun GameTestState.`select colour picker setting`() =
        runGameTest(AllSettingsScript::class) {
            player.ifOpenOverlay(interfaces.settings, components.toplevel_target_floater)

            player.ifButton(setting_components.categories_clickzone, comsub = 2)
            advance()
            player.ifButton(setting_components.settings_clickzone, comsub = 26)
            advance()

            assertTrue(player.ui.containsOverlay(interfaces.colour_pallet))
            assertEquals(1, player.vars[varbits.settings_colour_modal_opened])

            player.resumeCountDialog(0x123456)
            advance()

            assertFalse(player.ui.containsOverlay(interfaces.colour_pallet))
            assertEquals(0, player.vars[varbits.settings_colour_modal_opened])
            assertEquals(0x123457, player.vars[varps.option_chat_colour_public_opaque])
        }

    @Test
    fun GameTestState.`confirm volume reset setting`() =
        runGameTest(AllSettingsScript::class) {
            player.ifOpenOverlay(interfaces.settings, components.toplevel_target_floater)
            VarPlayerIntMapSetter.set(player, varps.option_master_volume, 12)
            VarPlayerIntMapSetter.set(player, varps.option_music, 12)
            VarPlayerIntMapSetter.set(player, varps.option_sounds, 12)
            VarPlayerIntMapSetter.set(player, varps.option_areasounds, 12)

            player.ifButton(setting_components.categories_clickzone, comsub = 1)
            advance()
            player.ifButton(setting_components.settings_clickzone, comsub = 5)
            advance()
            player.resumeActiveCoroutine(ResumePauseButtonInput(components.chatmenu_pbutton, 1))
            advance()

            assertEquals(100, player.vars[varps.option_master_volume])
            assertEquals(20, player.vars[varps.option_music])
            assertEquals(45, player.vars[varps.option_sounds])
            assertEquals(30, player.vars[varps.option_areasounds])
            assertEquals(100, player.vars[varbits.option_master_volume_desktop])
            assertEquals(20, player.vars[varbits.option_music_desktop])
            assertEquals(45, player.vars[varbits.option_sounds_desktop])
            assertEquals(30, player.vars[varbits.option_areasounds_desktop])
        }

    @Test
    fun GameTestState.`text setting normalizes csv input`() =
        runGameTest(AllSettingsScript::class) {
            player.ifOpenOverlay(interfaces.settings, components.toplevel_target_floater)

            player.ifButton(setting_components.categories_clickzone, comsub = 8)
            advance()
            player.ifButton(setting_components.settings_clickzone, comsub = 6)
            advance()
            player.resumeActiveCoroutine(
                ResumePStringDialogInput(" Bones,  Coins,,BONES,<col=red>Tag</col> ")
            )
            advance()

            assertEquals("bones, coins, tag", setting_hooks.textSettings.getValue(345).get(player))
            assertEquals(1, player.vars[varbits.option_loottracker_ignorelist_has_text])
        }
}
