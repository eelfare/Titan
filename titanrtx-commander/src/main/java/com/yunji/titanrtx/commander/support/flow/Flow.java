package com.yunji.titanrtx.commander.support.flow;

import java.util.List;

public interface Flow {

    List<String>  doSelect(List<String> agents, long concurrent, long singleMachineConcurrent);

    static List<String> doSelect(com.yunji.titanrtx.common.enums.Flow flowType, List<String> agents, long concurrent, long singleMachineConcurrent){
        if (concurrent < agents.size() || com.yunji.titanrtx.common.enums.Flow.AUTO == flowType){
            return new AutoFlow().doSelect(agents,concurrent,singleMachineConcurrent);
        }
        return new AverageFlow().doSelect(agents,concurrent,singleMachineConcurrent);
    }

}
