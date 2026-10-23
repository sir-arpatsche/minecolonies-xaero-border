package com.arpatsche.coloniexaerocompat.server;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Identifies one colony: its id is only unique together with the dimension.
 */
public record ColonyRef(ResourceKey<Level> dimension, int id) {

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension.location());
        buf.writeVarInt(id);
    }

    public static ColonyRef read(FriendlyByteBuf buf) {
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        return new ColonyRef(dimension, buf.readVarInt());
    }
}
