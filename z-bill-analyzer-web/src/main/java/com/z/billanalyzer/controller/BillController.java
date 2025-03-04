package com.z.billanalyzer.controller;

import com.z.billanalyzer.domain.PageResult;
import com.z.billanalyzer.domain.bill.BaseBillDetail;
import com.z.billanalyzer.domain.importrecord.ParseRecord;
import com.z.billanalyzer.domain.param.QueryParam;
import com.z.billanalyzer.domain.parse.BillExcelParseParam;
import com.z.billanalyzer.domain.vo.*;
import com.z.billanalyzer.domain.vo.echarts.PieDataVO;
import com.z.billanalyzer.enums.AmountTypeEnum;
import com.z.billanalyzer.enums.BillSourceEnum;
import com.z.billanalyzer.parser.FileNameParser;
import com.z.billanalyzer.service.IBillService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bill")
@Slf4j
public class BillController {


    @Autowired
    private IBillService billService;

    /**
     * 获取导入记录
     *
     * @param session
     * @return
     */
    @GetMapping("/getImportRecords")
    public Result<List<ParseRecord>> getImportRecords(HttpSession session) {
        return Result.success((List<ParseRecord>) session.getAttribute("IMPORT_RECORDS"));
    }

    /**
     * 解析账单
     *
     * @param files
     * @param session
     * @return
     */
    @PostMapping("/parse")
    public Result<ParseResultVO> parse(@RequestParam("files") List<MultipartFile> files, HttpSession session) {
        List<BillExcelParseParam> params = files.stream().map(file -> {
            try {
                String filename = file.getOriginalFilename();
                return new BillExcelParseParam(FileNameParser.parse(filename), filename, file.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        ParseResultVO parse = billService.parse(params);

        // 放到session里去
        Object importRecords = session.getAttribute("IMPORT_RECORDS");
        if (importRecords == null) {
            importRecords = new ArrayList<>();
            session.setAttribute("IMPORT_RECORDS", importRecords);
        }
        ((List<ParseRecord>) importRecords).add(new ParseRecord(parse.id(), LocalDateTime.now(), files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.joining(";"))));

        return Result.success(parse);
    }

    /**
     * 获取导入账单信息
     *
     * @param importRecordId
     * @return
     */
    @GetMapping("/getImportBillInfo")
    public Result<ImportBillInfoVO> getImportBillInfo(@CookieValue(value = "IMPORT_RECORD_ID", required = false) Integer importRecordId) {
        verifyPermission(importRecordId);
        return Result.success(billService.getImportBillInfo(importRecordId));
    }

    /**
     * 获取看板数据
     *
     * @param importRecordId
     * @param param
     * @return
     */
    @GetMapping("/dashboard")
    public Result<StatisticVO> getStatisticData(@CookieValue(value = "IMPORT_RECORD_ID", required = false) Integer importRecordId, QueryParam param) {
        verifyPermission(importRecordId);
        param.setId(importRecordId);
        return Result.success(billService.getStatisticData(param));
    }

    /**
     * 获取支出分类数据
     *
     * @param importRecordId
     * @param param
     * @return
     */
    @GetMapping("/categories")
    public Result<List<PieDataVO>> getExpenseCategories(@CookieValue(value = "IMPORT_RECORD_ID", required = false) Integer importRecordId, QueryParam param) {
        verifyPermission(importRecordId);
        param.setId(importRecordId);
        return Result.success(billService.getExpenseCategoryData(param).stream().map(BillController::convertToPieData).toList());
    }


    private static PieDataVO convertToPieData(ExpenseCategoryVO x) {
        return new PieDataVO(x.name(), x.value());
    }

    /**
     * 获取收支趋势数据
     *
     * @param importRecordId
     * @param param
     * @return
     */
    @GetMapping("/trends")
    public Result<TrendVO> getFinancialTrends(@CookieValue(value = "IMPORT_RECORD_ID", required = false) Integer importRecordId, QueryParam param) {
        verifyPermission(importRecordId);
        param.setId(importRecordId);
        return Result.success(billService.getTrendsData(param));
    }

    /**
     * 获取账单分页列表
     *
     * @param importRecordId
     * @param param
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult<BillDetailVO>> page(@CookieValue(value = "IMPORT_RECORD_ID", required = false) Integer importRecordId, QueryParam param) {
        verifyPermission(importRecordId);
        param.setId(importRecordId);
        PageResult<? extends BaseBillDetail> page = billService.getPage(param);
        List<? extends BaseBillDetail> list = page.getList();
        if (list == null) {
            return Result.success(new PageResult<>(Collections.emptyList(), page.getTotal()));
        }
        List<BillDetailVO> list1 = list.stream().map(this::convertToVo).toList();
        return Result.success(new PageResult<>(list1, page.getTotal()));
    }

    private BillDetailVO convertToVo(BaseBillDetail billDetail) {
        BillDetailVO billVO = new BillDetailVO();
        BeanUtils.copyProperties(billDetail, billVO);
        billVO.setAmountTypeStr(AmountTypeEnum.getEnum(billVO.getAmountType()).getDesc());
        billVO.setSourceStr(BillSourceEnum.getNameBy(billVO.getSource()));
        return billVO;
    }

    /**
     * 支出来源饼图
     *
     * @param importRecordId
     * @param param
     * @return
     */
    @GetMapping("/sources")
    public Result<List<PieDataVO>> getExpenseSources(@CookieValue(value = "IMPORT_RECORD_ID", required = false) Integer importRecordId, QueryParam param) {
        verifyPermission(importRecordId);
        param.setId(importRecordId);
        param.setAmountType(AmountTypeEnum.EXPENSE.getType());
        return Result.success(billService.getExpenseSources(param).stream().map(BillController::convertToPieData).toList());
    }

    private static PieDataVO convertToPieData(ExpenseSourceVO x) {
        return new PieDataVO(x.name(), x.value());
    }

    /**
     * 校验权限
     *
     * @param importRecordId
     */
    public void verifyPermission(Integer importRecordId) {
        if (importRecordId == null) {
            throw new RuntimeException("请先去导入账单文件");
        }
        // 0的话就是mock数据
        if (importRecordId.equals(0)) {
            return;
        }
        Object obj = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute("IMPORT_RECORDS");
        if (obj == null) {
            throw new RuntimeException("请先去导入账单文件");
        }
        if (obj instanceof List<?> importRecords) {
            boolean notExist = importRecords.stream()
                    .filter(x -> x instanceof ParseRecord)
                    .map(x -> (ParseRecord) x)
                    .noneMatch(x -> importRecordId.equals(x.getId()));
            if (notExist) {
                throw new RuntimeException("您没有权限访问该账单分析");
            }
        }
    }
}
