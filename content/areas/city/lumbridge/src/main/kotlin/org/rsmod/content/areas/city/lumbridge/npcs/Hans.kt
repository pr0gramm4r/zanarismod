package org.rsmod.content.areas.city.lumbridge.npcs

import java.time.Duration
import java.time.LocalDateTime
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Hans : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(lumbridge_npcs.hans) { hansDialogue(it.npc) }
        onOpNpc3(lumbridge_npcs.hans) { hansAgeDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.hansDialogue(npc: Npc) =
        startDialogue(npc) { optionsDialogue(npc) }

    private suspend fun Dialogue.optionsDialogue(npc: Npc) {
        chatNpc(neutral, "Hello. What are you doing here?")
        val choice =
            choice5(
                "I'm looking for whoever is in charge of this place.",
                1,
                "I have come to kill everyone in this castle!",
                2,
                "I don't know. I'm lost. Where am I?",
                3,
                "Can you tell me how long I've been here?",
                4,
                "Nothing.",
                5,
            )
        when (choice) {
            1 -> {
                chatPlayer(neutral, "I'm looking for whoever is in charge of this place.")
                chatNpc(neutral, "Who, the Duke? He's in his study, on the first floor.")
            }
            2 -> {
                chatPlayer(angry, "I have come to kill everyone in this castle!")
                npc.playerEscape(player)
                delay(2)
                npc.say("Help! Help!")
            }
            3 -> {
                chatPlayer(confused, "I don't know. I'm lost. Where am I?")
                chatNpc(
                    neutral,
                    "You are in Lumbridge Castle, in the Kingdom of " +
                        "Misthalin. Across the river, the road leads north to " +
                        "Varrock, and to the west lies Draynor Village.",
                )
            }
            4 -> {
                chatPlayer(quiz, "Can you tell me how long I've been here?")
                chatNpc(
                    laugh,
                    "Ahh, I see all the newcomers arriving in Lumbridge, " +
                        "fresh-faced and eager for adventure. I remember you...",
                )
                playtimeDialogue()
            }
            5 -> {
                chatPlayer(shifty, "Nothing.")
            }
        }
    }

    private suspend fun ProtectedAccess.hansAgeDialogue(npc: Npc) =
        startDialogue(npc) { playtimeDialogue() }

    private suspend fun Dialogue.playtimeDialogue() {
        val now = LocalDateTime.now()
        val playtime = HansPlaytime.formatPlaytime(access.player, now)
        val accountAge = HansPlaytime.accountAgeInDays(access.player, now)
        chatNpc(
            happy,
            "You've spent $playtime in the world since you arrived " +
                "$accountAge ${"day".plural(accountAge)} ago.",
        )
    }
}

internal object HansPlaytime {
    fun formatPlaytime(player: Player, now: LocalDateTime): String {
        val seconds = player.totalPlayTimeSeconds + player.currentSessionSeconds(now)
        return formatDuration(seconds)
    }

    fun accountAgeInDays(player: Player, now: LocalDateTime): Long {
        val createdAt = player.accountCreatedAt ?: player.lastLogin
        return Duration.between(createdAt, now).toDays().coerceAtLeast(0)
    }

    private fun Player.currentSessionSeconds(now: LocalDateTime): Long =
        Duration.between(lastLogin, now).seconds.coerceAtLeast(0)

    private fun formatDuration(totalSeconds: Long): String {
        val totalMinutes = totalSeconds.coerceAtLeast(0) / 60
        val days = totalMinutes / MINUTES_PER_DAY
        val hours = (totalMinutes % MINUTES_PER_DAY) / MINUTES_PER_HOUR
        val minutes = totalMinutes % MINUTES_PER_HOUR
        val parts = buildList {
            if (days > 0) {
                add("$days ${"day".plural(days)}")
            }
            if (hours > 0) {
                add("$hours ${"hour".plural(hours)}")
            }
            if (minutes > 0 || isEmpty()) {
                add("$minutes ${"minute".plural(minutes)}")
            }
        }
        return parts.joinToString()
    }

    private const val MINUTES_PER_HOUR = 60
    private const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
}

private fun String.plural(count: Long): String = if (count == 1L) this else "${this}s"
