package org.rsmod.api.cache.types.npc

import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.openrs2.cache.Cache

class NpcServerOnlyEditorClientCacheTest {
    @Test
    fun `server-only npc edits do not rewrite client cache definitions`() {
        Cache.open(Path.of(".data/cache/vanilla")).use { vanilla ->
            Cache.open(Path.of(".data/cache/js5")).use { js5 ->
                val serverOnlyEditedIds =
                    listOf(
                        1527, // 0_50_50_freshfish
                        1530, // 0_50_49_saltfish
                        2002, // duck_update_ducklings
                        3106, // man
                        3108, // man3
                    )
                for (id in serverOnlyEditedIds) {
                    assertArrayEquals(vanilla.readConfigBytes(id), js5.readConfigBytes(id))
                }
            }
        }
    }

    private fun Cache.readConfigBytes(id: Int): ByteArray {
        val buf = read(2, 9, id)
        return try {
            val bytes = ByteArray(buf.readableBytes())
            buf.getBytes(buf.readerIndex(), bytes)
            bytes
        } finally {
            buf.release()
        }
    }
}
