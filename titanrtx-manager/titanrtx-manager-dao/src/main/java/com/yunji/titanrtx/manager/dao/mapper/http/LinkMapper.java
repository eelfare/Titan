package com.yunji.titanrtx.manager.dao.mapper.http;

import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkStatusEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LinkMapper {

    List<LinkEntity> selectAll();

    List<LinkStatusEntity> selectAllOfId();

    int deleteById(Integer id);

    Integer insert(LinkEntity link);

    LinkEntity findById(Integer id);

    LinkEntity selectByUrl(String url);

    int update(LinkEntity link);

    List<LinkEntity> selectByIds(List<String> ids);

    LinkEntity selectById(int id);

    List<LinkEntity> searchLinks(String key);

    int count();

    int updateParamOrderStatus(@Param("id") int id, @Param("status") int status);

    List<Integer> selectLinkIds();
}
