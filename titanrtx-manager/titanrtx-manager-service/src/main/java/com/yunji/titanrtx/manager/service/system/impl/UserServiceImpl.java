package com.yunji.titanrtx.manager.service.system.impl;

import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.system.UserBo;
import com.yunji.titanrtx.manager.dao.entity.system.PathEntity;
import com.yunji.titanrtx.manager.dao.entity.system.UserEntity;
import com.yunji.titanrtx.manager.dao.mapper.system.UserMapper;
import com.yunji.titanrtx.manager.service.system.PathService;
import com.yunji.titanrtx.manager.service.system.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PathService pathService;

    @Override
    public int insert(UserEntity userEntity) {
        return userMapper.insert(userEntity);
    }

    @Override
    public int update(UserEntity userEntity) {
        return userMapper.update(userEntity);
    }

    @Override
    public int increaseLogTimes(UserEntity userEntity) {
        return userMapper.increaseLogTimes(userEntity);
    }

    @Override
    public UserEntity findUserByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    @Override
    public int modifyPw(int id, String newPassWord) {
        return userMapper.updateUserPW(id,newPassWord);
    }

    @Override
    public List<UserEntity> selectAll() {
        return userMapper.selectAll();
    }

    @Override
    public UserEntity findById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public int disable(Integer id) {
        return userMapper.disable(id);
    }

    @Override
    public int enable(Integer id) {
        return userMapper.enable(id);
    }

    @Override
    public UserBo userInfo(Integer id) {
        UserEntity user = findById(id);
        List<String> pathIds = CommonU.stringJoinToList(user.getPathIds());
        List<PathEntity> hashPermissions = new ArrayList<>();
        List<PathEntity>  extraPermissions= new ArrayList<>();
        List<PathEntity> paths = pathService.selectAll();
        for (PathEntity pe : paths){

            String pathId = String.valueOf(pe.getId());
            if (pathIds.contains(pathId)){
                hashPermissions.add(pe);
            }else{
                extraPermissions.add(pe);
            }
        }
        UserBo bo = new UserBo();
        bo.setUser(user);
        bo.setHasPermission(hashPermissions);
        bo.setExtraPermissions(extraPermissions);
        return bo;
    }


}
