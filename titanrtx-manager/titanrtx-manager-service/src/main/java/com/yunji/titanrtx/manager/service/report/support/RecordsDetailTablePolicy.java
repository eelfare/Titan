package com.yunji.titanrtx.manager.service.report.support;

import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.policy.DynamicTableRenderPolicy;
import com.deepoove.poi.policy.MiniTableRenderPolicy;
import com.yunji.titanrtx.manager.dao.bos.http.ReportRecordsBo;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecordsDetailTablePolicy extends DynamicTableRenderPolicy {

    private int endIndex;

    public RecordsDetailTablePolicy(int endIndex) {
        this.endIndex = endIndex;
    }

    @Override
    public void render(XWPFTable xwpfTable, Object o) {
        if (null == o) return;
        ReportRecordsBo reportRecordsBO = (ReportRecordsBo) o;
        List<RowRenderData> records = reportRecordsBO.getRecords();
        if (null != records) {
            int recordStartRow = 1;
            xwpfTable.removeRow(recordStartRow);
            for (RowRenderData record : records) {
                XWPFTableRow insertNewTableRow = xwpfTable.insertNewTableRow(recordStartRow);
                for (int j = 0; j < endIndex; j++) {
                    insertNewTableRow.createCell();
                }

//                MiniTableRenderPolicy.renderRow(xwpfTable, recordStartRow, record);
                MiniTableRenderPolicy.Helper.renderRow(xwpfTable, recordStartRow, record);

            }
        }
    }
}
