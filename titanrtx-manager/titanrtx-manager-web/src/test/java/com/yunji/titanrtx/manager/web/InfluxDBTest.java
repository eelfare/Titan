package com.yunji.titanrtx.manager.web;

import com.yunji.titanrtx.common.GlobalConstants;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

public class InfluxDBTest {


    public static void main(String[] args) {
         InfluxDB influxDB = InfluxDBFactory.connect("http://127.0.0.1:8086","root","root");

        String commander = String.format("SELECT SUM(requestTimes),SUM(successTimes),mean(elapse) FROM mate WHERE time < %s  GROUP BY time(10m),* LIMIT 1 SLIMIT %d", System.currentTimeMillis() + "ms",50);

        Query query = new Query(commander,GlobalConstants.TOP_LINK_MATE_DB_NAME);
        QueryResult queryResult = influxDB.query(query);

        System.out.println(queryResult);

    }




}
