package me.earth.earthhack.impl.util.discord;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.earth.earthhack.api.util.interfaces.Globals;
import me.earth.earthhack.impl.Earthhack;
import me.earth.earthhack.impl.modules.misc.rpc.RPC;
import me.earth.earthhack.impl.util.math.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class DiscordPresence implements Globals {
    private static final Logger LOGGER = LogManager.getLogger(DiscordPresence.class);
    private static final DiscordRichPresence presence = new DiscordRichPresence();
    private static final DiscordRPC rpc = DiscordRPC.INSTANCE;
    private final RPC module;
    private Thread thread;

    public DiscordPresence(RPC module) {
        this.module = module;
    }

    public synchronized void start() {
        if (thread != null) {
            thread.interrupt();
        }

        //TODO: add more settings

        LOGGER.info("Initializing Discord RPC");
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        rpc.Discord_Initialize("1184749984348913694", handlers, true, "");
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.details = getName();
        presence.state  = getServer();
        presence.largeImageKey = "logo";
        presence.smallImageKey = "skin";
        presence.smallImageText = Earthhack.NAME + " " + Earthhack.VERSION;
        presence.largeImageText = Earthhack.NAME + " " + Earthhack.VERSION;
        rpc.Discord_UpdatePresence(DiscordPresence.presence);
        StopWatch timer = new StopWatch();
        timer.reset();
        thread = new Thread(() ->
        {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }

                rpc.Discord_RunCallbacks();
                presence.details = getName();
                presence.state  = getServer();
                if (timer.passed(TimeUnit.SECONDS.toMillis(15))) {
                    // TODO: only when an update is needed?
                    rpc.Discord_UpdatePresence(presence);
                    timer.reset();
                }
            }
        }, "RPC-Callback-Handler");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stop() {
        LOGGER.info("Shutting down Discord RPC");
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
            thread = null;
        }

        rpc.Discord_Shutdown();
    }

    private String getName() {
        if (module.showName.getValue() && (mc.player != null)) {
            return mc.player.getName();
        } else {
            return "Loading";
        }
    }

    private String getServer() {
        if (module.showIP.getValue() && (mc.player) == null) {
            return "Not in-game";
        } else if (mc.isIntegratedServerRunning()) {
            return "Playing SinglePlayer";
        } else if (mc.getCurrentServerData() != null) {
            // Check if the player is in a multiplayer game and get the server IP
            String serverIp = mc.getCurrentServerData().serverIP;
            return "Playing " + serverIp;
        } else {
            return "Playing Multiplayer";
        }
    }
}
