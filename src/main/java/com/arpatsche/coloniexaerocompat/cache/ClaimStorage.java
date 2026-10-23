package com.arpatsche.coloniexaerocompat.cache;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


public final class ClaimStorage {

    // File of the world we are currently in. Remembered at login so it does not
    // have to be worked out again when saving.
    private static Path currentFile = null;

    private ClaimStorage() {
    }

    public static void onLogin() {
        ClaimCache.clear();
        BuildingCache.clear();
        currentFile = resolveFile();
        if (currentFile == null || !Files.exists(currentFile)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(currentFile)).getAsJsonObject();
            // Older files only contained the claims directly at the top level.
            JsonObject claims = root.has("claims") ? root.getAsJsonObject("claims") : root;
            ClaimCache.fromJson(claims);
            if (root.has("buildings")) {
                BuildingCache.fromJson(root.getAsJsonObject("buildings"));
            }
            ColonyXaeroCompat.LOGGER.info("Cache loaded: {} ({} buildings)", currentFile, BuildingCache.totalCount());
        } catch (Exception e) {
            ColonyXaeroCompat.LOGGER.warn("Could not read cache: {}", currentFile, e);
            ClaimCache.clear();
            BuildingCache.clear();
        }
    }

    public static void onLogout() {
        saveIfDirty();
        currentFile = null;
        ClaimCache.clear();
        BuildingCache.clear();
    }

    public static void saveIfDirty() {
        if (currentFile == null || !(ClaimCache.isDirty() || BuildingCache.isDirty())) {
            return;
        }
        try {
            JsonObject root = new JsonObject();
            root.add("claims", ClaimCache.toJson());
            root.add("buildings", BuildingCache.toJson());

            Files.createDirectories(currentFile.getParent());
            // Write to a temp file first, then replace: if the game crashes mid-write,
            // the old file stays intact.
            Path temp = currentFile.resolveSibling(currentFile.getFileName() + ".tmp");
            Files.writeString(temp, root.toString());
            Files.move(temp, currentFile, StandardCopyOption.REPLACE_EXISTING);
            ClaimCache.markClean();
            BuildingCache.markClean();
        } catch (Exception e) {
            ColonyXaeroCompat.LOGGER.warn("Could not save cache: {}", currentFile, e);
        }
    }

    private static Path resolveFile() {
        Minecraft mc = Minecraft.getInstance();
        String key;

        MinecraftServer singleplayer = mc.getSingleplayerServer();
        ServerData server = mc.getCurrentServer();
        if (singleplayer != null) {
            // Name of the world folder, e.g. "New World"
            Path worldFolder = singleplayer.getWorldPath(LevelResource.ROOT).normalize().getFileName();
            key = "sp_" + (worldFolder != null ? worldFolder.toString() : "unknown");
        } else if (server != null) {
            key = "mp_" + server.ip;
        } else {
            return null; // e.g. Realms: nothing is saved here (yet)
        }

        String safeName = key.replaceAll("[^a-zA-Z0-9._-]", "_");
        return mc.gameDirectory.toPath().resolve("coloniexaerocompat").resolve(safeName + ".json");
    }
}