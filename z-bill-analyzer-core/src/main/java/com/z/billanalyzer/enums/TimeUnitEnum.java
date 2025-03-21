package com.z.billanalyzer.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TimeUnitEnum {
    DAY(0, "日度"),
    MONTH(1, "月度"),
    QUARTER(2, "季度"),
    YEAR(3, "年度");

    private final Integer type;
    private final String desc;

    TimeUnitEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static TimeUnitEnum getBy(Integer type) {
        return Arrays.stream(values()).filter(x -> x.getType().equals(type)).findFirst().orElse(null);
    }
}
