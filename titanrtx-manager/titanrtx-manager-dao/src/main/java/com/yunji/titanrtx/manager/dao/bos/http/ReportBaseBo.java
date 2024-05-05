package com.yunji.titanrtx.manager.dao.bos.http;

import com.deepoove.poi.data.DocxRenderData;
import lombok.Data;

@Data
public class ReportBaseBo {

    private String date;

    private DocxRenderData reportItems;

}
