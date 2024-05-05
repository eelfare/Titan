package com.yunji.titanrtx.manager.dao.mapper.system;

import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.dao.entity.system.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    UserEntity findByPhone(String phone);

    int updateUserPW(@Param("id") int id, @Param("newPw") String newPassWord);

    List<UserEntity> selectAll();

    int insert(UserEntity userEntity);

    int update(UserEntity userEntity);

    int increaseLogTimes(UserEntity userEntity);

    UserEntity findById(Integer id);

    int disable(Integer id);

    int enable(Integer id);
}
