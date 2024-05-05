package com.yunji.titanrtx.manager.service.http.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;
import com.yunji.titanrtx.manager.dao.mapper.http.LinkParamsMapper;
import com.yunji.titanrtx.manager.service.http.LinkParamsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
@Slf4j
public class LinkParamsServiceImpl implements LinkParamsService {

    @Resource
    private LinkParamsMapper linkParamsMapper;

    @Override
    public List<Integer> selectAllIdByLinkId(Integer linkId) {
        return linkParamsMapper.selectAllIdByLinkId(linkId);
    }

    @Override
    public String selectParamRandom(Integer linkId) {
        return linkParamsMapper.selectParamRandom(linkId);
    }


    @Override
    public int deleteById(Integer id) {
        return linkParamsMapper.deleteById(id);
    }

    @Override
    public int deleteAllByLinkId(Integer linkId) {
        return linkParamsMapper.deleteAllByLinkId(linkId);
    }

    @Override
    public int insert(LinkParamsEntity paramsEntity) {
        return linkParamsMapper.insert(paramsEntity);
    }

    @Override
    public LinkParamsEntity findById(Integer id) {
        return linkParamsMapper.findById(id);
    }

    @Override
    public int update(LinkParamsEntity paramsEntity) {
        return linkParamsMapper.update(paramsEntity);
    }

    @Override
    public PageInfo<LinkParamsEntity> selectByLinkId(Integer linkId, Integer currentPage) {
        PageHelper.startPage(currentPage, GlobalConstants.GLOBAL_PARAMS_PAGE_SIZE);
        return new PageInfo<>(linkParamsMapper.selectByLinkId(linkId));
    }

    @Override
    public void batchInsertList(List<LinkParamsEntity> paramsEntities) {
        linkParamsMapper.batchInsertList(paramsEntities);
    }

    @Override
    public int findTotalRecordsByLinkId(int linkId) {
        return linkParamsMapper.findTotalRecordsByLinkId(linkId);
    }

    /**
     * 判读当前linkId链路的参数是否是顺序的.
     */
    @Override
    public boolean checkParamIsOrder(int linkId, int lastCount) {
        Integer maxId = linkParamsMapper.findMaxIdByLinkId(linkId);
        if (maxId == null) {
            log.warn("链路{} 的参数为空,忽略排序.", linkId);
            return true;
        }
        int orders = linkParamsMapper.findOrdersById(maxId);

        if (orders == lastCount) {
            return true;
        }
        log.warn("链路{},maxId:{},对应orders:{}, lastCount:{} 不相等，检测失败.",
                linkId, maxId, orders, lastCount);
        return false;
    }

    @Override
    public boolean checkNullParamsOrder(int linkId, int lastCount, List<Integer> nullParamIds) {
        Integer maxId = linkParamsMapper.findMaxIdByLinkId(linkId);
        if (maxId == null) {
            nullParamIds.add(linkId);
            return true;
        }
        int orders = linkParamsMapper.findOrdersById(maxId);
        if (orders == lastCount) {
            return true;
        }
        log.warn("链路{},maxId:{},对应orders:{}, lastCount:{} 不相等，检测失败.",
                linkId, maxId, orders, lastCount);
        return false;
    }

    @Override
    public List<Integer> selectLinkIdsOfParam() {
        return linkParamsMapper.selectLinkIdsOfParam();
    }

}
