package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.common.domain.auto.TopStressDeploy;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.bos.top.TopLinkAsFilterBo;
import com.yunji.titanrtx.manager.dao.bos.top.TurnSceneBo;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

/**
 * 场景处理中心
 *
 * @Author: 景风（彭秋雁）
 * @Date: 27/2/2020 8:16 下午
 * @Version 1.0
 */
public interface SceneOperatingCenterService {
    /**
     * 获取top接口
     *
     * @param domain
     * @param blackGroupId
     * @param sDate
     * @param eDate
     * @param number
     * @param sourceContent
     * @return
     */
    List<TopLinkAsFilterBo> getTopLink(String domain, Integer blackGroupId, Date sDate, Date eDate, Integer number, String sourceContent);

    /**
     * 创建场景
     *
     * @param bo
     * @return
     */
    HttpSceneEntity createScene(TurnSceneBo bo);

    /**
     * top链路自动创建压测场景
     *
     * @param enableLinkList
     * @return
     */
    boolean topLinkToAutoScene(List<TopLinkAsFilterBo> enableLinkList);

    /**
     * 添加创建场景温馨提示，进行人工操作的的场景链路数据
     *
     * @param topLinks
     * @return
     */
    void addTipsTopLinks(List<TopLinkAsFilterBo> topLinks);

    /**
     * 获取需要人工操作的场景链路数据
     *
     * @return
     */
    List<TopLinkAsFilterBo> queryTipsTopLinks();

    /**
     * 删除提示的数据
     *
     * @return
     */
    void deleteTips();

    /**
     * 提交参数
     *
     * @param id
     * @param url
     * @param size
     * @throws UnsupportedEncodingException
     */
    void pullParams(Integer id, String url, int size) throws UnsupportedEncodingException;


    /**
     * 查询需要自动化压测的场景Id
     *
     * @return
     */
    String queryAutoTestSceneData();

    /**
     * 删除需要压测的ID
     *
     * @return
     */
    void deleteAutoTestSceneData();

    /**
     * 判断自动创建的场景是否有效
     *
     * @param id
     * @return
     */
    boolean existScene(int id);

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
    boolean resetSceneToTarget(int id, TopStressDeploy.TopOrder order);

    /**
     * 开启压测
     *
     * @param id
     * @return
     */
    RespMsg start(int id);

    /**
     * 停止压测
     */
    RespMsg stop(int id);

    /**
     * 检查压测机器是否足够
     *
     * @return
     */
    boolean checkMachine();

    /**
     * 根据传入的压测时间,修改该场景的总压测次数,会给 * time 的缓冲时间,传入时间为0 则不修改,单位:分钟。
     */
    RespMsg updateSceneContinuousRequest(int sceneId, int continuousTime);
}
