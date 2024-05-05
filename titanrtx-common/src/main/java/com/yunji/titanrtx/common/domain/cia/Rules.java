package com.yunji.titanrtx.common.domain.cia;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.enums.RulesType;
import lombok.Data;

import java.io.Serializable;

@Data
public class Rules implements Serializable {

    private Integer id;

    private String domain = GlobalConstants.ALL;

    private String path  = GlobalConstants.ALL;

    private String param = GlobalConstants.ALL;

    private RulesType rulesType = RulesType.FILTER;

}
