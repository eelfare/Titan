package com.yunji.titanrtx.agent.collect;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-10-29 23:00
 * @Version 1.0
 */
@Slf4j
public class Collector {
    // 采集数据数组的长度
    public static final int COLLECT_LIST_LENGTH = 8;
    // 对应秒级的请求的QPS统计数据
    public static final ConcurrentSkipListMap<String, ConcurrentHashMap<String, AtomicInteger>> requestConcurrent = new ConcurrentSkipListMap<>();
    // 对应秒级的响应的QPS统计数据
    public static final ConcurrentHashMap<String, ConcurrentHashMap<String,AtomicReference<long[]>>> responseConcurrent = new ConcurrentHashMap<>();

    public static void doCollect(boolean request, String path, long[] responseData) {
        String time = Integer.toString((int) (new Date().getTime() / 1000));

        {
            // 采集请求数据
            ConcurrentHashMap<String, AtomicInteger> atomicLinkeQpsMap = requestConcurrent.get(time);
            // 递增请求数据
            if (atomicLinkeQpsMap == null) {
                requestConcurrent.putIfAbsent(time, new ConcurrentHashMap<>());
                atomicLinkeQpsMap = requestConcurrent.get(time);
            }

            AtomicInteger atomicInteger = atomicLinkeQpsMap.get(path);
            if (atomicInteger == null) {
                atomicLinkeQpsMap.putIfAbsent(path, new AtomicInteger());
                atomicInteger = atomicLinkeQpsMap.get(path);
            }

            if (request) {
                atomicInteger.incrementAndGet();
            }
//            log.info("{}数据采集:{}",request?"请求-":"响应-",atomicLinkeQpsMap);
        }
        {

            // 采集响应数据
            ConcurrentHashMap<String, AtomicReference<long[]>> atomicLinkIdQpsMap = responseConcurrent.get(time);
            if (atomicLinkIdQpsMap == null) {
                responseConcurrent.putIfAbsent(time,new ConcurrentHashMap<>());
                atomicLinkIdQpsMap = responseConcurrent.get(time);
            }
            AtomicReference<long[]> atomicReference = atomicLinkIdQpsMap.get(path);
            if (atomicReference == null) {
                atomicLinkIdQpsMap.putIfAbsent(path, new AtomicReference<>());
                atomicReference = atomicLinkIdQpsMap.get(path);
            }
            long[] current;
            long[] update = new long[COLLECT_LIST_LENGTH];
            do {
                current = atomicReference.get();
                if (current == null) {
                    update[0] = responseData == null || responseData[0] == 0 ? 0 : 1;
                    update[1] = responseData == null || responseData[1] == 0 ? 0 : 1;
                    update[2] = responseData == null || responseData[2] == 0 ? 0 : 1;
                    update[3] = responseData == null || responseData[3] == 0 ? 0 : 1;
                    update[4] = responseData == null || responseData[4] == 0 ? 0 : 1;
                    update[5] = responseData == null || responseData[5] == 0 ? 0 : 1;
                    update[6] = responseData == null || responseData[6] == 0 ? 0 : 1;
                    update[7] = responseData == null || responseData[7] == 0 ? 0 : 1;
                } else {
                    update[0] = responseData == null || responseData[0] == 0 ? current[0] : current[0] + 1;
                    update[1] = responseData == null || responseData[1] == 0 ? current[1] : current[1] + 1;
                    update[2] = responseData == null || responseData[2] == 0 ? current[2] : current[2] + 1;
                    update[3] = responseData == null || responseData[3] == 0 ? current[3] : current[3] + 1;
                    update[4] = responseData == null || responseData[4] == 0 ? current[4] : current[4] + 1;
                    update[5] = responseData == null || responseData[5] == 0 ? current[5] : current[5] + 1;
                    update[6] = responseData == null || responseData[6] == 0 ? current[6] : current[6] + 1;
                    update[7] = responseData == null || responseData[7] == 0 ? current[7] : current[7] + 1;
                }
            } while (!atomicReference.compareAndSet(current, update));
        }
    }

    public static void clear(String key) {
        requestConcurrent.remove(key);
        responseConcurrent.remove(key);
    }
}
