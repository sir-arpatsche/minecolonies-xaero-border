package com.arpatsche.coloniexaerocompat.network;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import com.arpatsche.coloniexaerocompat.server.ColonyRef;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server -> client: this colony no longer exists.
 */
public record ColonyRemovedPayload(ColonyRef colony) implements CustomPacketPayload {

    public static final Type<ColonyRemovedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyXaeroCompat.MODID, "colony_removed"));

    public static final StreamCodec<FriendlyByteBuf, ColonyRemovedPayload> CODEC =
            StreamCodec.of((buf, payload) -> payload.colony().write(buf),
                    buf -> new ColonyRemovedPayload(ColonyRef.read(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
