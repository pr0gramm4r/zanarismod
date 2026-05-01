package org.rsmod.api.net.rsprot.provider

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import net.rsprot.protocol.api.ChannelExceptionHandler
import net.rsprot.protocol.api.IncomingGameMessageConsumerExceptionHandler
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.api.handlers.ExceptionHandlers
import net.rsprot.protocol.message.IncomingGameMessage
import org.rsmod.game.entity.Player

object ExceptionHandlersProvider {
    private val logger = InlineLogger()

    fun provide(): ExceptionHandlers<Player> {
        val channelHandler = ChannelExceptionHandler { ctx: ChannelHandlerContext, cause: Throwable ->
            logger.error(cause) { "Closing RSProt channel after network exception." }
            ctx.close()
        }
        val messageHandler =
            IncomingGameMessageConsumerExceptionHandler {
                session: Session<Player>,
                message: IncomingGameMessage,
                throwable: Throwable ->
                logger.error(throwable) {
                    "Closing RSProt session after incoming message exception: $message"
                }
                session.requestClose()
            }
        return ExceptionHandlers(channelHandler, messageHandler)
    }
}
