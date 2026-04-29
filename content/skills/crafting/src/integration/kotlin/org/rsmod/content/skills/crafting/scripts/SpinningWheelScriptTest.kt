package org.rsmod.content.skills.crafting.scripts

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
import org.rsmod.content.skills.crafting.configs.spinning_components
import org.rsmod.content.skills.crafting.configs.spinning_interfaces
import org.rsmod.content.skills.crafting.configs.spinning_locs
import org.rsmod.content.skills.crafting.configs.spinning_objs
import org.rsmod.content.skills.crafting.configs.spinning_varps
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.ui.Component
import org.rsmod.game.ui.UserInterface
import org.rsmod.map.CoordGrid

@Execution(ExecutionMode.SAME_THREAD)
class SpinningWheelScriptTest {
    @Test
    fun GameTestState.`open spinning interface`() =
        runGameTest(SpinningWheel::class) {
            val wheel = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), spinning_locs.spinningwheel)
            player.teleport(wheel.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.crafting] = 10
            player.invAdd(player.inv, spinning_objs.wool, count = 2)
            player.invAdd(player.inv, spinning_objs.flax)

            player.opLoc2(wheel)
            advance(ticks = 5)

            assertModalOpen(spinning_interfaces.skillmulti)
            assertEquals(
                Component(components.chatbox_chatmodal.packed),
                player.ui.modals.getComponent(UserInterface(spinning_interfaces.skillmulti.id)),
            )
            assertEquals(
                constants.modal_infinitewidthandheight,
                player.vars[varbits.chatmodal_unclamp],
            )
            assertTrue(player.ui.hasEvent(spinning_components.skillmulti_a, 1, IfEvent.PauseButton))
            assertTrue(player.ui.hasEvent(spinning_components.skillmulti_a, 2, IfEvent.PauseButton))
            assertFalse(
                player.ui.hasEvent(spinning_components.skillmulti_a, 3, IfEvent.PauseButton)
            )
            assertTrue(player.ui.hasEvent(spinning_components.skillmulti_b, 1, IfEvent.PauseButton))
        }

    @Test
    fun GameTestState.`open no spinning options messagebox`() =
        runGameTest(SpinningWheel::class) {
            val wheel = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), spinning_locs.spinningwheel)
            player.teleport(wheel.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.crafting] = 99

            player.opLoc2(wheel)
            advance(ticks = 5)

            assertModalOpen(interfaces.messagebox)
            assertModalNotOpen(spinning_interfaces.skillmulti)
            assertEquals(
                Component(components.chatbox_chatmodal.packed),
                player.ui.modals.getComponent(UserInterface(interfaces.messagebox.id)),
            )
        }

    @Test
    fun GameTestState.`spin wool into ball of wool`() =
        runGameTest(SpinningWheel::class) {
            val wheel = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), spinning_locs.spinningwheel)
            player.teleport(wheel.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.crafting] = 1
            player.invAdd(player.inv, spinning_objs.wool, count = 2)

            player.withProtectedAccess { publish(LocEvents.Op1(wheel, wheel, locTypes[wheel])) }
            advance(ticks = 1)
            player.resumeActiveCoroutine(
                ResumePauseButtonInput(spinning_components.skillmulti_a, 2)
            )
            advance(ticks = 1)
            assertModalNotOpen(spinning_interfaces.skillmulti)
            advance(ticks = 6)

            assertEquals(2, player.inv.count(objTypes[spinning_objs.ball_of_wool]))
            assertDoesNotContain(player.inv, spinning_objs.wool)
            assertEquals(0, player.vars[spinning_varps.skillmulti_previousselection])
        }

    @Test
    fun GameTestState.`filter unavailable recipes by crafting level`() =
        runGameTest(SpinningWheel::class) {
            val wheel = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), spinning_locs.spinningwheel)
            player.teleport(wheel.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.crafting] = 1
            player.invAdd(player.inv, spinning_objs.wool)
            player.invAdd(player.inv, spinning_objs.flax)

            player.withProtectedAccess { publish(LocEvents.Op1(wheel, wheel, locTypes[wheel])) }
            advance(ticks = 1)

            assertTrue(player.ui.hasEvent(spinning_components.skillmulti_a, 1, IfEvent.PauseButton))
            assertFalse(
                player.ui.hasEvent(spinning_components.skillmulti_b, 1, IfEvent.PauseButton)
            )
            player.resumeActiveCoroutine(
                ResumePauseButtonInput(spinning_components.skillmulti_b, 1)
            )
            advance(ticks = 4)

            assertContains(player.inv, spinning_objs.flax)
            assertDoesNotContain(player.inv, spinning_objs.bow_string)
        }

    @Test
    fun GameTestState.`magic roots can make crossbow string and magic string`() =
        runGameTest(SpinningWheel::class) {
            val wheel = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), spinning_locs.spinningwheel)
            player.teleport(wheel.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.crafting] = 30
            player.invAdd(player.inv, spinning_objs.magic_roots, count = 2)

            player.withProtectedAccess { publish(LocEvents.Op1(wheel, wheel, locTypes[wheel])) }
            advance(ticks = 1)

            assertTrue(player.ui.hasEvent(spinning_components.skillmulti_a, 1, IfEvent.PauseButton))
            assertTrue(player.ui.hasEvent(spinning_components.skillmulti_b, 1, IfEvent.PauseButton))
            player.resumeActiveCoroutine(
                ResumePauseButtonInput(spinning_components.skillmulti_b, 1)
            )
            advance(ticks = 4)

            assertContains(player.inv, spinning_objs.magic_roots)
            assertContains(player.inv, spinning_objs.magic_string)
            assertDoesNotContain(player.inv, spinning_objs.xbows_crossbow_string)
            assertEquals(1, player.vars[spinning_varps.skillmulti_previousselection])
        }

    @Test
    fun GameTestState.`all available materials fit skillmulti options`() =
        runGameTest(SpinningWheel::class) {
            val wheel = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), spinning_locs.spinningwheel)
            player.teleport(wheel.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.crafting] = 99
            player.invAdd(player.inv, spinning_objs.wool)
            player.invAdd(player.inv, spinning_objs.viking_golden_fleece)
            player.invAdd(player.inv, spinning_objs.flax)
            player.invAdd(player.inv, spinning_objs.xbows_sinew)
            player.invAdd(player.inv, spinning_objs.oak_roots)
            player.invAdd(player.inv, spinning_objs.willow_roots)
            player.invAdd(player.inv, spinning_objs.maple_roots)
            player.invAdd(player.inv, spinning_objs.yew_roots)
            player.invAdd(player.inv, spinning_objs.magic_roots, count = 2)
            player.invAdd(player.inv, spinning_objs.yak_hair)

            player.withProtectedAccess { publish(LocEvents.Op1(wheel, wheel, locTypes[wheel])) }
            advance(ticks = 1)

            assertTrue(player.ui.hasEvent(spinning_components.skillmulti_a, 1, IfEvent.PauseButton))
            assertTrue(player.ui.hasEvent(spinning_components.skillmulti_j, 1, IfEvent.PauseButton))
            player.resumeActiveCoroutine(
                ResumePauseButtonInput(spinning_components.skillmulti_j, 1)
            )
            advance(ticks = 4)

            assertContains(player.inv, spinning_objs.rope)
            assertDoesNotContain(player.inv, spinning_objs.yak_hair)
            assertEquals(9, player.vars[spinning_varps.skillmulti_previousselection])
        }

    @Test
    fun GameTestState.`using fiber on spinning wheel opens spinning interface`() =
        runGameTest(SpinningWheel::class) {
            val wheel = placeMapLoc(CoordGrid(0, 50, 50, 34, 31), spinning_locs.spinningwheel)
            player.teleport(wheel.coords.translateX(-1))
            player.clearInv()
            player.stats[stats.crafting] = 10
            player.invAdd(player.inv, spinning_objs.flax)

            player.withProtectedAccess {
                publish(
                    LocUEvents.Op(
                        wheel,
                        wheel,
                        locTypes[wheel],
                        objTypes[spinning_objs.flax],
                        invSlot = 0,
                    )
                )
            }
            advance(ticks = 1)

            assertModalOpen(spinning_interfaces.skillmulti)
            player.resumeActiveCoroutine(
                ResumePauseButtonInput(spinning_components.skillmulti_a, 1)
            )
            advance(ticks = 4)

            assertContains(player.inv, spinning_objs.bow_string)
            assertDoesNotContain(player.inv, spinning_objs.flax)
        }
}
