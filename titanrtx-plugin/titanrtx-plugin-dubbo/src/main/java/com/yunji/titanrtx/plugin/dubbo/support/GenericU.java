package com.yunji.titanrtx.plugin.dubbo.support;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GenericU {

    public static final String DUBBO_STRESS_TAG = "N/A";

    private static final ScheduledExecutorService es = Executors.newScheduledThreadPool(1);


    private static final Map<String, String> primitiveClassNameMap = new HashMap<>();
    /**
     * parameter type cache
     */
    private static final Map<String, String[]> parameterTypeMap = new ConcurrentHashMap<>();
    /**
     * parameter value cache
     */
    private static final Map<String, Object[]> parameterValueMap = new ConcurrentHashMap<>();

    static {
        primitiveClassNameMap.put("int", int.class.getName());
        primitiveClassNameMap.put("int[]", int[].class.getName());
        primitiveClassNameMap.put("integer", Integer.class.getName());
        primitiveClassNameMap.put("Integer", Integer.class.getName());
        primitiveClassNameMap.put("Integer[]", Integer[].class.getName());

        primitiveClassNameMap.put("short", short.class.getName());
        primitiveClassNameMap.put("short[]", short[].class.getName());
        primitiveClassNameMap.put("Short", Short.class.getName());
        primitiveClassNameMap.put("Short[]", Short[].class.getName());

        primitiveClassNameMap.put("long", long.class.getName());
        primitiveClassNameMap.put("long[]", long[].class.getName());
        primitiveClassNameMap.put("Long", Long.class.getName());
        primitiveClassNameMap.put("Long[]", Long[].class.getName());

        primitiveClassNameMap.put("byte", byte.class.getName());
        primitiveClassNameMap.put("byte[]", byte[].class.getName());
        primitiveClassNameMap.put("Byte", Byte.class.getName());
        primitiveClassNameMap.put("Byte[]", Byte[].class.getName());

        primitiveClassNameMap.put("float", float.class.getName());
        primitiveClassNameMap.put("float[]", float[].class.getName());
        primitiveClassNameMap.put("Float", Float.class.getName());
        primitiveClassNameMap.put("Float[]", Float[].class.getName());

        primitiveClassNameMap.put("double", double.class.getName());
        primitiveClassNameMap.put("double[]", double[].class.getName());
        primitiveClassNameMap.put("Double", Double.class.getName());
        primitiveClassNameMap.put("Double[]", Double[].class.getName());

        primitiveClassNameMap.put("char", char.class.getName());
        primitiveClassNameMap.put("char[]", char[].class.getName());
        primitiveClassNameMap.put("character", Character.class.getName());
        primitiveClassNameMap.put("character[]", Character[].class.getName());
        primitiveClassNameMap.put("Character", Character.class.getName());
        primitiveClassNameMap.put("Character[]", Character[].class.getName());

        primitiveClassNameMap.put("boolean", boolean.class.getName());
        primitiveClassNameMap.put("boolean[]", boolean[].class.getName());
        primitiveClassNameMap.put("Boolean", Boolean.class.getName());
        primitiveClassNameMap.put("Boolean[]", Boolean[].class.getName());

        primitiveClassNameMap.put("string", String.class.getName());
        primitiveClassNameMap.put("string[]", String[].class.getName());
        primitiveClassNameMap.put("String", String.class.getName());
        primitiveClassNameMap.put("String[]", String[].class.getName());

        primitiveClassNameMap.put("list", List.class.getName());
        primitiveClassNameMap.put("java.util.list", List.class.getName());
        primitiveClassNameMap.put("map", Map.class.getName());
        primitiveClassNameMap.put("java.util.map", Map.class.getName());
        primitiveClassNameMap.put("set", Set.class.getName());
        primitiveClassNameMap.put("java.util.set", Set.class.getName());
        primitiveClassNameMap.put("queue", Queue.class.getName());
        primitiveClassNameMap.put("java.util.queue", Queue.class.getName());

        es.scheduleWithFixedDelay(() -> {
            if (parameterTypeMap.size() > 20000) {
                log.info("GenericU:Clear parameterTypeMap when it size greater than 20000.");
                parameterTypeMap.clear();
            }
            if (parameterValueMap.size() > 100000) {
                log.info("GenericU:Clear parameterValueMap when it size greater than 100000.");
                parameterValueMap.clear();
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    private static String primitiveClassName(String type) {
        String trimType = type.trim();
        return primitiveClassNameMap.getOrDefault(trimType.toLowerCase(), trimType);
    }

    private static String[] buildParamsType(String paramsType) {
        if (StringUtils.isEmpty(paramsType)) {
            return new String[0];
        }
        String[] types = paramsType.split(",");
        String[] realType = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            realType[i] = primitiveClassName(types[i]);
        }
        return realType;
    }


    private static Object[] buildParamsValue(String[] paramsType, String originalValue) throws ClassNotFoundException {
        if (paramsType.length == 0 || StringUtils.isEmpty(originalValue)) {
            return new Object[0];
        }
        String[] values = originalValue.split("&");
        if (values.length != paramsType.length) {
            values = originalValue.split("&&");
        }
        Object[] realValue = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            realValue[i] = buildParams(paramsType[i], values[i].trim());
        }
        return realValue;
    }


    public static Object buildParams(String typeName, String originalValue) throws ClassNotFoundException {

        if (originalValue.startsWith("[") && originalValue.endsWith("]")) {
            /*
             * 如果找不到当前传入的类，则为POJO数组类型
             */
            List<String> listParams = JSONArray.parseArray(originalValue, String.class);
            List<Object> resultParams = new ArrayList<>();
            for (String lp : listParams) {
                //POJO
                if (lp.startsWith("{") && lp.endsWith("}")) {
                    resultParams.add(JSONObject.parseObject(lp));
                } else {
                    resultParams.add(lp);
                }
            }
            Class<?> clazz = Class.forName(typeName);
            if (clazz.isArray()) {
                return buildPrimitiveArray(resultParams, clazz);
            }
            return resultParams;
        }
        if (originalValue.startsWith("{") && originalValue.endsWith("}")) {
            return buildCollectMap(originalValue);
        }
        return originalValue;
    }

    private static Object buildCollectMap(String originalValue) {
        return JSONObject.parseObject(originalValue);
    }

    private static Object buildPrimitiveArray(List<Object> listParams, Class<?> clazz) {
        Class<?> componentType = clazz.getComponentType();
        Object targetArrays = Array.newInstance(componentType, listParams.size());
        for (int i = 0; i < listParams.size(); i++) {
            if (StringUtils.equalsIgnoreCase(componentType.getName(), "int")) {
                Array.setInt(targetArrays, i, Integer.valueOf((String) listParams.get(i)));
            } else if (StringUtils.equalsIgnoreCase(componentType.getName(), "short")) {
                Array.setShort(targetArrays, i, Short.valueOf((String) listParams.get(i)));
            } else if (StringUtils.equalsIgnoreCase(componentType.getName(), "long")) {
                Array.setLong(targetArrays, i, Long.valueOf((String) listParams.get(i)));
            } else if (StringUtils.equalsIgnoreCase(componentType.getName(), "byte")) {
                Array.setByte(targetArrays, i, Byte.valueOf((String) listParams.get(i)));
            } else if (StringUtils.equalsIgnoreCase(componentType.getName(), "float")) {
                Array.setFloat(targetArrays, i, Float.valueOf((String) listParams.get(i)));
            } else if (StringUtils.equalsIgnoreCase(componentType.getName(), "double")) {
                Array.setDouble(targetArrays, i, Double.valueOf((String) listParams.get(i)));
            } else if (StringUtils.equalsIgnoreCase(componentType.getName(), "char")) {
                Array.setChar(targetArrays, i, ((String) listParams.get(i)).charAt(0));
            } else if (StringUtils.equalsIgnoreCase(componentType.getName(), "boolean")) {
                Array.setBoolean(targetArrays, i, Boolean.valueOf((String) listParams.get(i)));
            } else if (StringUtils.equalsIgnoreCase(componentType.getName(), "string")) {
                Array.set(targetArrays, i, listParams.get(i));
            }
        }
        return targetArrays;
    }


    public static RpcRequest newBuild(String serviceName, String methodName,
                                      String paramType, String params,
                                      String address, String rpcContent,
                                      String clusterAddressStr) throws ClassNotFoundException {

        String[] paramsType = GenericU.buildParamsType(paramType);

        boolean isCluster = false;
        if (DUBBO_STRESS_TAG.equals(address)) {
            isCluster = true;
        }
        List<String> specificAddress = stringToList(address);
        List<String> clusterAddress = stringToList(clusterAddressStr);

        return RpcRequest.builder()
                .serviceName(serviceName)
                .address(specificAddress)
                .clusterAddress(clusterAddress)
                .cluster(isCluster)
                .method(methodName)
                .paramsType(paramsType)
                .paramsValue(GenericU.buildParamsValue(paramsType, params))
                .rpcContent(rpcContent)
                .build();
    }


    /*public static RpcRequest builder(String serviceName, String methodName,
                                     String paramType, String params,
                                     String address, String rpcContent,
                                     List<String> clusterAddress) throws ClassNotFoundException {
        String[] paramsType = GenericU.buildParamsType(paramType);

        return RpcRequest.builder()
                .serviceName(serviceName)
                .address(address)
                .method(methodName)
                .paramsType(paramsType)
                .paramsValue(GenericU.buildParamsValue(paramsType, params))
                .rpcContent(rpcContent)
                .clusterAddress(clusterAddress)
                .build();
    }*/


    private static List<String> stringToList(String providerAddress) {
        return providerAddress != null ?
                Splitter.on(",").omitEmptyStrings().splitToList(providerAddress) : new ArrayList<>();
    }


}
