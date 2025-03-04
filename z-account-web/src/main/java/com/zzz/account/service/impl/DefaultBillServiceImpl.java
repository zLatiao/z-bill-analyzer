package com.zzz.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.zzz.account.entity.BaseBillInfo;
import com.zzz.account.entity.Bill;
import com.zzz.account.entity.BillAll;
import com.zzz.account.entity.QueryParam;
import com.zzz.account.entity.vo.*;
import com.zzz.account.enums.AmountTypeEnum;
import com.zzz.account.enums.BillSourceEnum;
import com.zzz.account.service.IBillService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * 默认实现类
 * 使用内存实现，没有持久化
 */
@Service
@ConditionalOnProperty(name = "bill-service", havingValue = "default", matchIfMissing = true)
public class DefaultBillServiceImpl implements IBillService {
    private final Map<Integer, BillAll> BILL_MAP = new ConcurrentHashMap<>();

    private final AtomicInteger ID_COUNTER = new AtomicInteger(1);

    private BillAll getBill(Integer id) {
        return BILL_MAP.get(id);
    }

    @Override
    public List<Bill> getBills(Integer id) {
        return getBill(id).bills();
    }

    public Stream<Bill> getBillStream(QueryParam param) {
        Stream<Bill> stream = getBill(param.getId()).bills().stream();
        if (param.getStartDate() != null) {
            stream = stream.filter(bill -> !bill.getTransactionTime().toLocalDate().isBefore(param.getStartDate()));
        }
        if (param.getEndDate() != null) {
            stream = stream.filter(bill -> !bill.getTransactionTime().toLocalDate().isAfter(param.getEndDate()));
        }
        if (param.getSourceList() != null && !param.getSourceList().isEmpty()) {
            stream = stream.filter(bill -> param.getSourceList().contains(bill.getSource()));
        }
        return stream;
    }

    @Override
    public List<BaseBillInfo> getBillInfos(Integer id) {
        return getBill(id).billInfos();
    }

    @Override
    public Integer saveBill(List<BaseBillInfo> billInfos) {
        int id = ID_COUNTER.getAndIncrement();
        BILL_MAP.put(id, new BillAll(billInfos, billInfos.stream().flatMap(x -> x.getBills().stream()).toList()));
        return id;
    }

    @Override
    public StatisticVO getStatisticData(QueryParam param) {
        List<Bill> incomeBillList = getBillStream(param)
                .filter(bill -> AmountTypeEnum.INCOME.getType().equals(bill.getAmountType()))
                .toList();

        int incomeBillCnt = incomeBillList.size();

        BigDecimal totalIncome = incomeBillList.stream()
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Bill> expenseBillList = getBillStream(param)
                .filter(bill -> AmountTypeEnum.EXPENSE.getType().equals(bill.getAmountType()))
                .toList();

        int expenseBillCnt = expenseBillList.size();

        BigDecimal totalExpense = expenseBillList.stream()
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new StatisticVO(totalIncome, incomeBillCnt, totalExpense, expenseBillCnt, balance);
    }

    @Override
    public List<ExpenseCategoryVO> getExpenseCategoryData(QueryParam param) {
        List<ExpenseCategoryVO> list = getBillStream(param)
                .filter(bill -> AmountTypeEnum.EXPENSE.getType().equals(bill.getAmountType()))
                .collect(Collectors.groupingBy(Bill::getTransactionType))
                .entrySet().stream()
                .map(x -> new ExpenseCategoryVO(x.getKey(), x.getValue().stream().map(Bill::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO)))
                .toList();
        return list;
    }

    @Override
    public TrendVO getTrendsData(QueryParam param) {
        List<Bill> bills = getBillStream(param).toList();
        if (bills.isEmpty()) {
            return new TrendVO(emptyList(), emptyList(), emptyList());
        }

        LocalDate min = bills.stream()
                .map(x -> x.getTransactionTime().toLocalDate().withDayOfMonth(1))
                .min(Comparator.comparing(x -> x))
                .orElseThrow(() -> new RuntimeException("没有min"));

        LocalDate max = bills.stream()
                .map(x -> x.getTransactionTime().toLocalDate().withDayOfMonth(1))
                .max(Comparator.comparing(x -> x))
                .orElse(null);

        List<String> timeList = new ArrayList<>();
        List<BigDecimal> incomeTrend = new ArrayList<>();
        List<BigDecimal> expenseTrend = new ArrayList<>();

        while (!min.isAfter(max)) {
            LocalDate finalMin = min;
            List<Bill> currBills = bills.stream()
                    .filter(bill -> bill.getTransactionTime().getYear() == finalMin.getYear())
                    .filter(bill -> bill.getTransactionTime().getMonthValue() == finalMin.getMonthValue())
                    .toList();

            BigDecimal income = currBills.stream()
                    .filter(bill -> AmountTypeEnum.INCOME.getType().equals(bill.getAmountType()))
                    .map(Bill::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = currBills.stream()
                    .filter(bill -> AmountTypeEnum.EXPENSE.getType().equals(bill.getAmountType()))
                    .map(Bill::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            timeList.add(String.format("%d-%02d", min.getYear(), min.getMonthValue()));
            incomeTrend.add(income);
            expenseTrend.add(expense);

            min = min.plusMonths(1);
        }

        return new TrendVO(timeList, incomeTrend, expenseTrend);
    }

    @Override
    public PageResult<BillVO> getPage(QueryParam param) {
        Integer pageIndex = param.getPageIndex();
        Integer pageSize = param.getPageSize();

        List<Bill> bills = getBillStream(param).toList();

        int total = bills.size();
        int fromIndex = (pageIndex - 1) * pageSize;
        if (fromIndex >= total) {
            return new PageResult<>(emptyList(), total);
        }
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<BillVO> pageData = bills.subList(fromIndex, toIndex).stream()
                .map(this::convertToVo)
                .toList();

        return new PageResult<>(pageData, total);
    }
    
    private BillVO convertToVo(Bill bill) {
        BillVO billVO = BeanUtil.copyProperties(bill, BillVO.class);
        billVO.setAmountTypeStr(AmountTypeEnum.getEnum(billVO.getAmountType()).getDesc());
        billVO.setSourceStr(BillSourceEnum.getNameBy(billVO.getSource()));
        return billVO;
    }
}
