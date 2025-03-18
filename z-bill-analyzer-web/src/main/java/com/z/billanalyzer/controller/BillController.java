package com.z.billanalyzer.controller;

import cn.hutool.core.bean.BeanUtil;
import com.z.billanalyzer.ParserCore;
import com.z.billanalyzer.domain.PageResult;
import com.z.billanalyzer.domain.bill.BaseBill;
import com.z.billanalyzer.domain.bill.BaseBillDetail;
import com.z.billanalyzer.domain.parse.BillExcelParseParam;
import com.z.billanalyzer.domain.param.QueryParam;
import com.z.billanalyzer.domain.vo.*;
import com.z.billanalyzer.domain.vo.echarts.PieDataVO;
import com.z.billanalyzer.domain.vo.TrendVO;
import com.z.billanalyzer.enums.AmountTypeEnum;
import com.z.billanalyzer.enums.BillSourceEnum;
import com.z.billanalyzer.parser.FileNameParser;
import com.z.billanalyzer.service.IBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/bill")
@Slf4j
public class BillController {

    @Autowired
    private IBillService billService;

    @PostMapping("/parse")
    public Result<ParseResultVO> parse(@RequestParam("files") List<MultipartFile> files) {
        List<BillExcelParseParam> params = files.stream().map(file -> {
            try {
                String filename = file.getOriginalFilename();
                return new BillExcelParseParam(FileNameParser.parse(filename), filename, file.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        return Result.success(billService.parse(params));
    }

    @GetMapping("/getImportBillInfo")
    public Result<ImportBillInfoVO> getImportBillInfo(@CookieValue(value = "file_session") Integer id) {
        log.info("接收请求，id: {}", id);
        return Result.success(billService.getImportBillInfo(id));
    }

    @GetMapping("/dashboard")
    public Result<StatisticVO> getStatisticData(@CookieValue(value = "file_session") Integer id, QueryParam param) {
        log.info("接收请求，id: {}", id);
        param.setId(id);
        return Result.success(billService.getStatisticData(param));
    }

    @GetMapping("/categories")
    public Result<List<PieDataVO>> getExpenseCategories(@CookieValue(value = "file_session") Integer id, QueryParam param) {
        log.info("接收请求，id: {}", id);
        param.setId(id);
        return Result.success(billService.getExpenseCategoryData(param).stream().map(BillController::convertToPieData).toList());
    }


    private static PieDataVO convertToPieData(ExpenseCategoryVO x) {
        return new PieDataVO(x.name(), x.value());
    }

    @GetMapping("/trends")
    public Result<TrendVO> getFinancialTrends(
            @CookieValue(value = "file_session") Integer id, QueryParam param) {
        log.info("接收请求，id: {}", id);
        param.setId(id);

        return Result.success(billService.getTrendsData(param));
    }

    @GetMapping("/page")
    public Result<PageResult<BillDetailVO>> page(@CookieValue(value = "file_session") Integer id, QueryParam param) {
        param.setId(id);
        PageResult<? extends BaseBillDetail> page = billService.getPage(param);
        List<? extends BaseBillDetail> list = page.getList();
        if (list == null) {
            return Result.success(new PageResult<>(Collections.emptyList(), page.getTotal()));
        }
        List<BillDetailVO> list1 = list.stream().map(this::convertToVo).toList();
        return Result.success(new PageResult<>(list1, page.getTotal()));
    }

    private BillDetailVO convertToVo(BaseBillDetail billDetail) {
        BillDetailVO billVO = BeanUtil.copyProperties(billDetail, BillDetailVO.class);
        billVO.setAmountTypeStr(AmountTypeEnum.getEnum(billVO.getAmountType()).getDesc());
        billVO.setSourceStr(BillSourceEnum.getNameBy(billVO.getSource()));
        return billVO;
    }

    /**
     * 支出来源饼图
     *
     * @param id
     * @param param
     * @return
     */
    @GetMapping("/sources")
    public Result<List<PieDataVO>> getExpenseSources(@CookieValue(value = "file_session") Integer id, QueryParam param) {
        param.setId(id);
        param.setAmountType(AmountTypeEnum.EXPENSE.getType());
        return Result.success(billService.getExpenseSources(param).stream().map(BillController::convertToPieData).toList());
    }

    private static PieDataVO convertToPieData(ExpenseSourceVO x) {
        return new PieDataVO(x.name(), x.value());
    }
}
