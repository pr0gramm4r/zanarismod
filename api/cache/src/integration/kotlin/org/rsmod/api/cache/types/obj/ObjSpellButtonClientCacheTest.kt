package org.rsmod.api.cache.types.obj

import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.openrs2.cache.Cache
import org.rsmod.api.cache.types.comp.ComponentTypeDecoder
import org.rsmod.api.cache.types.enums.EnumTypeDecoder
import org.rsmod.api.cache.types.param.ParamTypeDecoder
import org.rsmod.game.type.enums.UnpackedEnumType
import org.rsmod.game.type.literal.CacheVarLiteral

class ObjSpellButtonClientCacheTest {
    @Test
    fun `spellbook objects keep client spell button params in js5 cache`() {
        assertSpellbookObjectsKeepClientSpellButtonParams(Path.of(".data/cache/js5"))
    }

    @Test
    fun `spellbook objects keep client spell button params in game cache`() {
        assertSpellbookObjectsKeepClientSpellButtonParams(Path.of(".data/cache/game"))
    }

    @Test
    fun `spellbook objects keep client spell button params in external cache`() {
        val cachePath = System.getProperty("rsmod.externalCachePath")?.let(Path::of)
        assumeTrue(cachePath != null, "Set -Drsmod.externalCachePath to verify an external cache.")
        assertSpellbookObjectsKeepClientSpellButtonParams(checkNotNull(cachePath))
    }

    private fun assertSpellbookObjectsKeepClientSpellButtonParams(cachePath: Path) {
        Cache.open(cachePath).use { cache ->
            val enums = EnumTypeDecoder.decodeAll(cache)
            val objs = ObjTypeDecoder.decodeAll(cache)
            val params = ParamTypeDecoder.decodeAll(cache)
            val components = ComponentTypeDecoder.decodeAll(cache)
            val spellButtonParam = checkNotNull(params[SPELL_BUTTON_PARAM])
            assertTrue(spellButtonParam.typeLiteral == CacheVarLiteral.COMPONENT) {
                "spell_button param $SPELL_BUTTON_PARAM must be a client component param: $spellButtonParam"
            }
            val spellObjs = allClientSpellEnumIds(enums).flatMap { enum ->
                checkNotNull(enums[enum]) { "Missing spell enum: $enum" }
                    .primitiveMap
                    .values
                    .map { it as Int }
            }
            val missing = spellObjs.filter { obj ->
                val spellButton =
                    checkNotNull(objs[obj]) { "Missing spell obj: $obj" }
                        .paramMap
                        ?.primitiveMap
                        ?.get(SPELL_BUTTON_PARAM)
                spellButton !is Int || spellButton == -1
            }
            assertTrue(missing.isEmpty()) {
                "$cachePath spell objs missing spell_button param $SPELL_BUTTON_PARAM: $missing"
            }
            val invalid = spellObjs.associateWith { obj ->
                checkNotNull(objs[obj]) { "Missing spell obj: $obj" }
                    .paramMap
                    ?.primitiveMap
                    ?.get(SPELL_BUTTON_PARAM) as Int
            }.filterValues { packedComponent ->
                val interfaceId = packedComponent shr 16
                val componentId = packedComponent and 0xFFFF
                interfaceId !in CLIENT_INTERFACE_RANGE ||
                    componentId < 0 ||
                    !components.containsKey(packedComponent)
            }
            assertTrue(invalid.isEmpty()) {
                "$cachePath spell objs with invalid spell_button components: $invalid"
            }
        }
    }

    private fun allClientSpellEnumIds(enums: Map<Int, *>): List<Int> {
        val spellbookEnumIds = enumValues(enums, SPELLBOOKS_ENUM)
        val nestedSpellbookEnumIds = enumValues(enums, SPELLBOOK_WRAPPERS_ENUM).flatMap { enum ->
            enumValues(enums, enum)
        }
        return spellbookEnumIds + nestedSpellbookEnumIds
    }

    private fun enumValues(enums: Map<Int, *>, enum: Int): List<Int> {
        val type =
            checkNotNull(enums[enum] as? UnpackedEnumType<*, *>) { "Missing spell enum: $enum" }
        return type.primitiveMap.values.map { it as Int }
    }

    private companion object {
        const val SPELLBOOKS_ENUM = 1981
        const val SPELLBOOK_WRAPPERS_ENUM = 5280
        const val SPELL_BUTTON_PARAM = 596
        val CLIENT_INTERFACE_RANGE = 0 until 955
    }
}
