package com.yunji.titanrtx.agent.service;

import com.yunji.titanrtx.common.domain.task.ParamRange;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 5/3/2020 4:16 下午
 * @Version 1.0
 */
public interface ParamsService {

    List<String> selectParamsByLinkId(Integer linkId);

    List<String> selectParamsBatch(List<Integer> ids);

    List<String> selectParamsByRange(int linkId, ParamRange range);
}
