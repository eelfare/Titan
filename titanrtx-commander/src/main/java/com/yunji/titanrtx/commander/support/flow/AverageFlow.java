package com.yunji.titanrtx.commander.support.flow;

import java.util.List;

public class AverageFlow implements Flow {

    @Override
    public List<String> doSelect(List<String> agents, long concurrent, long singleMachineConcurrent) {
        return agents;
    }
}
