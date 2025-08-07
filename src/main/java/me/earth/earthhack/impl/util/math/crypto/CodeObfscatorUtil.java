package me.earth.earthhack.impl.util.math.crypto;

public class CodeObfscatorUtil {

    /**
     * Applies a reversible bitwise transformation to obfuscate a message.
     * This method is not a traditional cipher: it works on any character (letters, symbols, Unicode),
     * preserves the original message length, and does not rely on keys or substitutions.
     *
     * @param message The original string to encode
     * @return The obfuscated version of the input string
     */
    public static String obfuscateCompact(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        StringBuilder obfuscated = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            char transformed = (char) (~c ^ i);
            obfuscated.append(transformed);
        }

        return obfuscated.toString();
    }

    /**
     * Reverses the transformation applied by obfuscateCompact,
     * restoring the original message exactly.
     *
     * @param obfuscated The transformed string to decode
     * @return The original string before obfuscation
     */
    public static String deobfuscateCompact(String obfuscated) {
        if (obfuscated == null || obfuscated.isEmpty()) {
            return "";
        }

        StringBuilder original = new StringBuilder();

        for (int i = 0; i < obfuscated.length(); i++) {
            char c = obfuscated.charAt(i);
            char restored = (char) (~(c ^ i));
            original.append(restored);
        }

        return original.toString();
    }
}
