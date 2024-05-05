package com.yunji.titanrtx.cia.agent.log.core;

/**
 * @author Denim.leihz 2019-12-10 9:46 AM
 */
public class Constants {

    public static final int INFLUXDB_POINT_QUEUE_SIZE = 20000;
    public static final int INFLUX_ENABLE_BATCH_SIZE = 5000;
    //ms
    public static final int INFLUX_WRITE_FLUSH_DURATION = 500;
    //ms
    public static final int POINT_QUEUE_DRAIN_TIMEOUT = 1000;
}
