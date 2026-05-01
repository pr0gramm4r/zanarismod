package org.rsmod.api.cache.map

import io.netty.buffer.ByteBuf
import jakarta.inject.Inject
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.openrs2.buffer.use
import org.openrs2.cache.Cache
import org.openrs2.crypto.SymmetricKey
import org.rsmod.annotations.GameCache
import org.rsmod.annotations.InternalApi
import org.rsmod.api.cache.Js5Archives
import org.rsmod.api.cache.map.area.MapAreaDecoder
import org.rsmod.api.cache.map.area.MapAreaDefinition
import org.rsmod.api.cache.map.loc.MapLocDefinition
import org.rsmod.api.cache.map.loc.MapLocListDecoder
import org.rsmod.api.cache.map.loc.MapLocListDefinition
import org.rsmod.api.cache.map.npc.MapNpcDefinition
import org.rsmod.api.cache.map.npc.MapNpcListDecoder
import org.rsmod.api.cache.map.npc.MapNpcListDefinition
import org.rsmod.api.cache.map.obj.MapObjDefinition
import org.rsmod.api.cache.map.obj.MapObjListDecoder
import org.rsmod.api.cache.map.obj.MapObjListDefinition
import org.rsmod.api.cache.map.tile.MapTileDecoder
import org.rsmod.api.cache.map.tile.MapTileSimpleDefinition
import org.rsmod.api.cache.util.InlineByteBuf
import org.rsmod.api.cache.util.readOrNull
import org.rsmod.api.cache.util.toInlineBuf
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.game.area.AreaIndex
import org.rsmod.game.entity.Npc
import org.rsmod.game.loc.LocEntity
import org.rsmod.game.map.LocZoneStorage
import org.rsmod.game.map.collision.toggleLoc
import org.rsmod.game.map.xtea.XteaMap
import org.rsmod.game.obj.Obj
import org.rsmod.game.obj.ObjEntity
import org.rsmod.game.obj.ObjScope
import org.rsmod.game.type.loc.LocTypeList
import org.rsmod.game.type.npc.NpcTypeList
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.map.CoordGrid
import org.rsmod.map.square.MapSquareGrid
import org.rsmod.map.square.MapSquareKey
import org.rsmod.map.util.LocalMapSquareZone
import org.rsmod.map.zone.ZoneGrid
import org.rsmod.map.zone.ZoneKey
import org.rsmod.routefinder.collision.CollisionFlagMap
import org.rsmod.routefinder.flag.CollisionFlag
import org.rsmod.routefinder.loc.LocLayerConstants

