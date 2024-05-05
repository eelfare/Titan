package com.yunji.titanrtx.manager.dao.bos.top;

import com.yunji.titanrtx.manager.dao.entity.auto.BlackGroupEntity;
import com.yunji.titanrtx.manager.dao.entity.auto.FilterEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-06 15:52
 * @Version 1.0
 */
@Data
public class BlackGroupBo {
    private BlackGroupEntity blackGroupEntity;
    private List<FilterEntity> list;
}
