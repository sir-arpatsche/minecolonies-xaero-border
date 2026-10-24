package com.arpatsche.coloniexaerocompat.minimap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * State shared between a minimap renderer and its provider. Set before every frame.
 */
public class MinimapContext {
    /** The dimension the minimap is showing. */
    public ResourceKey<Level> dimension;
    /** Position the minimap is centred on. */
    public double playerX;
    public double playerZ;
    /** Rotation of the minimap in radians, and pixels per block. */
    public double angle;
    public double zoom = 1.0;
    /** False if the rotation and zoom could not be read; then nothing is drawn. */
    public boolean transformKnown;
}
