package com.leonardobishop.quests.bukkit.util;

import java.util.regex.Pattern;

/*
 * From Apache Commons Lang
 * https://github.com/apache/commons-lang/blob/master/LICENSE.txt
 */
public class StringUtils {

    private static final Pattern ALPHANUMERIC = Pattern.compile("^[A-z0-9_-]+$");
    private static final Pattern NUMBER_REGEX = Pattern.compile("^[0-9]+$");

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    // 使用正则匹配速度会更快
    public static boolean isAlphanumeric(final CharSequence cs) {
        return ALPHANUMERIC.matcher(cs).matches();
    }

    public static boolean isNumeric(final CharSequence cs) {
        return NUMBER_REGEX.matcher(cs).matches();
    }

}
