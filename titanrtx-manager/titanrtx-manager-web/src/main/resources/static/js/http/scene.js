app.controller("httpSceneController", function ($http, $scope, $rootScope) {

    $scope.gradingListModel = [
        {"id": "PRECISE", "name": "精确"},
        {"id": "EVERYDAY", "name": "每天"},
        {"id": "EVERYHOUR", "name": "每小时"},
        {"id": "EVERYMINUTE", "name": "每分钟"}
    ]

    $scope.weekDayModel = [
        {"id": "Mon", "name": "周一"},
        {"id": "Tue", "name": "周二"},
        {"id": "Wed", "name": "周三"},
        {"id": "Thu", "name": "周四"},
        {"id": "Fri", "name": "周五"},
        {"id": "Sat", "name": "周六"},
        {"id": "Sun", "name": "周日"}
    ]

    $scope.selectedGrading = {"id": "PRECISE", "name": "精确"};

    $scope.continuousTime = 10;

    $scope.weekday  = '';

    $scope.initFn = function () {
        $scope.doInitFn(getUrlPath("httpSceneList"), getUrlPath("httpSceneSearch"));
    }();

    $scope.$watch('searchModel', function () {
        let searchKey = $scope.searchModel;
        if (isBlank(searchKey)) {
            $scope.resetTablePage();
            return;
        }
        let totalFilterIndexData = [];
        let targetIndex = [];
        $scope.searchModelTotalData.forEach(function (value, index) {
            let lowerName = value.name.toLowerCase();
            let linkIds = value.idsWeight;
            let enterLowerName = searchKey.toLowerCase();
            if (lowerName.indexOf(enterLowerName) !== -1 || linkIds.indexOf(enterLowerName) !== -1) {
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push($scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });

    $scope.showSceneLinkDialog = function (allot, idsScale, idsWeight, idsQps) {
        $http.get(getUrlPath("httpSceneLink"), {
            params: {
                "linkIdsScale": idsScale,
                "linkIdsWeight": idsWeight,
                "lingIdsQps": idsQps
            }
        }).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.selectScaleAllot = allot;
                $scope.showSceneLinkModel = respData.data;
                console.log($scope.showSceneLinkModel)
                layui.layer.open({
                    title: "场景链路详情",
                    type: 1,
                    zIndex: layer.zIndex,
                    shade: 0,
                    content: layui.$('#sceneLinkDialog'),
                    area: ['1106px', '620px']
                });
            } else {
                layer.msg(respData.msg);
            }
        })
    };

    $scope.sceneStart = function (id, $index) {
        const now = new Date();
        const hours = now.getHours();
        if (hours >= 7 && hours <= 24) {
            layer.confirm('确定需要在白天进行压测吗?', {icon: 3, title: '提示'}, function (tips) {
                $scope.doScene(id, $index)
                layer.close(tips);
            });
        } else {
            $scope.doScene(id, $index)
        }
    };

    $scope.doScene = function (id, $index) {
        layui.layer.load();
        $http.get(getUrlPath("httpSceneStart"), {params: {"id": id}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.showModel[$index].status = 1;

            }
            layui.layer.closeAll('loading');
        })
    };

    $scope.sceneStop = function (id) {
        layui.layer.load();
        $http.get(getUrlPath("httpSceneStop"), {params: {"id": id}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.routePage($rootScope.path);
            }
            layui.layer.closeAll('loading');
        })
    };


    $scope.concurrentConfig = function () {
        $scope.concurrentInfo();
        layui.layer.open({
            title: "修改并发",
            type: 1,
            zIndex: 9999,
            shade: 0,
            content: layui.$('#concurrentConfigDialog'),
            area: ['600px', '300px'],
            btn: ['保存'],
            yes: function (index) {
                if (isBlank($scope.concurrentInputModel)) return layer.msg("并发数不能为空");
                $http.get(getUrlPath("httpSceneConcurrentConfig"), {params: {"concurrent": $scope.concurrentInputModel}}).then(function (respData) {
                    layer.msg(respData.msg);
                    if (vailSuccess(respData)) {
                        layer.close(index);
                    }
                })
            }
        });
    };

    $scope.concurrentInfo = function () {
        $http.get(getUrlPath("httpSceneConcurrentInfo")).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.concurrentConfigModel = respData.data;
            }
        })
    };

    $scope.$watch('selectedGrading', function () {
        console.log("selectedGrading changed");
        layui.laydate.render({
            elem: '#executeTimePrecise',
            type: 'datetime',
            min: getBeginToday(),
            value: getNowPreMinuter(0)
        });
        layui.laydate.render({
            elem: '#executeTimeEveryday',
            type: 'time',
            format: 'HH点mm分'
        });
        layui.laydate.render({
            elem: '#executeTimeEveryHour',
            type: 'time',
            format: 'mm分'
        });
    });
    $scope.everyDay = false;
    $scope.addDispatch = function (scene) {
        layui.laydate.render({
            elem: '#executeTimePrecise',
            type: 'datetime',
            min: getBeginToday(),
            value: getNowPreMinuter(0)
        });
        layui.layer.open({
            title: "添加调度配置",
            type: 1,
            zIndex: 9999,
            shade: 0,
            content: layui.$('#addDispatchDialog'),
            area: ['600px', '300px'],
            btn: ['保存'],
            yes: function (index) {
                $http.post(getUrlPath("autoDeployAddOrEdit"), {
                    "businessId": scene.id,
                    "type":0,
                    "name": scene.name,
                    "time": $scope.selectedGrading.id == "PRECISE"?
                        layui.$("#executeTimePrecise").val():($scope.selectedGrading.id == "EVERYDAY"?
                            layui.$("#executeTimeEveryday").val():($scope.selectedGrading.id == "EVERYHOUR"?
                                layui.$("#executeTimeEveryHour").val():0)),
                    "grading": $scope.selectedGrading.id,
                    "continuousTime": $scope.continuousTime
                }).then(function (respData) {
                    layer.msg(respData.msg);
                    if (vailSuccess(respData)) {
                        layer.close(index);
                    }
                })
            }
        });
    };
});

