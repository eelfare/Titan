package com.yunji.titanrtx.common.domain.task;


import java.io.Serializable;
import java.util.Objects;

/**
 * Pair
 *
 * @author leihz
 * @since 2020-05-24 3:58 下午
 */

public class Pair<K, V> implements Serializable {

    private K key;

    public K getKey() {
        return key;
    }

    private V value;

    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Pair() {
    }

    public boolean isLeft() {
        return key != null;
    }

    public boolean isRight() {
        return value != null;
    }

    @Override
    public String toString() {
        return key + "->" + value;
    }


    @Override
    public int hashCode() {
        return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key) &&
                Objects.equals(value, pair.value);
    }
}
