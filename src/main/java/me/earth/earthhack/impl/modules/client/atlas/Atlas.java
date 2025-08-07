package me.earth.earthhack.impl.modules.client.atlas;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.StringSetting;
import me.earth.earthhack.impl.event.events.client.ShutDownEvent;
import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.events.network.PacketEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;

import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.text.ChatUtil;
import me.earth.earthhack.impl.util.text.TextColor;
import net.minecraft.network.play.client.CPacketChatMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Atlas extends Module {

    private final Setting<Boolean> noPrefix = register(new BooleanSetting("No Prefix", false));
    private final Setting<String> prefix = register(new StringSetting("Prefix", "$"));
    private final Setting<String> username = register(new StringSetting("Username", "$"));
    private final Setting<String> password = register(new StringSetting("Password", "$"));

    private final StopWatch timer = new StopWatch();
    private boolean finished = true;

    private static final String IRCTAG = TextColor.GRAY + "[" + TextColor.RED + "ATLAS" + TextColor.GRAY + "] " + TextColor.GOLD;
    private static final String ATLAS_NET = "https://atlasnet.pythonanywhere.com";

    private static final Gson GSON = new Gson();

    private final ScheduledExecutorService net = Executors.newScheduledThreadPool(1);

    public Atlas() {
        super("ATLAS", Category.Client);
        this.listeners.add(new LambdaListener<>(TickEvent.class, e -> {
            if (e.isSafe() && finished && timer.passed(500)) {
                timer.reset();
                finished = false;
                net.schedule(() -> {
                    try {
                        URL url = new URL(ATLAS_NET + "/irc/get?username=" + username.getValue() + "&password=" + password.getValue());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        String jsonString = sb.toString();
                        List<ExternalMessage> externalMessages = GSON.fromJson(jsonString, new TypeToken<List<ExternalMessage>>() {
                        }.getType());
                        for (ExternalMessage externalMessage : externalMessages) {
                            printMessage(externalMessage.getContent());
                        }
                    } catch (IOException ignored) {

                    }
                    finished = true;
                }, 0, TimeUnit.SECONDS);
            }
        }));

        this.listeners.add(new LambdaListener<>(PacketEvent.Send.class, Integer.MAX_VALUE - 1, event -> {
            if(event.getPacket() instanceof CPacketChatMessage) {
                CPacketChatMessage pkt = (CPacketChatMessage) event.getPacket();
                if (noPrefix.getValue()) {
                    event.setCancelled(true);
                    String s = pkt.getMessage().replaceFirst(prefix.getValue(), "");
                    sendMessage(s);
                } else {
                    if (pkt.getMessage().startsWith(prefix.getValue())) {
                        event.setCancelled(true);
                        String s = pkt.getMessage().replaceFirst(prefix.getValue(), "");
                        sendMessage(s);
                    }
                }
            }
        }));
        this.listeners.add(new LambdaListener<>(ShutDownEvent.class, e -> sendMessage("/disconnect")));
    }

    @Override
    protected void onEnable() {
        sendMessage("/connect");
    }

    @Override
    protected void onDisable() {
        sendMessage("/disconnect");
    }

    private void printMessage(String message) {
        ChatUtil.sendMessage(IRCTAG + message);
    }

    public void sendMessage(String message) {
        net.schedule(() -> {
            try {
                String finalMessage = message.replace(" ", "%20");
                String s = ATLAS_NET + "/irc/send?username=" + username.getValue() + "&password=" + password.getValue() + "&message=" + finalMessage;
                BufferedReader br = new BufferedReader(new InputStreamReader(new URL(s).openConnection().getInputStream()));
                br.close();
            } catch (Exception ignored){

            }
        }, 0, TimeUnit.SECONDS);
    }

    static class ExternalMessage {
        private String message;
        private long timestamp;

        public String getContent() {
            return message;
        }


        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setContent(String content) {
            this.message = content;
        }

    }


}
