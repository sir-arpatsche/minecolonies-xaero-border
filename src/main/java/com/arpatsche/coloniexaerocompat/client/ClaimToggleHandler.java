package com.arpatsche.coloniexaerocompat.client;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import com.arpatsche.coloniexaerocompat.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Switches the claim display and the building icons on and off with the keys from ClientKeys:
 * in game (for the minimap) and while Xaero's world map is open.
 */
@EventBusSubscriber(modid = ColonyXaeroCompat.MODID, value = Dist.CLIENT)
public class ClaimToggleHandler {

    private static final String XAERO_MAP_SCREEN = "xaero.map.gui.GuiMap";

    private static void message(String key) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.translatable("message." + ColonyXaeroCompat.MODID + "." + key), true);
        }
    }

    private static void toggleClaims(boolean showMessage) {
        boolean now = !ClientConfig.showClaims();
        ClientConfig.setShowClaims(now);
        if (showMessage) {
            message(now ? "claims_on" : "claims_off");
        }
    }

    private static void toggleBuildings(boolean showMessage) {
        boolean now = !ClientConfig.showBuildings();
        ClientConfig.setShowBuildings(now);
        if (showMessage) {
            message(now ? "buildings_on" : "buildings_off");
        }
    }

    // In game: a key was pressed (also used for the minimap).
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        while (ClientKeys.TOGGLE_CLAIMS.consumeClick()) {
            toggleClaims(true);
        }
        while (ClientKeys.TOGGLE_BUILDINGS.consumeClick()) {
            toggleBuildings(true);
        }
    }

    // On Xaero's world map: key bindings do not fire inside screens, so we listen for the keys here.
    @SubscribeEvent
    static void onKeyPressedInScreen(ScreenEvent.KeyPressed.Pre event) {
        if (!XAERO_MAP_SCREEN.equals(event.getScreen().getClass().getName())) {
            return;
        }
        if (ClientKeys.TOGGLE_CLAIMS.matches(event.getKeyCode(), event.getScanCode())) {
            toggleClaims(false);
            event.setCanceled(true);
        } else if (ClientKeys.TOGGLE_BUILDINGS.matches(event.getKeyCode(), event.getScanCode())) {
            toggleBuildings(false);
            event.setCanceled(true);
        }
    }
}