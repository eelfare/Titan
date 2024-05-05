package com.yunji.titanrtx.manager.service.dubbo.impl;

import com.yunji.titanrtx.common.domain.task.*;
import com.yunji.titanrtx.common.enums.Allot;
import com.yunji.titanrtx.common.enums.ParamTransmit;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import com.yunji.titanrtx.manager.service.OutService;
import com.yunji.titanrtx.manager.service.TaskService;
import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.manager.service.dubbo.*;
import com.yunji.titanrtx.manager.service.support.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * DubboStressServiceImpl
 *
 * @author leihz
 * @since 2020-07-02 4:08 下午
 */
@Slf4j
@Service
public class DubboStressServiceImpl implements DubboStressService {

    @Resource
    private DubboServiceService dubboService;
    @Resource
    private ServiceParamsService paramsService;
    @Resource
    private DubboSceneService sceneService;

    @Resource
    private TaskService taskService;

    @Resource
    private SystemProperties systemProps;

    @Resource
    private OutService outService;


    @Override
    public RespMsg startPressure(int id) {
        long st = System.currentTimeMillis();
        try {
            DubboSceneEntity sceneEntity = sceneService.findById(id);
            if (sceneEntity.getStatus() != 0) {
                return RespMsg.respErr("当前场景正在压测中");
            }
            //场景中的各个链路.
            List<ServiceEntity> serviceEntities = getServiceEntities(sceneEntity);
            //更新dubbo节点地址信息
            for (ServiceEntity entity : serviceEntities) {
                filledAddressToServiceEntity(entity);
            }

            List<Bullet> dubboBullets = taskService.buildBullet(serviceEntities, Allot.WEIGHT, DubboService.class);
            //Build params 判断links 参数排序是否已ok.
            ParamTransmit paramTransmit = systemProps.paramTransmit;
            sceneEntity.setParamTransmit(paramTransmit);

            //顺序的情况
            if (paramTransmit == ParamTransmit.ORDERS) {
                buildParamsOfOrders(dubboBullets);
            } else {
                throw new IllegalArgumentException("Dubbo param transmit only support  PARAM_ORDERS way.");
            }

            RespMsg respMsg = taskService.doStart(sceneEntity, dubboBullets, systemProps.dubboConcurrent, TaskType.DUBBO);


            StringBuilder sb = new StringBuilder();
            sb.append("DUBBO场景[").append(sceneEntity.getName()).append("]")
                    .append("taskNo: ").append(respMsg.getMsg())
                    .append(", Transmit way: ").append(paramTransmit)
                    .append(respMsg.isSuccess() ? ",启动成功" : "启动失败.")
                    .append("耗时:").append((System.currentTimeMillis() - st)).append("ms.........");

            log.info(sb.toString());

            if (respMsg.isSuccess()) {
                String taskNo = respMsg.getMsg();
                /*screenService.schedulePerMinAfterDelayScreenshot(taskNo);*/
                respMsg.setMsg("执行[" + respMsg.getMsg() + "]成功");
                sceneService.updateStatus(id, 1);
            }

            return respMsg;
        } catch (Exception e) {
            log.error("Dubbo Scene 压测启动失败: {}", e.getMessage());
            return RespMsg.respErr("压测失败: " + e.getMessage());
        }
    }

    @Override
    public Object reportSceneResult(int id, boolean onlyShowError, boolean deleteBadLinkParam) {
        return null;
    }

    private void filledAddressToServiceEntity(ServiceEntity entity) {
        String providerAddress = outService.getProviderAddress(entity.getApplicationName(), entity.getServiceName());
        if (providerAddress != null) {
            entity.setClusterAddress(providerAddress);
        }
    }


    private List<ServiceEntity> getServiceEntities(DubboSceneEntity sceneEntity) {
        List<String> ids = CommonU.parseIds(sceneEntity.getIdsWeight());
        Map<Integer, Integer> idWeightMap = CommonU.parseIdWeightMap(sceneEntity.getIdsWeight());
        List<ServiceEntity> entities = dubboService.selectByIds(ids);
        return fillIdsWeight(entities, idWeightMap);
    }

    private List<ServiceEntity> fillIdsWeight(List<ServiceEntity> serviceEntities, Map<Integer, Integer> idsWeightMap) {
        for (ServiceEntity se : serviceEntities) {
            se.setWeight(idsWeightMap.get(se.getId()));
        }
        return serviceEntities;
    }

    /**
     * 每一条链路的参数有一个orders标志，并根据 1--n 排好了序,
     */
    private void buildParamsOfOrders(List<Bullet> bullets) {
        for (Bullet bullet : bullets) {
            Integer id = bullet.getId();
            //只是查询了参数的id.
            int totalRecords = paramsService.findTotalRecordsByServiceId(id);
            //检查
            boolean check = paramsService.checkParamIsOrder(id, totalRecords);
            if (!check) {
                throw new RuntimeException("Dubbo链路(" + id + ") 的参数顺序没有从1到n排序完成,请检查.");
            }
            //为什么+1?因为右边区间不包含.
            bullet.setParamRange(new ParamRange(1, totalRecords + 1));
        }
        log.info("[DUBBO链路]: 设置每条链路的ParamRange完成,链路总数:{}", bullets.size());
    }
}
