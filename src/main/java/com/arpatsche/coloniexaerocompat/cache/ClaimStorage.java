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

    private static Path currentFile = null;

    private ClaimStorage() {
    }

    public static void onLogin() {
        ClaimCache.clear();
        currentFile = resolveFile();
        if (currentFile == null || !Files.exists(currentFile)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(currentFile)).getAsJsonObject();
            ClaimCache.fromJson(root);
            ColonyXaeroCompat.LOGGER.info("Claim-Cache loaded: {}", currentFile);
        } catch (Exception e) {
            ColonyXaeroCompat.LOGGER.warn("Claim-Cache couldn't be read: {}", currentFile, e);
            ClaimCache.clear();
        }
    }

    public static void onLogout() {
        saveIfDirty();
        currentFile = null;
        ClaimCache.clear();
    }

    public static void saveIfDirty() {
        if (currentFile == null || !ClaimCache.isDirty()) {
            return;
        }
        try {
            Files.createDirectories(currentFile.getParent());

            Path temp = currentFile.resolveSibling(currentFile.getFileName() + ".tmp");
            Files.writeString(temp, ClaimCache.toJson().toString());
            Files.move(temp, currentFile, StandardCopyOption.REPLACE_EXISTING);
            ClaimCache.markClean();
        } catch (Exception e) {
            ColonyXaeroCompat.LOGGER.warn("Claim-Cache couldn't be saved: {}", currentFile, e);
        }
    }

    private static Path resolveFile() {
        Minecraft mc = Minecraft.getInstance();
        String key;

        MinecraftServer singleplayer = mc.getSingleplayerServer();
        ServerData server = mc.getCurrentServer();
        if (singleplayer != null) {
            Path worldFolder = singleplayer.getWorldPath(LevelResource.ROOT).normalize().getFileName();
            key = "sp_" + (worldFolder != null ? worldFolder.toString() : "unknown");
        } else if (server != null) {
            key = "mp_" + server.ip;
        } else {
            return null;
        }

        String safeName = key.replaceAll("[^a-zA-Z0-9._-]", "_");
        return mc.gameDirectory.toPath().resolve("coloniexaerocompat").resolve(safeName + ".json");
    }
}
