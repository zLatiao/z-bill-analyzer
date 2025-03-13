package com.z.billanalyzer.constant;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * @author z-latiao
 * @since 2025/2/21 11:18
 */
public class GlobalConstant {

    public static final Pattern CMB_PATTERN = Pattern.compile("招商银行储蓄卡\\((\\d+)\\)");
    public static final Pattern BOC_PATTERN = Pattern.compile("中国银行储蓄卡\\((\\d+)\\)");



    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

}
