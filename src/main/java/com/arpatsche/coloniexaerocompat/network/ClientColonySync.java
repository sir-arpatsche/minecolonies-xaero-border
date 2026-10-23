package com.arpatsche.coloniexaerocompat.network;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import com.arpatsche.coloniexaerocompat.cache.BuildingCache;
import com.arpatsche.coloniexaerocompat.cache.ClaimCache;
import com.arpatsche.coloniexaerocompat.cache.ColonyInfoCache;
import com.arpatsche.coloniexaerocompat.server.BuildingData;
import com.arpatsche.coloniexaerocompat.server.ColonyData;
import com.arpatsche.coloniexaerocompat.server.ColonyRef;
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

    /** One colony's claims and buildings (on joining, and whenever they change). */
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

        ColonyXaeroCompat.LOGGER.debug("[client] received colony '{}' (ID {}) in {}: {} chunks, {} buildings",
                colony.name(), colony.id(), colony.dimension().location(), chunks.size(), buildings.size());
    }

    /** A colony was deleted while we are online. */
    public static void handleRemoved(ColonyRemovedPayload payload, IPayloadContext context) {
        ColonyRef ref = payload.colony();
        ClaimCache.removeColony(ref.dimension(), ref.id());
        BuildingCache.removeColony(ref.dimension(), ref.id());
        ColonyInfoCache.remove(ref.dimension(), ref.id());
        ColonyXaeroCompat.LOGGER.debug("[client] colony {} in {} was removed",
                ref.id(), ref.dimension().location());
    }

    /** All colonies that exist on the server. Everything else we remembered is outdated. */
    public static void handleList(ColonyListPayload payload, IPayloadContext context) {
        Set<ColonyRef> keep = new HashSet<>(payload.colonies());
        int removed = ClaimCache.retainOnly(keep)
                + BuildingCache.retainOnly(keep)
                + ColonyInfoCache.retainOnly(keep);
        ColonyXaeroCompat.LOGGER.debug("[client] server knows {} colonies; dropped {} outdated cache entries",
                keep.size(), removed);
    }
}