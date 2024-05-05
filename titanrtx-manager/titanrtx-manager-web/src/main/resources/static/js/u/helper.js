let Path={
    login                        :"backup/login/verify.query",
    layout                       :"backup/login/layout.query",

    /*overview*/
    overviewInfo                 :"overview/info.query",
    netRateMonitor               :"overview/netRateMonitor.query",
    restartAll                   :"overview/restartAll.do",
    resetAll                     :"overview/resetAll.do",

    /*link*/
    linkList                    :"link/list.query",
    linkFind                    :"link/find.query",
    linkAddOrUpdate             :"link/addOrUpdate.do",
    linkAddParams               :"link/addParams.do",
    linkQueryParams             :"link/queryParams.query",
    linkUpdateParam             :"link/updateParam.do",
    linkParamDelete             :"link/paramDelete.do",
    linkClearParam              :"link/clearParam.do",
    linkSearch                  :"link/search.query",
    linkInfo                    :"link/info.query",
    linkDelete                  :"link/delete.do",
    //2020-05-21 ADD.
    linkParamOrder              :"link/linkParamOrder.query",

    /*httpScene*/
    httpSceneList               :"httpScene/list.query",
    httpSceneLink               :"httpScene/link.query",
    httpSceneMeta               :"httpScene/meta.query",
    httpSceneAddOrUpdate        :"httpScene/addOrUpdate.do",
    httpSceneSearch             :"httpScene/search.query",
    httpSceneInfo               :"httpScene/info.query",
    httpSceneDelete             :"httpScene/delete.do",
    httpSceneStart              :"httpScene/start.do",
    httpSceneStop               :"httpScene/stop.do",
    httpSceneConcurrentInfo     :"httpScene/concurrentInfo.query",
    httpSceneConcurrentConfig   :"httpScene/concurrentConfig.do",

    /*report*/
    httpReportList              :"httpReport/list.query",
    httpReportSearch            :"httpReport/search.query",
    httpReportDelete            :"httpReport/delete.do",
    httpReportDetail            :"httpReport/detail.query",
    httpBaseLineAddOrUpdate     :"httpBaseLine/addOrUpdate.do",

    /*export*/
    reportExport                :"export/export.query",

    /*dubbo*/
    applicationNames             :"dubbo/providers.query",
    serviceUrls                  :"dubbo/serviceUrls.query",
    serviceMethods               :"dubbo/serviceMethods.query",
    serviceAddress               :"dubbo/serviceAddress.query",
    /*service*/
    serviceList                   :"service/list.query",
    serviceFind                   :"service/find.query",
    serviceAddParams               :"service/addParams.do",
    serviceQueryParams             :"service/queryParams.query",
    serviceUpdateParam             :"service/updateParam.do",
    serviceParamDelete             :"service/paramDelete.do",
    serviceClearParam              :"service/clearParam.do",
    serviceSearch                 :"service/search.query",
    serviceInfo                   :"service/info.query",
    serviceAddOrUpdate            :"service/addOrUpdate.do",
    serviceDelete                 :"service/delete.do",

    /*dubboScene*/
    dubboSceneList               :"dubboScene/list.query",
    dubboSceneSearch             :"dubboScene/search.query",
    dubboSceneInfo               :"dubboScene/info.query",
    dubboSceneMeta               :"dubboScene/meta.query",
    dubboSceneServices           :"dubboScene/services.query",
    dubboSceneAddOrUpdate        :"dubboScene/addOrUpdate.do",
    dubboSceneDelete             :"dubboScene/delete.do",
    dubboSceneStart              :"dubboScene/start.do",
    dubboSceneStop               :"dubboScene/stop.do",

    /*dubboreport*/
    dubboReportList              :"dubboReport/list.query",
    dubboReportSearch            :"dubboReport/search.query",
    dubboReportDelete            :"dubboReport/delete.do",
    dubboReportDetail            :"dubboReport/detail.query",
    /*progress*/
    progressState                 :"progress/state.query",


    /*data-factory*/
    taskList                    :"task/list.query",
    taskFind                    :"task/find.query",
    taskAddOrUpdate             :"task/addOrUpdate.do",
    taskAddOrUpdateParam        :"task/addOrUpdateParamDeploy.do",
    taskAddOrUpdateOutput       :"task/addOrUpdateOutputDeploy.do",
    taskQueryParams             :"task/queryParamDeploy.query",
    taskQueryOutputs            :"task/queryOutputDeploy.query",
    taskOutputInfo              :"task/outputInfo.query",
    taskParamDelete             :"task/paramDeployDelete.do",
    taskOutputDelete            :"task/outputDeployDelete.do",
    taskSearch                  :"task/search.query",
    taskInfo                    :"task/info.query",
    taskDelete                  :"task/delete.do",
    taskAutoCreate              :"task/autoCreate.do",

    batchList                   :"batch/list.query",
    batchSearch                 :"batch/search.query",
    batchAddOrUpdate            :"batch/addOrUpdate.do",
    batchDelete                 :"batch/delete.do",
    batchInfo                   :"batch/info.query",
    batchStart                  :"batch/start.do",
    queryTaskParamSource        :"batch/queryTaskParamSource.query",
    batchAddOrUpdateParamSource      :"batch/addOrUpdateParamSource.do",
    batchExport                 :"batch/export.do",
    resetBatch                  :"batch/resetBatch.do",
    moveTask                    :"batch/moveTask.do",

    /*topLink*/
    topLinkUpload                   :"topLink/upload.do",
    topLinkList                   :"topLink/list.query",
    topLinkParams                 :"topLink/params.query",
    topLinkPullParams             :"topLink/pullParams.query",
    topTurnSceneByUrl             :"topLink/turnSceneByUrl.do",
    topLinkDomains                :"topLink/domains.query",
    topLinkRemove                 :"topLink/remove.do",

    /* autoTop */
    autoTopLinkList               :"autoTop/list.query",
    autoTopLinkAddBlack           :"autoTop/addBlack.do",
    autoTopLinkAddWhite           :"autoTop/addWhite.do",
    autoTopLinkRemove             :"autoTop/remove.do",
    autoTopLinkConfirm            :"autoTop/confirm.do",
    autoTop300StressSwitch        :"autoTop/top300Switch.do",
    autoTop300StressStatus        :"autoTop/topStress.query",
    autoDeployList                :"autoDeploy/list.query",
    autoDeployAddOrEdit           :"autoDeploy/addOrEditDeploy.do",
    autoTestDeployDelete          :"autoDeploy/delete.do",
    autoTestDeployInfo            :"autoDeploy/info.query",

    // 黑名单分组
    blackGroupInfo                :"blackGroup/info.query",
    blackGroupSearch              :"blackGroup/search.query",
    queryBlackGroup               :"blackGroup/list.query",
    addOrUpdateBlackGroup         :"blackGroup/addOrUpdateRecord.do",
    deleteBlackGroup              :"blackGroup/delete.do",
    // 过滤
    blackListInfo                 :"blackList/info.query",
    addOrUpdateBlackList          :"blackList/addOrUpdateRecord.do",
    deleteFilter                  :"blackList/delete.do",
    // 白名单
    whiteListInfo                 :"whiteList/info.query",
    queryWhiteList                :"whiteList/list.query",
    addOrUpdateWhiteList          :"whiteList/addOrUpdateRecord.do",
    deleteWhiteList               :"whiteList/delete.do",

    /*machine*/
    machineList                 :"machine/list.query",
    machineHosts                 :"machine/hosts.query",
    machineBatchModify           :"machine/batchModify.do",
    machineModifyHosts           :"machine/modifyHosts.do",
    machineDisable              :"machine/disable.do",
    machineEnable               :"machine/enable.do",
    machineMonitor              :"machine/monitor.query",
    /*opsLog*/
    opsLogList                   :"opsLog/list.query",
    opsLogSearch                  :"opsLog/search.query",
    /*rules*/
    rulesInfo                     :"rulesLink/info.query",
    rulesLinkRules                :"rulesLink/rules.query",
    rulesAddRules                 :"rulesLink/addRules.do",
    rulesDeleteRules              :"rulesLink/delete.do",
    /**
     * user
     */
    userList                      :"user/list.query",
    userInfo                      :"user/info.query",
    userSession                   :"user/session.query",
    userDisable                   :"user/disable.do",
    userEnable                    :"user/enable.do",
    permissionList                :"user/permissionList.query",
    userAddOrUpdate               :"user/addOrUpdate.do",
    /*common*/
    doHttpParamCase              :"common/doHttpParamCase.do",
    doDubboParamCase             :"common/doDubboParamCase.do",
    queryDubboParams             :"common/queryDubboParams.query",
    queryDomain                   :"common/queryDomain.query"

};
function getUrlPath(strPath) {
    let path = Path[strPath];
    return "/"+path;
}
function vailLogin(d) {
    let code = d.code;
    if (code === 9999) {
        window.location.href = "/login.html"
    }
    return true;
}
function vailSuccess(d) {
    return d.code === 1;
}
function isBlank(str) {
    return str === '' || str === undefined || str == null || String(str).trim().length === 0;
}
function isNotBlank(str) {
    return !isBlank(str);
}
function isUndefined(str) {
    return str == undefined;
}
function isEquals(str1,str2) {
    return str1 ==  str2;
}
function isInteger(str){
    if(str==null||str === "") return false;
    let result = str.match(/^[-+]?\d+$/);
    return result != null;
}
function isDouble(str){
    if(str== null||str==="") return false;
    let result = String(str).match(/^[-+]?\d+(\.\d+)?$/);
    return result != null;
}
function isNumber(str){
    return isDouble(str) || isInteger(str);
}
function getNowPreMinuter(n) {
    return formatDateTime(new Date().getTime() - n * 60 * 1000);
}
function getBeginToday() {
    let date = new Date();
    let y = date.getFullYear();
    let m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    let d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    let h = "00";
    let minute = "00";
    let second = "00";
    return y + '-' + m + '-' + d+' '+h+':'+minute+':'+second;
}
function formatDateTime(inputTime) {
    let date = new Date(inputTime);
    let y = date.getFullYear();
    let m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    let d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    let h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    let minute = date.getMinutes();
    let second = date.getSeconds();
    minute = minute < 10 ? ('0' + minute) : minute;
    second = second < 10 ? ('0' + second) : second;
    return y + '-' + m + '-' + d+' '+h+':'+minute+':'+second;
}
function formatDate(inputTime) {
    let date = new Date(inputTime);
    let y = date.getFullYear();
    let m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    let d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    let h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    let minute = date.getMinutes();
    let second = date.getSeconds();
    minute = minute < 10 ? ('0' + minute) : minute;
    second = second < 10 ? ('0' + second) : second;
    return  h+':'+minute+':'+second;
}
function formatMonitorDate(inputTime) {
    let date = new Date(inputTime);
    let y = date.getFullYear();
    let m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    let d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    let h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    let minute = date.getMinutes();
    let second = date.getSeconds();
    minute = minute < 10 ? ('0' + minute) : minute;
    second = second < 10 ? ('0' + second) : second;
    return  y+'-'+m + '-' + d+' '+h+':'+minute+':'+second;
}


function formatDataHelper(temple,inputData) {
    let lineData = inputData.split("\n");
    let formatData='';
    lineData.forEach(function (value) {
        let format1 = format(temple,value.split(","));
        formatData += format1+"\n";
    });
    return formatData;
}

function format(source, params) {
    if (arguments.length === 1) {
        return function () {
            let args = $.makeArray(arguments);
            args.unshift(source);
            return layui.$.validator.format.apply(this, args);
        };
    }
    if (arguments.length > 2 && params.constructor !== Array) {
        params = $.makeArray(arguments).slice(1);
    }
    if (params.constructor !== Array) {
        params = [params];
    }
    layui.$.each(params, function (i, n) {
        source = source.replace(new RegExp("\\{" + i + "\\}", "g"), function () {
            return n;
        });
    });
    return source;
}


