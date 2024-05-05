package com.yunji.titanrtx.manager.service.report.support;

/**
 * Listener
 *
 * @author leihz
 * @since 2020-06-11 7:00 下午
 */
public interface Listener<T> {

    void onListen(T ret);
}
