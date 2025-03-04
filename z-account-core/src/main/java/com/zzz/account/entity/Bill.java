package com.zzz.account.entity;

import com.zzz.account.enums.BankEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 账单
 * </p>
 *
 * @author zzz
 * @since 2024-05-23
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class Bill implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
//    private Integer id;

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
     * 商品
     */
    private String product;

    /**
     * 支付方式
     */
    private String paymentMode;

    /**
     * 交易状态
     */
    private String transactionStatus;

    /**
     * 账单单号
     */
    private String billNo;

    /**
     * 商家单号
     */
    private String merchantNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 账单导入记录ID
     */
//    private Integer importRecordId;

    /**
     * todo 名称暂定
     * 银行卡号后4位，微信和支付宝表示用银行卡支付的卡号，银行表示自身的卡号
     */
    private String bankAccountLast4Number;

    /**
     * todo 名称暂定
     * 银行名称
     */
    private BankEnum bank;

    private List<Bill> similarBills;

    private Boolean isMerge = false;

    private Boolean noMatchRecord;

    /**
     * 是否参与统计
     * todo
     */
    private boolean isStatistic;
}
