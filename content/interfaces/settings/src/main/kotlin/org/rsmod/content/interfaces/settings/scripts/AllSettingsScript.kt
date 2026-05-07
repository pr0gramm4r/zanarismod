package org.rsmod.content.interfaces.settings.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.ui.ifSetEvents
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onIfOpen
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.content.interfaces.settings.configs.ColourSetting
import org.rsmod.content.interfaces.settings.configs.ConfirmationAction
import org.rsmod.content.interfaces.settings.configs.ConfirmationSetting
import org.rsmod.content.interfaces.settings.configs.TextSetting
import org.rsmod.content.interfaces.settings.configs.setting_components
import org.rsmod.content.interfaces.settings.configs.setting_hooks
import org.rsmod.game.entity.Player
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class AllSettingsScript @Inject constructor(private val protectedAccess: ProtectedAccessLauncher) :
    PluginScript() {
    private var Player.settingsCategory by intVarBit(varbits.settings_category)
    private var Player.selectedSetting by intVarBit(varbits.settings_selected_setting)
    private var Player.settingsColourModalOpened by intVarBit(varbits.settings_colour_modal_opened)

    override fun ScriptContext.startup() {
        onIfOpen(interfaces.settings) { player.updateIfEvents() }
        onIfOpen(interfaces.colour_pallet) { player.updateColourPickerEvents() }

        onIfOverlayButton(setting_components.categories_clickzone) { player.selectCategory(comsub) }

        onIfOverlayButton(setting_components.settings_clickzone) { player.selectSetting(comsub) }
    }

    private fun Player.updateIfEvents() {
        ifSetEvents(setting_components.categories_clickzone, 0..9, IfEvent.Op1)
        ifSetEvents(setting_components.settings_clickzone, 0..512, IfEvent.Op1)
    }

    private fun Player.updateColourPickerEvents() {
        ifSetEvents(setting_components.colour_pallet_colours, 0..152, IfEvent.Op1)
        ifSetEvents(setting_components.colour_pallet_custom_clickzone, 0..4, IfEvent.Op1)
    }

    private fun Player.selectCategory(category: Int) {
        settingsCategory = category
        selectedSetting = 0
    }

    private fun Player.selectSetting(sub: Int) {
        val settingId = setting_hooks.findSettingId(settingsCategory, sub) ?: return
        selectedSetting = settingId

        val colourSetting = setting_hooks.colourSettings[settingId]
        if (colourSetting != null) {
            protectedAccess.launch(this) { setColourSetting(colourSetting) }
            return
        }

        val confirmationSetting = setting_hooks.confirmationSettings[settingId]
        if (confirmationSetting != null) {
            protectedAccess.launch(this) { confirmSetting(confirmationSetting) }
            return
        }

        val textSetting = setting_hooks.textSettings[settingId]
        if (textSetting != null) {
            protectedAccess.launch(this) { setTextSetting(textSetting) }
            return
        }

        val numberSetting = setting_hooks.numberSettings[settingId]
        if (numberSetting != null) {
            protectedAccess.launch(this) { setNumberSetting(settingId, numberSetting.prompt) }
            return
        }

        setting_hooks.toggle(this, settingId)
    }

    private suspend fun ProtectedAccess.setNumberSetting(settingId: Int, prompt: String) {
        val value = countDialog(prompt)
        setting_hooks.setNumber(player, settingId, value)
    }

    private suspend fun ProtectedAccess.setColourSetting(setting: ColourSetting) {
        player.settingsColourModalOpened = 1
        try {
            ifOpenOverlay(interfaces.colour_pallet, setting_components.settings_popup)
            runClientScript(
                COLOUR_PALLET_OPEN_CLIENTSCRIPT,
                COLOUR_PALLET_MASK,
                setting.getPickerColour(player),
            )
            val colour = countDialogInput()
            setting_hooks.setColour(player, setting.settingId, colour)
        } finally {
            player.settingsColourModalOpened = 0
            ifCloseSub(interfaces.colour_pallet)
            runClientScript(MES_LAYER_CLOSE_CLIENTSCRIPT, 0)
        }
    }

    private suspend fun ProtectedAccess.confirmSetting(setting: ConfirmationSetting) {
        val confirmed = choice2("Yes.", true, "No.", false, title = setting.title)
        if (!confirmed) {
            return
        }
        when (setting.action) {
            ConfirmationAction.NoOp -> Unit
            ConfirmationAction.ResetOpaqueChatColours -> resetOpaqueChatColours()
            ConfirmationAction.ResetSplitChatColours -> resetSplitChatColours()
            ConfirmationAction.ResetQuestListColours -> resetQuestListColours()
            ConfirmationAction.ResetVolumeSliders -> resetVolumeSliders()
        }
    }

    private suspend fun ProtectedAccess.setTextSetting(setting: TextSetting) {
        val text = stringDialog(setting.prompt)
        setting_hooks.setText(player, setting.settingId, text)
    }

    private fun ProtectedAccess.resetOpaqueChatColours() {
        setting_hooks.resetColours(player, setting_hooks.opaqueChatColourSettingIds)
        mes("Default opaque colours restored.")
    }

    private fun ProtectedAccess.resetSplitChatColours() {
        setting_hooks.resetColours(player, setting_hooks.splitChatColourSettingIds)
        mes("Default split chat colours restored.")
    }

    private fun ProtectedAccess.resetQuestListColours() {
        setting_hooks.resetColours(player, setting_hooks.questListColourSettingIds)
        mes("Default quest list text colours restored.")
    }

    private fun ProtectedAccess.resetVolumeSliders() {
        player.setVolume(
            varps.option_master_volume,
            varbits.option_master_volume_desktop,
            MASTER_VOLUME_DEFAULT,
        )
        player.setVolume(varps.option_music, varbits.option_music_desktop, MUSIC_VOLUME_DEFAULT)
        player.setVolume(varps.option_sounds, varbits.option_sounds_desktop, SOUND_VOLUME_DEFAULT)
        player.setVolume(
            varps.option_areasounds,
            varbits.option_areasounds_desktop,
            AREA_SOUND_VOLUME_DEFAULT,
        )
        player.setSavedVolume(
            varbits.option_master_volume_saved,
            varbits.option_master_volume_saved_desktop,
        )
        player.setSavedVolume(varbits.option_music_saved, varbits.option_music_saved_desktop)
        player.setSavedVolume(varbits.option_sounds_saved, varbits.option_sounds_saved_desktop)
        player.setSavedVolume(
            varbits.option_areasounds_saved,
            varbits.option_areasounds_saved_desktop,
        )
        mes("Your volume sliders have been reset to their default values.")
    }

    private fun Player.setVolume(
        legacy: org.rsmod.game.type.varp.VarpType,
        desktop: org.rsmod.game.type.varbit.VarBitType,
        value: Int,
    ) {
        VarPlayerIntMapSetter.set(this, legacy, value)
        VarPlayerIntMapSetter.set(this, desktop, value)
    }

    private fun Player.setSavedVolume(
        legacy: org.rsmod.game.type.varbit.VarBitType,
        desktop: org.rsmod.game.type.varbit.VarBitType,
    ) {
        VarPlayerIntMapSetter.set(this, legacy, UNMUTE_VOLUME)
        VarPlayerIntMapSetter.set(this, desktop, UNMUTE_VOLUME)
    }

    private companion object {
        private const val COLOUR_PALLET_OPEN_CLIENTSCRIPT = 4185
        private const val COLOUR_PALLET_MASK = 8781833
        private const val MES_LAYER_CLOSE_CLIENTSCRIPT = 101
        private const val MASTER_VOLUME_DEFAULT = 100
        private const val MUSIC_VOLUME_DEFAULT = 20
        private const val SOUND_VOLUME_DEFAULT = 45
        private const val AREA_SOUND_VOLUME_DEFAULT = 30
        private const val UNMUTE_VOLUME = 5
    }
}
