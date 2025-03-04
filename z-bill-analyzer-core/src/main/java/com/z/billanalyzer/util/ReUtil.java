package com.z.billanalyzer.util;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author z-latiao
 * @since 2025/2/26 16:14
 */
public class ReUtil {
    public static boolean findAll(Pattern pattern, CharSequence content, Consumer<Matcher> consumer) {
        if (null == pattern || null == content) {
            return false;
        }

        final Matcher matcher = pattern.matcher(content);
        boolean find = false;
        while (matcher.find()) {
            consumer.accept(matcher);
            find = true;
        }
        return find;
    }
}
