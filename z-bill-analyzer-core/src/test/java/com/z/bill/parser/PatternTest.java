package com.z.bill.parser;

import com.z.bill.constant.GlobalConstant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

/**
 * @author z-latiao
 * @since 2025/2/21 11:37
 */
public class PatternTest {
    @Test
     void test_CMB_PATTERN() {
        String str = "招商银行储蓄卡(1234)";
        Matcher matcher = GlobalConstant.CMB_PATTERN.matcher(str);
        Assertions.assertTrue(matcher.find());
        Assertions.assertEquals("1234", matcher.group(1));
    }

    @Test
     void test_BOC_PATTERN() {
        String str = "中国银行储蓄卡(1234)";
        Matcher matcher = GlobalConstant.BOC_PATTERN.matcher(str);
        Assertions.assertTrue(matcher.find());
        Assertions.assertEquals("1234", matcher.group(1));
    }
}
