package com.zzz.account.controller;

import com.zzz.account.ParserCore;
import com.zzz.account.entity.BaseBillInfo;
import com.zzz.account.entity.BillExcelParseParam;
import com.zzz.account.entity.QueryParam;
import com.zzz.account.entity.vo.*;
import com.zzz.account.parser.FileNameParser;
import com.zzz.account.service.IBillService;
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
    public ParseResultVO parse(@RequestParam("files") List<MultipartFile> files) {
        List<BillExcelParseParam> params = files.stream().map(file -> {
            try {
                return new BillExcelParseParam(FileNameParser.parse(file.getOriginalFilename()), file.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        List<BaseBillInfo> billInfos = ParserCore.parse(params);

        return new ParseResultVO(billService.saveBill(billInfos));
    }

    // 模拟仪表盘数据
    @GetMapping("/dashboard")
    public Result<StatisticVO> getStatisticData(@CookieValue(value = "file_session") Integer id, QueryParam param) {
        log.info("接收请求，id: {}", id);
        param.setId(id);
        return Result.success(billService.getStatisticData(param));
    }

    // 模拟支出分类数据
    @GetMapping("/categories")
    public Result<List<ExpenseCategoryVO>> getExpenseCategories(@CookieValue(value = "file_session") Integer id, QueryParam param) {
        log.info("接收请求，id: {}", id);
        param.setId(id);

        return Result.success(billService.getExpenseCategoryData(param));
    }

    // 模拟趋势数据（支持动态区间）
    @GetMapping("/trends")
    public Result<TrendVO> getFinancialTrends(
            @CookieValue(value = "file_session") Integer id, QueryParam param) {
        log.info("接收请求，id: {}", id);
        param.setId(id);

        return Result.success(billService.getTrendsData(param));
    }

    // 模拟分页交易数据
    @GetMapping("/page")
    public Result<PageResult<BillVO>> page(
            @CookieValue(value = "file_session") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            QueryParam param) {
        param.setId(id);

        return Result.success(billService.getPage(param));
    }

}
