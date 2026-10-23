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

/**
 * Sends the colony data to players: everything when they join, afterwards only what changed,
 * and a notice when a colony no longer exists. What is sent follows ServerConfig.
 * Runs on the server (also the integrated server of singleplayer).
 */
@EventBusSubscriber(modid = ColonyXaeroCompat.MODID)
public class ServerColonySync {

    // what we sent last, per colony ("dimension#id"), to detect changes and deletions
    private static final Map<String, ColonyData> LAST_SENT = new HashMap<>();
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

    @SubscribeEvent
    static void onServerStarting(ServerStartingEvent event) {
        LAST_SENT.clear(); // singleplayer reuses the JVM for the next world
    }

    // A player joined: first the list of all existing colonies, then every colony.
    @SubscribeEvent
    static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (ServerConfig.shareMode() == ServerConfig.ShareMode.OFF) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        List<ColonyData> colonies = ServerColonyReader.readAll();

        List<ColonyRef> refs = new ArrayList<>();
        for (ColonyData colony : colonies) {
            refs.add(new ColonyRef(colony.dimension(), colony.id()));
        }
        send(player, new ColonyListPayload(List.copyOf(refs)));

        for (ColonyData colony : colonies) {
            send(player, new ColonySyncPayload(prepare(colony)));
        }
    }

    // Regularly: send what changed to everyone, and report colonies that are gone.
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
        Set<String> present = new HashSet<>();

        for (ColonyData colony : ServerColonyReader.readAll()) {
            ColonyData toSend = prepare(colony);
            String key = key(colony.dimension(), colony.id());
            present.add(key);
            if (toSend.equals(LAST_SENT.get(key))) {
                continue;
            }
            LAST_SENT.put(key, toSend);
            broadcast(server, new ColonySyncPayload(toSend));
        }

        // Colonies we sent earlier that no longer exist
        Iterator<Map.Entry<String, ColonyData>> iterator = LAST_SENT.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ColonyData> entry = iterator.next();
            if (!present.contains(entry.getKey())) {
                ColonyData gone = entry.getValue();
                broadcast(server, new ColonyRemovedPayload(new ColonyRef(gone.dimension(), gone.id())));
                iterator.remove();
            }
        }
    }

    private static void broadcast(MinecraftServer server, CustomPacketPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            send(player, payload);
        }
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        // Only send to players who have this mod (the messages are optional).
        if (player.connection.hasChannel(payload.type())) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }
}