package com.z.billanalyzer.service;

import com.z.billanalyzer.entity.BaseBillInfo;
import com.z.billanalyzer.entity.Bill;
import com.z.billanalyzer.entity.QueryParam;
import com.z.billanalyzer.entity.vo.*;
import com.z.billanalyzer.entity.vo.echarts.TrendVO;

import java.util.Arrays;
import java.util.List;

public interface IBillService {

    /**
     * 根据ID获取Bill列表
     *
     * @param id
     * @return List<Bill>
     */
    List<Bill> getBills(Integer id);

    /**
     * 根据ID获取BaseBillInfo列表
     *
     * @param id
     * @return List<BaseBillInfo>
     */
    List<BaseBillInfo> getBillInfos(Integer id);

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
    Integer saveBill(List<BaseBillInfo> billInfos);

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
    PageResult<BillVO> getPage(QueryParam param);

    /**
     * 获取支出来源数据
     *
     * @param param
     * @return List<ExpenseCategoryVO>
     */
    List<ExpenseSourceVO> getExpenseSources(QueryParam param);
}