public class GameMapDecoder
@Inject
constructor(
    @GameCache private val gameCache: Cache,
    private val collision: CollisionFlagMap,
    private val locZones: LocZoneStorage,
    private val locTypes: LocTypeList,
    private val areaIndex: AreaIndex,
    private val npcTypes: NpcTypeList,
    private val npcRepo: NpcRepository,
    private val objTypes: ObjTypeList,
    private val objRepo: ObjRepository,
    private val xteaMap: XteaMap,
) {
    public fun decodeAll(): Unit =
        runBlocking(Dispatchers.IO) {
            val mapBuffers = gameCache.readMapBuffers(xteaMap)
            val decodedMaps = decodeAll(mapBuffers)

            putMapCollision(decodedMaps)
            putAreas(decodedMaps)

            val mapBuilder = GameMapBuilder()
            putSpawns(mapBuilder, decodedMaps)
            cacheLocs(mapBuilder)
        }

    private fun Cache.readMapBuffers(xteaMap: XteaMap): List<MapBuffer> =
        when {
            xteaMap.isNotEmpty() -> {
                xteaMap.mapNotNull { (mapSquareKey, keyArray) ->
                    readMapBufferOrNull(mapSquareKey, SymmetricKey.fromIntArray(keyArray))
                }
            }
            else -> {
                findMapSquares().mapNotNull { mapSquareKey -> readMapBufferOrNull(mapSquareKey) }
            }
        }

    private fun Cache.findMapSquares(): List<MapSquareKey> = buildList {
        for (group in list(Js5Archives.MAPS)) {
            val unnamedGroup = group.nameHash == -1
            val validMapSquareId = group.id in 0..0xFFFF
            if (unnamedGroup && validMapSquareId && hasMapSquareTerrain(group.id)) {
                add(MapSquareKey(group.id))
            }
        }
    }

    private fun Cache.hasMapSquareTerrain(group: Int): Boolean {
        val map =
            try {
                read(Js5Archives.MAPS, group, file = 0)
            } catch (_: FileNotFoundException) {
                return false
            }
        return map.use { it.readableBytes() >= MIN_MAP_TERRAIN_BYTES }
    }

    private fun Cache.readMapBufferOrNull(
        mapSquareKey: MapSquareKey,
        key: SymmetricKey = SymmetricKey.ZERO,
    ): MapBuffer? {
        val name = "${mapSquareKey.x}_${mapSquareKey.z}"
        val map = readMapSquareOrNull(mapSquareKey, "m$name", file = 0) ?: return null
        val locs = readMapSquareOrNull(mapSquareKey, "l$name", file = 1, key)
        val npcs = readOrNull(Js5Archives.MAPS, "n$name")?.use(ByteBuf::toInlineBuf)
        val objs = readOrNull(Js5Archives.MAPS, "o$name")?.use(ByteBuf::toInlineBuf)
        val areas = readOrNull(Js5Archives.MAPS, "a$name")?.use(ByteBuf::toInlineBuf)
        return MapBuffer(mapSquareKey, map, locs, npcs, objs, areas)
    }

    private fun Cache.readMapSquareOrNull(
        mapSquareKey: MapSquareKey,
        namedGroup: String,
        file: Int,
        key: SymmetricKey = SymmetricKey.ZERO,
    ): InlineByteBuf? {
        val archive = Js5Archives.MAPS
        val packed = mapSquareKey.id
        val packedBuffer =
            try {
                read(archive, packed, file, key)
            } catch (_: FileNotFoundException) {
                null
            }
        if (packedBuffer != null) {
            return packedBuffer.use(ByteBuf::toInlineBuf)
        }
        return readOrNull(archive, namedGroup, file = 0, key)?.use(ByteBuf::toInlineBuf)
    }

    private suspend fun decodeAll(buffers: List<MapBuffer>): List<DecodedMap> = coroutineScope {
        buffers.map { buffer -> async { buffer.decode() } }.awaitAll()
    }

    private suspend fun putMapCollision(maps: List<DecodedMap>): Unit = coroutineScope {
        maps.map { decoded -> async { putMaps(collision, decoded.key, decoded.map) } }.awaitAll()
    }

    private fun putAreas(maps: List<DecodedMap>) {
        for (map in maps) {
            val areas = map.areas ?: continue
            putAreas(areaIndex, map.key, areas)
        }
    }

    private fun putSpawns(builder: GameMapBuilder, decodedMaps: List<DecodedMap>) {
        for (decoded in decodedMaps) {
            decoded.locs?.let { putLocs(builder, collision, locTypes, decoded.key, decoded.map, it) }
            decoded.npcs?.let { putNpcs(npcRepo, npcTypes, decoded.key, it) }
            decoded.objs?.let { putObjs(objRepo, objTypes, decoded.key, it) }
        }
    }

    private fun cacheLocs(builder: GameMapBuilder) {
        for ((zoneKey, zoneBuilder) in builder.zoneBuilders) {
            locZones.mapLocs[zoneKey] = zoneBuilder.build()
        }
    }

    public companion object {
        public fun putMaps(
            collision: CollisionFlagMap,
            square: MapSquareKey,
            mapDef: MapTileSimpleDefinition,
        ) {
            val baseX = square.x * MapSquareGrid.LENGTH
            val baseZ = square.z * MapSquareGrid.LENGTH
            for (level in 0 until CoordGrid.LEVEL_COUNT) {
                for (x in 0 until MapSquareGrid.LENGTH) {
                    for (z in 0 until MapSquareGrid.LENGTH) {
                        val flags = mapDef[x, z, level].toInt()
                        if (flags == 0) {
                            continue
                        }

                        var mask = 0
                        if ((flags and MapTileSimpleDefinition.BLOCK_MAP_SQUARE) != 0) {
                            mask = mask or CollisionFlag.BLOCK_WALK
                        }
                        if ((flags and MapTileSimpleDefinition.REMOVE_ROOFS) != 0) {
                            mask = mask or CollisionFlag.ROOF
                        }

                        val absX = baseX + x
                        val absZ = baseZ + z
                        val resolvedLevel =
                            if ((flags and MapTileSimpleDefinition.LINK_BELOW) != 0) {
                                level - 1
                            } else {
                                level
                            }
                        collision[absX, absZ, resolvedLevel] = mask
                    }
                }
            }
        }

        public fun putLocs(
            mapBuilder: GameMapBuilder,
            collision: CollisionFlagMap,
            locTypes: LocTypeList,
            square: MapSquareKey,
            mapDef: MapTileSimpleDefinition,
            locDef: MapLocListDefinition,
        ) {
            with(mapBuilder) {
                for (packedLoc in locDef.spawns.longIterator()) {
                    val loc = MapLocDefinition(packedLoc)
                    val local = MapSquareGrid(loc.localX, loc.localZ, loc.level)
                    val tileFlags = mapDef[local.x, local.z, local.level].toInt()
                    val tileAboveFlags =
                        if (local.level >= CoordGrid.LEVEL_COUNT - 1) {
                            tileFlags
                        } else {
                            mapDef[local.x, local.z, local.level + 1].toInt()
                        }
                    val resolvedTileFlags =
                        if ((tileAboveFlags and MapTileSimpleDefinition.LINK_BELOW) != 0) {
                            tileAboveFlags
                        } else {
                            tileFlags
                        }
                    // Take into account that any tile that has this bit flag will cause locs below
                    // it to "visually" go one level down.
                    val visualLevel =
                        if ((resolvedTileFlags and MapTileSimpleDefinition.LINK_BELOW) != 0) {
                            loc.level - 1
                        } else {
                            loc.level
                        }
                    if (visualLevel < 0) {
                        continue
                    }
                    val coords =
                        square
                            .toCoords(0.coerceAtLeast(visualLevel))
                            .translate(loc.localX, loc.localZ)
                    val zoneGridX = coords.x and ZoneGrid.X_BIT_MASK
                    val zoneGridZ = coords.z and ZoneGrid.Z_BIT_MASK
                    val zone = computeIfAbsent(ZoneKey.from(coords)) { ZoneBuilder() }
                    val layer = LocLayerConstants.of(loc.shape)
                    val entity = LocEntity(loc.id, loc.shape, loc.angle)
                    val type = locTypes[loc.id] ?: error("Invalid loc type: $loc ($square)")
                    zone.add(zoneGridX, zoneGridZ, layer, entity)
                    collision.toggleLoc(
                        coords = coords,
                        width = type.width,
                        length = type.length,
                        shape = loc.shape,
                        angle = loc.angle,
                        blockWalk = type.blockWalk,
                        blockRange = type.blockRange,
                        breakRouteFinding = type.breakRouteFinding,
                        add = true,
                    )
                }
            }
        }

        @OptIn(InternalApi::class)
        public fun putAreas(index: AreaIndex, square: MapSquareKey, areaDef: MapAreaDefinition) {
            val squareBase = square.toCoords(level = 0)

            if (areaDef.mapSquareAreas.isNotEmpty()) {
                index.registerAll(square, areaDef.mapSquareAreas.iterator())
            }

            if (areaDef.zoneAreas.isNotEmpty()) {
                for ((packedZone, areas) in areaDef.zoneAreas.byte2ObjectEntrySet()) {
                    val localZone = LocalMapSquareZone(packedZone.toInt())
                    val zoneBase = localZone.toCoords(baseX = squareBase.x, baseZ = squareBase.z)
                    val zoneKey = ZoneKey.from(zoneBase)
                    index.registerAll(zoneKey, areas.iterator())
                }
            }

            if (areaDef.coordAreas.isNotEmpty()) {
                for ((packedGrid, areas) in areaDef.coordAreas.short2ObjectEntrySet()) {
                    val grid = MapSquareGrid(packedGrid.toInt())
                    val coord = squareBase.translate(grid.x, grid.z, grid.level)
                    index.registerAll(coord, areas.iterator())
                }
            }
        }

        public fun putNpcs(
            repo: NpcRepository,
            types: NpcTypeList,
            square: MapSquareKey,
            npcs: MapNpcListDefinition,
        ) {
            val base = square.toCoords(level = 0)
            for (packed in npcs.packedSpawns.intIterator()) {
                val def = MapNpcDefinition(packed)
                val coords = base.translate(def.localX, def.localZ, def.level)
                val type = types[def.id] ?: error("Invalid npc type: $def ($square)")
                val npc = Npc(type, coords)
                repo.addDelayed(npc, spawnDelay = 0, duration = Int.MAX_VALUE)
            }
        }

        public fun putObjs(
            repo: ObjRepository,
            types: ObjTypeList,
            square: MapSquareKey,
            objs: MapObjListDefinition,
        ) {
            val base = square.toCoords(level = 0)
            for (packed in objs.packedSpawns.longIterator()) {
                val def = MapObjDefinition(packed)
                val coords = base.translate(def.localX, def.localZ, def.level)
                val type = types[def.id] ?: error("Invalid obj type: $def ($square)")
                val entity = ObjEntity(type.id, count = def.count, scope = ObjScope.Perm.id)
                val obj = Obj(coords, entity, creationCycle = 0, receiverId = Obj.NULL_OBSERVER_ID)
                repo.addDelayed(obj, spawnDelay = 0, duration = Int.MAX_VALUE)
            }
        }
    }
}

