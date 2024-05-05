package com.yunji.titanrtx.manager.service.http;

import com.github.pagehelper.PageInfo;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;

import java.util.List;

public interface LinkParamsService {

    List<Integer> selectAllIdByLinkId(Integer linkId);

    String selectParamRandom(Integer linkId);

    int deleteById(Integer id);

    int deleteAllByLinkId(Integer linkId);

    int insert(LinkParamsEntity paramsEntity);

    LinkParamsEntity findById(Integer id);

    int update(LinkParamsEntity paramsEntity);

    PageInfo<LinkParamsEntity> selectByLinkId(Integer linkId, Integer currentPage);


    void batchInsertList(List<LinkParamsEntity> paramsEntities);


    int findTotalRecordsByLinkId(int linkId);

    boolean checkParamIsOrder(int linkId, int lastCount);

    boolean checkNullParamsOrder(int linkId, int lastCount, List<Integer> nullParamIds);

    List<Integer> selectLinkIdsOfParam();
}
