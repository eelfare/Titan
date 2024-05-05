package com.yunji.titanrtx.manager.dao.bos.http;

import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HttpSceneBo implements Serializable {

    private HttpSceneEntity httpSceneEntity;

    private List<LinkEntity> links;

}
