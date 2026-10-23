package com.arpatsche.coloniexaerocompat.network;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import com.arpatsche.coloniexaerocompat.server.ColonyRef;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Server -> client, sent once when joining: all colonies that exist right now.
 * The client drops every remembered colony that is not in this list.
 */
public record ColonyListPayload(List<ColonyRef> colonies) implements CustomPacketPayload {

    public static final Type<ColonyListPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyXaeroCompat.MODID, "colony_list"));

    public static final StreamCodec<FriendlyByteBuf, ColonyListPayload> CODEC =
            StreamCodec.of((buf, payload) -> {
                buf.writeVarInt(payload.colonies().size());
                for (ColonyRef ref : payload.colonies()) {
                    ref.write(buf);
                }
            }, buf -> {
                int count = buf.readVarInt();
                List<ColonyRef> list = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    list.add(ColonyRef.read(buf));
                }
                return new ColonyListPayload(List.copyOf(list));
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
