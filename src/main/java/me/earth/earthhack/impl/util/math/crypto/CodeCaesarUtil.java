package me.earth.earthhack.impl.util.math.crypto;

public class CodeCaesarUtil {

    public static String encryptCaesar(String input, int CaesarShift) {
        if (input == null) return "";

        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 'a' && c <= 'z') {
                chars[i] = (char) ('a' + (c - 'a' + CaesarShift) % 26);
            } else if (c >= 'A' && c <= 'Z') {
                chars[i] = (char) ('A' + (c - 'A' + CaesarShift) % 26);
            }
        }
        return new String(chars);
    }
    public static String decryptCaesar(String input, int CaesarShift) {
        if (input == null) return "";

        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 'a' && c <= 'z') {
                chars[i] = (char) ('a' + (c - 'a' - CaesarShift + 26) % 26);
            } else if (c >= 'A' && c <= 'Z') {
                chars[i] = (char) ('A' + (c - 'A' - CaesarShift + 26) % 26);
            }
        }
        return new String(chars);
    }
}
