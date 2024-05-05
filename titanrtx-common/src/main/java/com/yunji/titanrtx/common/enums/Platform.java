package com.yunji.titanrtx.common.enums;

import com.yunji.titanrtx.bash.support.CatShell;
import com.yunji.titanrtx.bash.support.CurlShell;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.u.CommonU;

import java.io.IOException;

public enum Platform {


    /**
     * 腾讯云实例ID 通过curl http://metadata.tencentyun.com/latest/meta-data/instance-id 方式获取 太low了  麻蛋
     */
    TENCENT{
        @Override
        public String getInstanceId() {
            if (CommonU.istLinux()){
                return GlobalConstants.DEFAULT_VCM_ID;
            }
            String id;
            try {
                id = new CurlShell(null, GlobalConstants.CVM_ID_PATH).execCommand();
                if (id != null) {
                    String[] array = id.split(" ");
                    id = array[0];
                }
            } catch (IOException e) {
                id = GlobalConstants.DEFAULT_VCM_ID;
                e.printStackTrace();
            }
            return id;
        }
    },

    /**
     * 阿里云实例ID  通过cat /etc/hostname
     */
    ALI{
        @Override
        public String getInstanceId() {
            if (CommonU.istLinux()){
                return GlobalConstants.DEFAULT_ECS_ID;
            }
            String id;
            try {
                String resp = new CatShell(null, GlobalConstants.ECS_ID_PATH).execCommand();
                String tag = resp.substring(0,1);
                String subId = resp.substring(2, resp.length() - 1);
                id = tag+"-"+subId;
            } catch (IOException e) {
                id = GlobalConstants.DEFAULT_ECS_ID;
                e.printStackTrace();
            }
            return id;
        }
    };

    public abstract String getInstanceId();
}
