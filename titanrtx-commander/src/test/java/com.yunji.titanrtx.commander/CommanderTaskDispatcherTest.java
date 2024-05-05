package com.yunji.titanrtx.commander;

import com.yunji.titanrtx.commander.service.LiaisonService;
import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.AgentService;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import org.apache.dubbo.rpc.RpcContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommanderTaskDispatcherTest {

    @Resource
    protected LiaisonService liaisonService;

    @Resource
    protected AgentService agentService;

    @Resource
    private TaskDispatcher commanderTaskDispatcher;

    @Test
    public void reportTest() {
        Thread t1 = new Thread(() -> {
            Statistics statistics = new Statistics();
            statistics.setTaskNo("task-919412359-140");
            statistics.setTaskType(TaskType.HTTP);
            statistics.setAddress("10.0.118.1");
            commanderTaskDispatcher.report(statistics);
        });

        Thread t2 = new Thread(() -> {
            Statistics statistics = new Statistics();
            statistics.setTaskNo("task-919412359-140");
            statistics.setTaskType(TaskType.HTTP);
            statistics.setAddress("10.0.118.4");
            commanderTaskDispatcher.report(statistics);
        });

        t1.start();
        t2.start();

        try {
            Thread.sleep(3600 * 24 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void startTest() {
        //下发任务到agent节点
        for (int i = 0; i < 10; i++) {
            liaisonService.ensureRemoteDomain("10.0.75.1");
            agentService.stop();
            CompletableFuture<RespMsg> future = RpcContext.getContext().getCompletableFuture();
            future.whenComplete((respMsg, exception) -> {
                if (exception != null){
                    exception.printStackTrace();
                }else{
                    System.out.println("任务下发agent节点成功:taskNo:{},agent:{}.........................................");
                }
            });
        }

        try {
            Thread.sleep(1000000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

