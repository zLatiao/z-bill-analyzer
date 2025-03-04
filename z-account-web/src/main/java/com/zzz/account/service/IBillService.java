package com.zzz.account.service;

import com.zzz.account.entity.BaseBillInfo;
import com.zzz.account.entity.Bill;
import com.zzz.account.entity.QueryParam;
import com.zzz.account.entity.vo.*;
import com.zzz.account.entity.vo.*;

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
     * 获取支出数据
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
}
