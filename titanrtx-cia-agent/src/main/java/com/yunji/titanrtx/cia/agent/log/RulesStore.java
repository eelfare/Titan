package com.yunji.titanrtx.cia.agent.log;

import com.yunji.titanrtx.common.domain.cia.Rules;
import com.yunji.titanrtx.common.enums.RulesType;

import java.util.List;

public interface RulesStore {


    List<Rules> rules(RulesType type);

}
