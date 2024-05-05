package com.yunji.titanrtx.plugin.monitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Execute<E> {

    String INSTANCE_ID = "instanceId";

    List<MonitorBo> doExecute(E metric, String project, int period , String id, String start, String end) throws Exception;

    Map<String, List<MonitorBo>> doExecute(E metric, String project, int period , Set<String> ids, String start, String end) throws Exception;

}
