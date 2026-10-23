package com.arpatsche.coloniexaerocompat.network;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import com.arpatsche.coloniexaerocompat.config.ServerConfig;
import com.arpatsche.coloniexaerocompat.server.ColonyData;
import com.arpatsche.coloniexaerocompat.server.ColonyRef;
import com.arpatsche.coloniexaerocompat.server.ServerColonyReader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Sends colony data to the players who have this mod: everything they are allowed to see when
 * they join, afterwards only what changed, and a notice when a colony disappears for them
 * (deleted, or they are no longer a member). What is allowed follows ServerConfig.
 * Runs on the server (also the integrated server of singleplayer).
 */
@EventBusSubscriber(modid = ColonyXaeroCompat.MODID)
public class ServerColonySync {

    // per player: what we sent them last, per colony ("dimension#id")
    private static final Map<UUID, Map<String, ColonyData>> SENT = new HashMap<>();
    private static int counter = 0;

    private static String key(ResourceKey<Level> dimension, int id) {
        return dimension.location() + "#" + id;
    }

    /** What the config allows us to send for this colony. */
    private static ColonyData prepare(ColonyData colony) {
        if (ServerConfig.shareBuildings()) {
            return colony;
        }
        return new ColonyData(colony.id(), colony.name(), colony.center(), colony.dimension(),
                colony.chunks(), List.of());
    }

    private static boolean canSee(ServerPlayer player, ColonyData colony) {
        return switch (ServerConfig.shareMode()) {
            case ALL -> true;
            case OFF -> false;
            case MEMBERS -> (ServerConfig.opsSeeAll() && player.hasPermissions(2))
                    || ServerColonyReader.isMember(player.getUUID(), new ColonyRef(colony.dimension(), colony.id()));
        };
    }

    private static boolean hasMod(ServerPlayer player) {
        return player.connection.hasChannel(ColonySyncPayload.TYPE);
    }

    @SubscribeEvent
    static void onServerStarting(ServerStartingEvent event) {
        SENT.clear(); // singleplayer reuses the JVM for the next world
    }

    @SubscribeEvent
    static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SENT.remove(event.getEntity().getUUID());
    }

    // A player joined: first the list of the colonies they may see, then each of those colonies.
    @SubscribeEvent
    static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (ServerConfig.shareMode() == ServerConfig.ShareMode.OFF) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player) || !hasMod(player)) {
            return;
        }

        List<ColonyData> all = ServerColonyReader.readAll();
        List<ColonyData> visible = new ArrayList<>();
        for (ColonyData colony : all) {
            if (canSee(player, colony)) {
                visible.add(colony);
            }
        }

        List<ColonyRef> refs = new ArrayList<>();
        for (ColonyData colony : visible) {
            refs.add(new ColonyRef(colony.dimension(), colony.id()));
        }
        send(player, new ColonyListPayload(List.copyOf(refs)));

        Map<String, ColonyData> sent = new HashMap<>();
        for (ColonyData colony : visible) {
            ColonyData prepared = prepare(colony);
            send(player, new ColonySyncPayload(prepared));
            sent.put(key(colony.dimension(), colony.id()), prepared);
        }
        SENT.put(player.getUUID(), sent);

        ColonyXaeroCompat.LOGGER.debug("[server] {} may see {} of {} colonies (mode {})",
                player.getGameProfile().getName(), visible.size(), all.size(), ServerConfig.shareMode());
    }

    // Regularly: bring every player up to date.
    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        counter++;
        if (counter < ServerConfig.checkIntervalTicks()) {
            return;
        }
        counter = 0;

        if (ServerConfig.shareMode() == ServerConfig.ShareMode.OFF) {
            return;
        }

        MinecraftServer server = event.getServer();
        List<ColonyData> all = ServerColonyReader.readAll();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!hasMod(player)) {
                continue;
            }
            Map<String, ColonyData> sent = SENT.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
            Set<String> visibleKeys = new HashSet<>();

            for (ColonyData colony : all) {
                if (!canSee(player, colony)) {
                    continue;
                }
                ColonyData prepared = prepare(colony);
                String key = key(colony.dimension(), colony.id());
                visibleKeys.add(key);
                if (!prepared.equals(sent.get(key))) {
                    send(player, new ColonySyncPayload(prepared));
                    sent.put(key, prepared);
                }
            }

            // Colonies this player had but may no longer see (deleted, or no longer a member)
            Iterator<Map.Entry<String, ColonyData>> iterator = sent.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ColonyData> entry = iterator.next();
                if (!visibleKeys.contains(entry.getKey())) {
                    ColonyData gone = entry.getValue();
                    send(player, new ColonyRemovedPayload(new ColonyRef(gone.dimension(), gone.id())));
                    iterator.remove();
                }
            }
        }
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}