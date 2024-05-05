package com.yunji.titanrtx.agent.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceParamsMapper {

    String selectParamRandom(Integer serviceId);

    List<String> selectParamsBatch(List<Integer> ids);


    List<String> selectParamsByRange(@Param("serviceId") int serviceId,
                                     @Param("start") int start,
                                     @Param("end") int end);


    List<String> selectParamsByLinkId(Integer serviceId);
}
