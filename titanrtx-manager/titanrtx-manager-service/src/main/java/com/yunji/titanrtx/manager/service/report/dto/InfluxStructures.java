package com.yunji.titanrtx.manager.service.report.dto;

import lombok.Data;

import java.util.List;


/**
 * 自动化压测报告,Influx DB 返回结果解析。
 *
 * <pre>
 *     {
 * 	    "results": [{
 * 		    "series": [{
 * 			    "columns": [
 * 				    "time",
 * 				    "owner",
 * 				    "app",
 * 				    "service",
 * 				    "method",
 * 				    "avgElapsed",
 * 				    "success",
 * 				    "softKey"
 * 			    ],
 * 			    "name": "top_data",
 * 			    "values": [
 * 				    [
 * 					    1588068930000,
 * 					    "abc",
 * 					    "demoService",
 * 					    "com.yunji.gateway.api.IDemoService",
 * 					    "query",
 * 					    77.4112,
 * 					    2037.0,
 * 					    159724.0
 * 				    ]
 * 			    ]
 *           }]
 *        }]
 *      }
 * </pre>
 *
 * @author leihuazhe
 * @since 2020.4.28
 */
public class InfluxStructures {

    @Data
    public static class Root {
        private List<Series> results;
    }

    @Data
    public static class Series {

        private List<Metric> series;

    }

    @Data
    public static class Metric {

        private String name;

        private List<String> columns;

        private List<List<Object>> values;
    }


}
