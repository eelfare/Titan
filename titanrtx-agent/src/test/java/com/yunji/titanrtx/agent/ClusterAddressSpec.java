package com.yunji.titanrtx.agent;

import com.google.common.base.Splitter;
import com.yunji.titanrtx.bash.support.PingShell;
import org.junit.Test;

import java.util.List;

/**
 * @author Denim.leihz 2019-11-14 10:38 AM
 */
public class ClusterAddressSpec {

    @Test
    public void testStringAddress() {
        String clusterAddress = "10.0.1.2:20981";
        String clusterAddress2 = "10.0.1.2:20980,10.0.1.3:20981,10.0.1.4:20982,10.0.1.5:20983";

        List<String> address = Splitter.on(",").omitEmptyStrings().splitToList(clusterAddress2);
        if (address.size() > 0) {

            System.out.println(address);

        }


    }
}
