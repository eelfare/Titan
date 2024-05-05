package com.yunji.titanrtx.manager.web.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.yunji.titanrtx.manager.service.common.datasource.DataSources;
import com.yunji.titanrtx.manager.service.common.datasource.DynamicRoutingDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Youjie on 2017/7/23.
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    @Qualifier("DATASOURCE_TITAN")
    public DataSource dataSourceTitan() {
        return new DruidDataSource();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.beacon")
    @Qualifier("DATASOURCE_BEACON")
    public DataSource dataSourceBeacon() {
        return new DruidDataSource();
    }


    @Bean
    @Primary
    public DataSource multipleDataSource() {
        DynamicRoutingDataSource dynamicDataSource = new DynamicRoutingDataSource();
        dynamicDataSource.setDefaultTargetDataSource(dataSourceTitan());

        Map<Object, Object> dataSourcesType = new HashMap<>();
        dataSourcesType.put(DataSources.DATASOURCE_TITAN, dataSourceTitan());
        dataSourcesType.put(DataSources.DATASOURCE_BEACON, dataSourceBeacon());
        dynamicDataSource.setTargetDataSources(dataSourcesType);
        return dynamicDataSource;
    }


    @Bean
    public SqlSessionFactory sqlSessionFactoryBean() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(multipleDataSource());
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mybatis/*/*.xml"));
        sqlSessionFactoryBean.setTypeAliasesPackage("com.yunji.titanx.manager.dao.mapper.*");
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("configuration.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(multipleDataSource());
    }


}
