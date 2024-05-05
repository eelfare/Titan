package com.yunji.titanrtx.common.u;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class CollectionU {

    public static final String EMPTY = "";

    public static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();


    public static boolean isArray(Object obj) {
        return (obj != null && obj.getClass().isArray());
    }
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }
    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.size() == 0;
    }
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }
    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }

    public static <T> String toStr(Collection<T> collection) {
        return toStr(collection, ",");
    }
    public static <T> String toStr(Collection<T> collection, String split) {
        return toStr(collection.toArray(), split);
    }
    public static String toStr(Object[] array) {
        return toStr(array, ",");
    }
    public static String toStr(Object[] array, String split) {
        if (isEmpty(array)) {return EMPTY;}

        StringBuilder sbd = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sbd.append(array[i]);
            if (i + 1 != array.length) {sbd.append(split);}
        }
        return sbd.toString();
    }

    /** 构造 HashMap, 必须保证每两个参数的类型是一致的! 当参数是奇数时, 最后一个 key 将会被忽略 */
    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> maps(Object... keysAndValues) {
        return (HashMap<K, V>) maps(new HashMap<K, V>(), keysAndValues);
    }
    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> maps(Map<K, V> result, Object... keysAndValues) {
        if (isNotEmpty(keysAndValues)) {
            for (int i = 0; i < keysAndValues.length; i += 2) {
                if (keysAndValues.length > (i + 1)) {
                    result.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
                }
            }
        }
        return result;
    }
    /** 构造 LinkedHashMap, 必须保证每两个参数的类型是一致的! 当参数是奇数时, 最后一个 key 将会被忽略 */
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> linkedMaps(Object... keysAndValues) {
        return (LinkedHashMap<K, V>) maps(new LinkedHashMap<K, V>(), keysAndValues);
    }

    /** 获取集合的第一个元素 */
    public static <T> T first(Collection<T> collection) {
        return isEmpty(collection) ? null : collection.iterator().next();
    }
    /** 获取集合的最后一个元素 */
    public static <T> T last(Collection<T> collection) {
        if (isEmpty(collection)) {return null;}

        // 当类型为 List 时, 直接取得最后一个元素
        if (collection instanceof List) {
            List<T> list = (List<T>) collection;
            return list.get(list.size() - 1);
        }
        // 其他类型通过 iterator 滚动到最后一个元素
        Iterator<T> iterator = collection.iterator();
        while (true) {
            T current = iterator.next();
            if (!iterator.hasNext()) {
                return current;
            }
        }
    }

    /** 集合中随机返回一个 */
    @SuppressWarnings("unchecked")
    public static <T> T rand(Collection<T> source) {
        return isEmpty(source) ? null : (T) source.toArray()[RANDOM.nextInt(source.size())];
    }


}
