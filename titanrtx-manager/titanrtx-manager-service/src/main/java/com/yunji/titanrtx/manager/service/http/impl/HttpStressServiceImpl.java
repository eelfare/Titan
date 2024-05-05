package com.yunji.titanrtx.manager.service.http.impl;

import com.yunji.titanrtx.common.domain.task.*;
import com.yunji.titanrtx.common.enums.*;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.resp.RespCodeOperator;
import com.yunji.titanrtx.common.u.*;
import com.yunji.titanrtx.manager.dao.entity.http.*;
import com.yunji.titanrtx.manager.service.TaskService;
import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.manager.service.http.*;
import com.yunji.titanrtx.manager.service.report.service.ScreenService;
import com.yunji.titanrtx.manager.service.support.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.yunji.titanrtx.common.message.ErrorMsg.*;

/**
 * HttpStressServiceImpl
 *
 * @author leihz
 * @since 2020-05-24 11:46 上午
 */
@Slf4j
@Service
public class HttpStressServiceImpl implements HttpStressService {
    @Resource
    private LinkService linkService;
    @Resource
    private TaskService taskService;
    @Resource
    private LinkParamsService paramsService;
    @Resource
    private HttpSceneService httpSceneService;
    @Resource
    private SystemProperties systemProps;
    @Resource
    private ScreenService screenService;

    @Override
    public RespMsg startPressure(int id) {
        try {
            long st = System.currentTimeMillis();
            HttpSceneEntity httpSceneEntity = httpSceneService.findById(id);
            Pair<RespMsg, List<LinkEntity>> result = prepareAndGetLinks(httpSceneEntity);
            if (result.isLeft()) {
                return result.getKey();
            }
            List<LinkEntity> links = result.getValue();
            //判断links 参数排序是否已ok.
            ParamTransmit paramTransmit = systemProps.paramTransmit;
            httpSceneEntity.setParamTransmit(paramTransmit);

            //准备压测的bullets.
            List<Bullet> bullets = taskService.buildBullet(links, httpSceneEntity.getAllot(), HttpLink.class);
            //顺序的情况
            if (paramTransmit == ParamTransmit.ORDERS) {
                buildParamsOfOrders(bullets);
            } else if (paramTransmit == ParamTransmit.IDS) {
                //获取全量ID情况.
                buildParamsOfIds(bullets);
            } else {
                //TODO 直接获取参数.
                throw new IllegalArgumentException("The PARAM_SELF way is not implementation.");
            }

            RespMsg respMsg = taskService.doStart(httpSceneEntity, bullets, systemProps.httpConcurrent, TaskType.HTTP);

            StringBuilder sb = new StringBuilder();
            sb.append("场景[").append(httpSceneEntity.getName()).append("]")
                    .append("taskNo: ").append(respMsg.getMsg())
                    .append(", Transmit way: ").append(paramTransmit)
                    .append(respMsg.isSuccess() ? ",启动成功" : "启动失败.")
                    .append("耗时:").append((System.currentTimeMillis() - st)).append("ms.........");

            log.info(sb.toString());

            sb.append("\n");
            for (Bullet bullet : bullets) {
                sb.append(",LinkId[")
                        .append(bullet.getId())
                        .append("], param range or size:")
                        .append(paramTransmit == ParamTransmit.ORDERS ? bullet.getParamRange() : bullet.getParamIds().size());
            }
            LogU.info(sb.toString());

            if (respMsg.isSuccess()) {
                String taskNo = respMsg.getMsg();
                screenService.schedulePerMinAfterDelayScreenshot(taskNo);
                respMsg.setMsg("执行[" + respMsg.getMsg() + "]成功");
                httpSceneService.updateStatus(httpSceneEntity.getId(), 1);
            }
            return respMsg;
        } catch (Exception e) {
            log.error("Manager start got error,cause: {}", e.getMessage());
            return RespMsg.respErr("压测失败: " + e.getMessage());
        }
    }

