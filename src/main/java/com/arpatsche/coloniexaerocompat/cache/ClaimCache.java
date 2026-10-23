package com.arpatsche.coloniexaerocompat.cache;

import com.arpatsche.coloniexaerocompat.server.ColonyRef;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;

public final class ClaimCache {

    private static final Map<ResourceKey<Level>, Map<Integer, Set<ChunkPos>>> CLAIMS = new HashMap<>();


    private static boolean dirty = false;

    private ClaimCache() {
    }


    public static void update(Level level) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        List<IColonyView> colonies = IColonyManager.getInstance().getColonyViews(level);
        if (colonies.isEmpty()) {
            return;
        }

        Map<Integer, Set<ChunkPos>> dimClaims = CLAIMS.computeIfAbsent(level.dimension(), k -> new HashMap<>());

        int radius = mc.options.renderDistance().get();
        ChunkPos center = mc.player.chunkPosition();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = center.x + dx;
                int cz = center.z + dz;

                // Nur Chunks prüfen, die der Client wirklich geladen hat.
                if (!level.getChunkSource().hasChunk(cx, cz)) {
                    continue;
                }

                ChunkPos chunkPos = new ChunkPos(cx, cz);
                BlockPos probe = new BlockPos((cx << 4) + 8, level.getMinBuildHeight() + 1, (cz << 4) + 8);

                for (IColonyView colony : colonies) {
                    Set<ChunkPos> set = dimClaims.computeIfAbsent(colony.getID(), k -> new HashSet<>());
                    boolean changed;
                    if (colony.isCoordInColony(level, probe)) {
                        changed = set.add(chunkPos);
                    } else {
                        changed = set.remove(chunkPos); // Claim existiert nicht (mehr)
                    }
                    if (changed) {
                        dirty = true;
                    }
                }
            }
        }
    }


    public static Set<ChunkPos> getClaims(ResourceKey<Level> dimension, int colonyId) {
        Map<Integer, Set<ChunkPos>> dimClaims = CLAIMS.get(dimension);
        if (dimClaims == null) {
            return Set.of();
        }
        return dimClaims.getOrDefault(colonyId, Set.of());
    }

    /** Forgets one colony (it was deleted on the server). */
    public static void removeColony(ResourceKey<Level> dimension, int colonyId) {
        Map<Integer, Set<ChunkPos>> dimClaims = CLAIMS.get(dimension);
        if (dimClaims != null && dimClaims.remove(colonyId) != null) {
            dirty = true;
        }
    }

    /** Forgets every colony that is not in the given set. Returns how many were dropped. */
    public static int retainOnly(Set<ColonyRef> keep) {
        int removed = 0;
        for (Map.Entry<ResourceKey<Level>, Map<Integer, Set<ChunkPos>>> dimEntry : CLAIMS.entrySet()) {
            ResourceKey<Level> dimension = dimEntry.getKey();
            Iterator<Integer> it = dimEntry.getValue().keySet().iterator();
            while (it.hasNext()) {
                if (!keep.contains(new ColonyRef(dimension, it.next()))) {
                    it.remove();
                    removed++;
                    dirty = true;
                }
            }
        }
        return removed;
    }

    public static Set<Integer> getColonyIds(ResourceKey<Level> dimension) {
        Map<Integer, Set<ChunkPos>> dimClaims = CLAIMS.get(dimension);
        if (dimClaims == null) {
            return Set.of();
        }
        return new HashSet<>(dimClaims.keySet());
    }

    /** Replaces the remembered chunks of a colony (used for data from the server). */
    public static void setClaims(ResourceKey<Level> dimension, int colonyId, Set<ChunkPos> chunks) {
        Map<Integer, Set<ChunkPos>> dimClaims = CLAIMS.computeIfAbsent(dimension, k -> new HashMap<>());
        if (!chunks.equals(dimClaims.get(colonyId))) {
            dimClaims.put(colonyId, new HashSet<>(chunks));
            dirty = true;
        }
    }

    public static void clear() {
        CLAIMS.clear();
        dirty = false;
    }

    public static boolean isDirty() {
        return dirty;
    }

    public static void markClean() {
        dirty = false;
    }

    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (Map.Entry<ResourceKey<Level>, Map<Integer, Set<ChunkPos>>> dimEntry : CLAIMS.entrySet()) {
            JsonObject dimObj = new JsonObject();
            for (Map.Entry<Integer, Set<ChunkPos>> colonyEntry : dimEntry.getValue().entrySet()) {
                JsonArray array = new JsonArray();
                for (ChunkPos pos : colonyEntry.getValue()) {
                    array.add(pos.toLong());
                }
                dimObj.add(String.valueOf(colonyEntry.getKey()), array);
            }
            root.add(dimEntry.getKey().location().toString(), dimObj);
        }
        return root;
    }

    public static void fromJson(JsonObject root) {
        CLAIMS.clear();
        for (Map.Entry<String, JsonElement> dimEntry : root.entrySet()) {
            ResourceKey<Level> dimension =
                    ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimEntry.getKey()));
            Map<Integer, Set<ChunkPos>> colonies = new HashMap<>();
            for (Map.Entry<String, JsonElement> colonyEntry : dimEntry.getValue().getAsJsonObject().entrySet()) {
                Set<ChunkPos> set = new HashSet<>();
                for (JsonElement element : colonyEntry.getValue().getAsJsonArray()) {
                    set.add(new ChunkPos(element.getAsLong()));
                }
                colonies.put(Integer.parseInt(colonyEntry.getKey()), set);
            }
            CLAIMS.put(dimension, colonies);
        }
        dirty = false;
    }
}