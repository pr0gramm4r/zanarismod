package org.rsmod.content.generic.locs.signpost

import jakarta.inject.Inject
import org.rsmod.api.player.output.Camera.camReset
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onIfClose
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpLoc1
import org.rsmod.game.entity.Player
import org.rsmod.game.enums.EnumTypeMap
import org.rsmod.game.enums.EnumTypeMapResolver
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.map.Direction
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SignpostScript
@Inject
constructor(private val enums: EnumTypeMapResolver) : PluginScript() {
    private lateinit var signposts: EnumTypeMap<CoordGrid, String>

    override fun ScriptContext.startup() {
        signposts = enums[SignpostEnums.signpost_directions]
        for (signpost in signpost_locs.all) {
            onOpLoc1(signpost) { readSignpost(it.loc) }
        }
        onIfModalButton(signpost_components.close_button) { ifClose() }
        onIfClose(signpost_interfaces.signpost) { player.exitSignpost() }
    }

    private fun ProtectedAccess.readSignpost(loc: BoundLocInfo) {
        camForceAngle(rate = 280, rate2 = 0)
        camMoveTo(loc.coords.translateX(1).translateZ(-7), height = 1500, rate = 2, rate2 = 10)
        camLookAt(loc.coords.translateX(1).translateZ(2), height = 450, rate = 2, rate2 = 10)

        val directions = signposts.getValue(loc.coords).split("|")
        val (west, south, north, east) = directions
        ifSetText(signpost_components.signpost_west, west)
        ifSetText(signpost_components.signpost_south, south)
        ifSetText(signpost_components.signpost_north, north)
        ifSetText(signpost_components.signpost_east, east)
        ifOpenMainModal(signpost_interfaces.signpost, colour = -1, transparency = -1)
        ifSetEvents(signpost_components.close_button, -1..-1, IfEvent.Op1)

        faceDirection(Direction.North)
    }

    private fun Player.exitSignpost() {
        // TODO: Investigate when/how this gets sent sometimes.
        // faceDirection(Direction.North)
        camReset(this)
    }
}