app.controller("httpSceneAddOrEditController", function ($http, $scope, $rootScope, $routeParams, ngInvoke, $timeout) {

    $scope.linkListModel = [];
    $scope.selectLinkModel = [];
    $scope.linkParamsModel = [];
    $scope.batchImportModel = {};

    $scope.isModify = false;

    $scope.linkList = function () {
        $http.get(getUrlPath("linkList")).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.linkListModel = respData.data;
            }
        })
    }();

    $scope.sceneMeta = function () {
        $http.get(getUrlPath("httpSceneMeta")).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.sceneMetaModel = respData.data;
            }
        })
    }();

    $scope.sceneInfo = function (id) {
        $http.get(getUrlPath("httpSceneInfo"), {params: {"id": id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.showSceneInfoModel = respData.data.httpSceneEntity;
                respData.data.links.forEach(function (value) {
                    $scope.selectLinkModel.push(value);
                });
                $scope.buildWeigthShareQps(Number($scope.showSceneInfoModel.concurrent))
            }
        })
    };

    $scope.buildWeigthShareQps = function (totalQps) {
        let totalWeight = 0;
        // 获取weight的总数
        for (let i = 0; i < $scope.selectLinkModel.length; i++) {
            totalWeight += Number($scope.selectLinkModel[i].weight);
        }
        for (let i = 0; i < $scope.selectLinkModel.length; i++) {
            let weightShareQps = Math.round($scope.selectLinkModel[i].weight * totalQps / totalWeight)
            $scope.selectLinkModel[i].weightShareQps = weightShareQps;
        }
    }

    $scope.initFn = function () {
        let id = $routeParams.id;
        if (isBlank(id)) {
            $scope.isEdit = false;
            return;
        } else {
            $scope.isEdit = true;
        }
        setTimeout(function () {
            layui.$("#strategy").val($scope.showSceneInfoModel.strategy);
            layui.$("#flow").val($scope.showSceneInfoModel.flow);
            layui.$("#sequence").val($scope.showSceneInfoModel.sequence);
            console.log("1");
            $rootScope.render();
        }, 200);

        return $scope.sceneInfo(id)
    }();


    $scope.$watch('searchLinkMode', function () {
        let searchKey = $scope.searchLinkMode;
        if (isNotBlank(searchKey)) {
            let showServiceArray = $scope.searchLinkHelper($scope.linkListModel, searchKey);
            if (showServiceArray.length > 0) {
                $scope.showLinkModel = showServiceArray;
                layui.$(".searchLinkNameSelect").show();
            } else {
                layui.$(".searchLinkNameSelect").hide();
            }
        } else {
            layui.$(".searchLinkNameSelect").hide();
        }
    });

    $scope.searchLinkHelper = function (data, name) {
        let showArray = [];
        let trimName = name.trim().toLowerCase();
        for (let j = 0; j < data.length; j++) {
            let linkModel = data[j];
            let lowerName = linkModel.name.toLowerCase();
            let id = linkModel.id;
            let lowerUrl = linkModel.url.toLowerCase();
            if (isEquals(id, name) || lowerName.indexOf(trimName) !== -1 || lowerUrl.indexOf(trimName) !== -1) {
                if (showArray.length <= 10) {
                    showArray.push(linkModel);
                } else {
                    break;
                }
            }
        }
        return showArray;
    };

    $scope.enterEvent = function (e) {
        let keycode = window.event ? e.keyCode : e.which;
        if (keycode === 13) {
            let searchKey = $scope.searchLinkMode;
            if (isBlank(searchKey)) return;
            $http.get(getUrlPath("linkSearch"), {params: {"key": searchKey}}).then(function (respData) {
                if (vailSuccess(respData)) {
                    $scope.showLinkModel = respData.data;
                    layui.$(".searchLinkNameSelect").show();
                }
            })
        }
    };

    $scope.selectLinkName = function (linkModel) {
        for (let i = 0; i < $scope.selectLinkModel.length; i++) {
            if ($scope.selectLinkModel[i].id === linkModel.id) {
                return layer.msg("链路已存在列表中");
            }
        }
        $scope.selectLinkModel.push(linkModel);
        layui.$(".searchLinkNameSelect").hide();
    };

    $scope.removeLink = function ($index) {
        $scope.selectLinkModel.remove($index, $index);
    };

    layui.form.on('submit(sceneSubmit)', function (data) {
        let linkIdsScale = [];
        let linkIdsWeight = [];
        let linkIdsQps = [];
        $scope.selectLinkModel.forEach(function (value) {
            linkIdsScale.push(value.id + "_" + value.scale);
            linkIdsWeight.push(value.id + "_" + value.weight);
            linkIdsQps.push(value.id + "_" + value.qps);
        });
        if (linkIdsWeight.length === 0) {
            layer.msg("场景需要最少一条链路");
            return;
        }
        let formData = data.field;
        formData.idsScale = linkIdsScale.join();
        formData.idsWeight = linkIdsWeight.join();
        formData.idsQps = linkIdsQps.join();
        console.log(formData);
        $http.post(getUrlPath("httpSceneAddOrUpdate"), formData).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.goRoutePage("/httpScene", {});
            }
        });
        return false;
    });


    $scope.refreshParamsData = function (linkId, currentPage) {
        $http.get(getUrlPath("linkQueryParams"), {
            params: {
                "linkId": linkId,
                "currentPage": currentPage
            }
        }).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.buildTablePageBar(linkId, respData.data);
            }
        });
    };

    $scope.buildTablePageBar = function (linkId, data) {
        $scope.linkParamsModel = data.list;
        $rootScope.render();
        layui.laypage.render({
            elem: 'paramsModelPage',
            theme: '#5FB878',
            layout: ['count', 'prev', 'page', 'next'],
            count: data.total,
            curr: data.pageNum,
            limit: 10,
            jump: function (obj, first) {
                $scope.currentPage = obj.curr;
                if (!first) {
                    $scope.refreshParamsData(linkId, obj.curr);
                }
            }
        })
    };

    $scope.refreshSingleLink = function (linkId) {
        $http.get(getUrlPath("linkFind"), {params: {"id": linkId}}).then(function (respData) {
            if (vailSuccess(respData)) {
                let refreshLink = respData.data;
                console.log(refreshLink);
                let refreshModel = [];
                for (let i = 0; i < $scope.selectLinkModel.length; i++) {
                    if ($scope.selectLinkModel[i].id !== refreshLink.id) {
                        refreshModel.push($scope.selectLinkModel[i]);
                    } else {
                        refreshLink.scale = $scope.selectLinkModel[i].scale;
                        refreshLink.weight = $scope.selectLinkModel[i].weight;
                        refreshLink.qps = $scope.selectLinkModel[i].qps;
                        refreshModel.push(refreshLink);
                    }
                }
                $scope.selectLinkModel = refreshModel;
                $scope.buildWeigthShareQps(Number($scope.showSceneInfoModel.concurrent))
            }
        })
    };


    $scope.paramDialog = function (id) {
        $scope.refreshParamsData(id, 1);
        $scope.currentLinkId = id;
        layui.layer.open({
            title: "参数详情",
            type: 1,
            zIndex: 9999,
            shade: 0,
            content: layui.$("#paramDialog"),
            area: ['1106px', '620px'],
            end: function () {
                $scope.refreshSingleLink(id);
            }
        });
    };


    $scope.queryLinkInfo = function (linkId) {
        return ngInvoke.syncInvoke(getUrlPath("linkInfo"), 'get', {"id": linkId});
    };

    $scope.getParamModel = function (id) {
        let modelParam = undefined;
        $scope.linkParamsModel.forEach(function (value) {
            if (isEquals(value.id, id)) {
                modelParam = value.param;
                return modelParam;
            }
        });
        return modelParam;
    };

    $scope.updateParam = function (linkId, paramId) {
        let modelParam = $scope.getParamModel(paramId);
        if (isBlank(modelParam)) {
            layer.msg("参数异常");
            return;
        }
        $http.get(getUrlPath("linkUpdateParam"), {
            params: {
                "paramId": paramId,
                "param": modelParam
            }
        }).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                return $scope.refreshParamsData(linkId, $scope.currentPage);
            }
        });
    };

    $scope.doParamCase = function (linkId, requestParam) {
        $scope.queryLinkInfo(linkId).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.doRequestCase(getUrlPath("doHttpParamCase"), {
                    "link": respData.data.link,
                    "requestParam": requestParam
                });
            } else {
                layer.msg(respData.msg);
            }
        });
    };

    $scope.doPreParamCase = function (linkId, paramId) {
        return $scope.doParamCase(linkId, $scope.getParamModel(paramId));
    };


    $scope.insertParam = function (linkId) {
        if (isBlank($scope.inputParamModel)) {
            layer.msg("参数不能为空");
            return;
        }
        return $scope.doInputParams(linkId, $scope.inputParamModel);
    };

    $scope.doInputParams = function (linkId, params, index) {
        $http.post(getUrlPath("linkAddParams"), {"id": linkId, "params": params}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.refreshParamsData(linkId, $scope.currentPage);
                if (isNotBlank(index)) {
                    layer.close(index);
                }
            }
        });
    };

    $scope.doInputParamCase = function (linkId) {
        return $scope.doParamCase(linkId, $scope.inputParamModel);
    };

    $scope.clearInputParam = function () {
        $scope.inputParamModel = '';
    };

    $scope.batchDeleteParams = function (linkId) {
        layer.confirm('确定删除全部参数?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("linkClearParam"), {params: {"linkId": linkId}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.refreshParamsData(linkId);
                }
            });
            layer.close(index);
        });
    };


    $scope.deleteParam = function (linkId, paramId, url) {
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath(url), {params: {"id": paramId}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.refreshParamsData(linkId, $scope.currentPage);
                }
            });
            layer.close(index);
        });
    };

    $scope.batchDoParamCase = function (linkId) {
        if ($scope.linkParamsModel.length === 0) {
            return $scope.doInputParamCase();
        }
        for (let i = 0; i < $scope.linkParamsModel.length; i++) {
            $timeout(function () {
                layer.msg("ID:" + ($scope.linkParamsModel[i].id) + "响应结果:");
                $scope.doParamCase(linkId, $scope.linkParamsModel[i].param)
            }, i * 3000);
        }
    };


    $scope.batchImportParams = function (linkId) {
        layui.layer.open({
            title: "批量导入参数",
            type: 1,
            zIndex: layer.zIndex,
            shade: 0,
            content: layui.$('#batchImportParamsDialog'),
            area: ['1040px', '580px'],
            btn: ['确定'],
            yes: function (index) {
                if (isBlank($scope.batchImportModel.inputData)) {
                    layer.msg("参数不能为空");
                    return;
                }
                let inputData = $scope.batchImportModel.inputData;
                if (isNotBlank($scope.batchImportModel.template)) {
                    inputData = formatDataHelper($scope.batchImportModel.template, inputData);
                }
                return $scope.doInputParams(linkId, inputData, index);
            },
            end: function () {
                $scope.batchImportModel = {};
                $scope.refreshSingleLink(linkId);
            }
        });
    };

    $scope.pullHttParams = function (linkId) {
        let fullUrl;
        for (let i = 0; i < $scope.selectLinkModel.length; i++) {
            let linkModel = $scope.selectLinkModel[i];
            if (isEquals(linkModel.id, linkId)) {
                fullUrl = linkModel.protocol + "://" + linkModel.url;
                break;
            }
        }
        ngInvoke.syncInvoke(getUrlPath("topLinkParams"), 'get', {"url": fullUrl}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.linkParamsSize = respData.data.count;
                layui.layer.open({
                    title: "数据工厂参数信息",
                    type: 1,
                    zIndex: layer.zIndex,
                    shade: 0,
                    content: layui.$('#pullParamsDialog'),
                    area: ['440px', '280px'],
                    btn: ['确定'],
                    yes: function (index) {
                        if ($scope.inputPullParamsSizeModel > $scope.linkParamsSize) {
                            layer.msg("获取参数数量不能大于数据工厂数量");
                            return;
                        }
                        $http.get(getUrlPath("topLinkPullParams"), {
                            params: {
                                "id": linkId,
                                "url": fullUrl,
                                "size": $scope.inputPullParamsSizeModel
                            }
                        }).then(function (respData) {
                            layer.msg(respData.msg);
                            if (vailSuccess(respData)) {
                                layer.close(index);
                                $scope.refreshParamsData(linkId, $scope.currentPage);
                            }
                        });
                    },
                    end: function () {
                        $scope.linkParamsSize = 0;
                    }
                });
            } else {
                layer.msg(respData.msg);
            }
        });
    };

    // 重置链路权重配置
    $scope.resetAllot = function (){
        for (let i = 0; i < $scope.selectLinkModel.length; i++) {
            $scope.selectLinkModel[i].weight = 100;
        }
    }

