package me.earth.earthhack.impl.modules.dev.securechat;


import me.earth.earthhack.api.module.data.DefaultData;

final class SecureChatData extends DefaultData<SecureChat> {
    public SecureChatData(SecureChat module) {
        super(module);
        register(module.prefixCaesar, "Caesar cipher prefix for the encrypted messages.");
        register(module.prefixCompact, "Compact cipher prefix for the encrypted messages.");
        register(module.prefixShuffle, "Shuffle cipher prefix for the encrypted messages.");
        register(module.prefixEncrypted, "Encrypted cipher prefix for the encrypted messages.");
        register(module.autoPrefix,"Do the prefix automatically instead of manually having to do them.");
        register(module.encryptCommand,"Encrypts the message you send even throught commands or msg.");

        register(module.encryption, "The mode to use for encryption, Caesar, Compact, Shuffle or Encrypted\n" +
                "(Compact and caesar are the most reliable and Shuffle the most unstable.");
        register(module.keepEncrypted, "Keep the encrypted messages in the chat instead of removing them.");
        register(module.decryption, "Which decryption algo to use (use all if you keep this module on).");
        register(module.decryptOwn, "Decrypt the own message you encrypted ealier.");
    }

    @Override
    public int getColor() {
        return 0xff34A1FF;
    }

    @Override
    public String getDescription() {
        return "Let you send encrypted message and decode them to bypass potential server restrictions or to prevent others from understanding it.";
    }
}