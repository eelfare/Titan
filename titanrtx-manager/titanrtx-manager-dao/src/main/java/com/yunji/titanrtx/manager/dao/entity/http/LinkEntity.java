package com.yunji.titanrtx.manager.dao.entity.http;

import com.yunji.titanrtx.common.enums.*;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;

@EqualsAndHashCode(callSuper = true)
@Data
public class LinkEntity extends BaseEntity {

    private String name;

    private Protocol protocol;

    private String url;

    private Method method;

    private Content contentType ;

    private Charset charset;

    private ParamMode paramMode;

    @Transient
    private long scale = 100;

    @Transient
    private long weight = 100;

    @Transient
    private long qps = 0;
}
