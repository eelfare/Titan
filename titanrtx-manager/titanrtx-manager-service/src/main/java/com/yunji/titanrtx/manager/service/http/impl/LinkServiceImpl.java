package com.yunji.titanrtx.manager.service.http.impl;

import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.HttpLink;
import com.yunji.titanrtx.common.enums.Allot;
import com.yunji.titanrtx.common.resp.RespCodeOperator;
import com.yunji.titanrtx.common.u.AHCHttpU;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkStatusEntity;
import com.yunji.titanrtx.manager.dao.mapper.http.LinkMapper;
import com.yunji.titanrtx.manager.dao.mapper.http.LinkParamsMapper;
import com.yunji.titanrtx.manager.service.http.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.yunji.titanrtx.common.message.ErrorMsg.*;

@Service
@Slf4j
public class LinkServiceImpl implements LinkService {


    @Resource
    private LinkMapper linkMapper;

    @Resource
    private LinkParamsMapper linkParamsMapper;


    @Override
    public List<LinkEntity> selectAll() {
        return linkMapper.selectAll();
    }

    @Override
    public List<LinkStatusEntity> selectAllOfId() {
        return linkMapper.selectAllOfId();
    }

    @Override
    public int deleteById(Integer id) {
        //linkParamsMapper.deleteAllByLinkId(id);
        return linkMapper.deleteById(id);
    }

    @Override
    public int insert(LinkEntity link) {
        return linkMapper.insert(link);
    }

    @Override
    public LinkEntity findById(Integer id) {
        return linkMapper.findById(id);
    }

    @Override
    public LinkEntity selectByUrl(String url) {
        return linkMapper.selectByUrl(url);
    }

    @Override
    public int update(LinkEntity link) {
        return linkMapper.update(link);
    }

    @Override
    public List<LinkEntity> selectByIds(List<String> ids) {
        return linkMapper.selectByIds(ids);
    }

    @Override
    public List<LinkEntity> searchLinks(String key) {
        return linkMapper.searchLinks(key);
    }

    @Override
    public int count() {
        return linkMapper.count();
    }

    @Override
    public int updateParamOrderStatus(int id, int status) {
        return linkMapper.updateParamOrderStatus(id, status);
    }

    @Override
    public String collectBadParamIds(int linkId, boolean delete) {
        LinkEntity linkEntity = linkMapper.selectById(linkId);

        List<LinkParamsEntity> linkParamsEntities = linkParamsMapper.selectByLinkId(linkId);

        HttpLink httpLink = new HttpLink();
        httpLink.setMethod(linkEntity.getMethod());
        httpLink.setProtocol(linkEntity.getProtocol());
        httpLink.setContentType(linkEntity.getContentType());
        httpLink.setCharset(linkEntity.getCharset());
        httpLink.setUrl(linkEntity.getUrl());

        List<String> badIds = new ArrayList<>();
        for (LinkParamsEntity paramsEntity : linkParamsEntities) {
            try {
                httpLink.setParams(new ArrayList<>());
                Request request = AHCHttpU.buildRequest(httpLink, paramsEntity.getParam());
                String result = AHCHttpU.executeRequest(request);

                Integer respCode = RespCodeOperator.getRespCode(result);
                if (respCode != 0) {
                    badIds.add(paramsEntity.getId() + "");
                }
            } catch (Exception e) {
                log.warn("Test linkId {},paramId:{}  got error:" + e.getMessage(), linkId, paramsEntity.getId());
                badIds.add(paramsEntity.getId() + "");
            }
        }
        if (badIds.isEmpty()) {
            return "NO bad params.";
        } else {
            if (delete) {
                log.info("Delete Bad param ids size:{},detail:{}", badIds.size(), badIds);
                for (String badId : badIds) {
                    linkParamsMapper.deleteById(Integer.parseInt(badId));
                }
            }
            return StringUtils.join(badIds, ",");
        }
    }

    @Override
    public List<Integer> selectLinkIds() {
        return linkMapper.selectLinkIds();
    }

    private List<Bullet> buildBullet(List<? extends BaseEntity> entities, Allot allot, Class<? extends Bullet> clazz) throws Exception {
        List<Bullet> bullets = new ArrayList<>(entities.size());
        if (allot == Allot.QPS) { // 如果是QPS，则需要重新计算权重
            // 获取最大值
            List<LinkEntity> list = (List<LinkEntity>) entities;
            long maxQps = list.stream().mapToLong(LinkEntity::getQps).max().getAsLong();
            for (LinkEntity e : list) {
                Bullet instance = clazz.newInstance();
                BeanUtils.copyProperties(e, instance);
                // 重新设置权重
                long newQps = (long) Math.ceil((100 * e.getQps()) / (maxQps * 1.0));
                instance.setWeight(newQps);
                bullets.add(instance);
            }
        } else {
            for (BaseEntity e : entities) {
                Bullet instance = clazz.newInstance();
                BeanUtils.copyProperties(e, instance);
                bullets.add(instance);
            }
        }

        return bullets;
    }

}