// 自动化
    $scope.autoComputer = function () {
        let scaleBack = $scope.selectLinkModel[0].scale;
        if ($scope.autoInput == undefined && $scope.autoInput < 0) {
            return;
        }
        if ($scope.showSceneInfoModel.allot === 'WEIGHT') {
            if ($scope.autoInput == 0) {
                for (let i = 0; i < $scope.selectLinkModel.length; i++) {
                    $scope.selectLinkModel[i].weight = 0;
                }
            } else {
                //以下所有修改都是参照weight进行转换
                for (let i = 0; i < $scope.selectLinkModel.length; i++) {
                    let temp = Math.ceil($scope.autoInput * $scope.selectLinkModel[i].scale / scaleBack)
                    $scope.selectLinkModel[i].weight = temp;
                }
            }
        } else {
            // 计算总的QPS
            let totalQps = 0;
            if ($scope.autoInput == 0) {
                for (let i = 0; i < $scope.selectLinkModel.length; i++) {
                    $scope.selectLinkModel[i].qps = 0;
                }
            } else {
                //以下所有修改都是参照weight进行转换
                for (let i = 0; i < $scope.selectLinkModel.length; i++) {
                    let temp = Math.ceil($scope.autoInput * $scope.selectLinkModel[i].scale / scaleBack)
                    $scope.selectLinkModel[i].qps = temp;
                    totalQps += Number(temp);
                }
            }
            // 更改总并发
            $scope.showSceneInfoModel.concurrent = totalQps;
        }
    }
