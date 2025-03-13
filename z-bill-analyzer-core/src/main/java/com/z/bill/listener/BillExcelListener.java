package com.z.bill.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author z-latiao
 * @since 2025/2/26 14:55
 */
public class BillExcelListener extends AnalysisEventListener<LinkedHashMap<Integer, String>> {
    private final List<Integer> readRowList;
    private final List<String> dataList;
    private final int stopNumber;

    public BillExcelListener(List<Integer> readRowList, int stopNumber, List<String> dataList) {
        this.readRowList = readRowList;
        this.dataList = dataList;
        this.stopNumber = stopNumber;
    }

    @Override
    public void invoke(LinkedHashMap<Integer, String> data, AnalysisContext context) {
        Integer currentRow = context.readRowHolder().getRowIndex();
        if (readRowList.contains(currentRow)) {
            dataList.add(data.get(0));
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        if (context.readRowHolder().getRowIndex() >= stopNumber) {
            return false;
        }
        return super.hasNext(context);
    }

}
