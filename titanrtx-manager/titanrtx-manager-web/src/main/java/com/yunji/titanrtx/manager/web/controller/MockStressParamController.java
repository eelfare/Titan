package com.yunji.titanrtx.manager.web.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.yunji.titanrtx.common.alarm.AlarmService;
import com.yunji.titanrtx.common.alarm.MessageSender;
import com.yunji.titanrtx.common.domain.task.ParamRange;
import com.yunji.titanrtx.manager.dao.mapper.http.LinkParamsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 模拟压测,看数据库是否能够抗住
 *
 * @author leihz
 * @since 2020-09-14 11:46 上午
 */
@RestController
@Slf4j
@RequestMapping("common/")
public class MockStressParamController implements InitializingBean {
    //ses
    private ScheduledExecutorService ses = Executors.newScheduledThreadPool(100);

    private List<Integer> list = new ArrayList<>();

    private Map<Integer, List<String>> stats = new HashMap<>();


    @Resource
    private LinkParamsMapper paramsMapper;

    @Resource
    private AlarmService alarmService;

    @RequestMapping("/helloAlarm.query")
    public String helloAlarm(String msg) {
        if (msg.contains("auto")) {
            alarmService.send(MessageSender.Type.AUTO_ALARM, msg);
        } else {
            alarmService.send(MessageSender.Type.MAINTAINER, msg);
        }
        return "OK";
    }


    @RequestMapping("/stats.query")
    public String getStats() {
        return JSON.toJSONString(stats);
    }

    @RequestMapping("/begin.query")
    public void beginTest() {
        list.forEach(linkId -> {
            log.info("Linked {} begin.", linkId);
            int totalRecords = paramsMapper.findTotalRecordsByLinkId(linkId);
            ses.execute(() -> fetchData(linkId, 5000, totalRecords));
        });
    }

    //15w
    public void fetchData(int linkId, int lumpSize, int maxOrder) {
        int selectedCount = 1, n = 1;
        ParamRange range = new ParamRange(0, maxOrder);
        do {
            try {
                List<String> params = selectParamsByRange(linkId, n * lumpSize > range.size() ?
                        //最后一轮,要获取的参数已经超过了给定的参数的最大值
                        range.subRange((n - 1) * lumpSize, range.size()) :
                        // (n-1)*lumpSize - n*lumpSize
                        range.subRange((n - 1) * lumpSize, n * lumpSize));

                //将查询出来的参数放近队列中,队列默认只有4个容量,所以最多存放4个lump参数块。
                // 插入成功才需要移位
                selectedCount++;
                //顺序使用,如果达到最大值，也不会从头取获取参数了.
                n = selectedCount;
                //这种情况在不可重复使用参数消费(顺序消费ORDER)情况下发生,不再继续消费了
                if ((n - 1) * lumpSize >= range.size()) {
                    log.info("链路: ({}) 所有数据已经生产完成。数据总量:{},参数块:{},生产批次:{}.",
                            linkId, range.size(), lumpSize, selectedCount - 1);
                }
            } catch (Exception e) {
                log.error("链路 {} 获取参数数据失败" + e.getMessage(), linkId, e);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        } while ((n - 1) * lumpSize < range.size());
    }

    private List<String> selectParamsByRange(int linkId, ParamRange range) {
        long st = System.currentTimeMillis();
        List<String> res = paramsMapper.selectParamsByRange(linkId, range.getStart(), range.getEnd());

        long cost = System.currentTimeMillis() - st;

        String msg = "size: " + res.size() + ", cost: " + cost + "ms";

        stats.compute(linkId, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
                v.add(msg);
            } else {
                v.add(msg);
            }
            return v;
        });

        log.info("链路:{}, range:{}, 获取耗时:{}ms, size:{}.", linkId, range, cost, res.size());

        return res;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        list.add(13788);
        list.add(13789);
        list.add(13791);
        list.add(13792);
        list.add(13793);
        list.add(13794);
        list.add(13795);
        list.add(13797);
        list.add(13798);
        list.add(13799);
        list.add(13800);
        list.add(13801);
        list.add(13802);
        list.add(13804);
        list.add(13805);
        list.add(13806);
        list.add(13807);
        list.add(13808);
        list.add(13809);
        list.add(13810);
        list.add(13811);
        list.add(13812);
        list.add(13813);
        list.add(13815);
        list.add(13816);
        list.add(13817);
        list.add(13818);
        list.add(13819);
        list.add(13820);
        list.add(13821);
        list.add(13822);
        list.add(13823);
        list.add(13824);
        list.add(13825);
        list.add(13826);
        list.add(13830);
        list.add(13831);
        list.add(13832);
        list.add(13837);
        list.add(13839);
        list.add(13840);
        list.add(13842);

        log.info("填充linkId list over, size:{}.", list.size());
    }
}
