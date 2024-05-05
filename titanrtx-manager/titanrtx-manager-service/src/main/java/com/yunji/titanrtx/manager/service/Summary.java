package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.manager.dao.bos.SummaryStatistics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public interface Summary {

    SummaryStatistics summary(int sceneId, Statistics statistics, AtomicInteger emptyCGIDataCount, AtomicReference<String> improperLinkBaseLine) throws InterruptedException;


}
