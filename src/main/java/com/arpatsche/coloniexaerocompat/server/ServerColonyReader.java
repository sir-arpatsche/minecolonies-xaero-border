package com.arpatsche.coloniexaerocompat.server;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.claim.IChunkClaimData;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Rank;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Reads all colonies, their claimed chunks and their buildings from MineColonies' server-side data.
 * Must be called on the server thread.
 * The lists are sorted, so two reads of unchanged data compare as equal.
 */
public final class ServerColonyReader {

    private ServerColonyReader() {
    }

    /**
     * True if the player owns the colony or is an officer or friend in it.
     * Anything unclear counts as "not a member", so nothing is shown by mistake.
     */
    public static boolean isMember(UUID player, ColonyRef ref) {
        IColony colony = IColonyManager.getInstance().getColonyByDimension(ref.id(), ref.dimension());
        if (colony == null) {
            return false;
        }
        IPermissions permissions = colony.getPermissions();
        if (player.equals(permissions.getOwner())) {
            return true;
        }
        Rank rank = permissions.getRank(player);
        return rank != null
                && (rank.equals(permissions.getRankOwner())
                || rank.equals(permissions.getRankOfficer())
                || rank.equals(permissions.getRankFriend()));
    }

    public static List<ColonyData> readAll() {
        IColonyManager manager = IColonyManager.getInstance();
        List<IColony> colonies = manager.getAllColonies();

        // dimension -> colony id -> claimed chunks
        Set<ResourceKey<Level>> dimensions = new HashSet<>();
        for (IColony colony : colonies) {
            dimensions.add(colony.getDimension());
        }
        Map<ResourceKey<Level>, Map<Integer, List<ChunkPos>>> chunksByDimension = new HashMap<>();
        for (ResourceKey<Level> dimension : dimensions) {
            Map<Integer, List<ChunkPos>> perColony = new HashMap<>();
            for (Map.Entry<ChunkPos, IChunkClaimData> entry : manager.getClaimData(dimension).entrySet()) {
                perColony.computeIfAbsent(entry.getValue().getOwningColony(), k -> new ArrayList<>())
                        .add(entry.getKey());
            }
            chunksByDimension.put(dimension, perColony);
        }

        List<ColonyData> result = new ArrayList<>();
        for (IColony colony : colonies) {
            List<ChunkPos> claimed = new ArrayList<>(chunksByDimension
                    .getOrDefault(colony.getDimension(), Map.of())
                    .getOrDefault(colony.getID(), List.of()));
            claimed.sort(Comparator.comparingLong(ChunkPos::toLong));

            List<BuildingData> buildings = new ArrayList<>();
            for (IBuilding building : colony.getServerBuildingManager().getBuildings().values()) {
                buildings.add(new BuildingData(building.getID().immutable(), building.getBuildingDisplayName()));
            }
            buildings.sort(Comparator.comparingLong(b -> b.pos().asLong()));

            result.add(new ColonyData(colony.getID(), colony.getName(), colony.getCenter(), colony.getDimension(),
                    List.copyOf(claimed), List.copyOf(buildings)));
        }
        return result;
    }
}