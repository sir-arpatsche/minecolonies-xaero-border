package com.arpatsche.coloniexaerocompat.network;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import com.arpatsche.coloniexaerocompat.server.BuildingData;
import com.arpatsche.coloniexaerocompat.server.ColonyData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Server -> client: everything the map needs to know about one colony.
 */
public record ColonySyncPayload(ColonyData colony) implements CustomPacketPayload {

    public static final Type<ColonySyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyXaeroCompat.MODID, "colony_sync"));

    public static final StreamCodec<FriendlyByteBuf, ColonySyncPayload> CODEC =
            StreamCodec.of((buf, payload) -> write(buf, payload.colony()), buf -> new ColonySyncPayload(read(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void write(FriendlyByteBuf buf, ColonyData colony) {
        buf.writeVarInt(colony.id());
        buf.writeUtf(colony.name());
        buf.writeBlockPos(colony.center());
        buf.writeResourceLocation(colony.dimension().location());

        buf.writeVarInt(colony.chunks().size());
        for (ChunkPos chunk : colony.chunks()) {
            buf.writeLong(chunk.toLong());
        }

        buf.writeVarInt(colony.buildings().size());
        for (BuildingData building : colony.buildings()) {
            buf.writeBlockPos(building.pos());
            buf.writeUtf(building.typeKey());
        }
    }

    private static ColonyData read(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        String name = buf.readUtf();
        BlockPos center = buf.readBlockPos();
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());

        int chunkCount = buf.readVarInt();
        List<ChunkPos> chunks = new ArrayList<>(chunkCount);
        for (int i = 0; i < chunkCount; i++) {
            chunks.add(new ChunkPos(buf.readLong()));
        }

        int buildingCount = buf.readVarInt();
        List<BuildingData> buildings = new ArrayList<>(buildingCount);
        for (int i = 0; i < buildingCount; i++) {
            BlockPos pos = buf.readBlockPos();
            String typeKey = buf.readUtf();
            buildings.add(new BuildingData(pos, typeKey));
        }

        return new ColonyData(id, name, center, dimension, List.copyOf(chunks), List.copyOf(buildings));
    }
}
