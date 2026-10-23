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

/**
 * Saves and loads ClaimCache, BuildingCache and ColonyInfoCache as one JSON file per world,
 * inside the "coloniexaerocompat" folder of the game directory.
 */
public final class ClaimStorage {

    // File of the world we are currently in. Remembered at login so it does not
    // have to be worked out again when saving.
    private static Path currentFile = null;

    private ClaimStorage() {
    }

    private static void clearAll() {
        ClaimCache.clear();
        BuildingCache.clear();
        ColonyInfoCache.clear();
    }

    /** On entering a world: work out the file and load the caches. */
    public static void onLogin() {
        clearAll();
        currentFile = resolveFile();
        ColonyXaeroCompat.LOGGER.info("Cache file for this world: {} (exists: {})",
                currentFile, currentFile != null && Files.exists(currentFile));
        if (currentFile == null || !Files.exists(currentFile)) {
            return;
        }
        JsonObject root;
        try {
            root = JsonParser.parseString(Files.readString(currentFile)).getAsJsonObject();
        } catch (Exception e) {
            ColonyXaeroCompat.LOGGER.warn("Could not read cache file: {}", currentFile, e);
            clearAll();
            return;
        }

        // Load every section on its own, so one damaged section does not cost the others.
        try {
            // Older files only contained the claims directly at the top level.
            JsonObject claims = root.has("claims") ? root.getAsJsonObject("claims") : root;
            ClaimCache.fromJson(claims);
        } catch (Exception e) {
            ColonyXaeroCompat.LOGGER.warn("Ignoring damaged 'claims' section in {}", currentFile, e);
            ClaimCache.clear();
        }
        try {
            if (root.has("buildings")) {
                BuildingCache.fromJson(root.getAsJsonObject("buildings"));
            }
        } catch (Exception e) {
            ColonyXaeroCompat.LOGGER.warn("Ignoring damaged 'buildings' section in {}", currentFile, e);
            BuildingCache.clear();
        }
        try {
            if (root.has("colonies")) {
                ColonyInfoCache.fromJson(root.getAsJsonObject("colonies"));
            }
        } catch (Exception e) {
            ColonyXaeroCompat.LOGGER.warn("Ignoring damaged 'colonies' section in {}", currentFile, e);
            ColonyInfoCache.clear();
        }
        ColonyXaeroCompat.LOGGER.info("Cache loaded: {} ({} buildings)", currentFile, BuildingCache.totalCount());
    }

    /** On leaving a world: save and empty the caches. */
    public static void onLogout() {
        saveIfDirty();
        currentFile = null;
        clearAll();
    }

    /** Saves only if something changed since the last save. */
    public static void saveIfDirty() {
        if (currentFile == null
                || !(ClaimCache.isDirty() || BuildingCache.isDirty() || ColonyInfoCache.isDirty())) {
            return;
        }
        try {
            JsonObject root = new JsonObject();
            root.add("claims", ClaimCache.toJson());
            root.add("buildings", BuildingCache.toJson());
            root.add("colonies", ColonyInfoCache.toJson());

            Files.createDirectories(currentFile.getParent());
            // Write to a temp file first, then replace: if the game crashes mid-write,
            // the old file stays intact.
            Path temp = currentFile.resolveSibling(currentFile.getFileName() + ".tmp");
            Files.writeString(temp, root.toString());
            Files.move(temp, currentFile, StandardCopyOption.REPLACE_EXISTING);
            ClaimCache.markClean();
            BuildingCache.markClean();
            ColonyInfoCache.markClean();
            ColonyXaeroCompat.LOGGER.info("Cache saved: {}", currentFile);
        } catch (Exception e) {
            ColonyXaeroCompat.LOGGER.warn("Could not save cache: {}", currentFile, e);
        }
    }

    /** Works out the file for the current world or server. */
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