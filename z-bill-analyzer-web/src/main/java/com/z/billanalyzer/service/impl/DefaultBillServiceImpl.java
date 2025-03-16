package com.z.billanalyzer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.z.billanalyzer.domain.BaseBill;
import com.z.billanalyzer.domain.BillDetail;
import com.z.billanalyzer.domain.BillAll;
import com.z.billanalyzer.domain.QueryParam;
import com.z.billanalyzer.domain.vo.*;
import com.z.billanalyzer.domain.vo.echarts.TrendVO;
import com.z.billanalyzer.enums.AmountTypeEnum;
import com.z.billanalyzer.enums.BillSourceEnum;
import com.z.billanalyzer.service.IBillService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    public List<BillDetail> getBills(Integer id) {
        return getBill(id).billDetails();
    }

    public Stream<BillDetail> getBillStream(QueryParam param) {
        Stream<BillDetail> stream = getBill(param.getId()).billDetails().stream();
        if (param.getStartDate() != null) {
            stream = stream.filter(bill -> !bill.getTransactionTime().toLocalDate().isBefore(param.getStartDate()));
        }
        if (param.getEndDate() != null) {
            stream = stream.filter(bill -> !bill.getTransactionTime().toLocalDate().isAfter(param.getEndDate()));
        }
        if (param.getSourceList() != null && !param.getSourceList().isEmpty()) {
            stream = stream.filter(bill -> param.getSourceList().contains(bill.getSource()));
        }
        if (param.getFilterMergeType() != null && !param.getFilterMergeType().isEmpty()) {
            stream = stream.filter(bill -> !bill.isMerge() || (bill.getMergeType() != null && !param.getFilterMergeType().contains(bill.getMergeType())));
        }
        if (param.getAmountType() != null) {
            stream = stream.filter(bill -> bill.getAmountType().equals(param.getAmountType()));
        }
        if (param.getOrderType() != null && param.getOrderBy() != null && !param.getOrderBy().isBlank()) {
            Comparator<BillDetail> comparator = getComparator(param);
            if (comparator != null) {
                stream = stream.sorted(comparator);
            }
        }
        return stream;
    }

    private static Comparator<BillDetail> getComparator(QueryParam param) {
        Comparator<BillDetail> comparator = switch (param.getOrderBy()) {
            case "transactionTime" -> Comparator.comparing(BillDetail::getTransactionTime);
            case "amount" -> Comparator.comparing(BillDetail::getAmount);
            case "transactionType" -> Comparator.comparing(BillDetail::getTransactionType);
            default -> Comparator.comparing(BillDetail::getTransactionTime);
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
        List<BaseBill> baseBills = billAll.billInfos();
        List<BillDetail> billDetails = billAll.billDetails();

        List<String> fileNames = baseBills.stream().map(BaseBill::getFileName).distinct().toList();
        List<String> transactionTypes = billDetails.stream().map(BillDetail::getTransactionType).distinct().toList();
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
    public List<BaseBill> getBillInfos(Integer id) {
        return getBill(id).billInfos();
    }

    @Override
    public Integer saveBill(List<BaseBill> billInfos) {
        int id = ID_COUNTER.getAndIncrement();
        BILL_MAP.put(id, new BillAll(billInfos, billInfos.stream().flatMap(x -> x.getBillDetails().stream()).toList()));
        return id;
    }

    @Override
    public StatisticVO getStatisticData(QueryParam param) {
        List<BillDetail> incomeBillListDetail = getBillStream(param)
                .filter(bill -> AmountTypeEnum.INCOME.getType().equals(bill.getAmountType()))
                .toList();

        int incomeBillCnt = incomeBillListDetail.size();

        BigDecimal totalIncome = incomeBillListDetail.stream()
                .map(BillDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BillDetail> expenseBillListDetail = getBillStream(param)
                .filter(bill -> AmountTypeEnum.EXPENSE.getType().equals(bill.getAmountType()))
                .toList();

        int expenseBillCnt = expenseBillListDetail.size();

        BigDecimal totalExpense = expenseBillListDetail.stream()
                .map(BillDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new StatisticVO(totalIncome, incomeBillCnt, totalExpense, expenseBillCnt, balance);
    }

    @Override
    public List<ExpenseCategoryVO> getExpenseCategoryData(QueryParam param) {
        List<ExpenseCategoryVO> list = getBillStream(param)
                .filter(bill -> AmountTypeEnum.EXPENSE.getType().equals(bill.getAmountType()))
                .collect(Collectors.groupingBy(BillDetail::getTransactionType))
                .entrySet().stream()
                .map(x -> new ExpenseCategoryVO(x.getKey(), x.getValue().stream().map(BillDetail::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO)))
                .toList();
        return list;
    }

    @Override
    public TrendVO getTrendsData(QueryParam param) {
        List<BillDetail> billDetails = getBillStream(param).toList();
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

        while (!min.isAfter(max)) {
            LocalDate finalMin = min;
            List<BillDetail> currBillDetails = billDetails.stream()
                    .filter(bill -> bill.getTransactionTime().getYear() == finalMin.getYear())
                    .filter(bill -> bill.getTransactionTime().getMonthValue() == finalMin.getMonthValue())
                    .toList();

            BigDecimal income = currBillDetails.stream()
                    .filter(bill -> AmountTypeEnum.INCOME.getType().equals(bill.getAmountType()))
                    .map(BillDetail::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = currBillDetails.stream()
                    .filter(bill -> AmountTypeEnum.EXPENSE.getType().equals(bill.getAmountType()))
                    .map(BillDetail::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            timeList.add(String.format("%d-%02d", min.getYear(), min.getMonthValue()));
            incomeTrend.add(income);
            expenseTrend.add(expense);

            min = min.plusMonths(1);
        }

        return new TrendVO(timeList, incomeTrend, expenseTrend);
    }

    @Override
    public PageResult<BillDetailVO> getPage(QueryParam param) {
        Integer pageIndex = param.getPageIndex();
        Integer pageSize = param.getPageSize();

        List<BillDetail> billDetails = getBillStream(param).toList();

        int total = billDetails.size();
        int fromIndex = (pageIndex - 1) * pageSize;
        if (fromIndex >= total) {
            return new PageResult<>(emptyList(), total);
        }
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<BillDetailVO> pageData = billDetails.subList(fromIndex, toIndex).stream()
                .map(this::convertToVo)
                .toList();

        return new PageResult<>(pageData, total);
    }

    private BillDetailVO convertToVo(BillDetail billDetail) {
        BillDetailVO billVO = BeanUtil.copyProperties(billDetail, BillDetailVO.class);
        billVO.setAmountTypeStr(AmountTypeEnum.getEnum(billVO.getAmountType()).getDesc());
        billVO.setSourceStr(BillSourceEnum.getNameBy(billVO.getSource()));
        return billVO;
    }

    @Override
    public List<ExpenseSourceVO> getExpenseSources(QueryParam param) {
        return getBillStream(param)
                .filter(x -> x.getSource() != null)
                .collect(Collectors.groupingBy(BillDetail::getSource))
                .entrySet().stream()
                .map(entry -> new ExpenseSourceVO(BillSourceEnum.getNameBy(entry.getKey()), entry.getValue().stream()
                        .map(BillDetail::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();
    }
}
