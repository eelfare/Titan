package com.yunji.titanrtx.manager.service.auto;

import com.yunji.titanrtx.manager.dao.entity.auto.FilterEntity;

import java.util.List;

/**
 * 白名单
 * @Author: 景风（彭秋雁）
 * @Date: 2019-11-27 22:44
 * @Version 1.0
 */
public interface WhiteListService {
    List<FilterEntity> selectAll();

    int deleteById(Integer id);

    int insert(FilterEntity filterEntity);

    FilterEntity findById(Integer id);

    int update(FilterEntity filterEntity);
}
