package com.yunji.titanrtx.manager.dao.mapper.auto;

import com.yunji.titanrtx.manager.dao.entity.auto.FilterEntity;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-11-27 22:49
 * @Version 1.0
 */
public interface FilterMapper {
    List<FilterEntity> selectAll();

    int deleteById(Integer id);

    int insert(FilterEntity filterEntity);

    FilterEntity findById(Integer id);

    int update(FilterEntity filterEntity);

    List<FilterEntity> findByGroupId(Integer groupId);
}
