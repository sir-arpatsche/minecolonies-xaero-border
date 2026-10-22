package com.arpatsche.coloniexaerocompat.map;

public record ClaimElement(int colonyId, String colonyName, int chunkX, int chunkZ,
                           boolean openNorth, boolean openSouth, boolean openWest, boolean openEast) {
}
