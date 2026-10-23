package com.arpatsche.coloniexaerocompat.network;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers our network messages. They are "optional": players without this mod can still join a
 * server that has it, and players with it can still join servers that do not.
 */
@EventBusSubscriber(modid = ColonyXaeroCompat.MODID)
public class ModNetwork {

    @SubscribeEvent
    static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(ColonySyncPayload.TYPE, ColonySyncPayload.CODEC, ClientColonySync::handle);
    }
}
