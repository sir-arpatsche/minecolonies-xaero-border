package com.arpatsche.coloniexaerocompat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings for the server part of the mod, in config/minecoloniexaeroscompat-server.toml.
 * Only the server (or the integrated server of singleplayer) reads these values.
 */
public final class ServerConfig {

    public enum ShareMode {
        /** Every player with the mod gets all colonies. */
        ALL,
        /** The server sends nothing. */
        OFF
    }

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.EnumValue<ShareMode> SHARE_MODE;
    private static final ModConfigSpec.BooleanValue SHARE_BUILDINGS;
    private static final ModConfigSpec.IntValue CHECK_INTERVAL_SECONDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("What this server tells players who have the mod installed.").push("sharing");
        SHARE_MODE = builder
                .comment("ALL = every player with the mod gets the claims and buildings of all colonies.",
                        "OFF = this server sends nothing; players only see what they discover themselves.")
                .defineEnum("shareMode", ShareMode.ALL);
        SHARE_BUILDINGS = builder
                .comment("false = send only the claimed area, name and centre of a colony, not its buildings.")
                .define("shareBuildings", true);
        builder.pop();

        builder.push("performance");
        CHECK_INTERVAL_SECONDS = builder
                .comment("How often (in seconds) the server checks the colonies for changes.")
                .defineInRange("checkIntervalSeconds", 5, 1, 60);
        builder.pop();

        SPEC = builder.build();
    }

    private ServerConfig() {
    }

    public static ShareMode shareMode() {
        return SPEC.isLoaded() ? SHARE_MODE.get() : ShareMode.ALL;
    }

    public static boolean shareBuildings() {
        return !SPEC.isLoaded() || SHARE_BUILDINGS.get();
    }

    public static int checkIntervalTicks() {
        return (SPEC.isLoaded() ? CHECK_INTERVAL_SECONDS.get() : 5) * 20;
    }
}
