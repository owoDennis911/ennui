package me.earth.earthhack.impl.modules.client.translator;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.impl.event.events.network.PacketEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.modules.client.translator.modes.receivedMessageMode;
import me.earth.earthhack.impl.modules.client.translator.modes.sentMessageMode;
import me.earth.earthhack.impl.util.network.GoogleTranslate;
import me.earth.earthhack.impl.util.text.ChatUtil;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;

import java.io.IOException;

public class Translator extends Module {

    private final EnumSetting<sentMessageMode> sentLanguage =
            register(new EnumSetting<>("Translate-Sent", sentMessageMode.Disabled));

    private final EnumSetting<receivedMessageMode> receivedLanguage =
            register(new EnumSetting<>("Translate-Receiver", receivedMessageMode.Disabled));

    private final BooleanSetting msgs =
            register(new BooleanSetting("Messages", false));


    private boolean isTranslating = false;

    private final String TRANSLATED_PREFIX = ChatFormatting.LIGHT_PURPLE + "[" + ChatFormatting.GOLD + "Translator" + ChatFormatting.LIGHT_PURPLE + "] ";


    public Translator() {
        super("Translator", Category.Client);

        // Receiving Packet
        this.listeners.add(new LambdaListener<>(PacketEvent.Receive.class, e -> {
            if (e.getPacket() instanceof SPacketChat) {
                SPacketChat chatPacket = (SPacketChat) e.getPacket();
                String message = chatPacket.getChatComponent().getUnformattedText();

                if (message.isEmpty() || message.startsWith(TRANSLATED_PREFIX)) {
                    return;
                }
                if (receivedLanguage.getValue() == receivedMessageMode.Disabled ||
                        (msgs.getValue() &&
                                (message.startsWith("/") || message.startsWith("/msg") ||
                                        message.startsWith("/r") || message.startsWith("/w") ||
                                        message.startsWith("/tell")))) {
                    return;
                }

                String finalLanguage = receivedLanguage.getValue().getLang();
                String translatedText;
                try {
                    translatedText = TRANSLATED_PREFIX + ChatFormatting.RESET +
                            GoogleTranslate.translateIncomingOrOutgoing(message, finalLanguage);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                ChatUtil.sendMessage(translatedText);
            }
        }));

        // Sending Packet
        this.listeners.add(new LambdaListener<>(PacketEvent.Send.class, e -> {
            if (e.getPacket() instanceof CPacketChatMessage) {
                if (isTranslating) {
                    return;
                }
                CPacketChatMessage chatPacket = (CPacketChatMessage) e.getPacket();
                String message = chatPacket.getMessage();

                if (message.isEmpty() || message.startsWith(TRANSLATED_PREFIX)) {
                    return;
                }
                if (sentLanguage.getValue() == sentMessageMode.Disabled ||
                        (msgs.getValue() &&
                                (message.startsWith("/") || message.startsWith("/msg") ||
                                        message.startsWith("/r") || message.startsWith("/w") ||
                                        message.startsWith("/tell")))) {
                    return;
                }
                isTranslating = true;
                e.setCancelled(true);
                String finalLanguage = sentLanguage.getValue().getLang();
                String translatedText;
                try {
                    translatedText = GoogleTranslate.translateIncomingOrOutgoing(message, finalLanguage);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                mc.player.sendChatMessage(translatedText);
                isTranslating = false;
            }
        }));
    }
}
