package com.yunji.titanrtx.manager.dao.bos.http;

import lombok.Data;

@Data
public class OverviewInfoBo {

    private AgentInfoBo agentInfo;

    private int linkCount;

    private int sceneCount;

    private int reportCount;

}
