package com.yunji.titanrtx.plugin.monitor.ali;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.cms.model.v20170301.QueryMetricListRequest;
import com.aliyuncs.cms.model.v20170301.QueryMetricListResponse;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.yunji.titanrtx.plugin.monitor.Execute;
import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import com.yunji.titanrtx.plugin.monitor.enums.ECS;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ECSExecute implements Execute<ECS> {

    private  IAcsClient client;

    ECSExecute(String regionId, String accessKeyId, String secret) {
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, secret);
        this.client = new DefaultAcsClient(profile);
    }


    @Override
    public List<MonitorBo> doExecute(ECS metric, String project, int period , String id, String start, String end) throws Exception {
        QueryMetricListRequest request = new QueryMetricListRequest();
        request.setProject(project);
        request.setMetric(metric.toString());
        request.setPeriod(String.valueOf(period));
        request.setStartTime(start);
        request.setEndTime(end);
        JSONObject dim = new JSONObject();
        dim.put(INSTANCE_ID, id);
        request.setDimensions(dim.toJSONString());
        request.setAcceptFormat(FormatType.JSON);
        QueryMetricListResponse response = client.getAcsResponse(request);
        return JSON.parseArray(response.getDatapoints(),MonitorBo.class);
    }

    @Override
    public Map<String, List<MonitorBo>> doExecute(ECS metric, String project, int period, Set<String> ids, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

}
