package com.yunji.titanrtx.common.domain.task;

import lombok.Data;

import java.io.Serializable;

/**
 * ParamRange 当titan压测参数传递选择使用orders，即传递参数范围时,将会通过这个类来封装 start 和 end.
 *
 * @author leihz
 * @since 2020-05-27 3:51 下午
 */
@Data
public class ParamRange implements Serializable {
    /**
     * inclusive 范围起点,包含当前值
     */
    private int start;
    /**
     * exclusive 范围终点,不包含当前值
     */
    private int end;

    public ParamRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean isEmpty() {
        return end - start == 0;
    }

    @Override
    public String toString() {
        return "(start=" + start + ", end=" + end + ").";
    }

    public int size() {
        return end - start;
    }

    public ParamRange subRange(int from, int to) {
        return new ParamRange(start + from, start + to);
    }
}
