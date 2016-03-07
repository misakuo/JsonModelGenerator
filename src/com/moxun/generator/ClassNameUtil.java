package com.moxun.generator;

/**
 * Created by moxun on 16/3/7.
 */
public class ClassNameUtil {
    public static String getName(String name) {
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (!Character.isLetter(c)) {
                chars[i] = '_';
                if (i + 1 < chars.length) {
                    chars[i + 1] = Character.toUpperCase(chars[i + 1]);
                }
            }
        }

        return String.valueOf(chars).replaceAll("_","");
    }


}
