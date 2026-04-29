package org.rsmod.content.skills.crafting.scripts

import jakarta.inject.Inject
import kotlin.math.min
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.craftingLvl
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.content.skills.crafting.configs.spinning_components
import org.rsmod.content.skills.crafting.configs.spinning_interfaces
import org.rsmod.content.skills.crafting.configs.spinning_locs
import org.rsmod.content.skills.crafting.configs.spinning_objs
import org.rsmod.content.skills.crafting.configs.spinning_seqs
import org.rsmod.content.skills.crafting.configs.spinning_varps
import org.rsmod.game.inv.Inventory
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.interf.IfSubType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SpinningWheel
@Inject
constructor(private val objTypes: ObjTypeList, private val xpMods: XpModifiers) : PluginScript() {
    override fun ScriptContext.startup() {
        for (loc in spinning_locs.all) {
            onOpLoc1(loc) { openSpinning(it.loc) }
            onOpLoc2(loc) { openSpinning(it.loc) }
            for (material in spinningMaterials) {
                onOpLocU(loc, material) { openSpinning(it.loc) }
            }
        }
    }

    private suspend fun ProtectedAccess.openSpinning(wheel: BoundLocInfo) {
        faceSquare(wheel.adjustedCentre)
        val choices = availableChoices()
        if (choices.isEmpty()) {
            mesbox(NO_SPINNING_OPTIONS_MESSAGE)
            return
        }
        openSpinningChatbox(choices)

        val maxCount = choices.maxOf { it.recipe.maxCount(inv) }
        for (choice in choices) {
            ifSetEvents(choice.component, 1..maxCount, IfEvent.PauseButton)
        }

        val input = pauseButton()
        val choice = choices.firstOrNull { it.component.isType(input.component) } ?: return
        vars[spinning_varps.skillmulti_previousselection] = choice.slot
        val count = min(input.subcomponent.coerceAtLeast(1), choice.recipe.maxCount(inv))
        spin(wheel, choice.recipe, count)
    }

    private fun ProtectedAccess.availableChoices(): List<SpinningChoice> {
        val available =
            spinningRecipes.filter { recipe ->
                player.craftingLvl >= recipe.level && recipe.maxCount(inv) > 0
            }
        return available.fitSkillmultiSlots().mapIndexed { index, recipe ->
            SpinningChoice(recipe, skillmultiComponents[index], slot = index)
        }
    }

    private fun List<SpinningRecipe>.fitSkillmultiSlots(): List<SpinningRecipe> {
        if (size <= skillmultiComponents.size) {
            return this
        }
        val recipes = toMutableList()
        while (recipes.size > skillmultiComponents.size) {
            val duplicateMaterial =
                recipes.indices.firstOrNull { index ->
                    recipes.drop(index + 1).any { it.material == recipes[index].material }
                }
            if (duplicateMaterial != null) {
                recipes.removeAt(duplicateMaterial)
            } else {
                recipes.removeAt(recipes.lastIndex)
            }
        }
        return recipes
    }

    private fun ProtectedAccess.openSpinningChatbox(choices: List<SpinningChoice>) {
        vars[varbits.chatmodal_unclamp] = constants.modal_infinitewidthandheight
        runClientScript(TOPLEVEL_CHATBOX_RESET_BACKGROUND)
        ifOpenSub(spinning_interfaces.skillmulti, components.chatbox_chatmodal, IfSubType.Modal)
        runClientScript(SKILLMULTI_SETUP, *skillmultiSetupArgs(choices).toTypedArray())
    }

    private fun ProtectedAccess.skillmultiSetupArgs(choices: List<SpinningChoice>): List<Any> {
        val maxCount = choices.maxOfOrNull { it.recipe.maxCount(inv) } ?: 0
        val itemIds = choices.map { it.recipe.product.id }
        val paddedItemIds = itemIds + List(SKILLMULTI_ITEM_SLOTS - itemIds.size) { -1 }
        val title =
            if (choices.size == 1) {
                "How many would you like to spin?"
            } else {
                "What would you like to spin?"
            }
        val labels =
            choices.joinToString(separator = "|", prefix = "$title|") {
                objTypes[it.recipe.product].name
            }
        return buildList {
            add(SKILLMULTI_SPIN_MODE)
            add(maxCount)
            addAll(paddedItemIds)
            add(maxCount)
            add(labels)
        }
    }

    private suspend fun ProtectedAccess.spin(
        wheel: BoundLocInfo,
        recipe: SpinningRecipe,
        count: Int,
    ) {
        if (count <= 0 || !hasMaterial(recipe)) {
            mes("You do not have the required materials to spin this.")
            return
        }
        if (player.craftingLvl < recipe.level) {
            mes("You need a Crafting level of ${recipe.level} to make this.")
            return
        }
        ifClose()
        repeat(count) {
            if (!hasMaterial(recipe)) {
                mes("You do not have the required materials to spin this.")
                return
            }
            faceSquare(wheel.adjustedCentre)
            anim(spinning_seqs.human_spinningwheel)
            delay(3)
            if (!invDel(inv, recipe.material).success) {
                mes("You do not have the required materials to spin this.")
                return
            }
            invAdd(inv, recipe.product)
            statAdvance(stats.crafting, recipe.xp * xpMods.get(player, stats.crafting))
            spam(recipe.message)
        }
    }

    private fun ProtectedAccess.hasMaterial(recipe: SpinningRecipe): Boolean {
        return inv.count(recipe.material) >= 1
    }

    private fun Inventory.count(obj: ObjType): Int = count(objTypes[obj])

    private fun SpinningRecipe.maxCount(inv: Inventory): Int = inv.count(material)

    private data class SpinningRecipe(
        val material: ObjType,
        val product: ObjType,
        val level: Int,
        val xp: Double,
        val message: String,
    )

    private data class SpinningChoice(
        val recipe: SpinningRecipe,
        val component: ComponentType,
        val slot: Int,
    )

    private companion object {
        const val SKILLMULTI_ITEM_SLOTS = 18
        const val SKILLMULTI_SETUP = 2046
        const val SKILLMULTI_SPIN_MODE = 13
        const val TOPLEVEL_CHATBOX_RESET_BACKGROUND = 2379
        const val NO_SPINNING_OPTIONS_MESSAGE =
            "You don't have anything suitable to spin at this spinning wheel."

        val spinningRecipes =
            listOf(
                SpinningRecipe(
                    spinning_objs.wool,
                    spinning_objs.ball_of_wool,
                    level = 1,
                    xp = 2.5,
                    message = "You spin the wool into a ball of wool.",
                ),
                SpinningRecipe(
                    spinning_objs.viking_golden_fleece,
                    spinning_objs.viking_golden_wool,
                    level = 1,
                    xp = 2.5,
                    message = "You spin the golden fleece into golden wool.",
                ),
                SpinningRecipe(
                    spinning_objs.flax,
                    spinning_objs.bow_string,
                    level = 10,
                    xp = 15.0,
                    message = "You spin the flax into a bow string.",
                ),
                SpinningRecipe(
                    spinning_objs.xbows_sinew,
                    spinning_objs.xbows_crossbow_string,
                    level = 10,
                    xp = 15.0,
                    message = "You spin the sinew into a crossbow string.",
                ),
                SpinningRecipe(
                    spinning_objs.oak_roots,
                    spinning_objs.xbows_crossbow_string,
                    level = 10,
                    xp = 15.0,
                    message = "You spin the oak roots into a crossbow string.",
                ),
                SpinningRecipe(
                    spinning_objs.willow_roots,
                    spinning_objs.xbows_crossbow_string,
                    level = 10,
                    xp = 15.0,
                    message = "You spin the willow roots into a crossbow string.",
                ),
                SpinningRecipe(
                    spinning_objs.maple_roots,
                    spinning_objs.xbows_crossbow_string,
                    level = 10,
                    xp = 15.0,
                    message = "You spin the maple roots into a crossbow string.",
                ),
                SpinningRecipe(
                    spinning_objs.yew_roots,
                    spinning_objs.xbows_crossbow_string,
                    level = 10,
                    xp = 15.0,
                    message = "You spin the yew roots into a crossbow string.",
                ),
                SpinningRecipe(
                    spinning_objs.magic_roots,
                    spinning_objs.xbows_crossbow_string,
                    level = 10,
                    xp = 15.0,
                    message = "You spin the magic roots into a crossbow string.",
                ),
                SpinningRecipe(
                    spinning_objs.magic_roots,
                    spinning_objs.magic_string,
                    level = 19,
                    xp = 30.0,
                    message = "You spin the magic roots into a magic string.",
                ),
                SpinningRecipe(
                    spinning_objs.yak_hair,
                    spinning_objs.rope,
                    level = 30,
                    xp = 25.0,
                    message = "You spin the yak hair into rope.",
                ),
            )

        val spinningMaterials = spinningRecipes.map { it.material }.distinct()

        val skillmultiComponents =
            listOf(
                spinning_components.skillmulti_a,
                spinning_components.skillmulti_b,
                spinning_components.skillmulti_c,
                spinning_components.skillmulti_d,
                spinning_components.skillmulti_e,
                spinning_components.skillmulti_f,
                spinning_components.skillmulti_g,
                spinning_components.skillmulti_h,
                spinning_components.skillmulti_i,
                spinning_components.skillmulti_j,
            )
    }
}
