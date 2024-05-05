package com.yunji.titanrtx.common;

public class RuntimeExceptionMain {


    public static void main(String[] args) {


        Thread s =new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println(s.getId());
        s.start();


    }


    private static void throwRuntime(){
        throw new RuntimeException();
    }


}
