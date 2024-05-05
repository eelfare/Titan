package com.yunji.titanrtx.commander.support.flow;

import java.util.*;

public class AutoFlow implements Flow {

    private  Random random = new Random();

    @Override
    public List<String> doSelect(List<String> agents, long concurrent, long singleMachineConcurrent) {
        long size = concurrent / singleMachineConcurrent ;
        if (size * singleMachineConcurrent  < concurrent ){
            size = size + 1;
        }

        List<String> selectAgents = new ArrayList<>((int)size);

        while (selectAgents.size() != size){
            String selectedAgent = agents.get(random.nextInt(agents.size()));
            selectAgents.add(selectedAgent);
            agents.remove(selectedAgent);
        }
        return selectAgents;
    }


}
