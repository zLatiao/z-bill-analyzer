package com.z.bill.controller;

import com.z.bill.ParserCore;
import com.z.bill.entity.BaseBillInfo;
import com.z.bill.entity.BillExcelParseParam;
import com.z.bill.entity.QueryParam;
import com.z.bill.entity.vo.*;
import com.z.bill.parser.FileNameParser;
import com.z.bill.service.IBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

        List<BaseBillInfo> billInfos = ParserCore.parse(params);

        return Result.success(new ParseResultVO(billService.saveBill(billInfos)));
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
    public Result<List<ExpenseCategoryVO>> getExpenseCategories(@CookieValue(value = "file_session") Integer id, QueryParam param) {
        log.info("接收请求，id: {}", id);
        param.setId(id);

        return Result.success(billService.getExpenseCategoryData(param));
    }

    @GetMapping("/trends")
    public Result<TrendVO> getFinancialTrends(
            @CookieValue(value = "file_session") Integer id, QueryParam param) {
        log.info("接收请求，id: {}", id);
        param.setId(id);

        return Result.success(billService.getTrendsData(param));
    }

    @GetMapping("/page")
    public Result<PageResult<BillVO>> page(@CookieValue(value = "file_session") Integer id, QueryParam param) {
        param.setId(id);
        return Result.success(billService.getPage(param));
    }

}
