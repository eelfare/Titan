package com.yunji.titanrtx.manager.service;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.auto.TopStressDeploy;
import com.yunji.titanrtx.common.enums.*;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.CommanderService;
import com.yunji.titanrtx.common.u.CollectionU;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.common.u.DateU;
import com.yunji.titanrtx.common.zookeeper.ZookeeperService;
import com.yunji.titanrtx.manager.dao.bos.top.*;
import com.yunji.titanrtx.manager.dao.entity.auto.FilterEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;
import com.yunji.titanrtx.manager.service.auto.FilterService;
import com.yunji.titanrtx.manager.service.auto.WhiteListService;
import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.manager.service.common.eventbus.EventBusCenter;
import com.yunji.titanrtx.manager.service.common.eventbus.ParamEvent;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import com.yunji.titanrtx.manager.service.http.LinkParamsService;
import com.yunji.titanrtx.manager.service.http.LinkService;
import com.yunji.titanrtx.manager.service.http.impl.HttpStressServiceImpl;
import com.yunji.titanrxt.plugin.influxdb.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 27/2/2020 8:36 下午
 * @Version 1.0
 */
@Slf4j
public class SceneOperatingCenterServiceImpl implements SceneOperatingCenterService {
    @Autowired
    private SystemProperties systemProps;

    @Resource
    private StoreService storeService;
    @Resource
    private FilterService filterService;
    @Resource
    private WhiteListService whiteListService;
    @Resource
    private LinkService linkService;
    @Resource
    private TaskService taskService;
    @Resource
    private HttpSceneService httpSceneService;
    @Resource
    private LinkParamsService httpParamsService;
    @Resource
    private ZookeeperService zookeeperService;
    @Resource
    private CommanderService commanderService;
    @Resource
    private HttpStressServiceImpl stressService;

    private static final Long DEFAULT_QPS = 200000L; // 默认QPS值

    /**
     * 获取top接口
     *
     * @param domain
     * @param blackGroupId
     * @param sDate
     * @param eDate
     * @param number
     * @param sourceContent
     * @returnø
     */
    @Override
    public List<TopLinkAsFilterBo> getTopLink(String domain, Integer blackGroupId, Date sDate, Date eDate, Integer number, String sourceContent) {
        List<TopLinkBo> topLinkBos = new ArrayList<>();
        List<TopLinkAsFilterBo> resultBos = new ArrayList<>(number);
        // 获取对应的精度数据库
        String measurementName = getDbName(eDate.getTime() - sDate.getTime());
        if (StringUtils.isEmpty(sourceContent)) {
            String format = "";
            if (StringUtils.isNotEmpty(domain)) {
                format = String.format(" and domain='%s' ", domain);
            }
            String commander = String.format("SELECT SUM(requestTimes) as requestTimes,SUM(successTimes) as successTimes,mean(elapsed)as elapsed FROM %s WHERE time >= %s and time <= %s %s GROUP BY * LIMIT 1", measurementName, sDate.getTime() + "ms", eDate.getTime() + "ms", format);
            topLinkBos = doQuery(commander, GlobalConstants.TOP_LINK_MATE_DB_NAME, measurementName, TopLinkBo.class);

        } else {
            String[] listUrl = sourceContent.contains("\r\n") ? sourceContent.split("\r\n") : sourceContent.split("\n");
            for (String url : listUrl) {
                // 有筛选的域名
                if (StringUtils.isBlank(url) || (StringUtils.isNotEmpty(domain) && !url.contains(domain))) {
                    continue;
                }
                String condition = buildCondition(url);
                // 查询数据
                String commander = String.format("SELECT SUM(requestTimes) as requestTimes,SUM(successTimes) as successTimes,mean(elapsed)as elapsed FROM %s WHERE time >= %s and time <= %s %s GROUP BY * LIMIT 1", measurementName, sDate.getTime() + "ms", eDate.getTime() + "ms", condition);
                List<TopLinkBo> list = doQuery(commander, GlobalConstants.TOP_LINK_MATE_DB_NAME, measurementName, TopLinkBo.class);
                System.out.println(list);
                topLinkBos.addAll(list);
            }
        }
        // 过滤操作
        List<TopLinkAsFilterBo> topLinkBosAsFilter = new ArrayList<>(topLinkBos.size());
        {
            Map<String, Set<Object>> blackListMap = null;
            if (blackGroupId != null) {
                blackListMap = buildFilter(filterService.findByGroupId(blackGroupId));
            }
            Set<Object> blackListDomain = blackListMap == null ? new HashSet<>() : blackListMap.get("domain");
            Set<Object> blackListPath = blackListMap == null ? new HashSet<>() : blackListMap.get("path");

            Map<String, Set<Object>> whiteListMap = buildFilter(whiteListService.selectAll());
            Set<Object> whiteListDomain = whiteListMap.get("domain");
            Set<Object> whiteListPath = whiteListMap.get("path");

            for (TopLinkBo topLinkBo : topLinkBos) {
                TopLinkAsFilterBo topLinkAsFilterBo = new TopLinkAsFilterBo();
                BeanUtils.copyProperties(topLinkBo, topLinkAsFilterBo);
                // 黑名单
                if (blackListDomain.contains(topLinkBo.getDomain())) {
                    topLinkAsFilterBo.setBlnBlack(true);
                } else if (blackListPath.contains(topLinkBo.getPath())) {
                    topLinkAsFilterBo.setBlnBlack(true);
                } else {
                    topLinkAsFilterBo.setBlnBlack(false);
                }
                // 白名单
                if (whiteListDomain.contains(topLinkBo.getDomain())) {
                    topLinkAsFilterBo.setBlnWhite(true);
                } else if (whiteListPath.contains(topLinkBo.getPath())) {
                    topLinkAsFilterBo.setBlnWhite(true);
                } else {
                    topLinkAsFilterBo.setBlnWhite(false);
                }
                topLinkBosAsFilter.add(topLinkAsFilterBo);
            }
        }

        topLinkBosAsFilter.sort((o1, o2) -> (int) (o2.getRequestTimes() - o1.getRequestTimes()));
        if (topLinkBosAsFilter.size() <= number) {
            resultBos = topLinkBosAsFilter;
        } else {
            resultBos.addAll(topLinkBosAsFilter.subList(0, number));
        }

        return resultBos;
    }