private class MapBuffer(
    val key: MapSquareKey,
    val map: InlineByteBuf,
    val locs: InlineByteBuf?,
    val npcs: InlineByteBuf?,
    val objs: InlineByteBuf?,
    val areas: InlineByteBuf?,
) {
    suspend fun decode(): DecodedMap =
        try {
            coroutineScope {
                val mapDef = async { MapTileDecoder.decode(map) }
                val locDef = if (locs != null) async { MapLocListDecoder.decode(locs) } else null
                val npcDef = if (npcs != null) async { MapNpcListDecoder.decode(npcs) } else null
                val objDef = if (objs != null) async { MapObjListDecoder.decode(objs) } else null
                val areaDef = if (areas != null) async { MapAreaDecoder.decode(areas) } else null
                DecodedMap(
                    key = key,
                    map = mapDef.await(),
                    locs = locDef?.await(),
                    npcs = npcDef?.await(),
                    objs = objDef?.await(),
                    areas = areaDef?.await(),
                )
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to decode map square: $key", e)
        }
}

private data class DecodedMap(
    val key: MapSquareKey,
    val map: MapTileSimpleDefinition,
    val locs: MapLocListDefinition?,
    val npcs: MapNpcListDefinition?,
    val objs: MapObjListDefinition?,
    val areas: MapAreaDefinition?,
)

private const val MIN_MAP_TERRAIN_BYTES = MapSquareGrid.LENGTH * MapSquareGrid.LENGTH
