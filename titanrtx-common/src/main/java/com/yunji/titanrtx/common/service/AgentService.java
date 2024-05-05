package com.yunji.titanrtx.common.service;

import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.task.*;

import java.io.IOException;
import java.util.List;

public interface AgentService extends Start,Stop,Progress,Able,Restart{

    List<String> hosts() ;

    RespMsg modifyHosts(String content);
}
