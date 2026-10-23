package com.arpatsche.coloniexaerocompat.network;

import com.arpatsche.coloniexaerocompat.server.ColonyData;
import com.arpatsche.coloniexaerocompat.server.ServerColonyReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;

import java.util.HashMap;
import java.util.Map;

/**
 * Sends the colony data to players: everything when they join, afterwards only what changed.
 * Runs on the server (also the integrated server of singleplayer).
 */
@EventBusSubscriber(modid = ColonyXaeroCompat.MODID)
public class ServerColonySync {

    // what we sent last, per colony ("dimension#id"), to detect changes
    private static final Map<String, ColonyData> LAST_SENT = new HashMap<>();
    private static int counter = 0;

    @SubscribeEvent
    static void onServerStarting(ServerStartingEvent event) {
        LAST_SENT.clear(); // singleplayer reuses the JVM for the next world
    }

    // A player joined: send every colony.
    @SubscribeEvent
    static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        for (ColonyData colony : ServerColonyReader.readAll()) {
            send(player, colony);
        }
    }

    // Every 5 seconds: send the colonies that changed to everyone.
    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        counter++;
        if (counter < 100) {
            return;
        }
        counter = 0;

        MinecraftServer server = event.getServer();
        for (ColonyData colony : ServerColonyReader.readAll()) {
            String key = colony.dimension().location() + "#" + colony.id();
            if (colony.equals(LAST_SENT.get(key))) {
                continue;
            }
            LAST_SENT.put(key, colony);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                send(player, colony);
            }
        }
    }

    private static void send(ServerPlayer player, ColonyData colony) {
        // Only send to players who have this mod (the message is optional).
        if (player.connection.hasChannel(ColonySyncPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, new ColonySyncPayload(colony));
        }
    }
}
