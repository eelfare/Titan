package com.yunji.titanrtx.manager.dao.bos.http;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AgentInfoBo implements Serializable {

    private List<String> total;

    private List<String>  running;

    private List<String>  available;

    private List<String>  disable;


    public AgentInfoBo(List<String> total, List<String> running, List<String> available, List<String> disable) {
        this.total = total;
        this.running = running;
        this.available = available;
        this.disable = disable;
    }
}
