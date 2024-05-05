package com.yunji.titanrtx.manager.web;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * PriorityQueueTest
 *
 * @author leihz
 * @since 2020-07-22 9:39 上午
 */
public class PriorityQueueTest {

    public static void main(String[] args) {
        test2();
    }

    public static void test2() {
        int size = 9;
        int res = (size >>> 1) - 1;
        //---
        System.out.println(">>>> " + res);
        System.out.println("No " + (size - 2) / 2);
        //---
    }


    public static void test1() {
        Queue<Integer> priorityQueue = new PriorityQueue<>();
        priorityQueue.add(10);
        priorityQueue.add(20);
        priorityQueue.add(30);

    }
}
