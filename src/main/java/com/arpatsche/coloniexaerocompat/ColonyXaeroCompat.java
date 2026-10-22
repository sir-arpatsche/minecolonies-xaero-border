package com.arpatsche.coloniexaerocompat;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Main mod class. This mod is client-side only: all the real work happens in
 * ColonyXaeroClient, which is never loaded on a dedicated server.
 */
// The value here must match the mod_id in gradle.properties / neoforge.mods.toml
@Mod(ColonyXaeroCompat.MODID)
public class ColonyXaeroCompat {

    public static final String MODID = "minecoloniexaeroscompat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ColonyXaeroCompat() {
    }
}