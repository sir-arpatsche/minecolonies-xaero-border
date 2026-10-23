package com.arpatsche.coloniexaerocompat.server;

import net.minecraft.core.BlockPos;

/**
 * One building of a colony as read on the server.
 * typeKey is MineColonies' translation key, e.g. "com.minecolonies.building.townhall".
 */
public record BuildingData(BlockPos pos, String typeKey) {
}
