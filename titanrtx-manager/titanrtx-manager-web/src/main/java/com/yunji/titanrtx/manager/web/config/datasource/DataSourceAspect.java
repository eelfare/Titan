package com.yunji.titanrtx.manager.web.config.datasource;

import com.yunji.titanrtx.manager.service.common.datasource.DataSource;
import com.yunji.titanrtx.manager.service.common.datasource.DataSourceManager;
import com.yunji.titanrtx.manager.service.common.datasource.DataSources;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Created by Youjie on 2017/7/23.
 */
@Aspect
@Component
@Slf4j
public class DataSourceAspect {

    @Before("@annotation(ds)")
    public void setAnnotationDataSource(JoinPoint jp, DataSource ds) {
        DataSourceManager.set(Enum.valueOf(DataSources.class, ds.value()));

        log.info("切换数据源为 -> {}", ds.value());
    }

    @After("@annotation(ds)")
    public void resetAnnotationDataSource(DataSource ds) {
        DataSourceManager.reset();
        log.info("Reset 数据源 -> {}", DataSourceManager.get());
    }


}




