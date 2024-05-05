package com.yunji.titanrtx.manager.dao.bos.http;

import com.github.pagehelper.PageInfo;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import lombok.Data;

@Data
public class LinkBo {

    private LinkEntity link;

    private PageInfo<LinkParamsEntity> params;

}
