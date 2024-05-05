package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.common.u.NamedThreadFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class CompletableFutureTest {
    private static ExecutorService executorService = Executors.newFixedThreadPool(20, new NamedThreadFactory("influx-db-pool"));

    @Data
    public static class DO {
        private int status;
        private int num;

        private long cost;

        public DO(int status, int num) {
            this.status = status;
            this.num = num;
        }
    }


    public static void test(List<DO> doList) {
        long st = System.currentTimeMillis();
        List<CompletableFuture<DO>> doFutureList = new ArrayList<>();
        for (DO dos : doList) {
            if (dos.getStatus() == 1) {
                CompletableFuture<DO> innerBoFuture = CompletableFuture.supplyAsync(() -> {
                    long ist = System.currentTimeMillis();
                    int random = ThreadLocalRandom.current().nextInt(10);
                    try {
                        TimeUnit.SECONDS.sleep(random);
                    } catch (InterruptedException ignored) {
                    }
                    dos.setNum(dos.getNum() + random);
                    dos.setCost(System.currentTimeMillis() - ist);
                    return dos;
                }, executorService);
                doFutureList.add(innerBoFuture);
            } else {
                dos.setCost(0);
                doFutureList.add(CompletableFuture.completedFuture(dos));
            }
        }

        //List<Future<StatisticsBo>> -> Future<List<StatisticsBo>>
        CompletableFuture<List<DO>> listCompletableFuture = CompletableFuture
                .allOf(doFutureList.toArray(new CompletableFuture[0]))
                .thenApply(value -> doFutureList.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<DO> innerDos = new ArrayList<>();
        try {
            innerDos = listCompletableFuture.get();
        } catch (Exception e) {
            log.error("Got CGI error,cause: " + e.getMessage(), e);
        }

        long et = System.currentTimeMillis();

        System.out.println("Cost: " + (et - st) + "ms");

        for (int i = 0; i < innerDos.size(); i++) {
            System.out.println("DO-" + i + ",value: " + innerDos.get(i));
        }
    }


    public static void main(String[] args) {
        List<DO> doList = new ArrayList<>();

        doList.add(new DO(1, 27));
        doList.add(new DO(1, 12));
        doList.add(new DO(1, 12));
        doList.add(new DO(1, 12));
        doList.add(new DO(1, 12));
        doList.add(new DO(1, 12));
        doList.add(new DO(1, 12));
        doList.add(new DO(1, 34));
        doList.add(new DO(1, 18));
        doList.add(new DO(1, 98));
        doList.add(new DO(1, 25));
        doList.add(new DO(1, 21));
        doList.add(new DO(0, 13));
        doList.add(new DO(0, 14));

        test(doList);

    }
}