    /**
     * 创建场景
     *
     * @param bo
     * @return
     */
    @Override
    public HttpSceneEntity createScene(TurnSceneBo bo) {
        System.out.println("被调用一次创建场景");
        if (StringUtils.isEmpty(bo.getName())) {
            log.error("场景名称为空");
            return null;
        }
        // 校验名称是否存在
        List<HttpSceneEntity> list = httpSceneService.findByName(bo.getName());
        if (!CollectionUtils.isEmpty(list)) {
            log.error("场景名称已存在");
            return null;
        }
        HttpSceneEntity sceneEntity = new HttpSceneEntity();
        sceneEntity.setName(bo.getName());
        sceneEntity.setFlow(Flow.AUTO);
        sceneEntity.setConcurrent(DEFAULT_QPS);
        sceneEntity.setTotal(DEFAULT_QPS * 100 * 60);
        sceneEntity.setTimeout(10 * 60); // 执行十分钟
        sceneEntity.setSequence(Sequence.IN);
        sceneEntity.setStrategy(Strategy.FIXATION);

        insertWeight(bo, sceneEntity);

        sceneEntity.setAllot(Allot.WEIGHT);
        httpSceneService.insert(sceneEntity);
        sceneEntity.setCreateTime(new Date());
        log.info("创建完成场景：{}", sceneEntity.getId());
        return sceneEntity;
    }

