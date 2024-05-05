package com.yunji.titanrtx.plugin.monitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractExecute<E> {

    protected static final String CVM_NAME_SPACE="QCE/CVM";

    protected static final String ECS_PROJECT_NAME = "acs_ecs_dashboard";

    private Execute<E> execute;

    public AbstractExecute(Execute<E> execute) {
        this.execute = execute;
    }


    public List<MonitorBo> execute(E metric, String projectName, int PERIOD, String id, String start, String end) throws Exception {
        return execute.doExecute(metric,projectName,PERIOD,id,start,end);
    }

    public Map<String, List<MonitorBo>> execute(E metric, String projectName, int PERIOD , Set<String> ids, String start, String end) throws Exception {
        return execute.doExecute(metric,projectName,PERIOD,ids,start,end);
    }
}
