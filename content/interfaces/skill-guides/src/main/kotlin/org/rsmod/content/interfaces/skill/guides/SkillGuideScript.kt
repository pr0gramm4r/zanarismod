package org.rsmod.content.interfaces.skill.guides

import jakarta.inject.Inject
import org.rsmod.api.player.output.runClientScript
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.ui.ifClose
import org.rsmod.api.player.ui.ifCloseSub
import org.rsmod.api.player.ui.ifOpenOverlay
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.content.interfaces.skill.guides.configs.guide_components
import org.rsmod.content.interfaces.skill.guides.configs.guide_enums
import org.rsmod.content.interfaces.skill.guides.configs.guide_interfaces
import org.rsmod.content.interfaces.skill.guides.configs.guide_varbits
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player
import org.rsmod.game.enums.EnumTypeMapResolver
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SkillGuideScript
@Inject
constructor(
    private val eventBus: EventBus,
    private val enumResolver: EnumTypeMapResolver,
    private val protectedAccess: ProtectedAccessLauncher,
) : PluginScript() {
    override fun ScriptContext.startup() {
        val mappedTabButtons = enumResolver[guide_enums.open_buttons].filterValuesNotNull()
        for ((button, varbit) in mappedTabButtons) {
            onIfOverlayButton(button) { player.selectGuide(varbit) }
        }

        val mappedSubsections = enumResolver[guide_enums.subsection_buttons].filterValuesNotNull()
        for ((button, varbit) in mappedSubsections) {
            onIfOverlayButton(button) { player.changeSubsection(varbit) }
        }

        onIfOverlayButton(guide_components.close_button) { player.closeGuide() }
    }

    private fun Player.selectGuide(guideVarBit: Int) {
        ifClose(eventBus)
        protectedAccess.launch(this) { openGuide(guideVarBit, sectionVar = 0) }
    }

    private fun Player.openGuide(skillVar: Int, sectionVar: Int) {
        selectedSkill = skillVar
        selectedSubsection = sectionVar
        ifOpenOverlay(guide_interfaces.skill_guide_v2, eventBus)
        runClientScript(SKILL_GUIDE_V2_INIT_CLIENTSCRIPT, skillVar, sectionVar, 0, 0)
    }

    private fun Player.changeSubsection(sectionVar: Int) {
        openGuide(selectedSkill, sectionVar)
    }

    private fun Player.closeGuide() {
        ifCloseSub(guide_interfaces.skill_guide_v2, eventBus)
    }
}

private var Player.selectedSkill by intVarBit(guide_varbits.selected_skill)
private var Player.selectedSubsection by intVarBit(guide_varbits.selected_subsection)

private const val SKILL_GUIDE_V2_INIT_CLIENTSCRIPT = 1902
