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
        /** Players only get the colonies they own or are an officer or friend in. */
        MEMBERS,
        /** The server sends nothing. */
        OFF
    }

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.EnumValue<ShareMode> SHARE_MODE;
    private static final ModConfigSpec.BooleanValue OPS_SEE_ALL;
    private static final ModConfigSpec.BooleanValue SHARE_BUILDINGS;
    private static final ModConfigSpec.IntValue CHECK_INTERVAL_SECONDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("What this server tells players who have the mod installed.").push("sharing");
        SHARE_MODE = builder
                .comment("MEMBERS = a player only gets the colonies they own or are an officer or friend in.",
                        "ALL = every player with the mod gets the claims and buildings of all colonies.",
                        "OFF = this server sends nothing; players only see what they discover themselves.")
                .defineEnum("shareMode", ShareMode.MEMBERS);
        OPS_SEE_ALL = builder
                .comment("true = server operators (permission level 2 or higher) get all colonies, even in MEMBERS mode.")
                .define("opsSeeAll", true);
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
        // Until the file is loaded, share as little as possible.
        return SPEC.isLoaded() ? SHARE_MODE.get() : ShareMode.MEMBERS;
    }

    public static boolean opsSeeAll() {
        return !SPEC.isLoaded() || OPS_SEE_ALL.get();
    }

    public static boolean shareBuildings() {
        return !SPEC.isLoaded() || SHARE_BUILDINGS.get();
    }

    public static int checkIntervalTicks() {
        return (SPEC.isLoaded() ? CHECK_INTERVAL_SECONDS.get() : 5) * 20;
    }
}