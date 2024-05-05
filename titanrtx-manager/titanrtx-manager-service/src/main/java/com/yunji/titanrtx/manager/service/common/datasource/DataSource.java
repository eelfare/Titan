package com.yunji.titanrtx.manager.service.common.datasource;

import java.lang.annotation.*;

/**
 * Created by Youjie on 2017/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataSource {
    String value() default "";
}
