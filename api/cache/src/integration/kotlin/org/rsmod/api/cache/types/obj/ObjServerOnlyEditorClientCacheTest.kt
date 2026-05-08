package org.rsmod.api.cache.types.obj

import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.openrs2.cache.Cache

class ObjServerOnlyEditorClientCacheTest {
    @Test
    fun `server-only obj edits do not rewrite client cache definitions`() {
        Cache.open(Path.of(".data/cache/vanilla")).use { vanilla ->
            Cache.open(Path.of(".data/cache/js5")).use { js5 ->
                val serverOnlyEditedIds =
                    listOf(
                        1265, // bronze_pickaxe
                        1267, // iron_pickaxe
                        1351, // bronze_axe
                    )
                for (id in serverOnlyEditedIds) {
                    assertArrayEquals(vanilla.readConfigBytes(id), js5.readConfigBytes(id))
                }
            }
        }
    }

    private fun Cache.readConfigBytes(id: Int): ByteArray {
        val buf = read(2, 10, id)
        return try {
            val bytes = ByteArray(buf.readableBytes())
            buf.getBytes(buf.readerIndex(), bytes)
            bytes
        } finally {
            buf.release()
        }
    }
}
