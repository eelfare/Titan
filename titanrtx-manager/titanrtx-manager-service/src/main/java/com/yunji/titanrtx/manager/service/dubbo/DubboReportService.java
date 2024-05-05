package com.yunji.titanrtx.manager.service.dubbo;

import com.yunji.titanrtx.manager.dao.bos.PressureReportBo;
import com.yunji.titanrtx.manager.dao.bos.http.ReportItemBo;
import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboReportEntity;
import com.yunji.titanrtx.manager.service.ExportService;

import java.util.List;

public interface DubboReportService extends ExportService {

    int insert(DubboReportEntity dubboReportEntity);

    List<PressureReportBo> selectBySceneId(Integer sceneId);

    PressureReportBo findById(Integer id);

    List<PressureReportBo> selectAll();

    int deleteById(Integer id);

    List<PressureReportBo> searchSceneName(String key);

    int count();

    List<PressureReportBo> selectByIds(List<String> reportIds);

}
