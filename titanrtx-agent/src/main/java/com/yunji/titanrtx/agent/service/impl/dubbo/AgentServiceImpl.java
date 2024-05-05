package com.yunji.titanrtx.agent.service.impl.dubbo;

import com.google.common.io.Files;
import com.yunji.titanrtx.agent.service.LiaisonService;
import com.yunji.titanrtx.bash.support.CatShell;
import com.yunji.titanrtx.bash.support.ShShell;
import com.yunji.titanrtx.bash.support.SystemCtl;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.enums.AgentStatus;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.AgentService;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class AgentServiceImpl implements AgentService {


    @Resource
    private TaskDispatcher taskDispatcher;


    @Resource
    private LiaisonService liaisonService;


    @Override
    public RespMsg start(Task task) throws InterruptedException {
        log.info("接收到开始任务:{}..................................................", task);
        /*
         * 更新当前状态为忙碌状态，后续请求将不再处理
         */
        synchronized (this) {
            AgentStatus status = liaisonService.agentStatus();
            if (status == AgentStatus.RUNNING) {
                log.error("当前节点重复接受新任务，task:{}........................", task);
                /*
                 * 默认响应成功是防止dubbo在极端情况下会连续重试多次，而结果以最后一次为准，到时commander误进入快速失败
                 */
                return RespMsg.respSuc();
            }
            return taskDispatcher.start(task);
        }
    }


    @Override
    public RespMsg stop() {
        log.info("接收到停止任务..................................................");
        synchronized (this) {
            return taskDispatcher.stop();
        }
    }

    @Override
    public RespMsg progress() {
        return taskDispatcher.progress();
    }

    @Override
    public RespMsg disable() {
        AgentStatus status = liaisonService.agentStatus();
        if (status == AgentStatus.RUNNING) {
            return RespMsg.respErr("当前机器正在运行任务中......................");
        }
        log.info("当前机器已禁用.........................................................");
        liaisonService.updateAgentStatus(AgentStatus.DISABLE);
        return RespMsg.respSuc();
    }

    @Override
    public RespMsg enable() {
        log.info("当前机器已启用.........................................................");
        liaisonService.updateAgentStatus(AgentStatus.IDLE);
        return RespMsg.respSuc();
    }

    @Override
    public void restart() {
        try {
            liaisonService.downLine();
            new ShShell(null, GlobalConstants.RESTART_SH_PATH).execCommand();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> hosts() {
        try {
            return new CatShell(null, GlobalConstants.HOSTS_PATH).hosts();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public RespMsg modifyHosts(String content) {
        try {
            Files.write((content + "\n").getBytes(), new File(GlobalConstants.HOSTS_PATH));
            new SystemCtl("restart", "nscd").execCommand();
        } catch (IOException e) {
            e.printStackTrace();
            return RespMsg.respErr(e.getMessage());
        }
        return RespMsg.respSuc();
    }
}
