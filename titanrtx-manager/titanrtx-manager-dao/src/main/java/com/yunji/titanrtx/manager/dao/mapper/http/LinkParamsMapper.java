package com.yunji.titanrtx.manager.dao.mapper.http;

import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 需要对 linkParams 表考虑分表,分为 1-10 10个表.
 * 7
 */
@Mapper
public interface LinkParamsMapper {

    List<Integer> selectAllIdByLinkId(Integer linkId);

    String selectParamRandom(Integer linkId);

    int deleteById(Integer id);

    Integer insert(LinkParamsEntity paramsEntity);

    void batchInsertList(@Param("paramsEntities") List<LinkParamsEntity> paramsEntities);

    LinkParamsEntity findById(Integer id);

    int update(LinkParamsEntity link);

    List<LinkParamsEntity> selectByLinkId(Integer linkId);

    int deleteAllByLinkId(Integer linkId);

    int findTotalRecordsByLinkId(int linkId);

    Integer findMaxIdByLinkId(int linkId);

    int findOrdersById(int id);

    List<Integer> selectLinkIdsOfParam();

    List<String> selectParamsByRange(@Param("linkId") int linkId,
                                     @Param("start") int start,
                                     @Param("end") int end);
}
