package com.yunji.titanrtx.manager.service.common.datasource;

/**
 * Created by Youjie on 2017/7/23.
 */
public class DataSourceManager {

    private static final ThreadLocal<DataSources> dataSources = new ThreadLocal<DataSources>() {
        //重写ThreadLocal的initialValue()方法，注入默认的数据源A
        @Override
        protected DataSources initialValue() {
            return DataSources.DATASOURCE_TITAN;
        }
    };

    public static DataSources get() {

        return dataSources.get();

    }

    public static void set(DataSources dataSourceType) {
        dataSources.set(dataSourceType);
    }

    public static void reset() {
        dataSources.set(DataSources.DATASOURCE_TITAN);
    }

    public static void remove() {
        dataSources.remove();

    }


}
