package com.yunji.titanrtx.manager.dao.bos.top;

import lombok.Data;

@Data
public class TopLinkAsFilterBo extends TopLinkBo {
    // 是否在黑名单
    private boolean blnBlack;
    // 是否在白名单
    private boolean blnWhite;
}