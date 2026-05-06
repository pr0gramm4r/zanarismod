package org.rsmod.content.generic.locs.signpost

import net.rsprot.protocol.game.outgoing.camera.CamLookAtV2
import net.rsprot.protocol.game.outgoing.camera.CamMoveToV2
import net.rsprot.protocol.game.outgoing.interfaces.IfSetEventsV2
import net.rsprot.protocol.game.outgoing.misc.player.RunClientScript
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.components
import org.rsmod.api.player.events.interact.LocEvents
import org.rsmod.api.player.ui.ifClose
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.interact.InteractionOp
import org.rsmod.game.ui.Component
import org.rsmod.game.ui.UserInterface
import org.rsmod.map.CoordGrid

class SignpostScriptTest {
    @Test
    fun GameTestState.`lumbridge signpost opens direction modal`() =
        runGameTest(SignpostScript::class) {
            val signpost = placeMapLoc(LUMBRIDGE_SIGNPOST, test_locs.lumbridge)
            player.teleport(signpost.coords.translateZ(-1))

            player.withProtectedAccess {
                publish(LocEvents.Op1(signpost, signpost, locTypes[signpost]))
            }
            advance(ticks = 1)

            assertModalOpen(test_interfaces.signpost)
            assertEquals(
                Component(components.mainmodal.packed),
                player.ui.modals.getComponent(UserInterface(test_interfaces.signpost.id)),
            )

        }

    @Test
    fun GameTestState.`clicking signpost from range walks before opening modal`() =
        runGameTest(SignpostScript::class) {
            val signpost = placeMapLoc(LUMBRIDGE_SIGNPOST, test_locs.lumbridge)
            player.teleport(signpost.coords.translateZ(-5))
            advance()

            player.opLoc1(signpost)
            advance()

            assertModalNotOpen(test_interfaces.signpost)

            advanceUntil(
                predicate = { player.ui.containsModal(test_interfaces.signpost) },
                timeoutTicks = 20,
                timeoutMessage = { "Signpost modal did not open after walking into range." },
            )
            assertTrue(
                client.anyOf<RunClientScript> {
                    it.id == 143 && it.values.map(Any::toString) == listOf("280", "0")
                }
            )
            assertTrue(
                client.anyOf<RunClientScript> {
                    it.id == 2524 && it.values.map(Any::toString) == listOf("-1", "-1")
                }
            )
            assertTrue(
                client.anyOf<CamMoveToV2> {
                    it.x == 3236 && it.z == 3221 && it.height == 1500 && it.rate == 2 &&
                        it.rate2 == 10
                }
            )
            assertTrue(
                client.anyOf<CamLookAtV2> {
                    it.x == 3236 && it.z == 3230 && it.height == 450 && it.rate == 2 &&
                        it.rate2 == 10
                }
            )
            assertFalse(
                client.anyOf<IfSetEventsV2> {
                    it.interfaceId == components.mainmodal.interfaceId &&
                        it.componentId == components.mainmodal.component
                }
            )
        }

    @Test
    fun GameTestState.`ground click interrupts pending signpost interaction`() =
        runGameTest(SignpostScript::class) {
            val signpost = placeMapLoc(LUMBRIDGE_SIGNPOST, test_locs.lumbridge)
            val start = signpost.coords.translateZ(-5)
            val interrupt = start.translateX(-2)
            player.teleport(start)
            advance()

            player.opLoc1(signpost)
            advance()
            assertModalNotOpen(test_interfaces.signpost)

            player.moveGameClick(interrupt)
            advanceUntil(
                predicate = { player.coords == interrupt },
                timeoutTicks = 10,
                timeoutMessage = { "Player did not move to interrupt destination." },
            )

            assertModalNotOpen(test_interfaces.signpost)
        }

    @Test
    fun GameTestState.`closing signpost modal clears it`() =
        runGameTest(SignpostScript::class) {
            val signpost = placeMapLoc(LUMBRIDGE_SIGNPOST, test_locs.lumbridge)
            player.teleport(signpost.coords.translateZ(-1))

            player.withProtectedAccess {
                publish(LocEvents.Op1(signpost, signpost, locTypes[signpost]))
            }
            advance(ticks = 1)
            assertModalOpen(test_interfaces.signpost)

            player.ifClose(eventBus)

            assertModalNotOpen(test_interfaces.signpost)
        }

    @Test
    fun GameTestState.`world click closes signpost modal without moving player`() =
        runGameTest(SignpostScript::class) {
            val signpost = placeMapLoc(LUMBRIDGE_SIGNPOST, test_locs.lumbridge)
            val start = signpost.coords.translateZ(-1)
            player.teleport(start)

            player.withProtectedAccess {
                publish(LocEvents.Op1(signpost, signpost, locTypes[signpost]))
            }
            advance(ticks = 1)
            assertModalOpen(test_interfaces.signpost)

            player.moveGameClick(start.translateX(3))
            advance(ticks = 1)

            assertModalNotOpen(test_interfaces.signpost)
            assertEquals(start, player.coords)
            assertTrue(player.routeDestination.isEmpty())
        }

    @Test
    fun GameTestState.`lumbridge signpost has a readable op`() =
        runGameTest(SignpostScript::class) {
            assertTrue(locTypes[test_locs.lumbridge].hasOp(InteractionOp.Op1))
            assertEquals("Read", locTypes[test_locs.lumbridge].op[0])
        }

    private companion object {
        val LUMBRIDGE_SIGNPOST = CoordGrid(0, 50, 50, 35, 28)
    }
}

internal typealias test_locs = TestLocs

internal typealias test_interfaces = TestInterfaces

internal object TestLocs : LocReferences() {
    val lumbridge = find("signpost_good_fourway", 5944252288185951991)
}

internal object TestInterfaces : InterfaceReferences() {
    val signpost = find("aide_compass", 9223372036367484796)
}
