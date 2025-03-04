package com.zzz.account.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 金额类型 枚举
 *
 * @author zzz
 * @since 2024/5/23 22:08
 */
@Getter
@AllArgsConstructor
public enum AmountTypeEnum {
    UNKNOWN(-1, "未知"),
    INCOME(0, "收入"),
    EXPENSE(1, "支出"),
    NON_IN_EX(2, "不计收支")
    ;

    private final Integer type;
    private final String desc;

    public static AmountTypeEnum getEnum(String desc) {
        return Arrays.stream(values()).filter(x->x.getDesc().equals(desc)).findFirst().orElse(UNKNOWN);
    }

    public static AmountTypeEnum getEnum(Integer type) {
        return Arrays.stream(values()).filter(x->x.getType().equals(type)).findFirst().orElse(UNKNOWN);
    }
}
