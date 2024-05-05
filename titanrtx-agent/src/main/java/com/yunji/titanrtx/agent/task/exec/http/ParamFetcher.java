package com.yunji.titanrtx.agent.task.exec.http;

import com.yunji.titanrtx.agent.service.ParamsService;
import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.ParamRange;
import com.yunji.titanrtx.common.enums.ParamMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * ParamFetcher 测试类
 *
 * @author leihz
 * @since 2020-05-24 5:42 下午
 */
@Slf4j
@Component
public class ParamFetcher {

    @Resource(name = "http")
    private ParamsService paramsService;
    /**
     * 各链路参数数据块,key为链路id,value为链路参数数据块.
     */
    private static Map<Integer, BlockingQueue<List<String>>> paramsBlockingQueueMap = new ConcurrentHashMap<>();
    private static Bullet bullet;
    private static Integer lumpSize;

    static {
        paramsBlockingQueueMap.put(1000, new LinkedBlockingQueue<>(100));
        bullet = new Bullet();
        bullet.setParamRange(new ParamRange(5, 10003));
        bullet.setParamMode(ParamMode.RANDOM);
        lumpSize = 121;
    }

    public void paramFetch(Integer linkId) {
        BlockingQueue<List<String>> queue = paramsBlockingQueueMap.get(linkId);
        ParamRange range = bullet.getParamRange();

        int selectedCount = 1, n = 1;

        do {
            try {
                List<String> params = paramsService.selectParamsByRange(linkId, n * lumpSize > range.size() ?
                        //最后一轮,要获取的参数已经超过了给定的参数的最大值
                        range.subRange((n - 1) * lumpSize, range.size()) :
                        // (n-1)*lumpSize - n*lumpSize
                        range.subRange((n - 1) * lumpSize, n * lumpSize));

                //将查询出来的参数放近队列中,队列默认只有4个容量,所以最多存放4个lump参数块。
                if (queue.offer(params, 5, TimeUnit.SECONDS)) {
                    // 插入成功才需要移位
                    selectedCount++;

                    if (bullet.getParamMode() == ParamMode.RANDOM) {
                        //参数总数/块的大小+1 = 取块的大小时的最大的块数.
                        int maxLoopSize = (int) Math.ceil(range.size() / (lumpSize * 1.0) + 1);
                        //主要是判断当前要取的块,在总的参数中的下标位置,如果下标已经为0了,说明要复位，重新从最开始取值.
                        int currentLoopIndex = selectedCount % maxLoopSize;
                        // 循环使用
                        if (currentLoopIndex == 0) {
                            log.info("下一个循环");
                        }
                        n = (currentLoopIndex == 0 ? 1 : currentLoopIndex);
                    } else {
                        //顺序使用,如果达到最大值，也不会从头取获取参数了.
                        n = selectedCount;
                    }
                    //这种情况在不可重复使用参数消费(顺序消费ORDER)情况下发生,不再继续消费了
                    if ((n - 1) * lumpSize >= range.size()) {
                        log.info("链路ID:{},ParamMode:{}所有数据已经生产完成。数据总量:{},参数块:{},生产批次:{}.",
                                linkId, bullet.getParamMode(), range.size(), lumpSize, selectedCount - 1);
                    }
                } else {
                    log.warn("链路{}参数插入队列数据超时,队列长度为：{}", linkId, queue.size());
                    Thread.sleep(10); // 释放cpu执行权限
                }
            } catch (Exception e) {
                log.error("链路 {} 获取参数数据失败" + e.getMessage(), linkId, e);
            }
        } while ((n - 1) * lumpSize < range.size());
        log.warn("All Over.");
    }
}
