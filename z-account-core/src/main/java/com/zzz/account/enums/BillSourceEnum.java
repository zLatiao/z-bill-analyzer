package com.zzz.account.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 账单来源枚举
 *
 * @author zzz
 * @since 2024/5/23 22:01
 */
@Getter
@AllArgsConstructor
public enum BillSourceEnum {
    WX("微信"),
    ALIPAY("支付宝"),
    CMB("招商银行");

    private final String name;

    public static BillSourceEnum getBy(Integer source) {
        return Arrays.stream(values()).filter(x -> Integer.valueOf(x.ordinal()).equals(source)).findFirst().orElse(null);
    }

    public static String getNameBy(Integer source) {
        return Arrays.stream(values()).filter(x -> Integer.valueOf(x.ordinal()).equals(source)).map(BillSourceEnum::getName).findFirst().orElse(null);
    }
}
