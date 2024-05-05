package com.yunji.titanrtx.manager.service.system;

import com.yunji.titanrtx.manager.dao.bos.system.UserBo;
import com.yunji.titanrtx.manager.dao.entity.system.UserEntity;

import java.util.List;

public interface UserService {

    int insert(UserEntity userEntity);

    int update(UserEntity userEntity);

    int increaseLogTimes(UserEntity userEntity);

     UserEntity findUserByPhone(String phone);

    int modifyPw(int id, String newPassWord);

    List<UserEntity> selectAll();

    UserEntity findById(Integer id);

    int disable(Integer id);

    int enable(Integer id);

    UserBo userInfo(Integer id);
}
