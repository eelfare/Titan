package com.yunji.titanrtx.manager.service.report.dto;

import com.deepoove.poi.data.PictureRenderData;
import lombok.Data;

/**
 * PictureDataWrapper
 *
 * @author leihz
 * @since 2020-06-30 4:26 下午
 */
@Data
public class PictureDataWrapper {

    private String name;

    private int batch;

    private PictureRenderData renderData;

    public PictureDataWrapper(String name, int batch, PictureRenderData renderData) {
        this.name = name;
        this.batch = batch;
        this.renderData = renderData;
    }
}
