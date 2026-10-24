package com.arpatsche.coloniexaerocompat.client;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import com.arpatsche.coloniexaerocompat.minimap.MinimapCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Keeps our renderers connected to Xaero's Minimap, if that mod is installed.
 * MinimapCompat is only loaded once this class actually calls it, so nothing breaks without the minimap.
 */
@EventBusSubscriber(modid = ColonyXaeroCompat.MODID, value = Dist.CLIENT)
public class MinimapHook {

    private static Boolean minimapInstalled;

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (minimapInstalled == null) {
            minimapInstalled = ModList.get().isLoaded("xaerominimap");
        }
        if (minimapInstalled) {
            MinimapCompat.tick();
        }
    }
}
