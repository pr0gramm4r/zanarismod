package org.rsmod.content.skills.smithing.scripts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.events.interact.LocEvents
import org.rsmod.api.player.events.interact.LocUEvents
import org.rsmod.api.player.input.ResumePauseButtonInput
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.smithing.configs.furnace_components
import org.rsmod.content.skills.smithing.configs.furnace_interfaces
import org.rsmod.content.skills.smithing.configs.furnace_locs
import org.rsmod.content.skills.smithing.configs.furnace_objs
import org.rsmod.content.skills.smithing.configs.furnace_varps
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.ui.Component
import org.rsmod.game.ui.UserInterface
import org.rsmod.map.CoordGrid

@Execution(ExecutionMode.SAME_THREAD)
class FurnaceScriptTest {
    @Test
    fun GameTestState.`open furnace smelting interface`() =
        runGameTest(Furnace::class) {
            val furnace = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), furnace_locs.newbiefurnace)
            player.teleport(furnace.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.smithing] = 1
            player.invAdd(player.inv, furnace_objs.copper_ore)
            player.invAdd(player.inv, furnace_objs.tin_ore)

            player.withProtectedAccess {
                publish(LocEvents.Op2(furnace, furnace, locTypes[furnace]))
            }
            advance(ticks = 1)

            assertModalOpen(furnace_interfaces.skillmulti)
            assertEquals(
                Component(components.chatbox_chatmodal.packed),
                player.ui.modals.getComponent(UserInterface(furnace_interfaces.skillmulti.id)),
            )
            assertEquals(
                constants.modal_infinitewidthandheight,
                player.vars[varbits.chatmodal_unclamp],
            )
            assertTrue(player.ui.hasEvent(furnace_components.skillmulti_a, 1, IfEvent.PauseButton))
            assertFalse(player.ui.hasEvent(furnace_components.skillmulti_a, 2, IfEvent.PauseButton))
        }

    @Test
    fun GameTestState.`open no smelting options messagebox`() =
        runGameTest(Furnace::class) {
            val furnace = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), furnace_locs.newbiefurnace)
            player.teleport(furnace.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.smithing] = 99

            player.withProtectedAccess {
                publish(LocEvents.Op2(furnace, furnace, locTypes[furnace]))
            }
            advance(ticks = 1)

            assertModalOpen(interfaces.messagebox)
            assertModalNotOpen(furnace_interfaces.skillmulti)
            assertEquals(
                Component(components.chatbox_chatmodal.packed),
                player.ui.modals.getComponent(UserInterface(interfaces.messagebox.id)),
            )
        }

    @Test
    fun GameTestState.`smelt bronze bar`() =
        runGameTest(Furnace::class) {
            val furnace = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), furnace_locs.newbiefurnace)
            player.teleport(furnace.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.smithing] = 1
            player.invAdd(player.inv, furnace_objs.copper_ore)
            player.invAdd(player.inv, furnace_objs.tin_ore)

            player.withProtectedAccess {
                publish(LocEvents.Op2(furnace, furnace, locTypes[furnace]))
            }
            advance(ticks = 1)
            player.resumeActiveCoroutine(ResumePauseButtonInput(furnace_components.skillmulti_a, 1))
            assertMessageSent("You smelt the copper and tin together in the furnace.")
            advance(ticks = 2)

            assertContains(player.inv, furnace_objs.bronze_bar)
            assertDoesNotContain(player.inv, furnace_objs.copper_ore)
            assertDoesNotContain(player.inv, furnace_objs.tin_ore)
            assertMessageSent("You retrieve a bar of bronze.")
            assertEquals(0, player.vars[furnace_varps.skillmulti_previousselection])
        }

    @Test
    fun GameTestState.`fail to smelt iron bar`() =
        runGameTest(Furnace::class) {
            val furnace = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), furnace_locs.newbiefurnace)
            player.teleport(furnace.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.smithing] = 15
            player.invAdd(player.inv, furnace_objs.iron_ore)

            player.withProtectedAccess {
                publish(LocEvents.Op2(furnace, furnace, locTypes[furnace]))
            }
            advance(ticks = 1)
            random.nextBoolean = true
            player.resumeActiveCoroutine(ResumePauseButtonInput(furnace_components.skillmulti_a, 1))
            assertMessageSent("You smelt the iron in the furnace.")
            advance(ticks = 2)

            assertDoesNotContain(player.inv, furnace_objs.iron_bar)
            assertDoesNotContain(player.inv, furnace_objs.iron_ore)
            assertMessageSent("The ore is too impure and you fail to refine it.")
        }

    @Test
    fun GameTestState.`validate smelting level requirement`() =
        runGameTest(Furnace::class) {
            val furnace = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), furnace_locs.newbiefurnace)
            player.teleport(furnace.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.smithing] = 1
            player.invAdd(player.inv, furnace_objs.copper_ore)
            player.invAdd(player.inv, furnace_objs.tin_ore)
            player.invAdd(player.inv, furnace_objs.iron_ore)

            player.withProtectedAccess {
                publish(LocEvents.Op2(furnace, furnace, locTypes[furnace]))
            }
            advance(ticks = 1)
            assertTrue(player.ui.hasEvent(furnace_components.skillmulti_a, 1, IfEvent.PauseButton))
            assertFalse(player.ui.hasEvent(furnace_components.skillmulti_b, 1, IfEvent.PauseButton))
            player.resumeActiveCoroutine(ResumePauseButtonInput(furnace_components.skillmulti_b, 1))
            advance(ticks = 3)

            assertContains(player.inv, furnace_objs.iron_ore)
            assertDoesNotContain(player.inv, furnace_objs.iron_bar)
        }

    @Test
    fun GameTestState.`remaps visible bar buttons after filtering unavailable bars`() =
        runGameTest(Furnace::class) {
            val furnace = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), furnace_locs.newbiefurnace)
            player.teleport(furnace.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.smithing] = 30
            player.invAdd(player.inv, furnace_objs.copper_ore)
            player.invAdd(player.inv, furnace_objs.tin_ore)
            player.invAdd(player.inv, furnace_objs.iron_ore)
            player.invAdd(player.inv, furnace_objs.coal, count = 2)

            player.withProtectedAccess {
                publish(LocEvents.Op2(furnace, furnace, locTypes[furnace]))
            }
            advance(ticks = 1)
            assertTrue(player.ui.hasEvent(furnace_components.skillmulti_c, 1, IfEvent.PauseButton))
            player.resumeActiveCoroutine(ResumePauseButtonInput(furnace_components.skillmulti_c, 1))
            assertMessageSent("You smelt the iron in the furnace along with two heaps of coal.")
            advance(ticks = 2)

            assertContains(player.inv, furnace_objs.steel_bar)
            assertDoesNotContain(player.inv, furnace_objs.blurite_bar)
            assertMessageSent("You retrieve a bar of steel.")
            assertEquals(2, player.vars[furnace_varps.skillmulti_previousselection])
        }

    @Test
    fun GameTestState.`make gold ring`() =
        runGameTest(Furnace::class) {
            val furnace = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), furnace_locs.newbiefurnace)
            player.teleport(furnace.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.crafting] = 5
            player.invAdd(player.inv, furnace_objs.gold_bar)
            player.invAdd(player.inv, furnace_objs.ring_mould)

            player.withProtectedAccess {
                publish(
                    LocUEvents.Op(
                        furnace,
                        furnace,
                        locTypes[furnace],
                        objTypes[furnace_objs.gold_bar],
                        invSlot = 0,
                    )
                )
            }
            advance(ticks = 1)
            player.ifButton(furnace_components.gold_ring)
            advance(ticks = 3)

            assertContains(player.inv, furnace_objs.gold_ring)
            assertContains(player.inv, furnace_objs.ring_mould)
            assertDoesNotContain(player.inv, furnace_objs.gold_bar)
        }

    @Test
    fun GameTestState.`use silver bar opens silver crafting interface`() =
        runGameTest(Furnace::class) {
            val furnace = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), furnace_locs.newbiefurnace)
            player.teleport(furnace.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.crafting] = 23
            player.invAdd(player.inv, furnace_objs.silver_bar)
            player.invAdd(player.inv, furnace_objs.tiara_mould)

            player.withProtectedAccess {
                publish(
                    LocUEvents.Op(
                        furnace,
                        furnace,
                        locTypes[furnace],
                        objTypes[furnace_objs.silver_bar],
                        invSlot = 0,
                    )
                )
            }
            advance(ticks = 1)

            assertModalOpen(furnace_interfaces.silver_crafting)
            assertModalNotOpen(furnace_interfaces.crafting_gold)
            assertTrue(player.ui.hasEvent(furnace_components.tiara, 0, IfEvent.Op1))
            assertFalse(
                player.ui.hasEvent(furnace_components.silver_crafting_make_1, 0, IfEvent.Op1)
            )
            assertTrue(
                player.ui.hasEvent(furnace_components.silver_crafting_make_all, 0, IfEvent.Op1)
            )
            player.ifButton(furnace_components.tiara)
            advance(ticks = 1)
            assertModalNotOpen(furnace_interfaces.silver_crafting)
            advance(ticks = 3)

            assertContains(player.inv, furnace_objs.tiara)
            assertContains(player.inv, furnace_objs.tiara_mould)
            assertDoesNotContain(player.inv, furnace_objs.silver_bar)
        }

    @Test
    fun GameTestState.`gold crafting quantity buttons hide duplicate amounts`() =
        runGameTest(Furnace::class) {
            val furnace = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), furnace_locs.newbiefurnace)
            player.teleport(furnace.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.crafting] = 5
            player.invAdd(player.inv, furnace_objs.gold_bar, count = 5)
            player.invAdd(player.inv, furnace_objs.ring_mould)

            player.withProtectedAccess {
                publish(
                    LocUEvents.Op(
                        furnace,
                        furnace,
                        locTypes[furnace],
                        objTypes[furnace_objs.gold_bar],
                        invSlot = 0,
                    )
                )
            }
            advance(ticks = 1)

            assertModalOpen(furnace_interfaces.crafting_gold)
            assertTrue(player.ui.hasEvent(furnace_components.crafting_gold_make_1, 0, IfEvent.Op1))
            assertFalse(player.ui.hasEvent(furnace_components.crafting_gold_make_5, 0, IfEvent.Op1))
            assertFalse(
                player.ui.hasEvent(furnace_components.crafting_gold_make_10, 0, IfEvent.Op1)
            )
            assertTrue(player.ui.hasEvent(furnace_components.crafting_gold_make_x, 0, IfEvent.Op1))
            assertTrue(
                player.ui.hasEvent(furnace_components.crafting_gold_make_all, 0, IfEvent.Op1)
            )
            player.ifButton(furnace_components.crafting_gold_make_all)
            advance(ticks = 1)
            player.ifButton(furnace_components.gold_ring)
            advance(ticks = 1)
            assertModalNotOpen(furnace_interfaces.crafting_gold)
            advance(ticks = 12)

            assertEquals(5, player.inv.count(objTypes[furnace_objs.gold_ring]))
            assertContains(player.inv, furnace_objs.ring_mould)
            assertDoesNotContain(player.inv, furnace_objs.gold_bar)
        }
}
