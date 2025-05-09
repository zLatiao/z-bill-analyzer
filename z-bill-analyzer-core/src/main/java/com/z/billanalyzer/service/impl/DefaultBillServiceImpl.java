package com.z.billanalyzer.service.impl;

import com.z.billanalyzer.ParserCore;
import com.z.billanalyzer.domain.PageResult;
import com.z.billanalyzer.domain.bill.BaseBill;
import com.z.billanalyzer.domain.bill.BaseBillDetail;
import com.z.billanalyzer.domain.bill.BillAll;
import com.z.billanalyzer.domain.param.QueryParam;
import com.z.billanalyzer.domain.parse.BillExcelParseParam;
import com.z.billanalyzer.domain.vo.*;
import com.z.billanalyzer.enums.AmountTypeEnum;
import com.z.billanalyzer.enums.BillSourceEnum;
import com.z.billanalyzer.enums.TimeUnitEnum;
import com.z.billanalyzer.service.IBillService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * 默认实现类
 * 使用内存实现，没有持久化
 */
public class DefaultBillServiceImpl implements IBillService {
    private final Map<Integer, BillAll> BILL_MAP = new ConcurrentHashMap<>();

    private final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private BillAll getBill(Integer id) {
        return BILL_MAP.get(id);
    }

    @Override
    public ParseResultVO parse(List<BillExcelParseParam> params) {
        List<BaseBill<?>> bills = ParserCore.parse(params);
        return new ParseResultVO(saveBills(bills));
    }

    @Override
    public List<? extends BaseBillDetail> getBills(Integer id) {
        return getBill(id).billDetails();
    }

    public Stream<? extends BaseBillDetail> getBillStream(QueryParam param) {
        Stream<? extends BaseBillDetail> stream = getBill(param.getId()).billDetails().stream();
        if (param.getStartDate() != null) {
            stream = stream.filter(bill -> !bill.getTransactionTime().toLocalDate().isBefore(param.getStartDate()));
        }
        if (param.getEndDate() != null) {
            stream = stream.filter(bill -> !bill.getTransactionTime().toLocalDate().isAfter(param.getEndDate()));
        }
        if (param.getSourceList() != null && !param.getSourceList().isEmpty()) {
            stream = stream.filter(bill -> param.getSourceList().contains(bill.getSource()));
        }
        if (param.getFilterSourceList() != null && !param.getFilterSourceList().isEmpty()) {
            stream = stream.filter(bill -> !param.getFilterSourceList().contains(bill.getSource()));
        }
        if (param.getDuplicateTypeList() != null && !param.getDuplicateTypeList().isEmpty()) {
            stream = stream.filter(bill -> !bill.isMerge() || (bill.getMergeType() != null && !param.getDuplicateTypeList().contains(bill.getMergeType())));
        }
        if (param.getAmountType() != null) {
            stream = stream.filter(bill -> bill.getAmountType().equals(param.getAmountType()));
        }
        if (param.getOrderType() != null && param.getOrderBy() != null && !param.getOrderBy().isBlank()) {
            Comparator<BaseBillDetail> comparator = getComparator(param);
            if (comparator != null) {
                stream = stream.sorted(comparator);
            }
        }
        return stream;
    }

