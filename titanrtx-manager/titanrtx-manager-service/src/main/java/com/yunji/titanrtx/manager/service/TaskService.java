package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.enums.Allot;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.bos.http.AgentInfoBo;
import com.yunji.titanrtx.manager.dao.bos.http.SceneProgressBo;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;

import java.util.List;

public interface TaskService {

    boolean checkStart(long concurrent, long singleMachineConcurrent);

    AgentInfoBo agentInfoBo();

    List<String>  instanceIds();

    List<Bullet> buildBullet(List<? extends BaseEntity> entities, Allot allot,Class<? extends Bullet> clazz) throws Exception;

    RespMsg doStart(BaseEntity sceneEntity, List<Bullet> bulletEntity, int machineMaxConcurrent, TaskType taskType) throws InterruptedException;

    RespMsg doStop(Integer id, TaskType taskType);

    void restartAll();

    void reset();

    SceneProgressBo progress(Integer id, TaskType type);
}
