package com.z.billanalyzer.service.impl;

import com.z.billanalyzer.entity.BaseBillInfo;
import com.z.billanalyzer.entity.Bill;
import com.z.billanalyzer.entity.QueryParam;
import com.z.billanalyzer.entity.vo.echarts.TrendVO;
import com.z.billanalyzer.service.IBillService;
import com.z.billanalyzer.entity.vo.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 模拟数据实现类
 */
@Service
@ConditionalOnProperty(name = "bill-service", havingValue = "mock")
public class MockBillServiceImpl implements IBillService {
    @Override
    public List<Bill> getBills(Integer id) {
        return Collections.emptyList();
    }

    @Override
    public List<BaseBillInfo> getBillInfos(Integer id) {
        return Collections.emptyList();
    }

    @Override
    public ImportBillInfoVO getImportBillInfo(Integer id) {
        return null;
    }

    @Override
    public Integer saveBill(List<BaseBillInfo> billInfos) {
        return 0;
    }

    @Override
    public StatisticVO getStatisticData(QueryParam param) {
        return new StatisticVO(new BigDecimal("18500.00"), 10, new BigDecimal("12340.00"), 10, new BigDecimal("6160.00"));
    }

    @Override
    public List<ExpenseCategoryVO> getExpenseCategoryData(QueryParam param) {
        List<ExpenseCategoryVO> list = Arrays.asList(
                new ExpenseCategoryVO("餐饮", 4200),
                new ExpenseCategoryVO("住房", 3800),
                new ExpenseCategoryVO("交通", 1500),
                new ExpenseCategoryVO("娱乐", 1200),
                new ExpenseCategoryVO("其他", 1640)
        );
        return list;
    }

    @Override
    public TrendVO getTrendsData(QueryParam param) {
        List<String> timeList = Arrays.asList("2023-Q1", "2023-Q2", "2023-Q3", "2023-Q4",
                "2024-Q1", "2024-Q2");
        List<BigDecimal> income = Arrays.asList(
                new BigDecimal("16500"), new BigDecimal("17200"),
                new BigDecimal("18500"), new BigDecimal("17800"),
                new BigDecimal("19000"), new BigDecimal("19500")
        );
        List<BigDecimal> expense = Arrays.asList(
                new BigDecimal("14200"), new BigDecimal("13500"),
                new BigDecimal("12340"), new BigDecimal("12800"),
                new BigDecimal("13200"), new BigDecimal("12900")
        );

        return new TrendVO(timeList, income, expense);
    }

    @Override
    public PageResult<BillVO> getPage(QueryParam param) {
        // 生成50条测试数据
        List<BillVO> allData = new ArrayList<>();
        LocalDateTime baseDate = LocalDateTime.of(LocalDate.of(2025, 1, 1), LocalTime.MIN);
        for (int i = 0; i < 50; i++) {
            BillVO t = new BillVO();
            t.setAmount(new BigDecimal(80 + i * 10));
            t.setAmountTypeStr(i % 2 == 0 ? "收入" : "支出");
            t.setSourceStr(i % 3 == 0 ? "支付宝" : (i % 3 == 1 ? "微信" : "银行卡"));
            allData.add(t);
        }

        // 分页逻辑
        int total = allData.size();

        return new PageResult<>(allData, total);
    }

    @Override
    public List<ExpenseSourceVO> getExpenseSources(QueryParam param) {
        return null;
    }
}
