package com.yunji.titanrtx.common.domain.statistics;

import com.yunji.titanrtx.common.GlobalConstants;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class StatisticsDetail implements Serializable {

    protected volatile long duration = 0;

    private Map<Integer, AtomicInteger> STATUS_CODE_MAP = new ConcurrentHashMap<>();

    private Map<Integer, AtomicInteger> BUSINESS_CODE_MAP = new ConcurrentHashMap<>();
    //Newly add in 2020.7.22 错误返回 wrong error stats.
    private List<String> WRONG_RET_CONTENTS = new ArrayList<>();

    public long successTimes() {
        return STATUS_CODE_MAP.getOrDefault(GlobalConstants.HTTP_SUCCESS_CODE, new AtomicInteger(0)).intValue();
    }

    public long failTimes() {
        return STATUS_CODE_MAP.entrySet().stream().filter(integerEntry -> integerEntry.getKey() != GlobalConstants.HTTP_SUCCESS_CODE).mapToInt(integerAtomicIntegerEntry -> integerAtomicIntegerEntry.getValue().intValue()).sum();
    }

    public long requestTimes() {
        return successTimes() + failTimes();
    }

    public long businessSuccessTime() {
        return BUSINESS_CODE_MAP.getOrDefault(GlobalConstants.YUNJI_SUCCESS_CODE, new AtomicInteger(0)).intValue();
    }

    public long businessFailTime() {
        return BUSINESS_CODE_MAP.entrySet().stream().filter(integerEntry -> integerEntry.getKey() != GlobalConstants.YUNJI_SUCCESS_CODE).mapToInt(integerAtomicIntegerEntry -> integerAtomicIntegerEntry.getValue().intValue()).sum();
    }

    public void addStatusCode(int code) {
        AtomicInteger times = STATUS_CODE_MAP.get(code);
        if (times == null) {
            synchronized (this) {
                STATUS_CODE_MAP.putIfAbsent(code, new AtomicInteger());
                times = STATUS_CODE_MAP.get(code);
            }
        }
        times.incrementAndGet();
    }

    public void addBusiness(int code) {
        AtomicInteger times = BUSINESS_CODE_MAP.get(code);
        if (times == null) {
            synchronized (this) {
                BUSINESS_CODE_MAP.putIfAbsent(code, new AtomicInteger());
                times = BUSINESS_CODE_MAP.get(code);
            }
        }
        times.incrementAndGet();
    }

    public void addAll(Map<Integer, AtomicInteger> preMap, Map<Integer, AtomicInteger> reportMap) {
        reportMap.forEach((integer, atomicInteger) -> {
            AtomicInteger preTimes = preMap.get(integer);
            if (preTimes == null) {
                preMap.put(integer, atomicInteger);
            } else {
                preTimes.addAndGet(atomicInteger.intValue());
            }
        });


    }

    public void addWrongReturn(String content) {
        WRONG_RET_CONTENTS.add(content);
    }


}
