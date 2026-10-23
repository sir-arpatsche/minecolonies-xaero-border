package com.arpatsche.coloniexaerocompat;

import com.arpatsche.coloniexaerocompat.config.ServerConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Main mod class. It runs on both sides: the map drawing lives in ColonyXaeroClient
 * (never loaded on a dedicated server), the optional server part sends colony data to clients.
 */
// The value here must match the mod_id in gradle.properties / neoforge.mods.toml
@Mod(ColonyXaeroCompat.MODID)
public class ColonyXaeroCompat {

    public static final String MODID = "minecoloniexaeroscompat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ColonyXaeroCompat(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ServerConfig.SPEC, "minecoloniexaeroscompat-server.toml");
    }
}