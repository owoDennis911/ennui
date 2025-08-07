package me.earth.earthhack.impl.modules.dev.securechat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.api.setting.settings.StringSetting;
import me.earth.earthhack.impl.event.events.network.PacketEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.modules.dev.securechat.modes.DecryptMode;
import me.earth.earthhack.impl.modules.dev.securechat.modes.EncryptMode;
import me.earth.earthhack.impl.util.math.crypto.CodeCaesarUtil;
import me.earth.earthhack.impl.util.math.crypto.CodeObfscatorUtil;
import me.earth.earthhack.impl.util.math.crypto.CryptoUtil;
import me.earth.earthhack.impl.util.text.ChatUtil;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;

public class SecureChat extends Module {
    private static final int CAESAR_SHIFT = 5;

    protected final StringSetting prefixCaesar = register(new StringSetting("PrefixCaesar", "<cae>"));
    protected final StringSetting prefixCompact = register(new StringSetting("PrefixCompact", "$"));
    protected final StringSetting prefixShuffle = register(new StringSetting("PrefixShuffle", "<trS>"));
    protected final StringSetting prefixEncrypted = register(new StringSetting("PrefixEncrypted", "<trE>"));
    protected final BooleanSetting autoPrefix = register(new BooleanSetting("AutoPrefix", true));
    protected final BooleanSetting encryptCommand = register(new BooleanSetting("EncryptCommands", false));

    protected final Setting<EncryptMode> encryption = register(new EnumSetting<>("Encryption", EncryptMode.Caesar));
    protected final BooleanSetting keepEncrypted = register(new BooleanSetting("KeepEncrypted", true));
    protected final Setting<DecryptMode> decryption = register(new EnumSetting<>("Decryption", DecryptMode.All));
    protected final BooleanSetting decryptOwn = register(new BooleanSetting("DecryptOwn", true));



    private final String DecryptPrefix = ChatFormatting.RED + "[" + ChatFormatting.GOLD + "Decrypted" + ChatFormatting.RED + "]" + ChatFormatting.LIGHT_PURPLE;
    private String lastSent = null;
    private String lastReceived = null;
    private boolean processingPacket = false;

    public SecureChat() {
        super("SecureChat", Category.Dev);
        setData(new SecureChatData(this));

        // Receive
        listeners.add(new LambdaListener<>(PacketEvent.Receive.class, e -> {
            if (e.getPacket() instanceof SPacketChat) {
                SPacketChat chatPacket = (SPacketChat)e.getPacket();
                String msg = chatPacket.getChatComponent().getUnformattedText();

                EncryptMode detectedMode = getEncryptMode(msg);

                if (msg.equals(lastReceived) || decryption.getValue() == DecryptMode.None || detectedMode == null) {
                    return;
                }
                if(decryptOwn.getValue() &&  msg.equals(lastSent)) {
                    return;
                }

                if (decryption.getValue() != DecryptMode.All && !decryption.getValue().name().equals(detectedMode.name())) {
                    return;
                }

                String encryptedContent = extractEncryptedContent(msg, detectedMode);
                if (encryptedContent.isEmpty()) return;

                String decrypted = decryptMessage(encryptedContent, detectedMode);

                if (decrypted.isEmpty()) return;

                if (!keepEncrypted.getValue()) {
                    e.setCancelled(true);
                }

                String decryptedFull = DecryptPrefix + decrypted;
                lastReceived = decryptedFull;
                ChatUtil.sendMessage(decryptedFull);
            }
        }));


        // Send
        listeners.add(new LambdaListener<>(PacketEvent.Send.class, e -> {
            if (!(e.getPacket() instanceof CPacketChatMessage) || processingPacket) return;

            CPacketChatMessage packet = (CPacketChatMessage) e.getPacket();
            String msg = packet.getMessage();

            if (msg.equals(lastSent)) return;

            if (msg.startsWith("/") && !encryptCommand.getValue()) return;

            String currentPrefix = getPrefix(encryption.getValue());
            boolean manuallyPrefixed = msg.startsWith(currentPrefix);

            String messageToEncrypt = manuallyPrefixed ? msg.substring(currentPrefix.length()) : msg;

            if (messageToEncrypt.isEmpty()) return;

            String encrypted = encryptMessage(messageToEncrypt, encryption.getValue());

            String finalMessage = (autoPrefix.getValue() || manuallyPrefixed) ? currentPrefix + encrypted : encrypted;

            if (finalMessage.length() > 256) {
                finalMessage = finalMessage.substring(0, 256);
            }

            e.setCancelled(true);

            processingPacket = true;
            lastSent = finalMessage;

            mc.player.connection.sendPacket(new CPacketChatMessage(finalMessage));
            processingPacket = false;
        }));
    }

