package com.yunji.titanrtx.manager.dao.bos.dubbo;

import com.yunji.titanrtx.manager.dao.bos.SummaryStatistics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class SummaryDubboBo extends SummaryStatistics {

    private long Http200Code;

    private String Http200CodeRate;

    private long HttpOtherCode;

    private String HttpOtherCodeRate;

    private long business0Code;

    private String business0CodeRate;

    private long businessOtherCode;

    private String businessOtherCodeRate;

    private List<DubboServiceBo> serviceBos;

}
