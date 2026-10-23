package com.arpatsche.coloniexaerocompat.map;

import net.minecraft.core.BlockPos;

/**
 * One building of a colony, as shown on the map.
 * typeKey is MineColonies' translation key, e.g. "com.minecolonies.building.townhall".
 */
public record BuildingElement(int colonyId, String colonyName, String typeKey, BlockPos pos) {
}