    private static Comparator<BaseBillDetail> getComparator(QueryParam param) {
        Comparator<BaseBillDetail> comparator = switch (param.getOrderBy()) {
            case "transactionTime" -> Comparator.comparing(BaseBillDetail::getTransactionTime);
            case "amount" -> Comparator.comparing(BaseBillDetail::getAmount);
            case "transactionType" -> Comparator.comparing(BaseBillDetail::getTransactionType);
            default -> Comparator.comparing(BaseBillDetail::getTransactionTime);
        };
        if ("desc".equals(param.getOrderType())) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    public void getBillInfo(Integer id) {

    }

    @Override
    public ImportBillInfoVO getImportBillInfo(Integer id) {
        BillAll billAll = getBill(id);
        List<BaseBill<?>> baseBills = billAll.bills();
        List<? extends BaseBillDetail> billDetails = billAll.billDetails();

        List<String> fileNames = baseBills.stream().map(BaseBill::getFileName).distinct().toList();
        List<String> transactionTypes = billDetails.stream().map(BaseBillDetail::getTransactionType).distinct().toList();
        // todo 这里现在取得是账单信息的起止时间，而不是账单明细的起止时间
        LocalDate startDate = baseBills.stream()
                .map(BaseBill::getStartTime)
                .map(LocalDateTime::toLocalDate)
                .min(Comparator.comparing(x -> x))
                .orElse(null);
        LocalDate endDate = baseBills.stream()
                .map(BaseBill::getEndTime)
                .map(LocalDateTime::toLocalDate)
                .max(Comparator.comparing(x -> x))
                .orElse(null);

        return new ImportBillInfoVO(fileNames, transactionTypes, startDate, endDate);
    }

    @Override
    public List<BaseBill<?>> getBillInfos(Integer id) {
        return getBill(id).bills();
    }

    @Override
    public Integer saveBills(List<BaseBill<?>> bills) {
        int id = ID_COUNTER.getAndIncrement();
        BILL_MAP.put(id, new BillAll(bills, bills.stream().flatMap(x -> x.getBillDetails().stream()).toList()));
        return id;
    }

    @Override
    public void delete(Integer id) {
        if (id == null) {
            return;
        }
        BILL_MAP.remove(id);
    }

    @Override
    public StatisticVO getStatisticData(QueryParam param) {
        List<? extends BaseBillDetail> incomeBillListDetail = getBillStream(param)
                .filter(bill -> AmountTypeEnum.INCOME.getType().equals(bill.getAmountType()))
                .toList();

        int incomeBillCnt = incomeBillListDetail.size();

        BigDecimal totalIncome = incomeBillListDetail.stream()
                .map(BaseBillDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<? extends BaseBillDetail> expenseBillListDetail = getBillStream(param)
                .filter(bill -> AmountTypeEnum.EXPENSE.getType().equals(bill.getAmountType()))
                .toList();

        int expenseBillCnt = expenseBillListDetail.size();

        BigDecimal totalExpense = expenseBillListDetail.stream()
                .map(BaseBillDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new StatisticVO(totalIncome, incomeBillCnt, totalExpense, expenseBillCnt, balance);
    }

    @Override
    public List<ExpenseCategoryVO> getExpenseCategoryData(QueryParam param) {
        List<ExpenseCategoryVO> list = getBillStream(param)
                .filter(bill -> AmountTypeEnum.EXPENSE.getType().equals(bill.getAmountType()))
                .collect(Collectors.groupingBy(BaseBillDetail::getTransactionType))
                .entrySet().stream()
                .map(x -> new ExpenseCategoryVO(x.getKey(), x.getValue().stream().map(BaseBillDetail::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO)))
                .toList();
        return list;
    }

    @Override
    public TrendVO getTrendsData(QueryParam param) {
        List<? extends BaseBillDetail> billDetails = getBillStream(param).toList();
        if (billDetails.isEmpty()) {
            return new TrendVO(emptyList(), emptyList(), emptyList());
        }

        LocalDate min = billDetails.stream()
                .map(x -> x.getTransactionTime().toLocalDate().withDayOfMonth(1))
                .min(Comparator.comparing(x -> x))
                .orElseThrow(() -> new RuntimeException("没有min"));

        LocalDate max = billDetails.stream()
                .map(x -> x.getTransactionTime().toLocalDate().withDayOfMonth(1))
                .max(Comparator.comparing(x -> x))
                .orElse(null);

        List<String> timeList = new ArrayList<>();
        List<BigDecimal> incomeTrend = new ArrayList<>();
        List<BigDecimal> expenseTrend = new ArrayList<>();

        TimeUnitEnum timeUnitEnum = TimeUnitEnum.getBy(param.getTimeUnit());
        // 默认月度
        if (timeUnitEnum == null) {
            timeUnitEnum = TimeUnitEnum.MONTH;
        }

        while (!min.isAfter(max)) {
            LocalDate finalMin = min;

            Predicate<BaseBillDetail> predicate = null;
            switch (timeUnitEnum) {
                case DAY -> predicate = bill -> bill.getTransactionTime().toLocalDate().equals(finalMin);
                case MONTH -> predicate = bill -> bill.getTransactionTime().getYear() == finalMin.getYear() && bill.getTransactionTime().getMonthValue() == finalMin.getMonthValue();
                case QUARTER -> {
                }
                case YEAR -> predicate = bill -> bill.getTransactionTime().getYear() == finalMin.getYear();
            }

            List<? extends BaseBillDetail> currBillDetails = billDetails.stream().filter(predicate).toList();

            BigDecimal income = currBillDetails.stream()
                    .filter(bill -> AmountTypeEnum.INCOME.getType().equals(bill.getAmountType()))
                    .map(BaseBillDetail::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = currBillDetails.stream()
                    .filter(bill -> AmountTypeEnum.EXPENSE.getType().equals(bill.getAmountType()))
                    .map(BaseBillDetail::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String time = null;
            switch (timeUnitEnum) {
                case DAY -> time = finalMin.toString();
                case MONTH -> time = String.format("%d-%02d", min.getYear(), min.getMonthValue());
                case QUARTER -> {
                }
                case YEAR -> time = String.valueOf(finalMin.getYear());
            }

            timeList.add(time);
            incomeTrend.add(income);
            expenseTrend.add(expense);

            switch (timeUnitEnum) {
                case DAY -> min = min.plusDays(1);
                case MONTH -> min = min.plusMonths(1);
                case QUARTER -> min = min.plusMonths(3);
                case YEAR -> min = min.plusYears(1);
            }
        }

        return new TrendVO(timeList, incomeTrend, expenseTrend);
    }

    @Override
    public PageResult<? extends BaseBillDetail> getPage(QueryParam param) {
        Integer pageIndex = param.getPageIndex();
        Integer pageSize = param.getPageSize();

        List<? extends BaseBillDetail> billDetails = getBillStream(param).toList();

        int total = billDetails.size();
        int fromIndex = (pageIndex - 1) * pageSize;
        if (fromIndex >= total) {
            return new PageResult<>(emptyList(), total);
        }
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<? extends BaseBillDetail> pageData = billDetails.subList(fromIndex, toIndex).stream().toList();

        return new PageResult<>(pageData, total);
    }

    @Override
    public List<ExpenseSourceVO> getExpenseSources(QueryParam param) {
        return getBillStream(param)
                .filter(x -> x.getSource() != null)
                .collect(Collectors.groupingBy(BaseBillDetail::getSource))
                .entrySet().stream()
                .map(entry -> new ExpenseSourceVO(BillSourceEnum.getNameBy(entry.getKey()), entry.getValue().stream()
                        .map(BaseBillDetail::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();
    }
}
