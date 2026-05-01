package org.rsmod.content.areas.city.lumbridge.configs

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import jakarta.inject.Inject
import net.rsprot.protocol.game.outgoing.sound.MidiSongV2
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.annotations.InternalApi
import org.rsmod.api.config.refs.areas
import org.rsmod.api.music.MusicRepository
import org.rsmod.api.music.plugin.scripts.MusicAreaScript
import org.rsmod.api.music.plugin.scripts.MusicTimerScript
import org.rsmod.api.player.music.MusicPlayer
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.area.AreaIndex
import org.rsmod.map.CoordGrid

class LumbridgeMusicTest {
    @Test
    @OptIn(InternalApi::class)
    fun GameTestState.`entering lumbridge unlocks and plays area music`() =
        runInjectedGameTest(
            LumbridgeMusicDeps::class,
            childModule = LumbridgeMusicTestModule,
            scripts = arrayOf(MusicAreaScript::class, MusicTimerScript::class),
        ) { deps ->
            val spawn = CoordGrid(0, 50, 50, 21, 18)
            deps.music.load()
            deps.areaIndex.registerAll(spawn, listOf(areas.lumbridge.id.toShort()).iterator())
            player.placeAt(spawn)

            advance()

            assertTrue(client.hasAny<MidiSongV2>()) {
                "Expected Lumbridge area entry to send a midi song."
            }
            assertTrue(client.outgoingMessages.any { it.toString().contains("music track") }) {
                "Expected Lumbridge area entry to send at least one music unlock message."
            }
        }

    @Test
    @OptIn(InternalApi::class)
    fun GameTestState.`leaving lumbridge stops area music`() =
        runInjectedGameTest(
            LumbridgeMusicDeps::class,
            childModule = LumbridgeMusicTestModule,
            scripts = arrayOf(MusicAreaScript::class, MusicTimerScript::class),
        ) { deps ->
            val spawn = CoordGrid(0, 50, 50, 21, 18)
            deps.music.load()
            deps.areaIndex.registerAll(spawn, listOf(areas.lumbridge.id.toShort()).iterator())
            player.placeAt(spawn)
            advance()

            client.clearOutgoing()
            player.placeAt(CoordGrid(0, 48, 48, 0, 0))
            advance()

            assertTrue(client.hasAny<MidiSongV2>()) {
                "Expected Lumbridge area exit to stop area music."
            }
        }

    @Test
    @OptIn(InternalApi::class)
    fun GameTestState.`enabling music resends current area track`() =
        runInjectedGameTest(
            LumbridgeMusicDeps::class,
            childModule = LumbridgeMusicTestModule,
            scripts = arrayOf(MusicAreaScript::class, MusicTimerScript::class),
        ) { deps ->
            val spawn = CoordGrid(0, 50, 50, 21, 18)
            deps.music.load()
            deps.areaIndex.registerAll(spawn, listOf(areas.lumbridge.id.toShort()).iterator())
            player.placeAt(spawn)
            advance()

            deps.musicPlayer.stop(player)
            client.clearOutgoing()
            deps.musicPlayer.enable(player)

            assertTrue(client.hasAny<MidiSongV2>()) {
                "Expected music enable to resend a midi song."
            }
        }

    private class LumbridgeMusicDeps
    @Inject
    constructor(val music: MusicRepository, val musicPlayer: MusicPlayer, val areaIndex: AreaIndex)

    private object LumbridgeMusicTestModule : AbstractModule() {
        override fun configure() {
            bind(MusicRepository::class.java).`in`(Scopes.SINGLETON)
        }
    }
}