    /**
     * top链路自动创建压测场景
     *
     * @param enableLinkList
     * @return
     */
    @Override
    public boolean topLinkToAutoScene(List<TopLinkAsFilterBo> enableLinkList) {
        TurnSceneBo bo = new TurnSceneBo();
        bo.setName("AUTO_TOP300_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date()));
        bo.setLinkParamsNum(300);
        bo.setLinkParamsTreaty("HTTPS");
        List<String> urls = new ArrayList<>();
        List<Long> scales = new ArrayList<>();
        enableLinkList.stream().forEach(link -> {
            urls.add(link.getDomain() + link.getPath());
            scales.add(link.getSuccessTimes());
        });
        bo.setUrl(urls);
        bo.setScale(scales);
        HttpSceneEntity scene = createScene(bo);
        if (scene == null) {
            log.info("创建自动top300场景失败!");
            return false;
        } else {
            // 用于检测提示作用
            zookeeperService.update(GlobalConstants.MANAGER_AUTO_TEST_SCENE, scene.getId() + "," + scene.getCreateTime().getTime());
            // 新增的自动压测任务发布到压测区中
            // 添加TOP 第一波 20W QPS 压测任务
            addAutoTestDeply(scene, TopStressDeploy.TopOrder.FIRST);
            // 添加TOP 第2波 40W QPS 压测任务
            addAutoTestDeply(scene, TopStressDeploy.TopOrder.SECOND);
            // 添加TOP 第3波 60W QPS 压测任务
            addAutoTestDeply(scene, TopStressDeploy.TopOrder.THIRD);
        }
        return true;
    }

    /**
     * 添加创建场景温馨提示，进行人工操作的的场景链路数据
     *
     * @param topLinks
     * @return
     */
    @Override
    public void addTipsTopLinks(List<TopLinkAsFilterBo> topLinks) {
        zookeeperService.update(GlobalConstants.MANAGER_SCENE_LINK_PATH, JSON.toJSONString(topLinks));
    }

    /**
     * 获取需要人工操作的场景链路数据
     *
     * @return
     */
    @Override
    public List<TopLinkAsFilterBo> queryTipsTopLinks() {
        String data = zookeeperService.getData(GlobalConstants.MANAGER_SCENE_LINK_PATH);
        List<TopLinkAsFilterBo> topLinkAsFilterBos = JSON.parseArray(data, TopLinkAsFilterBo.class);
        if (CollectionU.isEmpty(topLinkAsFilterBos)) {
            topLinkAsFilterBos = new ArrayList<>();
        }
        return topLinkAsFilterBos;
    }

    /**
     * 删除提示的数据
     *
     * @return
     */
    @Override
    public void deleteTips() {
        zookeeperService.update(GlobalConstants.MANAGER_SCENE_LINK_PATH, "");
    }

    /**
     * 查询需要自动化压测的场景信息
     *
     * @return
     */
    @Override
    public String queryAutoTestSceneData() {
        return zookeeperService.getData(GlobalConstants.MANAGER_AUTO_TEST_SCENE);
    }

    /**
     * 删除需要压测的ID
     *
     * @return
     */
    @Override
    public void deleteAutoTestSceneData() {
        zookeeperService.update(GlobalConstants.MANAGER_AUTO_TEST_SCENE, "");
    }

    /**
     * 判断自动创建的场景是否有效
     *
     * @param id
     * @return
     */
    @Override
    public boolean existScene(int id) {
        return httpSceneService.findById(id) != null;
    }

    /**
     * 压测前重置当前场景为目标几类压测
     *
     * @param id
     * @param order 压测目标类别：
     *              1：第一类压测（qps:20w，执行10分钟）
     *              2：第二类压测（qps:40w，执行10分钟）
     *              3：第三类压测（qps:60w，执行10分钟）
     *              3：第四类压测（qps:80w，执行10分钟）
     * @return
     */
    @Override
    public boolean resetSceneToTarget(int id, TopStressDeploy.TopOrder order) {
        HttpSceneEntity sceneEntity = httpSceneService.findById(id);
        if (sceneEntity == null) {
            return false;
        }
        if (sceneEntity.getStatus() == 1) { // 压测中，需要停止
            taskService.doStop(id, TaskType.HTTP);
        }
        // 重置场景配置(修改对应的QPS和总请求量、执行时间，对应链路的权重与QPS)
        // qps

        sceneEntity.setConcurrent(DEFAULT_QPS * order.ordinal());
        sceneEntity.setTotal(DEFAULT_QPS * order.ordinal() * 100 * 60);
        // 执行时间
        sceneEntity.setTimeout(10 * 60); // 执行十分钟
        //对应链路的权重与QPS
        List<String> listLinkId = new ArrayList<>();
        List<Long> listWeight = new ArrayList<>();
        String idsWeight = sceneEntity.getIdsWeight();
        String[] idsWeightList = idsWeight.split(",");
        int length = idsWeightList.length;

        int totalWeight = 0;
        long maxWeight = 0;
        int index = 0;
        for (; index < length; index++) {
            String[] splitIdsWeight = idsWeightList[index].split("_");
            listLinkId.add(splitIdsWeight[0]);
            Long weight = Long.valueOf(splitIdsWeight[1]);
            if (index == 0) {
                maxWeight = weight;
            }
            totalWeight += weight;
            listWeight.add(weight);
        }

        StringBuffer linkIdsQps = new StringBuffer();
        long concurrent = sceneEntity.getConcurrent();
        for (index = 0; index < length; index++) {
            String linkId = listLinkId.get(index);
            // 计算权重
            double ceil = Math.ceil((listWeight.get(index) * 100) / (maxWeight * 1.0));
            int weight = (int) ceil;
            linkIdsQps.append(linkId).append("_").append((int) (weight * concurrent / (totalWeight * 1.0))).append(",");
        }
        // 需要最后把拼接的字符串最后一个逗号去除
        linkIdsQps.deleteCharAt(linkIdsQps.length() - 1);
        sceneEntity.setIdsQps(linkIdsQps.toString());
        return httpSceneService.update(sceneEntity) > 0;
    }

    /**
     * HTTP开启压测
     *
     * @param id 场景id
     */
    @Override
    public RespMsg start(int id) {
        return stressService.startPressure(id);
    }

    /**
     * 供 auto 使用
     */
    public RespMsg stop(int id) {
        HttpSceneEntity httpSceneEntity = httpSceneService.findById(id);
        if (httpSceneEntity.getStatus() != 1) {
            return RespMsg.respErr("当前场景未正在压测中");
        }
        log.info("停止压测开始中：{}.....................................................................", httpSceneEntity);

        RespMsg respMsg = taskService.doStop(id, TaskType.HTTP);
        if (respMsg.isSuccess()) {
            httpSceneService.updateStatus(id, 0);
            return RespMsg.respSuc();
        }
        return respMsg;
    }

    /**
     * 检查压测机器是否足够
     *
     * @return
     */
    @Override
    public boolean checkMachine() {
        return taskService.checkStart(800000, systemProps.httpConcurrent);
    }

    /**
     * 根据传入的压测时间,修改该场景的总压测次数,会给 * time 的缓冲时间,传入时间为0 则不修改,单位:分钟。
     */
    @Override
    public RespMsg updateSceneContinuousRequest(int sceneId, int continuousTime) {
        try {
            HttpSceneEntity sceneEntity = httpSceneService.findById(sceneId);
            long total = sceneEntity.getTotal();
            long concurrent = sceneEntity.getConcurrent();
            long minTotal = concurrent * continuousTime * 60 * 2;

            log.info("场景[{}] 配置请求数:{},并发:{},传入持续时间:{}min,重新设置请求数:{}",
                    sceneEntity.getName(), total, concurrent, continuousTime, minTotal);

            if (minTotal != 0) {
                sceneEntity.setTotal(minTotal);
            }
            httpSceneService.update(sceneEntity);
            return RespMsg.respSuc(minTotal);
        } catch (Exception e) {
            log.error("Manager updateSceneContinuousRequest got error,cause: {}", e.getMessage());
            return RespMsg.respErr("修改场景请求次数失败 : " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String getDbName(long space) {
        if (space <= TimeUnit.HOURS.toMillis(systemProps.changeInfluxdbBorderHourLength)) {
            return GlobalConstants.TOP_LINK_MATE_MEASUREMENT_NAME;
        } else if (space < TimeUnit.DAYS.toMillis(1)) {
            return GlobalConstants.TOP_LINK_MATE_CQ1H_MEASUREMENT_NAME;
        } else {
            return GlobalConstants.TOP_LINK_MATE_CQ1D_MEASUREMENT_NAME;
        }
    }

    @SuppressWarnings("unchecked")
    private String buildCondition(String url) {
        String temp = url;
        if (url.contains("//")) {
            temp = url.substring(url.indexOf("//") + 2);
        }
        String domain = temp.substring(0, temp.indexOf("/"));
        String path = temp.substring(temp.indexOf("/"));

        StringBuilder builder = new StringBuilder();
        builder.append(" and domain='").append(domain).append("' ");
        builder.append(" and path='").append(path).append("' ");
        return builder.toString();
    }

    /**
     * 获取黑白名单数据
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Set<Object>> buildFilter(List<FilterEntity> listInfo) {
        Set<Object> filterPaths = new HashSet<>();
        Set<Object> filterDomain = new HashSet<>();
        if (CollectionU.isNotEmpty(listInfo)) {
            for (FilterEntity r : listInfo) {
                // 根据domain过滤
                if (!r.getDomain().equals(GlobalConstants.ALL)
                        && r.getPath().equals(GlobalConstants.ALL)) {
                    filterDomain.add(r.getDomain());
                    continue;
                }
                // 根据具体的path过滤
                if (!r.getPath().equals(GlobalConstants.ALL)
                        && !filterDomain.contains(r.getDomain())
                        && !r.getPath().equals(GlobalConstants.ALL)) {
                    filterPaths.add(r.getPath());
                }
            }
        }
        Map<String, Set<Object>> filter = new HashMap<>();
        filter.put("domain", filterDomain);
        filter.put("path", filterPaths);
        return filter;
    }

    private <T> List<T> doQuery(String commander, String databases, String measurementName, Class<T> clazz) {
        log.info("【select influxdb statement】:{}", commander);
        Query query = new Query(commander, databases);
        QueryResult result = null;
        try {
            result = storeService.query(query);
        } catch (InfluxDBIOException e) {
            throw new RuntimeException("查询超时，请缩小查询时间段");
        }
        if (result == null || result.getResults() == null || result.getResults().size() == 0 ||
                StringUtils.isNotEmpty(result.getResults().get(0).getError())) {
            return new ArrayList<>();
        }
        return new InfluxDBResultMapper().toPOJO(result, clazz, measurementName);
    }

    private void insertWeight(TurnSceneBo bo, HttpSceneEntity sceneEntity) {
        Date startTime = new Date();
        StringBuffer linkIdsWeight = new StringBuffer();
        StringBuffer linkIdsQps = new StringBuffer();
        List<String> urls = bo.getUrl();
        List<Long> scales = bo.getScale();

        int totalWeight = 0;
        Long maxScale = bo.getScale().get(0);
        for (int i = 0; i < urls.size(); i++) {
            // 计算权重
            double ceil = Math.ceil((scales.get(i) * 100) / (maxScale * 1.0));
            int weight = (int) ceil;
            totalWeight += weight;
        }
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            LinkEntity entity = new LinkEntity();
            entity.setName(bo.getName() + "_" + (i + 1));
            entity.setUrl(url);
            entity.setProtocol(Protocol.valueOf(bo.getLinkParamsTreaty()));
            entity.setCharset(Charset.UTF8);
            entity.setContentType(Content.JSON);
            entity.setMethod(Method.GET);
            entity.setParamMode(ParamMode.RANDOM);
            linkService.insert(entity);

            String fullUrl = entity.getProtocol() + "://" + url;
            int linkId = entity.getId();

            // 获取参数个数
            ParamsSizeBo paramsSizeBo = (ParamsSizeBo) params(fullUrl);
            int count = paramsSizeBo.getCount();
            // 保存指定数量参数至链路中
            if (count != 0) {
                int parmamsCount;
                if (bo.getLinkParamsNum() <= count) {
                    parmamsCount = bo.getLinkParamsNum();
                } else {
                    parmamsCount = count;
                }
                try {
                    pullParams(linkId, fullUrl, parmamsCount);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }
            // 计算权重
            double ceil = Math.ceil((scales.get(i) * 100) / (maxScale * 1.0));
            int weight = (int) ceil;
            linkIdsWeight.append(linkId).append("_").append(weight).append(",");
            linkIdsQps.append(linkId).append("_").append((int) (weight * DEFAULT_QPS / (totalWeight * 1.0))).append(",");
        }
        log.info("创建链路所花总时间：{}ms", System.currentTimeMillis() - startTime.getTime());
        // 需要最后把拼接的字符串最后一个逗号去除
        linkIdsWeight.deleteCharAt(linkIdsWeight.length() - 1);
        linkIdsQps.deleteCharAt(linkIdsQps.length() - 1);

        sceneEntity.setIdsScale(linkIdsWeight.toString());
        sceneEntity.setIdsWeight(linkIdsWeight.toString());
        sceneEntity.setIdsQps(linkIdsQps.toString());
    }

    private void updateWeight(HttpSceneEntity sceneEntity) {
        String idsScale = sceneEntity.getIdsScale();
        String[] split = idsScale.split(",");
        List<Long> scales = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (String idScale : split) {
            String[] temp = idScale.split("_");
            ids.add(Integer.valueOf(temp[0]));
            scales.add(Long.valueOf(temp[1]));
        }

        StringBuffer linkIdsWeight = new StringBuffer();
        StringBuffer linkIdsQps = new StringBuffer();

        int totalWeight = 0;
        Long maxScale = scales.get(0);
        for (int i = 0; i < scales.size(); i++) {
            // 计算权重
            double ceil = Math.ceil((scales.get(i) * 100) / (maxScale * 1.0));
            int weight = (int) ceil;
            totalWeight += weight;
        }
        for (int i = 0; i < scales.size(); i++) {
            // 计算权重
            double ceil = Math.ceil((scales.get(i) * 100) / (maxScale * 1.0));
            int weight = (int) ceil;
            Integer linkId = ids.get(i);
            linkIdsWeight.append(linkId).append("_").append(weight).append(",");
            linkIdsQps.append(linkId).append("_").append((int) (weight * DEFAULT_QPS / (totalWeight * 1.0))).append(",");
        }
        // 需要最后把拼接的字符串最后一个逗号去除
        linkIdsWeight.deleteCharAt(linkIdsWeight.length() - 1);
        linkIdsQps.deleteCharAt(linkIdsQps.length() - 1);

        sceneEntity.setIdsScale(linkIdsWeight.toString());
        sceneEntity.setIdsWeight(linkIdsWeight.toString());
        sceneEntity.setIdsQps(linkIdsQps.toString());
    }

    private ParamsSizeBo params(String url) {
        URL inUrl;
        try {
            inUrl = new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
        String domain = inUrl.getHost();
        String path = "\"" + inUrl.getPath() + "\"";
        String commander = String.format("SELECT COUNT(param) FROM %s", path);
        List<ParamsSizeBo> sizeBos = doQuery(commander, domain, inUrl.getPath(), ParamsSizeBo.class);
        ParamsSizeBo bo = new ParamsSizeBo();
        if (sizeBos.size() > 0) {
            bo = sizeBos.get(0);
        }
        return bo;
    }

    @Override
    public void pullParams(Integer id, String url, int size) throws UnsupportedEncodingException {
        URL inUrl;
        try {
            inUrl = new URL(url);
        } catch (MalformedURLException e) {
            log.error("无效url信息：" + url);
            return;
        }
        String domain = inUrl.getHost();
        String path = "\"" + inUrl.getPath() + "\"";
        String commander = String.format("SELECT param FROM %s  ORDER BY time DESC LIMIT %d", path, size);
        List<LinkParamSizeBo> linkParamSizeBos = doQuery(commander, domain, inUrl.getPath(), LinkParamSizeBo.class);
        for (LinkParamSizeBo lb : linkParamSizeBos) {
            String param = CommonU.decodeParams(lb.getParam());
            LinkParamsEntity entity = new LinkParamsEntity(id, param);
            httpParamsService.insert(entity);
        }
        //插入参数变更.
        EventBusCenter.post(new ParamEvent(id, TaskType.HTTP, ParamEvent.Event.BATCH_EXPORT));
    }

    // 添加压测任务到压测区
    private void addAutoTestDeply(HttpSceneEntity scene, TopStressDeploy.TopOrder topOrder) {
        TopStressDeploy deploy = new TopStressDeploy();
        deploy.setId(zookeeperService.nextId(GlobalConstants.AUTO_PATH));
        deploy.setSceneId(scene.getId());
        deploy.setName(scene.getName());
        deploy.setTopOrder(topOrder);
        deploy.setTime(DateU.dateToString(getDate(deploy.getTopOrder()), DateU.LONG_PATTERN));
        deploy.setContinuousTime(0);
        commanderService.addAutoDeploy(deploy);
    }

    // 获取时间
    private Date getDate(TopStressDeploy.TopOrder topOrder) {
        Calendar instance = Calendar.getInstance();
        switch (topOrder) {
            case FIRST:
                instance.set(Calendar.HOUR_OF_DAY, 24);
                instance.set(Calendar.MINUTE, 30);
                break;
            case SECOND:
                instance.set(Calendar.HOUR_OF_DAY, 25);
                instance.set(Calendar.MINUTE, 0);
                break;
            case THIRD:
                instance.set(Calendar.HOUR_OF_DAY, 25);
                instance.set(Calendar.MINUTE, 30);
                break;
        }
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTime();
    }
}
