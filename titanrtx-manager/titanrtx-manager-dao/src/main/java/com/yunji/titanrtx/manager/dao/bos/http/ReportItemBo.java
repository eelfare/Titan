package com.yunji.titanrtx.manager.dao.bos.http;

import com.deepoove.poi.data.NumbericRenderData;
import lombok.Data;

@Data
public class ReportItemBo {

    private String name;

    private NumbericRenderData url;

    private ReportRecordsBo recordsBo;

}
