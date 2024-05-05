package com.yunji.titanrtx.common.domain.meta;

import com.yunji.titanrtx.common.domain.meta.AgentMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogAgentMeta extends AgentMeta {

    private String name;

    public LogAgentMeta(String name) {
        super();
        this.name = name;
    }

}
