package org.rsmod.content.generic.locs.doors

import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid

class DoorScriptTest {
    @Test
    fun GameTestState.`open and close elf door`() =
        runGameTest(DoorScript::class) {
            val closedType = findLocTypes { it.internalName == "elfdoor" }.first()
            val openedType = findLocTypes { it.internalName == "elfdooropen" }.first()
            assertEquals("An ornately-fashioned door.", closedType.desc)
            assertEquals("An ornately-fashioned door.", openedType.desc)

            val closedCoords = CoordGrid(1, 50, 50, 7, 14)
            val closed =
                placeMapLoc(
                    coords = closedCoords,
                    type = closedType,
                    shape = LocShape.WallStraight,
                    angle = LocAngle.North,
                )
            player.teleport(closedCoords.translateZ(-1))

            player.opLoc1(closed)
            advance(ticks = 2)

            val openedCoords = closedCoords.translateZ(1)
            val opened = findLoc(openedCoords, openedType)
            assertDoesNotExist(closed)
            assertNotNull(opened)
            assertEquals(LocShape.WallStraight.id, opened.shapeId)
            assertEquals(LocAngle.East.id, opened.angleId)

            player.opLoc1(BoundLocInfo(opened, openedType))
            advance(ticks = 2)

            val restored = findLoc(closedCoords, closedType)
            assertDoesNotExist(openedCoords, openedType)
            assertNotNull(restored)
            assertEquals(LocShape.WallStraight.id, restored.shapeId)
            assertEquals(LocAngle.North.id, restored.angleId)
        }
}
