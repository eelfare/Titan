package com.yunji.titanrtx.manager.dao.mapper.auto;

import com.yunji.titanrtx.manager.dao.entity.auto.BlackGroupEntity;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-11-27 22:49
 * @Version 1.0
 */
public interface BlackGroupMapper {
    List<BlackGroupEntity> selectAll();

    int deleteById(Integer id);

    int insert(BlackGroupEntity entity);

    BlackGroupEntity findById(Integer id);

    Boolean findByName(String name);

    int update(BlackGroupEntity entity);

    List<BlackGroupEntity> search(String key);
}