// 修改QPS
    $scope.editQps = function (index) {
        let link = $scope.selectLinkModel[index];
        console.log((link))
        if (link.qps == undefined && link.qps < 0) {
            return;
        }
        // 计算总的QPS
        let totalQps = 0;
        //以下所有修改都是参照weight进行转换
        for (let i = 0; i < $scope.selectLinkModel.length; i++) {
            totalQps += Number($scope.selectLinkModel[i].qps);
        }
        // 更改总并发
        $scope.showSceneInfoModel.concurrent = totalQps;
    }

    $scope.editConcurrent = function () {
        if ($scope.showSceneInfoModel.concurrent == undefined || $scope.showSceneInfoModel.concurrent < 0) {
            return;
        }
        if ($scope.showSceneInfoModel.concurrent == 0) {
            for (let i = 0; i < $scope.selectLinkModel.length; i++) {
                if (index != i)
                    $scope.selectLinkModel[i].qps = 0;
            }
            return;
        }
        let concurrent = Number($scope.showSceneInfoModel.concurrent);

        let totalScale = 0;
        for (let i = 0; i < $scope.selectLinkModel.length; i++) {
            totalScale += Number($scope.selectLinkModel[i].scale);
        }
        // 计算各个链路的QPS
        for (let i = 0; i < $scope.selectLinkModel.length; i++) {
            $scope.selectLinkModel[i].qps = Math.round($scope.selectLinkModel[i].scale * concurrent / totalScale);
        }

        $scope.buildWeigthShareQps(concurrent);
    }

    $scope.changeWeight = function () {
        $scope.buildWeigthShareQps(Number($scope.showSceneInfoModel.concurrent));
    }
})
;