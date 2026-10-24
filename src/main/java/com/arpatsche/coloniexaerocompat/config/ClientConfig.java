package com.arpatsche.coloniexaerocompat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings of the player, in config/minecoloniexaeroscompat-client.toml.
 * Only loaded on the client.
 */
public final class ClientConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue SHOW_CLAIMS;
    private static final ModConfigSpec.BooleanValue SHOW_BUILDINGS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SHOW_CLAIMS = builder
                .comment("Show the claimed area and border of colonies on the map.",
                        "You can switch this in game with the key set under Controls.")
                .define("showClaims", true);
        SHOW_BUILDINGS = builder
                .comment("Show the building icons of colonies on the map.",
                        "You can switch this in game with the key set under Controls.")
                .define("showBuildings", true);
        SPEC = builder.build();
    }

    private ClientConfig() {
    }

    public static boolean showClaims() {
        return !SPEC.isLoaded() || SHOW_CLAIMS.get();
    }

    public static boolean showBuildings() {
        return !SPEC.isLoaded() || SHOW_BUILDINGS.get();
    }

    public static void setShowBuildings(boolean value) {
        if (SPEC.isLoaded()) {
            SHOW_BUILDINGS.set(value);
            SHOW_BUILDINGS.save();
        }
    }

    public static void setShowClaims(boolean value) {
        if (SPEC.isLoaded()) {
            SHOW_CLAIMS.set(value);
            SHOW_CLAIMS.save();
        }
    }
}