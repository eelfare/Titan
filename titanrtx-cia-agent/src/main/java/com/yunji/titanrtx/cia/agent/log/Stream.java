package com.yunji.titanrtx.cia.agent.log;

public interface Stream<T> {

    T  onEvent(T t);

}
