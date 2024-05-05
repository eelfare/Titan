package com.yunji.titanrtx.manager.web;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrxt.plugin.influxdb.StoreService;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TitanxManagerWebApplicationTests {




    @Resource
    private StoreService storeService;


    @Test
    public void contextLoads() {


        Query query = BoundParameterQuery.QueryBuilder.newQuery("SELECT * FROM mate WHERE time > $time")
                .forDatabase(GlobalConstants.TOP_LINK_MATE_DB_NAME)
                .bind("time", System.currentTimeMillis())
                .create();
        QueryResult queryResult = storeService.query(query);
        System.out.println(queryResult);

    }

}

