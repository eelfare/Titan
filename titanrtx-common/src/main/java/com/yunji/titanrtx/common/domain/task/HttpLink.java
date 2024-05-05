package com.yunji.titanrtx.common.domain.task;

import com.yunji.titanrtx.common.enums.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HttpLink extends Bullet {

    private Protocol protocol;

    private String url;

    private Method method;

    private Content contentType;

    private Charset charset;

}
