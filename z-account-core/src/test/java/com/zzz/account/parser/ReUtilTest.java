package com.zzz.account.parser;

import cn.hutool.core.util.ReUtil;

import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author z-latiao
 * @since 2025/2/26 16:08
 */
public class ReUtilTest {
    public static void main(String[] args) {
        ReUtil.findAll(Pattern.compile("起始时间：\\[(.*?)] 终止时间：\\[(.*?)]"),
                "起始23时间：[2024-02-16 00:00:00] 终123止时间：[2024-05-16 21:06:54]",
                x -> {
                    System.out.println(x);
                });
    }
}
