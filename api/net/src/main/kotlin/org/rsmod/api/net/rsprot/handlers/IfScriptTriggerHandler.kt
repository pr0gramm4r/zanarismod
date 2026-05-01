package org.rsmod.api.net.rsprot.handlers

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import net.rsprot.protocol.game.incoming.buttons.IfScriptTrigger
import net.rsprot.protocol.game.incoming.buttons.IfScriptTrigger.ParameterTypes
import org.rsmod.api.player.ui.IfOverlayScriptTrigger
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player
import org.rsmod.game.type.comp.ComponentTypeList
import org.rsmod.game.type.interf.InterfaceTypeList
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.ui.Component

class IfScriptTriggerHandler
@Inject
constructor(
    private val eventBus: EventBus,
    private val interfaceTypes: InterfaceTypeList,
    private val componentTypes: ComponentTypeList,
    private val objTypes: ObjTypeList,
) : MessageHandler<IfScriptTrigger> {
    private val logger = InlineLogger()

    private val IfScriptTrigger.asComponent: Component
        get() = Component(interfaceId, componentId)

    override fun handle(player: Player, message: IfScriptTrigger) {
        val componentType = componentTypes[message.asComponent]
        val interfaceType = interfaceTypes[message.asComponent]
        if (!player.ui.containsOverlay(interfaceType) && !player.ui.containsTopLevel(interfaceType)) {
            message.release()
            return
        }

        val parameterTypes = message.parameterTypesOrNull()
        if (parameterTypes == null) {
            logger.debug { "[Overlay][Unhandled] IfScriptTrigger: $message" }
            message.release()
            return
        }

        val args = message.decode(parameterTypes).args
        val event =
            IfOverlayScriptTrigger(
                player = player,
                component = componentType,
                comsub = message.sub,
                obj = objTypes[message.obj],
                crc = message.crc,
                args = args,
            )
        logger.debug { "[Overlay] IfScriptTrigger: $message (event=$event)" }
        eventBus.publish(event)
    }

    private fun IfScriptTrigger.parameterTypesOrNull(): ParameterTypes? =
        when (crc) {
            SETTINGS_MASTER_VOLUME_SCRIPT,
            SETTINGS_AUDIO_VOLUME_SCRIPT -> ParameterTypes.of(ParameterTypes.INT)
            SETTINGS_SOUND_VOLUME_SCRIPT,
            SETTINGS_AREA_SOUND_VOLUME_SCRIPT -> ParameterTypes.of(ParameterTypes.INT)
            SETTINGS_VOLUME_SCRIPT -> ParameterTypes.of(ParameterTypes.INT, ParameterTypes.INT)
            else -> null
        }

    private companion object {
        private const val SETTINGS_MASTER_VOLUME_SCRIPT = -1213750982 // 3081216314
        private const val SETTINGS_AUDIO_VOLUME_SCRIPT = 406545006
        private const val SETTINGS_SOUND_VOLUME_SCRIPT = -1069744872 // 3225222424
        private const val SETTINGS_AREA_SOUND_VOLUME_SCRIPT = -1801605717 // 2493361579
        private const val SETTINGS_VOLUME_SCRIPT = -878888337 // 3416078959
    }
}
