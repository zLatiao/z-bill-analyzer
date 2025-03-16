package com.z.billanalyzer.domain;

import com.z.billanalyzer.enums.BankEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单明细基类
 *
 * @author zzz
 * @since 2025/3/16 19:09
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class BaseBillDetail {
    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 金额类型:0.收入,1.支出,2.不计收支
     */
    private Integer amountType;

    /**
     * 交易类型
     */
    private String transactionType;

    /**
     * 来源:0.微信,1.支付宝,2.招商银行
     */
    private Integer source;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

    /**
     * 交易对方
     */
    private String counterparty;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否合并了
     */
    private boolean isMerge = false;

    /**
     * 0同类相同，1银行卡
     */
    private Integer mergeType;

    /**
     * todo 名称暂定
     * 银行卡号后4位，微信和支付宝表示用银行卡支付的卡号，银行表示自身的卡号
     */
    private String bankAccountLast4Number;
}
