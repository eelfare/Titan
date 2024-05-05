package com.yunji.titanrtx.manager.service.http;

import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkStatusEntity;

import java.util.List;

public interface LinkService {

    List<LinkEntity> selectAll();

    List<LinkStatusEntity> selectAllOfId();

    int deleteById(Integer id);

    int insert(LinkEntity link);

    LinkEntity findById(Integer id);

    LinkEntity selectByUrl(String url);

    int update(LinkEntity link);

    List<LinkEntity> selectByIds(List<String> ids);

    List<LinkEntity> searchLinks(String key);

    int count();

    int updateParamOrderStatus(int id, int status);

    /**
     * 对给定的链路的所有参数进行测试,返回 errorCode 不为 0 的链路的 id list ","分隔.
     */
    String collectBadParamIds(int linkId, boolean delete);

    List<Integer> selectLinkIds();
}
