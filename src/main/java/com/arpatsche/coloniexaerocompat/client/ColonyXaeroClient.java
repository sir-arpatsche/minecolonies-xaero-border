package com.arpatsche.coloniexaerocompat.client;

import com.arpatsche.coloniexaerocompat.cache.ClaimCache;
import com.arpatsche.coloniexaerocompat.map.ClaimRenderer;
import com.arpatsche.coloniexaerocompat.cache.ClaimStorage;
import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import xaero.map.WorldMap;
import xaero.map.element.MapElementRenderHandler;

@Mod(value = ColonyXaeroCompat.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ColonyXaeroCompat.MODID, value = Dist.CLIENT)
public class ColonyXaeroClient {

    private static int updateCounter = 0;
    private static int saveCounter = 0;

    private static MapElementRenderHandler registeredHandler = null;

    public ColonyXaeroClient() {
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        registerRendererIfNeeded();

        saveCounter++;
        if (saveCounter >= 600) {
            saveCounter = 0;
            ClaimStorage.saveIfDirty();
        }

        updateCounter++;
        if (updateCounter < 100) {
            return;
        }
        updateCounter = 0;

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        ClaimCache.update(level);
    }

    @SubscribeEvent
    static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        ClaimStorage.onLogin();
    }

    @SubscribeEvent
    static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClaimStorage.onLogout();
    }

    private static void registerRendererIfNeeded() {
        MapElementRenderHandler handler = WorldMap.mapElementRenderHandler;
        if (handler == null || handler == registeredHandler) {
            return;
        }
        handler.add(new ClaimRenderer());
        registeredHandler = handler;
        ColonyXaeroCompat.LOGGER.info("ClaimRenderer logged in Xaero's World Map");
    }
}