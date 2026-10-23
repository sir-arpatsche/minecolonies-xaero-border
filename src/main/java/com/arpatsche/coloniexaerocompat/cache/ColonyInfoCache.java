package com.arpatsche.coloniexaerocompat.cache;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.arpatsche.coloniexaerocompat.server.ColonyRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Remembers name and centre of colonies that the server told us about.
 * Colonies we only know from the server often have no IColonyView on the client,
 * so the map takes the name and centre from here.
 */
public final class ColonyInfoCache {

    public record Info(String name, BlockPos center) {
    }

    // dimension -> colony id -> info
    private static final Map<ResourceKey<Level>, Map<Integer, Info>> INFOS = new HashMap<>();

    private static boolean dirty = false;

    private ColonyInfoCache() {
    }

    public static void put(ResourceKey<Level> dimension, int colonyId, String name, BlockPos center) {
        Info info = new Info(name, center.immutable());
        Map<Integer, Info> dim = INFOS.computeIfAbsent(dimension, k -> new HashMap<>());
        if (!info.equals(dim.get(colonyId))) {
            dim.put(colonyId, info);
            dirty = true;
        }
    }

    /** The remembered name, or null if the server never told us about this colony. */
    public static String getName(ResourceKey<Level> dimension, int colonyId) {
        Info info = get(dimension, colonyId);
        return info == null ? null : info.name();
    }

    /** The remembered centre, or null if the server never told us about this colony. */
    public static BlockPos getCenter(ResourceKey<Level> dimension, int colonyId) {
        Info info = get(dimension, colonyId);
        return info == null ? null : info.center();
    }

    private static Info get(ResourceKey<Level> dimension, int colonyId) {
        Map<Integer, Info> dim = INFOS.get(dimension);
        return dim == null ? null : dim.get(colonyId);
    }

    public static void remove(ResourceKey<Level> dimension, int colonyId) {
        Map<Integer, Info> dim = INFOS.get(dimension);
        if (dim != null && dim.remove(colonyId) != null) {
            dirty = true;
        }
    }

    /** Forgets every colony that is not in the given set. Returns how many were dropped. */
    public static int retainOnly(java.util.Set<ColonyRef> keep) {
        int removed = 0;
        for (Map.Entry<ResourceKey<Level>, Map<Integer, Info>> dimEntry : INFOS.entrySet()) {
            ResourceKey<Level> dimension = dimEntry.getKey();
            java.util.Iterator<Integer> it = dimEntry.getValue().keySet().iterator();
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

    public static void clear() {
        INFOS.clear();
        dirty = false;
    }

    public static boolean isDirty() {
        return dirty;
    }

    public static void markClean() {
        dirty = false;
    }

    /** { "minecraft:overworld": { "1": { "n": name, "c": centreLong } } } */
    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (Map.Entry<ResourceKey<Level>, Map<Integer, Info>> dimEntry : INFOS.entrySet()) {
            JsonObject dimObj = new JsonObject();
            for (Map.Entry<Integer, Info> colonyEntry : dimEntry.getValue().entrySet()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("n", colonyEntry.getValue().name());
                entry.addProperty("c", colonyEntry.getValue().center().asLong());
                dimObj.add(String.valueOf(colonyEntry.getKey()), entry);
            }
            root.add(dimEntry.getKey().location().toString(), dimObj);
        }
        return root;
    }

    public static void fromJson(JsonObject root) {
        INFOS.clear();
        for (Map.Entry<String, JsonElement> dimEntry : root.entrySet()) {
            ResourceKey<Level> dimension =
                    ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimEntry.getKey()));
            Map<Integer, Info> colonies = new HashMap<>();
            for (Map.Entry<String, JsonElement> colonyEntry : dimEntry.getValue().getAsJsonObject().entrySet()) {
                JsonObject entry = colonyEntry.getValue().getAsJsonObject();
                colonies.put(Integer.parseInt(colonyEntry.getKey()),
                        new Info(entry.get("n").getAsString(), BlockPos.of(entry.get("c").getAsLong())));
            }
            INFOS.put(dimension, colonies);
        }
        dirty = false;
    }
}
