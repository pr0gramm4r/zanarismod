package org.rsmod.content.skills.smithing.scripts

import jakarta.inject.Inject
import kotlin.math.min
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.craftingLvl
import org.rsmod.api.player.stat.smithingLvl
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.content.skills.smithing.configs.furnace_components
import org.rsmod.content.skills.smithing.configs.furnace_interfaces
import org.rsmod.content.skills.smithing.configs.furnace_locs
import org.rsmod.content.skills.smithing.configs.furnace_objs
import org.rsmod.content.skills.smithing.configs.furnace_seqs
import org.rsmod.content.skills.smithing.configs.furnace_varps
import org.rsmod.game.inv.Inventory
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.interf.IfSubType
import org.rsmod.game.type.interf.InterfaceType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Furnace
@Inject
constructor(
    private val objTypes: ObjTypeList,
    private val xpMods: XpModifiers,
    private val random: GameRandom,
) : PluginScript() {
    override fun ScriptContext.startup() {
        for (loc in furnace_locs.all) {
            onOpLoc1(loc) { openSmelting(it.loc) }
            onOpLoc2(loc) { openSmelting(it.loc) }
            for (ore in smeltingOres) {
                onOpLocU(loc, ore) { openSmelting(it.loc) }
            }
            onOpLocU(loc, furnace_objs.gold_bar) { openJewellery(it.loc, goldJewelleryContext) }
            onOpLocU(loc, furnace_objs.silver_bar) { openJewellery(it.loc, silverJewelleryContext) }
        }
        for (recipe in jewelleryRecipes) {
            onIfModalButton(recipe.component) {
                makeJewellery(recipe, selectedJewelleryQuantity(recipe))
            }
        }
        for (button in jewelleryQuantityButtons) {
            onIfModalButton(button.component) { selectJewelleryQuantity(button.quantity) }
        }
    }

    private suspend fun ProtectedAccess.openSmelting(furnace: BoundLocInfo) {
        faceSquare(furnace.adjustedCentre)
        val choices = availableSmeltingChoices()
        if (choices.isEmpty()) {
            mesbox(NO_SMELTING_OPTIONS_MESSAGE)
            return
        }
        openSmeltingChatbox(choices)

        val maxCount = choices.maxOf { it.recipe.maxCount(inv) }
        for (choice in choices) {
            ifSetEvents(choice.component, 1..maxCount, IfEvent.PauseButton)
        }

        val input = pauseButton()
        val choice = choices.firstOrNull { it.component.isType(input.component) } ?: return
        val recipe = choice.recipe
        vars[furnace_varps.skillmulti_previousselection] = choice.slot
        val count = min(input.subcomponent.coerceAtLeast(1), recipe.maxCount(inv))
        if (count <= 0) {
            mes("You do not have the required ores to smelt this bar.")
            return
        }
        smelt(furnace, recipe, count)
    }

    private fun ProtectedAccess.availableSmeltingChoices(): List<SmeltingChoice> {
        val available =
            smeltingRecipes.filter { recipe ->
                player.smithingLvl >= recipe.level && recipe.maxCount(inv) > 0
            }
        return available.mapIndexed { index, recipe ->
            SmeltingChoice(recipe, skillmultiComponents[index], slot = index)
        }
    }

    private fun ProtectedAccess.openSmeltingChatbox(choices: List<SmeltingChoice>) {
        vars[varbits.chatmodal_unclamp] = constants.modal_infinitewidthandheight
        runClientScript(TOPLEVEL_CHATBOX_RESET_BACKGROUND)
        ifOpenSub(furnace_interfaces.skillmulti, components.chatbox_chatmodal, IfSubType.Modal)
        runClientScript(SKILLMULTI_SETUP, *skillmultiSetupArgs(choices).toTypedArray())
    }

    private fun ProtectedAccess.skillmultiSetupArgs(choices: List<SmeltingChoice>): List<Any> {
        val maxCount = choices.maxOfOrNull { it.recipe.maxCount(inv) } ?: 0
        val itemIds = choices.map { it.recipe.product.id }
        val paddedItemIds = itemIds + List(SKILLMULTI_ITEM_SLOTS - itemIds.size) { -1 }
        val title =
            if (choices.size == 1) {
                "How many would you like to smelt?"
            } else {
                "What would you like to smelt?"
            }
        val labels =
            choices.joinToString(separator = "|", prefix = "$title|") {
                objTypes[it.recipe.product].name
            }
        return buildList {
            add(SKILLMULTI_SMELT_MODE)
            add(maxCount)
            addAll(paddedItemIds)
            add(maxCount)
            add(labels)
        }
    }

    private fun ProtectedAccess.openJewellery(furnace: BoundLocInfo, context: JewelleryContext) {
        faceSquare(furnace.adjustedCentre)
        ifOpenMainModal(context.interfaceType)
        for (recipe in context.recipes) {
            ifSetEvents(recipe.component, -1..-1, IfEvent.Op1)
        }
        setJewelleryQuantityButtons(context)
    }

    private suspend fun ProtectedAccess.selectJewelleryQuantity(quantity: JewelleryQuantity) {
        val selected =
            when (quantity) {
                JewelleryQuantity.One -> 1
                JewelleryQuantity.Five -> 5
                JewelleryQuantity.Ten -> 10
                JewelleryQuantity.X -> countDialog()
                JewelleryQuantity.All -> MAX_JEWELLERY_QUANTITY
            }
        vars[furnace_varps.makexcrafting] = selected.coerceAtLeast(1)
    }

    private fun ProtectedAccess.setJewelleryQuantityButtons(context: JewelleryContext) {
        val maxCount = context.recipes.maxOfOrNull { it.maxCount(inv) } ?: 0
        for (button in context.quantityButtons) {
            val visible = button.quantity.visibleAt(maxCount)
            ifSetHide(button.component, !visible)
            ifSetEvents(
                button.component,
                -1..-1,
                *(if (visible) arrayOf(IfEvent.Op1) else emptyArray()),
            )
        }
        vars[furnace_varps.makexcrafting] = if (maxCount == 1) MAX_JEWELLERY_QUANTITY else 1
    }

    private fun ProtectedAccess.selectedJewelleryQuantity(recipe: JewelleryRecipe): Int {
        val selected = vars[furnace_varps.makexcrafting].coerceAtLeast(1)
        return min(selected, recipe.maxCount(inv))
    }

    private suspend fun ProtectedAccess.smelt(
        furnace: BoundLocInfo,
        recipe: SmeltingRecipe,
        count: Int,
    ) {
        if (player.smithingLvl < recipe.level) {
            mes("You need a Smithing level of ${recipe.level} to smelt this bar.")
            return
        }
        repeat(count) {
            if (!hasMaterials(recipe.materials)) {
                mes("You do not have the required ores to smelt this bar.")
                return
            }
            faceSquare(furnace.adjustedCentre)
            spam(recipe.insertMessage)
            anim(furnace_seqs.human_furnace)
            delay(2)
            if (!deleteMaterials(recipe.materials)) {
                return
            }
            if (recipe == ironSmeltingRecipe && random.randomBoolean()) {
                spam(IRON_FAILURE_MESSAGE)
                return@repeat
            }
            invAdd(inv, recipe.product)
            statAdvance(stats.smithing, recipe.xp * xpMods.get(player, stats.smithing))
            spam(recipe.retrieveMessage)
        }
    }

    private suspend fun ProtectedAccess.makeJewellery(recipe: JewelleryRecipe, count: Int) {
        if (recipe.mould !in inv) {
            mes("You need a ${recipe.mouldName} mould to craft ${recipe.groupName}.")
            return
        }
        if (player.craftingLvl < recipe.level) {
            mes("You need a Crafting level of ${recipe.level} to make this.")
            return
        }
        if (!hasMaterials(recipe.materials)) {
            mes("You do not have the required materials to make this.")
            return
        }
        ifClose()
        repeat(count) {
            if (!hasMaterials(recipe.materials)) {
                mes("You do not have the required materials to make this.")
                return
            }
            anim(furnace_seqs.human_furnace)
            delay(2)
            if (!deleteMaterials(recipe.materials)) {
                return
            }
            invAdd(inv, recipe.product, recipe.productCount)
            statAdvance(stats.crafting, recipe.xp * xpMods.get(player, stats.crafting))
            val product = objTypes[recipe.product].name.lowercase()
            val message =
                if (recipe.productCount == 1) {
                    "You make a $product."
                } else {
                    "You make some $product."
                }
            spam(message)
        }
    }

    private fun ProtectedAccess.hasMaterials(materials: List<Material>): Boolean {
        return materials.all { inv.count(it.obj) >= it.count }
    }

    private fun ProtectedAccess.deleteMaterials(materials: List<Material>): Boolean {
        for (material in materials) {
            if (!invDel(inv, material.obj, material.count).success) {
                mes("You do not have the required materials to make this.")
                return false
            }
        }
        return true
    }

    private fun Inventory.count(obj: ObjType): Int = count(objTypes[obj])

    private fun SmeltingRecipe.maxCount(inv: Inventory): Int =
        materials.minOf { inv.count(it.obj) / it.count }

    private fun JewelleryRecipe.maxCount(inv: Inventory): Int =
        materials.minOf { inv.count(it.obj) / it.count }

    private data class Material(val obj: ObjType, val count: Int = 1)

    private data class SmeltingRecipe(
        val product: ObjType,
        val level: Int,
        val xp: Double,
        val insertMessage: String,
        val retrieveMessage: String,
        val materials: List<Material>,
    )

    private data class SmeltingChoice(
        val recipe: SmeltingRecipe,
        val component: ComponentType,
        val slot: Int,
    )

    private data class JewelleryContext(
        val interfaceType: InterfaceType,
        val recipes: List<JewelleryRecipe>,
        val quantityButtons: List<JewelleryQuantityButton>,
    )

    private data class JewelleryQuantityButton(
        val component: ComponentType,
        val quantity: JewelleryQuantity,
    )

    private enum class JewelleryQuantity {
        One,
        Five,
        Ten,
        X,
        All;

        fun visibleAt(maxCount: Int): Boolean =
            when (this) {
                One -> maxCount > 1
                Five -> maxCount > 5
                Ten -> maxCount > 10
                X -> maxCount > 2
                All -> maxCount > 0
            }
    }

    private data class JewelleryRecipe(
        val component: ComponentType,
        val product: ObjType,
        val mould: ObjType,
        val mouldName: String,
        val groupName: String,
        val level: Int,
        val xp: Double,
        val materials: List<Material>,
        val productCount: Int = 1,
    )

    private companion object {
        const val MAX_JEWELLERY_QUANTITY = 28
        const val SKILLMULTI_ITEM_SLOTS = 18
        const val SKILLMULTI_SETUP = 2046
        const val SKILLMULTI_SMELT_MODE = 13
        const val TOPLEVEL_CHATBOX_RESET_BACKGROUND = 2379
        const val IRON_FAILURE_MESSAGE = "The ore is too impure and you fail to refine it."
        const val NO_SMELTING_OPTIONS_MESSAGE =
            "You don't have the correct quantities of ore to be able to smelt anything."

        val ironSmeltingRecipe =
            SmeltingRecipe(
                furnace_objs.iron_bar,
                level = 15,
                xp = 12.5,
                insertMessage = "You smelt the iron in the furnace.",
                retrieveMessage = "You retrieve a bar of iron.",
                materials = listOf(Material(furnace_objs.iron_ore)),
            )

        val smeltingRecipes =
            listOf(
                SmeltingRecipe(
                    furnace_objs.bronze_bar,
                    level = 1,
                    xp = 6.2,
                    insertMessage = "You smelt the copper and tin together in the furnace.",
                    retrieveMessage = "You retrieve a bar of bronze.",
                    materials =
                        listOf(Material(furnace_objs.copper_ore), Material(furnace_objs.tin_ore)),
                ),
                ironSmeltingRecipe,
                SmeltingRecipe(
                    furnace_objs.blurite_bar,
                    level = 13,
                    xp = 8.0,
                    insertMessage = "You smelt the blurite in the furnace.",
                    retrieveMessage = "You retrieve a bar of blurite.",
                    materials = listOf(Material(furnace_objs.blurite_ore)),
                ),
                SmeltingRecipe(
                    furnace_objs.silver_bar,
                    level = 20,
                    xp = 13.7,
                    insertMessage = "You smelt the silver in the furnace.",
                    retrieveMessage = "You retrieve a bar of silver.",
                    materials = listOf(Material(furnace_objs.silver_ore)),
                ),
                SmeltingRecipe(
                    furnace_objs.steel_bar,
                    level = 30,
                    xp = 17.5,
                    insertMessage =
                        "You smelt the iron in the furnace along with two heaps of coal.",
                    retrieveMessage = "You retrieve a bar of steel.",
                    materials =
                        listOf(Material(furnace_objs.iron_ore), Material(furnace_objs.coal, 2)),
                ),
                SmeltingRecipe(
                    furnace_objs.gold_bar,
                    level = 40,
                    xp = 22.5,
                    insertMessage = "You smelt the gold in the furnace.",
                    retrieveMessage = "You retrieve a bar of gold.",
                    materials = listOf(Material(furnace_objs.gold_ore)),
                ),
                SmeltingRecipe(
                    furnace_objs.mithril_bar,
                    level = 50,
                    xp = 30.0,
                    insertMessage =
                        "You smelt the mithril in the furnace along with four heaps of coal.",
                    retrieveMessage = "You retrieve a bar of mithril.",
                    materials =
                        listOf(Material(furnace_objs.mithril_ore), Material(furnace_objs.coal, 4)),
                ),
                SmeltingRecipe(
                    furnace_objs.adamantite_bar,
                    level = 70,
                    xp = 37.5,
                    insertMessage =
                        "You smelt the adamantite in the furnace along with six heaps of coal.",
                    retrieveMessage = "You retrieve a bar of adamantite.",
                    materials =
                        listOf(
                            Material(furnace_objs.adamantite_ore),
                            Material(furnace_objs.coal, 6),
                        ),
                ),
                SmeltingRecipe(
                    furnace_objs.runite_bar,
                    level = 85,
                    xp = 50.0,
                    insertMessage =
                        "You smelt the runite in the furnace along with eight heaps of coal.",
                    retrieveMessage = "You retrieve a bar of runite.",
                    materials =
                        listOf(Material(furnace_objs.runite_ore), Material(furnace_objs.coal, 8)),
                ),
            )

        val smeltingOres =
            smeltingRecipes.flatMap { recipe -> recipe.materials.map { it.obj } }.distinct()

        val skillmultiComponents =
            listOf(
                furnace_components.skillmulti_a,
                furnace_components.skillmulti_b,
                furnace_components.skillmulti_c,
                furnace_components.skillmulti_d,
                furnace_components.skillmulti_e,
                furnace_components.skillmulti_f,
                furnace_components.skillmulti_g,
                furnace_components.skillmulti_h,
                furnace_components.skillmulti_i,
            )

        val goldJewelleryQuantityButtons =
            listOf(
                JewelleryQuantityButton(
                    furnace_components.crafting_gold_make_1,
                    JewelleryQuantity.One,
                ),
                JewelleryQuantityButton(
                    furnace_components.crafting_gold_make_5,
                    JewelleryQuantity.Five,
                ),
                JewelleryQuantityButton(
                    furnace_components.crafting_gold_make_10,
                    JewelleryQuantity.Ten,
                ),
                JewelleryQuantityButton(
                    furnace_components.crafting_gold_make_x,
                    JewelleryQuantity.X,
                ),
                JewelleryQuantityButton(
                    furnace_components.crafting_gold_make_all,
                    JewelleryQuantity.All,
                ),
            )

        val silverJewelleryQuantityButtons =
            listOf(
                JewelleryQuantityButton(
                    furnace_components.silver_crafting_make_1,
                    JewelleryQuantity.One,
                ),
                JewelleryQuantityButton(
                    furnace_components.silver_crafting_make_5,
                    JewelleryQuantity.Five,
                ),
                JewelleryQuantityButton(
                    furnace_components.silver_crafting_make_10,
                    JewelleryQuantity.Ten,
                ),
                JewelleryQuantityButton(
                    furnace_components.silver_crafting_make_x,
                    JewelleryQuantity.X,
                ),
                JewelleryQuantityButton(
                    furnace_components.silver_crafting_make_all,
                    JewelleryQuantity.All,
                ),
            )

        val jewelleryQuantityButtons = goldJewelleryQuantityButtons + silverJewelleryQuantityButtons

        fun goldMaterials(gem: ObjType? = null): List<Material> =
            if (gem == null) {
                listOf(Material(furnace_objs.gold_bar))
            } else {
                listOf(Material(furnace_objs.gold_bar), Material(gem))
            }

        fun silverMaterials(gem: ObjType? = null): List<Material> =
            if (gem == null) {
                listOf(Material(furnace_objs.silver_bar))
            } else {
                listOf(Material(furnace_objs.silver_bar), Material(gem))
            }

        fun goldRing(
            component: ComponentType,
            product: ObjType,
            level: Int,
            xp: Double,
            gem: ObjType? = null,
        ) =
            JewelleryRecipe(
                component,
                product,
                furnace_objs.ring_mould,
                "ring",
                "rings",
                level,
                xp,
                goldMaterials(gem),
            )

        fun goldNecklace(
            component: ComponentType,
            product: ObjType,
            level: Int,
            xp: Double,
            gem: ObjType? = null,
        ) =
            JewelleryRecipe(
                component,
                product,
                furnace_objs.necklace_mould,
                "necklace",
                "necklaces",
                level,
                xp,
                goldMaterials(gem),
            )

        fun goldAmulet(
            component: ComponentType,
            product: ObjType,
            level: Int,
            xp: Double,
            gem: ObjType? = null,
        ) =
            JewelleryRecipe(
                component,
                product,
                furnace_objs.amulet_mould,
                "amulet",
                "amulets",
                level,
                xp,
                goldMaterials(gem),
            )

        fun goldBracelet(
            component: ComponentType,
            product: ObjType,
            level: Int,
            xp: Double,
            gem: ObjType? = null,
        ) =
            JewelleryRecipe(
                component,
                product,
                furnace_objs.bracelet_mould,
                "bracelet",
                "bracelets",
                level,
                xp,
                goldMaterials(gem),
            )

        fun silverRing(
            component: ComponentType,
            product: ObjType,
            level: Int,
            xp: Double,
            gem: ObjType,
        ) =
            JewelleryRecipe(
                component,
                product,
                furnace_objs.ring_mould,
                "ring",
                "rings",
                level,
                xp,
                silverMaterials(gem),
            )

        fun silverNecklace(
            component: ComponentType,
            product: ObjType,
            level: Int,
            xp: Double,
            gem: ObjType,
        ) =
            JewelleryRecipe(
                component,
                product,
                furnace_objs.necklace_mould,
                "necklace",
                "necklaces",
                level,
                xp,
                silverMaterials(gem),
            )

        fun silverAmulet(
            component: ComponentType,
            product: ObjType,
            level: Int,
            xp: Double,
            gem: ObjType,
        ) =
            JewelleryRecipe(
                component,
                product,
                furnace_objs.amulet_mould,
                "amulet",
                "amulets",
                level,
                xp,
                silverMaterials(gem),
            )

        fun silverBracelet(
            component: ComponentType,
            product: ObjType,
            level: Int,
            xp: Double,
            gem: ObjType,
        ) =
            JewelleryRecipe(
                component,
                product,
                furnace_objs.bracelet_mould,
                "bracelet",
                "bracelets",
                level,
                xp,
                silverMaterials(gem),
            )

        fun silverMisc(
            component: ComponentType,
            product: ObjType,
            mould: ObjType,
            mouldName: String,
            groupName: String,
            level: Int,
            xp: Double,
            productCount: Int = 1,
        ) =
            JewelleryRecipe(
                component,
                product,
                mould,
                mouldName,
                groupName,
                level,
                xp,
                silverMaterials(),
                productCount,
            )

        val goldJewelleryRecipes =
            listOf(
                goldRing(furnace_components.gold_ring, furnace_objs.gold_ring, 5, 15.0),
                goldRing(
                    furnace_components.sapphire_ring,
                    furnace_objs.sapphire_ring,
                    20,
                    40.0,
                    furnace_objs.sapphire,
                ),
                goldRing(
                    furnace_components.emerald_ring,
                    furnace_objs.emerald_ring,
                    27,
                    55.0,
                    furnace_objs.emerald,
                ),
                goldRing(
                    furnace_components.ruby_ring,
                    furnace_objs.ruby_ring,
                    34,
                    70.0,
                    furnace_objs.ruby,
                ),
                goldRing(
                    furnace_components.diamond_ring,
                    furnace_objs.diamond_ring,
                    43,
                    85.0,
                    furnace_objs.diamond,
                ),
                goldRing(
                    furnace_components.dragonstone_ring,
                    furnace_objs.dragonstone_ring,
                    55,
                    100.0,
                    furnace_objs.dragonstone,
                ),
                goldRing(
                    furnace_components.onyx_ring,
                    furnace_objs.onyx_ring,
                    67,
                    115.0,
                    furnace_objs.onyx,
                ),
                goldRing(
                    furnace_components.zenyte_ring,
                    furnace_objs.zenyte_ring,
                    89,
                    150.0,
                    furnace_objs.zenyte,
                ),
                goldNecklace(furnace_components.gold_necklace, furnace_objs.gold_necklace, 6, 20.0),
                goldNecklace(
                    furnace_components.sapphire_necklace,
                    furnace_objs.sapphire_necklace,
                    22,
                    55.0,
                    furnace_objs.sapphire,
                ),
                goldNecklace(
                    furnace_components.emerald_necklace,
                    furnace_objs.emerald_necklace,
                    29,
                    60.0,
                    furnace_objs.emerald,
                ),
                goldNecklace(
                    furnace_components.ruby_necklace,
                    furnace_objs.ruby_necklace,
                    40,
                    75.0,
                    furnace_objs.ruby,
                ),
                goldNecklace(
                    furnace_components.diamond_necklace,
                    furnace_objs.diamond_necklace,
                    56,
                    90.0,
                    furnace_objs.diamond,
                ),
                goldNecklace(
                    furnace_components.dragonstone_necklace,
                    furnace_objs.dragonstone_necklace,
                    72,
                    105.0,
                    furnace_objs.dragonstone,
                ),
                goldNecklace(
                    furnace_components.onyx_necklace,
                    furnace_objs.onyx_necklace,
                    82,
                    120.0,
                    furnace_objs.onyx,
                ),
                goldNecklace(
                    furnace_components.zenyte_necklace,
                    furnace_objs.zenyte_necklace,
                    92,
                    165.0,
                    furnace_objs.zenyte,
                ),
                goldAmulet(furnace_components.gold_amulet, furnace_objs.gold_amulet, 8, 30.0),
                goldAmulet(
                    furnace_components.sapphire_amulet,
                    furnace_objs.sapphire_amulet,
                    24,
                    65.0,
                    furnace_objs.sapphire,
                ),
                goldAmulet(
                    furnace_components.emerald_amulet,
                    furnace_objs.emerald_amulet,
                    31,
                    70.0,
                    furnace_objs.emerald,
                ),
                goldAmulet(
                    furnace_components.ruby_amulet,
                    furnace_objs.ruby_amulet,
                    50,
                    85.0,
                    furnace_objs.ruby,
                ),
                goldAmulet(
                    furnace_components.diamond_amulet,
                    furnace_objs.diamond_amulet,
                    70,
                    100.0,
                    furnace_objs.diamond,
                ),
                goldAmulet(
                    furnace_components.dragonstone_amulet,
                    furnace_objs.dragonstone_amulet,
                    80,
                    150.0,
                    furnace_objs.dragonstone,
                ),
                goldAmulet(
                    furnace_components.onyx_amulet,
                    furnace_objs.onyx_amulet,
                    90,
                    165.0,
                    furnace_objs.onyx,
                ),
                goldAmulet(
                    furnace_components.zenyte_amulet,
                    furnace_objs.zenyte_amulet,
                    98,
                    200.0,
                    furnace_objs.zenyte,
                ),
                goldBracelet(furnace_components.gold_bracelet, furnace_objs.gold_bracelet, 7, 25.0),
                goldBracelet(
                    furnace_components.sapphire_bracelet,
                    furnace_objs.sapphire_bracelet,
                    23,
                    60.0,
                    furnace_objs.sapphire,
                ),
                goldBracelet(
                    furnace_components.emerald_bracelet,
                    furnace_objs.emerald_bracelet,
                    30,
                    65.0,
                    furnace_objs.emerald,
                ),
                goldBracelet(
                    furnace_components.ruby_bracelet,
                    furnace_objs.ruby_bracelet,
                    42,
                    80.0,
                    furnace_objs.ruby,
                ),
                goldBracelet(
                    furnace_components.diamond_bracelet,
                    furnace_objs.diamond_bracelet,
                    58,
                    95.0,
                    furnace_objs.diamond,
                ),
                goldBracelet(
                    furnace_components.dragonstone_bracelet,
                    furnace_objs.dragonstone_bracelet,
                    74,
                    110.0,
                    furnace_objs.dragonstone,
                ),
                goldBracelet(
                    furnace_components.onyx_bracelet,
                    furnace_objs.onyx_bracelet,
                    84,
                    125.0,
                    furnace_objs.onyx,
                ),
                goldBracelet(
                    furnace_components.zenyte_bracelet,
                    furnace_objs.zenyte_bracelet,
                    95,
                    180.0,
                    furnace_objs.zenyte,
                ),
            )

        val silverJewelleryRecipes =
            listOf(
                silverRing(
                    furnace_components.silver_opal_ring,
                    furnace_objs.opal_ring,
                    1,
                    10.0,
                    furnace_objs.opal,
                ),
                silverRing(
                    furnace_components.silver_jade_ring,
                    furnace_objs.jade_ring,
                    13,
                    32.0,
                    furnace_objs.jade,
                ),
                silverRing(
                    furnace_components.silver_topaz_ring,
                    furnace_objs.topaz_ring,
                    16,
                    35.0,
                    furnace_objs.red_topaz,
                ),
                silverNecklace(
                    furnace_components.silver_opal_necklace,
                    furnace_objs.opal_necklace,
                    16,
                    35.0,
                    furnace_objs.opal,
                ),
                silverNecklace(
                    furnace_components.silver_jade_necklace,
                    furnace_objs.jade_necklace,
                    25,
                    54.0,
                    furnace_objs.jade,
                ),
                silverNecklace(
                    furnace_components.silver_topaz_necklace,
                    furnace_objs.topaz_necklace,
                    45,
                    70.0,
                    furnace_objs.red_topaz,
                ),
                silverAmulet(
                    furnace_components.silver_opal_amulet,
                    furnace_objs.opal_amulet,
                    27,
                    55.0,
                    furnace_objs.opal,
                ),
                silverAmulet(
                    furnace_components.silver_jade_amulet,
                    furnace_objs.jade_amulet,
                    34,
                    70.0,
                    furnace_objs.jade,
                ),
                silverAmulet(
                    furnace_components.silver_topaz_amulet,
                    furnace_objs.topaz_amulet,
                    45,
                    80.0,
                    furnace_objs.red_topaz,
                ),
                silverBracelet(
                    furnace_components.silver_opal_bracelet,
                    furnace_objs.opal_bracelet,
                    22,
                    45.0,
                    furnace_objs.opal,
                ),
                silverBracelet(
                    furnace_components.silver_jade_bracelet,
                    furnace_objs.jade_bracelet,
                    29,
                    60.0,
                    furnace_objs.jade,
                ),
                silverBracelet(
                    furnace_components.silver_topaz_bracelet,
                    furnace_objs.topaz_bracelet,
                    38,
                    75.0,
                    furnace_objs.red_topaz,
                ),
                silverMisc(
                    furnace_components.holy_symbol,
                    furnace_objs.holy_symbol,
                    furnace_objs.holy_symbol_mould,
                    "holy symbol",
                    "holy symbols",
                    16,
                    50.0,
                ),
                silverMisc(
                    furnace_components.unholy_symbol,
                    furnace_objs.unholy_symbol,
                    furnace_objs.unholy_symbol_mould,
                    "unholy symbol",
                    "unholy symbols",
                    17,
                    50.0,
                ),
                silverMisc(
                    furnace_components.silver_sickle,
                    furnace_objs.silver_sickle,
                    furnace_objs.sickle_mould,
                    "sickle",
                    "sickles",
                    18,
                    50.0,
                ),
                silverMisc(
                    furnace_components.silver_crossbow_bolt,
                    furnace_objs.silver_crossbow_bolts,
                    furnace_objs.silver_bolt_mould,
                    "crossbow bolt",
                    "crossbow bolts",
                    21,
                    50.0,
                    productCount = 10,
                ),
                silverMisc(
                    furnace_components.tiara,
                    furnace_objs.tiara,
                    furnace_objs.tiara_mould,
                    "tiara",
                    "tiaras",
                    23,
                    52.5,
                ),
                silverMisc(
                    furnace_components.agrith_sigil,
                    furnace_objs.agrith_sigil,
                    furnace_objs.agrith_sigil_mould,
                    "demonic sigil",
                    "demonic sigils",
                    70,
                    50.0,
                ),
            )

        val jewelleryRecipes = goldJewelleryRecipes + silverJewelleryRecipes

        val goldJewelleryContext =
            JewelleryContext(
                furnace_interfaces.crafting_gold,
                goldJewelleryRecipes,
                goldJewelleryQuantityButtons,
            )

        val silverJewelleryContext =
            JewelleryContext(
                furnace_interfaces.silver_crafting,
                silverJewelleryRecipes,
                silverJewelleryQuantityButtons,
            )
    }
}
