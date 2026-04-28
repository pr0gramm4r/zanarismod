package org.rsmod.content.areas.city.lumbridge.npcs

import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.game.entity.Player

class HansPlaytimeTest {
    @Test
    fun `format playtime omits zero days and hours`() {
        val now = LocalDateTime.of(2026, 4, 28, 12, 0)
        val player =
            Player().apply {
                lastLogin = now.minusMinutes(17)
                totalPlayTimeSeconds = 0
            }

        val result = HansPlaytime.formatPlaytime(player, now)

        assertEquals("17 minutes", result)
    }

    @Test
    fun `format playtime includes only non-zero units`() {
        val now = LocalDateTime.of(2026, 4, 28, 12, 0)
        val player =
            Player().apply {
                lastLogin = now.minusMinutes(5)
                totalPlayTimeSeconds = (2L * 24 * 60 * 60) + (3L * 60 * 60)
            }

        val result = HansPlaytime.formatPlaytime(player, now)

        assertEquals("2 days, 3 hours, 5 minutes", result)
    }

    @Test
    fun `format playtime falls back to zero minutes`() {
        val now = LocalDateTime.of(2026, 4, 28, 12, 0)
        val player =
            Player().apply {
                lastLogin = now
                totalPlayTimeSeconds = 0
            }

        val result = HansPlaytime.formatPlaytime(player, now)

        assertEquals("0 minutes", result)
    }

    @Test
    fun `account age uses account creation time`() {
        val now = LocalDateTime.of(2026, 4, 28, 12, 0)
        val player =
            Player().apply {
                accountCreatedAt = now.minusDays(42).minusHours(3)
                lastLogin = now
            }

        val result = HansPlaytime.accountAgeInDays(player, now)

        assertEquals(42L, result)
    }
}
