package com.yunji.titanrtx.plugin.monitor.tencent;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.monitor.v20180724.MonitorClient;
import com.tencentcloudapi.monitor.v20180724.models.DataPoint;
import com.tencentcloudapi.monitor.v20180724.models.Dimension;
import com.tencentcloudapi.monitor.v20180724.models.GetMonitorDataRequest;
import com.tencentcloudapi.monitor.v20180724.models.GetMonitorDataResponse;
import com.yunji.titanrtx.plugin.monitor.Execute;
import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import com.yunji.titanrtx.plugin.monitor.enums.ECS;
import com.yunji.titanrtx.plugin.monitor.enums.VCM;

import java.math.BigDecimal;
import java.util.*;

public class VCMExecute implements Execute<VCM> {

    private MonitorClient client;


    public VCMExecute(String regionId, String accessKeyId, String secret) {
        this.client = new MonitorClient(new Credential(accessKeyId, secret), regionId);
    }


    @Override
    public List<MonitorBo> doExecute(VCM metric, String project, int period, String id, String start, String end) throws Exception {
        String params = String.format("{\"Namespace\":\"%s\",\"MetricName\":\"%s\",\"Period\":%d,\"StartTime\":\"%s\",\"EndTime\":\"%s\"," +
                "\"Instances\":[{\"Dimensions\":[{\"Name\":\"InstanceId\",\"Value\":\"%s\"}]}]}",project,metric.toString(), period, start, end, id);
        GetMonitorDataRequest req = GetMonitorDataRequest.fromJsonString(params, GetMonitorDataRequest.class);
        GetMonitorDataResponse resp = client.GetMonitorData(req);
        List<MonitorBo> bos = new ArrayList<>();
        DataPoint[] dataPoints = resp.getDataPoints();
        for (DataPoint dp : dataPoints){
            Long[] timestamps = dp.getTimestamps();
            Float[] values = dp.getValues();
            for (int i = 0 ; i < timestamps.length; i ++){
                MonitorBo bo = new MonitorBo();
                bo.setTimestamp(timestamps[i] * 1000);
                bo.setValue(new BigDecimal(String.valueOf(values[i])).setScale(2,BigDecimal.ROUND_UP).doubleValue());
                bos.add(bo);
            }
        }
        return bos;
    }

    @Override
    public Map<String, List<MonitorBo>> doExecute(VCM metric, String project, int period, Set<String> ids, String start, String end) throws Exception {
        Map<String, List<MonitorBo>> result = new HashMap<>();
        String format = "{\"Namespace\":\"%s\",\"MetricName\":\"%s\",\"Period\":%d,\"StartTime\":\"%s\",\"EndTime\":\"%s\",\"Instances\":[";
        List<Object> params = new ArrayList<Object>();
        params.add(project);
        params.add(metric.toString());
        params.add(period);
        params.add(start);
        params.add(end);
        int n = 0;
        for (String id : ids) {
            params.add(id);
            if (n > 0) {
                format += ",";
            }
            n++;
            format += "{\"Dimensions\":[{\"Name\":\"InstanceId\",\"Value\":\"%s\"}]}";
        }
        format += "]}";

        String str = String.format(format, params.toArray());
        GetMonitorDataRequest req = GetMonitorDataRequest.fromJsonString(str, GetMonitorDataRequest.class);
        GetMonitorDataResponse resp = client.GetMonitorData(req);
        DataPoint[] dataPoints = resp.getDataPoints();
        for (DataPoint dp : dataPoints){
            Dimension[] dimensions = dp.getDimensions();
            Dimension dimension = dimensions[0];
            String name = dimension.getName();
            Long[] timestamps = dp.getTimestamps();
            Float[] values = dp.getValues();
            List<MonitorBo> bos = new ArrayList<>();
            for (int i = 0 ; i < timestamps.length; i++){
                MonitorBo bo = new MonitorBo();
                bo.setTimestamp(timestamps[i] * 1000);
                bo.setValue(new BigDecimal(String.valueOf(values[i])).setScale(2,BigDecimal.ROUND_UP).doubleValue());
                bos.add(bo);
            }
            result.put(name, bos);
        }
        return result;
    }

}
