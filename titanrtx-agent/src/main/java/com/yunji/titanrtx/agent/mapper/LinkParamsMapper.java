package com.yunji.titanrtx.agent.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LinkParamsMapper {

    List<String> selectParamsByLinkId(Integer linkId);


    String selectParamRandom(Integer linkId);

    List<String> selectParamsBatch(List<Integer> ids);

    List<String> selectParamsByRange(@Param("linkId") int linkId,
                                     @Param("start") int start,
                                     @Param("end") int end);

}
