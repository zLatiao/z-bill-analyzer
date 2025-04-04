package com.z.billanalyzer.enums;

import lombok.Getter;

/**
 * 账单明细重复类型枚举
 *
 * @author zzz
 * @since 2025/3/16 20:57
 */
@Getter
public enum DuplicateTypeEnum {
    SAME_SOURCE(0, "相同来源"),
    BANK(1, "银行");

    private final int type;
    private final String desc;

    DuplicateTypeEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
