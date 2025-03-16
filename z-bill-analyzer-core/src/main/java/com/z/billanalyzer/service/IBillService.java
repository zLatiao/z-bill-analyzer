package com.z.billanalyzer.service;

import com.z.billanalyzer.domain.PageResult;
import com.z.billanalyzer.domain.bill.BaseBill;
import com.z.billanalyzer.domain.bill.BaseBillDetail;
import com.z.billanalyzer.domain.param.QueryParam;
import com.z.billanalyzer.domain.vo.*;

import java.util.List;

public interface IBillService {

    /**
     * 根据ID获取Bill列表
     *
     * @param id
     * @return List<Bill>
     */
    List<? extends BaseBillDetail> getBills(Integer id);

    /**
     * 根据ID获取BaseBillInfo列表
     *
     * @param id
     * @return List<BaseBillInfo>
     */
    List<BaseBill<?>> getBillInfos(Integer id);

    /**
     * 获取todo
     *
     * @param id
     * @return
     */
    ImportBillInfoVO getImportBillInfo(Integer id);

    /**
     * 保存账单
     *
     * @param billInfos
     * @return Integer
     */
    Integer saveBill(List<BaseBill<?>> billInfos);

    /**
     * 获取统计数据
     *
     * @param param
     * @return StatisticVO
     */
    StatisticVO getStatisticData(QueryParam param);

    /**
     * 获取支出分类数据
     *
     * @param param
     * @return List<ExpenseCategoryVO>
     */
    List<ExpenseCategoryVO> getExpenseCategoryData(QueryParam param);

    /**
     * 获取趋势数据
     *
     * @param param
     * @return TrendVO
     */
    TrendVO getTrendsData(QueryParam param);

    /**
     * 获取分页
     *
     * @param param
     * @return PageResult<BillVO>
     */
    PageResult<BillDetailVO> getPage(QueryParam param);

    /**
     * 获取支出来源数据
     *
     * @param param
     * @return List<ExpenseCategoryVO>
     */
    List<ExpenseSourceVO> getExpenseSources(QueryParam param);
}
