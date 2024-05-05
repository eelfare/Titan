package com.yunji.titanrtx.manager.dao.bos.http;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Data
public class HttpParamCaseBo {

    private LinkEntity link;

    private String requestParam;

    private String requestHeader;

    public void build() throws UnsupportedEncodingException{
        if (StringUtils.isNotEmpty(requestParam)){
            String reqParam ;
            if (requestParam.contains(GlobalConstants.PARAMS_HEADER_SEGMENT)){
                String[] paramsHeaderPair = requestParam.split(GlobalConstants.PARAMS_HEADER_SEGMENT,2);
                if (paramsHeaderPair.length == 2){
                    requestHeader=paramsHeaderPair[1];
                }
                reqParam = paramsHeaderPair[0];
            }else{
                reqParam =requestParam;
            }
            requestParam = URLDecoder.decode(reqParam,GlobalConstants.URL_DECODER);
        }
    }
}
