package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.manager.dao.bos.http.ReportItemBo;

import java.util.List;

public interface ExportService {

    List<ReportItemBo> reportItemBos(String ids);

}