    private EncryptMode getEncryptMode(String msg) {
        if (msg.startsWith(prefixCaesar.getValue()))
            return EncryptMode.Caesar;
        if (msg.startsWith(prefixCompact.getValue()))
            return EncryptMode.Compact;
        if (msg.startsWith(prefixShuffle.getValue()))
            return EncryptMode.Shuffled;
        if (msg.startsWith(prefixEncrypted.getValue()))
            return EncryptMode.Encrypted;

        return getMode(msg);
    }

    private EncryptMode getMode(String msg) {
        int caesarIndex = msg.indexOf(prefixCaesar.getValue());
        int compactIndex = msg.indexOf(prefixCompact.getValue());
        int shuffleIndex = msg.indexOf(prefixShuffle.getValue());
        int encryptedIndex = msg.indexOf(prefixEncrypted.getValue());

        int earliestIndex = Integer.MAX_VALUE;
        EncryptMode detectedMode = null;

        if (caesarIndex != -1) {
            earliestIndex = caesarIndex;
            detectedMode = EncryptMode.Caesar;
        }
        if (compactIndex != -1 && compactIndex < earliestIndex) {
            earliestIndex = compactIndex;
            detectedMode = EncryptMode.Compact;
        }
        if (shuffleIndex != -1 && shuffleIndex < earliestIndex) {
            earliestIndex = shuffleIndex;
            detectedMode = EncryptMode.Shuffled;
        }
        if (encryptedIndex != -1 && encryptedIndex < earliestIndex) {
            detectedMode = EncryptMode.Encrypted;
        }
        return detectedMode;
    }

    private String extractEncryptedContent(String msg, EncryptMode mode) {
        String prefix = getPrefix(mode);
        int prefixIndex = msg.indexOf(prefix);

        if (prefixIndex == -1) return "";

        return msg.substring(prefixIndex + prefix.length());
    }

    private String getPrefix(EncryptMode mode) {
        switch (mode) {
            case Caesar: return prefixCaesar.getValue();
            case Compact: return prefixCompact.getValue();
            case Shuffled: return prefixShuffle.getValue();
            case Encrypted: return prefixEncrypted.getValue();
            default: return "";
        }
    }

    private String encryptMessage(String message, EncryptMode mode) {
        switch (mode) {
            case Caesar: return CodeCaesarUtil.encryptCaesar(message, CAESAR_SHIFT);
            case Compact: return CodeObfscatorUtil.obfuscateCompact(message);
            case Shuffled: return CryptoUtil.shuffle(message);
            case Encrypted: return CryptoUtil.encrypt(message);
            default: return message;
        }
    }

    private String decryptMessage(String message, EncryptMode mode) {
        switch (mode) {
            case Caesar: return CodeCaesarUtil.decryptCaesar(message, CAESAR_SHIFT);
            case Compact: return CodeObfscatorUtil.deobfuscateCompact(message);
            case Shuffled: return CryptoUtil.deshuffle(message);
            case Encrypted: return CryptoUtil.decrypt(message);
            default: return message;
        }
    }
}