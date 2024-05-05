package com.yunji.titanrtx.agent.service.impl;

import com.yunji.titanrtx.agent.mapper.ServiceParamsMapper;
import com.yunji.titanrtx.agent.service.ParamsService;
import com.yunji.titanrtx.common.domain.task.ParamRange;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 5/3/2020 4:14 下午
 * @Version 1.0
 */
@Service("dubbo")
public class ServiceParamsServiceImpl implements ParamsService {
    @Resource
    ServiceParamsMapper mapper;

    @Override
    public List<String> selectParamsByLinkId(Integer serviceId) {
        return mapper.selectParamsByLinkId(serviceId);
    }

    @Override
    public List<String> selectParamsBatch(List<Integer> ids) {
        return mapper.selectParamsBatch(ids);
    }

    @Override
    public List<String> selectParamsByRange(int serviceId, ParamRange range) {
        List<String> res = mapper
                .selectParamsByRange(
                        serviceId,
                        range.getStart(),
                        range.getEnd()
                );

        if (range.getStart() == 0) {
            String param = mapper.selectParamRandom(serviceId);
            res.add(param);
        }

        return res;

    }
}
