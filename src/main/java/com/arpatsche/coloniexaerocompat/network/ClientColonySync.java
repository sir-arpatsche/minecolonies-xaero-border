package com.arpatsche.coloniexaerocompat.network;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import com.arpatsche.coloniexaerocompat.cache.BuildingCache;
import com.arpatsche.coloniexaerocompat.cache.ClaimCache;
import com.arpatsche.coloniexaerocompat.cache.ColonyInfoCache;
import com.arpatsche.coloniexaerocompat.server.BuildingData;
import com.arpatsche.coloniexaerocompat.server.ColonyData;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles colony data arriving from the server. Runs on the client:
 * the data goes into the same caches the map already draws from.
 */
public final class ClientColonySync {

    private ClientColonySync() {
    }

    public static void handle(ColonySyncPayload payload, IPayloadContext context) {
        ColonyData colony = payload.colony();

        Set<ChunkPos> chunks = new HashSet<>(colony.chunks());
        ClaimCache.setClaims(colony.dimension(), colony.id(), chunks);

        Set<BuildingCache.Info> buildings = new HashSet<>();
        for (BuildingData building : colony.buildings()) {
            buildings.add(new BuildingCache.Info(building.pos(), building.typeKey()));
        }
        BuildingCache.setBuildings(colony.dimension(), colony.id(), buildings);

        ColonyInfoCache.put(colony.dimension(), colony.id(), colony.name(), colony.center());

        ColonyXaeroCompat.LOGGER.info("[client] received colony '{}' (ID {}) in {}: {} chunks, {} buildings",
                colony.name(), colony.id(), colony.dimension().location(), chunks.size(), buildings.size());
        ColonyXaeroCompat.LOGGER.info("[client] caches now hold {} chunks and {} buildings for colony {}; colonies with claims here: {}",
                ClaimCache.getClaims(colony.dimension(), colony.id()).size(),
                BuildingCache.getBuildings(colony.dimension(), colony.id()).size(),
                colony.id(), ClaimCache.getColonyIds(colony.dimension()));
    }
}