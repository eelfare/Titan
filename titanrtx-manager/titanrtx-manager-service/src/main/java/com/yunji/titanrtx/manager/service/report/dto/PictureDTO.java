package com.yunji.titanrtx.manager.service.report.dto;

import com.yunji.titanrtx.common.u.LocalDateU;
import lombok.Data;

/**
 * PictureDTO
 *
 * @author leihz
 * @since 2020-06-30 3:33 下午
 */
@Data
public class PictureDTO {

    private String name;

    private int batch;

    private String path;

    public PictureDTO(String prefix, long timestamp, int batch, String path) {
        this.name = convert(prefix, timestamp);
        this.path = path;
        this.batch = batch;
    }

    private String convert(String prefix, long timestamp) {
        String hourMinSecDate = LocalDateU.getHourMinSecDate(timestamp);

        return prefix + "#" + hourMinSecDate;
    }
}
