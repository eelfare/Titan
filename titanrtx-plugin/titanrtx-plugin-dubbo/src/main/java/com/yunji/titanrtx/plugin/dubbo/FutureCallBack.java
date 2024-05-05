package com.yunji.titanrtx.plugin.dubbo;

public interface FutureCallBack<T> {

    void completed(T result);

    void failed(Exception e);

    void cancelled();

    void start();

}
