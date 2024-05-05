package com.yunji.titanrtx.manager.dao.bos;

import lombok.Data;

@Data
public class UserSessionBo {
    private String phone;

    private String userName;

    private boolean root;

}
