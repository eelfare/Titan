package com.yunji.titanrtx.manager.dao.entity.system;


import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserEntity extends BaseEntity {
    /**
     * 工号
     */
    private String userName;
    /**
     * 姓名
     */
    private String nickName;

    private String phone;

    private String passWord;

    private long loginTimes;

    private String pathIds;

}
