package com.arpatsche.coloniexaerocompat.cache;

import com.arpatsche.coloniexaerocompat.server.ColonyRef;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

public final class BuildingCache {

    public record Info(BlockPos pos, String typeKey) {
    }

    private static final Map<ResourceKey<Level>, Map<Integer, Set<Info>>> BUILDINGS = new HashMap<>();

    private static boolean dirty = false;

    private BuildingCache() {
    }

    public static void update(Level level) {
        List<IColonyView> colonies = IColonyManager.getInstance().getColonyViews(level);
        for (IColonyView colony : colonies) {
            Map<BlockPos, IBuildingView> known = colony.getClientBuildingManager().getBuildings();
            // Every colony has a town hall, so an empty result means "no data yet", not "no buildings".
            // In that case we keep what we remembered.
            if (known.isEmpty()) {
                continue;
            }

            Set<Info> fresh = new HashSet<>();
            for (IBuildingView building : known.values()) {
                fresh.add(new Info(building.getID().immutable(), building.getBuildingDisplayName()));
            }

            Map<Integer, Set<Info>> dimBuildings = BUILDINGS.computeIfAbsent(level.dimension(), k -> new HashMap<>());
            if (!fresh.equals(dimBuildings.get(colony.getID()))) {
                dimBuildings.put(colony.getID(), fresh);
                dirty = true;
            }
        }
    }

    public static Set<Info> getBuildings(ResourceKey<Level> dimension, int colonyId) {
        Map<Integer, Set<Info>> dimBuildings = BUILDINGS.get(dimension);
        if (dimBuildings == null) {
            return Set.of();
        }
        return dimBuildings.getOrDefault(colonyId, Set.of());
    }

    /** Replaces the remembered buildings of a colony (used for data from the server). */
    public static void setBuildings(ResourceKey<Level> dimension, int colonyId, Set<Info> buildings) {
        Map<Integer, Set<Info>> dimBuildings = BUILDINGS.computeIfAbsent(dimension, k -> new HashMap<>());
        if (!buildings.equals(dimBuildings.get(colonyId))) {
            dimBuildings.put(colonyId, new HashSet<>(buildings));
            dirty = true;
        }
    }

    /** Forgets one colony (it was deleted on the server). */
    public static void removeColony(ResourceKey<Level> dimension, int colonyId) {
        Map<Integer, Set<Info>> dimBuildings = BUILDINGS.get(dimension);
        if (dimBuildings != null && dimBuildings.remove(colonyId) != null) {
            dirty = true;
        }
    }

    /** Forgets every colony that is not in the given set. Returns how many were dropped. */
    public static int retainOnly(Set<ColonyRef> keep) {
        int removed = 0;
        for (Map.Entry<ResourceKey<Level>, Map<Integer, Set<Info>>> dimEntry : BUILDINGS.entrySet()) {
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

    /** Ids of all colonies that have remembered buildings in the given dimension. */
    public static Set<Integer> getColonyIds(ResourceKey<Level> dimension) {
        Map<Integer, Set<Info>> dimBuildings = BUILDINGS.get(dimension);
        if (dimBuildings == null) {
            return Set.of();
        }
        return new HashSet<>(dimBuildings.keySet());
    }

    /** Total number of remembered buildings (for logging). */
    public static int totalCount() {
        int count = 0;
        for (Map<Integer, Set<Info>> dim : BUILDINGS.values()) {
            for (Set<Info> set : dim.values()) {
                count += set.size();
            }
        }
        return count;
    }

    public static void clear() {
        BUILDINGS.clear();
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
        for (Map.Entry<ResourceKey<Level>, Map<Integer, Set<Info>>> dimEntry : BUILDINGS.entrySet()) {
            JsonObject dimObj = new JsonObject();
            for (Map.Entry<Integer, Set<Info>> colonyEntry : dimEntry.getValue().entrySet()) {
                JsonArray array = new JsonArray();
                for (Info info : colonyEntry.getValue()) {
                    JsonObject entry = new JsonObject();
                    entry.addProperty("p", info.pos().asLong());
                    entry.addProperty("t", info.typeKey());
                    array.add(entry);
                }
                dimObj.add(String.valueOf(colonyEntry.getKey()), array);
            }
            root.add(dimEntry.getKey().location().toString(), dimObj);
        }
        return root;
    }

    public static void fromJson(JsonObject root) {
        BUILDINGS.clear();
        for (Map.Entry<String, JsonElement> dimEntry : root.entrySet()) {
            ResourceKey<Level> dimension =
                    ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimEntry.getKey()));
            Map<Integer, Set<Info>> colonies = new HashMap<>();
            for (Map.Entry<String, JsonElement> colonyEntry : dimEntry.getValue().getAsJsonObject().entrySet()) {
                Set<Info> set = new HashSet<>();
                for (JsonElement element : colonyEntry.getValue().getAsJsonArray()) {
                    JsonObject entry = element.getAsJsonObject();
                    set.add(new Info(BlockPos.of(entry.get("p").getAsLong()), entry.get("t").getAsString()));
                }
                colonies.put(Integer.parseInt(colonyEntry.getKey()), set);
            }
            BUILDINGS.put(dimension, colonies);
        }
        dirty = false;
    }
}
