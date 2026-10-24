package com.arpatsche.coloniexaerocompat.minimap;

import com.arpatsche.coloniexaerocompat.cache.BuildingCache;
import com.arpatsche.coloniexaerocompat.cache.ClaimCache;
import com.arpatsche.coloniexaerocompat.cache.ColonyInfoCache;
import com.arpatsche.coloniexaerocompat.map.BuildingElement;
import com.arpatsche.coloniexaerocompat.map.ClaimElement;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Collects the elements the minimap draws, from the same caches as the world map.
 * Only elements near the centre of the minimap are returned.
 */
final class MinimapSources {

    /** Blocks around the centre that are considered. The minimap shows less than this. */
    private static final double RANGE = 320;

    private MinimapSources() {
    }

    /** Name of a colony, or null if the colony no longer exists. */
    private static String nameOf(ResourceKey<Level> dimension, int colonyId, boolean playerDimension) {
        IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, dimension);
        String serverName = ColonyInfoCache.getName(dimension, colonyId);
        if (colony == null && serverName == null && playerDimension) {
            return null;
        }
        return colony != null ? colony.getName() : (serverName != null ? serverName : "Colony " + colonyId);
    }

    static List<ClaimElement> claims(ResourceKey<Level> dimension, double centreX, double centreZ) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        boolean playerDimension = dimension.equals(level.dimension());
        List<ClaimElement> result = new ArrayList<>();

        for (int colonyId : ClaimCache.getColonyIds(dimension)) {
            String name = nameOf(dimension, colonyId, playerDimension);
            if (name == null) {
                continue;
            }
            Set<ChunkPos> claims = ClaimCache.getClaims(dimension, colonyId);
            for (ChunkPos chunk : claims) {
                if (Math.abs(chunk.x * 16 + 8 - centreX) > RANGE || Math.abs(chunk.z * 16 + 8 - centreZ) > RANGE) {
                    continue;
                }
                result.add(new ClaimElement(
                        colonyId, name, chunk.x, chunk.z,
                        !claims.contains(new ChunkPos(chunk.x, chunk.z - 1)),
                        !claims.contains(new ChunkPos(chunk.x, chunk.z + 1)),
                        !claims.contains(new ChunkPos(chunk.x - 1, chunk.z)),
                        !claims.contains(new ChunkPos(chunk.x + 1, chunk.z))));
            }
        }
        return result;
    }

    static List<BuildingElement> buildings(ResourceKey<Level> dimension, double centreX, double centreZ) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        boolean playerDimension = dimension.equals(level.dimension());
        List<BuildingElement> result = new ArrayList<>();

        for (int colonyId : BuildingCache.getColonyIds(dimension)) {
            String name = nameOf(dimension, colonyId, playerDimension);
            if (name == null) {
                continue;
            }
            for (BuildingCache.Info info : BuildingCache.getBuildings(dimension, colonyId)) {
                if (Math.abs(info.pos().getX() - centreX) > RANGE || Math.abs(info.pos().getZ() - centreZ) > RANGE) {
                    continue;
                }
                result.add(new BuildingElement(colonyId, name, info.typeKey(), info.pos()));
            }
        }
        return result;
    }
}
