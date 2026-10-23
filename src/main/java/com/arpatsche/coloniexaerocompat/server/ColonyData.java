package com.arpatsche.coloniexaerocompat.server;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Everything the map needs to know about one colony, as read on the server.
 */
public record ColonyData(int id, String name, BlockPos center, ResourceKey<Level> dimension,
                         List<ChunkPos> chunks, List<BuildingData> buildings) {
}