    /**
     * 对给定场景进行的所有链路进行测试,所有链路请求1次,并将返回结果展示回前端.
     *
     * @param id            场景id
     * @param onlyShowError 是否只展示有异常的链路,成功的则不展示.
     */
    @Override
    public Object reportSceneResult(int id, boolean onlyShowError, boolean deleteBadLinkParam) {
        try {
            HttpSceneEntity httpSceneEntity = httpSceneService.findById(id);
            Pair<RespMsg, List<LinkEntity>> prepareResult = prepareAndGetLinks(httpSceneEntity);
            if (prepareResult.isLeft()) {
                return prepareResult.getKey();
            }
            List<LinkEntity> links = prepareResult.getValue();

            List<Bullet> bullets = randomBuildLinkParams(taskService.buildBullet(links, httpSceneEntity.getAllot(), HttpLink.class));

            //build to http
            List<String> results = new ArrayList<>();
            List<Integer> errorIdList = new ArrayList<>();
            for (Bullet bullet : bullets) {
                HttpLink httpLink = (HttpLink) bullet;
                StringBuilder sb = new StringBuilder();
                sb
                        .append("### 链路(").append(bullet.getId()).append("),url: ")
                        .append(httpLink.getUrl()).append(".\n");
                boolean isError = false;
                try {
                    Request request = AHCHttpU.buildRequestByBullet(httpLink);
                    String result = AHCHttpU.executeRequest(request);
                    //YunJiRespDomain bo = JSON.parseObject(result, YunJiRespDomain.class);
                    Integer respCode = RespCodeOperator.getRespCode(result);

                    if (respCode != 0) {
                        isError = true;
                    }
                    sb
                            .append("\n```")
                            .append(result)
                            .append("```");
                } catch (Exception e) {
                    sb
                            .append("```")
                            .append("{\"result\": \"").append(e.getMessage()).append("\"}")
                            .append("```\n");
                }
                if (onlyShowError) {
                    if (isError) {
                        errorIdList.add(bullet.getId());
                        results.add(sb.toString());
                    }
                } else {
                    results.add(sb.toString());
                }
            }
            String result = CommonUtils.renderToHtml(results);

            log.info("Scene test result:{}", result);

            if (deleteBadLinkParam && !errorIdList.isEmpty()) {
                log.info("Delete bad link params,bad link size:{}", errorIdList.size());
                for (Integer linkId : errorIdList) {
                    linkService.collectBadParamIds(linkId, true);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Manager start got error,cause: {}", e.getMessage());
            return RespMsg.respErr("压测失败");
        }
    }

    private Pair<RespMsg, List<LinkEntity>> prepareAndGetLinks(HttpSceneEntity httpSceneEntity) {

        if (httpSceneEntity == null) {
            return new Pair<>(new RespMsg(SCENE_NOT_FOUND), null);
        }
        //当前场景正在压测中
        if (httpSceneEntity.getStatus() != 0) {
            return new Pair<>(new RespMsg(STRESS_DOING), null);
        }
        if (taskService.checkStart(httpSceneEntity.getConcurrent(), systemProps.httpConcurrent))
            return new Pair<>(new RespMsg(STRESS_MACHINE_LACK), null);
        //根据场景获取其所有链路.
        return new Pair<>(null, getLinkEntities(httpSceneEntity));
    }


    private List<LinkEntity> getLinkEntities(HttpSceneEntity httpSceneEntity) {
        // tips(景风):不管是权重分配还是QPS分配，两者数据都会存在
        List<String> ids = CommonU.parseIds(httpSceneEntity.getIdsWeight());
        Map<Integer, Integer> idWeightMap = CommonU.parseIdWeightMap(httpSceneEntity.getIdsWeight());
        Map<Integer, Integer> idScaleMap;
        if (httpSceneEntity.getIdsScale().isEmpty()) {
            idScaleMap = new HashMap<>(idWeightMap);
        } else {
            idScaleMap = CommonU.parseIdWeightMap(httpSceneEntity.getIdsScale());
        }
        Map<Integer, Long> idQpsMap = CommonU.parseIdQpsMap(httpSceneEntity.getIdsQps());
        List<LinkEntity> entities = linkService.selectByIds(ids);

        return fillLinkWeight(entities, idScaleMap, idWeightMap, idQpsMap);
    }

    private List<LinkEntity> fillLinkWeight(List<LinkEntity> links, Map<Integer, Integer> linkIdsScale, Map<Integer, Integer> linkIdsWeight, Map<Integer, Long> linkIdsQps) {
        for (LinkEntity link : links) {
            link.setScale(linkIdsScale.get(link.getId()));
            link.setWeight(linkIdsWeight.get(link.getId()));
            Long qps = linkIdsQps.get(link.getId());
            link.setQps(qps == null ? 0 : qps);
        }
        return links;
    }

    /**
     * 查询出链路参数的所有id出来,将id传递给agent,然后在agent端根据id查询出参数,进行压测.
     */
    private void buildParamsOfIds(List<Bullet> bullets) {
        for (Bullet bullet : bullets) {
            Integer id = bullet.getId();
            //只是查询了参数的id.
            List<Integer> paramIds = paramsService.selectAllIdByLinkId(id);
            bullet.setParamIds(paramIds);
        }
    }

    /**
     * 每一条链路的参数有一个orders标志，并根据 1--n 排好了序,
     */
    private void buildParamsOfOrders(List<Bullet> bullets) {
        for (Bullet bullet : bullets) {
            Integer id = bullet.getId();
            //只是查询了参数的id.
            int totalRecords = paramsService.findTotalRecordsByLinkId(id);
            //检查
            boolean check = paramsService.checkParamIsOrder(id, totalRecords);
            if (!check) {
                throw new RuntimeException("链路(" + id + ") 的参数顺序没有从1到n排序完成,请检查.");
            }
            //为什么+1?因为右边区间不包含.
            bullet.setParamRange(new ParamRange(1, totalRecords + 1));
        }
        log.info("设置每条链路的ParamRange完成,链路总数:{}", bullets.size());
    }

    /**
     * 随机获取一个参数进行请求
     */
    private List<Bullet> randomBuildLinkParams(List<Bullet> bullets) {
        for (Bullet bullet : bullets) {
            Integer id = bullet.getId();
            //只是查询了参数的id.
            String param = paramsService.selectParamRandom(id);
            bullet.setParams(Collections.singletonList(param));
        }
        return bullets;
    }
}
