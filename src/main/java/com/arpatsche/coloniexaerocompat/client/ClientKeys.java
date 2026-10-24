package com.arpatsche.coloniexaerocompat.client;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Our key bindings (shown under Controls).
 */
@EventBusSubscriber(modid = ColonyXaeroCompat.MODID, value = Dist.CLIENT)
public class ClientKeys {

    public static final KeyMapping TOGGLE_CLAIMS = new KeyMapping(
            "key." + ColonyXaeroCompat.MODID + ".toggle_claims",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories." + ColonyXaeroCompat.MODID);

    public static final KeyMapping TOGGLE_BUILDINGS = new KeyMapping(
            "key." + ColonyXaeroCompat.MODID + ".toggle_buildings",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "key.categories." + ColonyXaeroCompat.MODID);

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_CLAIMS);
        event.register(TOGGLE_BUILDINGS);
    }
}