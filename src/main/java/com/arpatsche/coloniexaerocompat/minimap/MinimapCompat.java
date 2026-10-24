package com.arpatsche.coloniexaerocompat.minimap;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import xaero.common.HudMod;
import xaero.common.minimap.render.MinimapFBORenderer;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.element.render.map.MinimapElementMapRendererHandler;

import java.lang.reflect.Field;

/**
 * Connects our renderers to Xaero's minimap. Only ever touched when Xaero's Minimap is installed.
 * <p>
 * The minimap creates the handler that draws elements inside its frame itself (a private field of
 * MinimapFBORenderer), and creates a new one whenever it reloads its framebuffers. So tick() checks
 * every tick whether the handler changed and registers our renderers with the new one.
 */
public final class MinimapCompat {

    private static Field handlerField;
    private static MinimapElementMapRendererHandler registeredHandler;
    private static boolean warned = false;

    private MinimapCompat() {
    }

    /** Called every client tick. */
    public static void tick() {
        MinimapElementMapRendererHandler handler = currentHandler();
        if (handler != null && handler != registeredHandler) {
            register(handler);
        }
    }

    private static MinimapElementMapRendererHandler currentHandler() {
        try {
            Minimap minimap = HudMod.INSTANCE.getMinimap();
            if (minimap == null) {
                return null;
            }
            MinimapFBORenderer fboRenderer = minimap.getMinimapFBORenderer();
            if (fboRenderer == null) {
                return null;
            }
            if (handlerField == null) {
                handlerField = MinimapFBORenderer.class.getDeclaredField("minimapElementMapRendererHandler");
                handlerField.setAccessible(true);
            }
            return (MinimapElementMapRendererHandler) handlerField.get(fboRenderer);
        } catch (Exception | LinkageError e) {
            if (!warned) {
                warned = true;
                ColonyXaeroCompat.LOGGER.warn("Could not connect to Xaero's Minimap; nothing will be shown on the minimap", e);
            }
            return null;
        }
    }

    /** Registers our renderers with the handler that draws elements inside the minimap frame. */
    private static void register(MinimapElementMapRendererHandler mapHandler) {
        registeredHandler = mapHandler;
        MinimapTransform.setHandler(mapHandler);
        mapHandler.add(new MinimapClaimRenderer());
        mapHandler.add(new MinimapBuildingRenderer());
        ColonyXaeroCompat.LOGGER.info("Minimap renderers registered with Xaero's Minimap");
    }
}
