package com.yunji.titanrtx.auto.support;

import com.yunji.titanrtx.common.domain.auto.CommonStressDeploy;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.service.SceneOperatingCenterService;
import lombok.extern.slf4j.Slf4j;

/**
 * AutoStressUtils
 *
 * @author leihz
 * @since 2020-06-22 10:49 上午
 */
@Slf4j
public class AutoStressUtils {

    /**
     * 重新设置自动压测场景的请求次数.
     */
    public static long resetSceneRequestTotal(
            CommonStressDeploy deploy,
            SceneOperatingCenterService sceneService) {

        int continuousTime = deploy.getContinuousTime();

        if (continuousTime > 0) {
            RespMsg respMsg = sceneService.
                    updateSceneContinuousRequest(deploy.getSceneId(), continuousTime);

            return respMsg.isSuccess() ? (long) respMsg.getData() : -1L;
        }

        return -1L;
    }
}
