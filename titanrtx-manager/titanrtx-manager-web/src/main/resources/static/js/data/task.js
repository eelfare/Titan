app.controller("taskController", function ($http, $scope) {
    $scope.outputInfo = ''

    $scope.initFn = function () {
        $scope.doInitFn(getUrlPath("taskList"), getUrlPath("taskSearch"));
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
            let lowerUrl = value.restBo.url.toLowerCase();
            let taskId = value.id;
            let enterLowerName = searchKey.toLowerCase();
            if (lowerName.indexOf(enterLowerName) !== -1 || lowerUrl.indexOf(enterLowerName) !== -1 || isEquals(taskId, enterLowerName)) {
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push($scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });


    $scope.autoAddSocketTask = function () {
        layui.layer.open({
            title: "解析socket指令集合",
            type: 1,
            zIndex: 9999,
            shade: 0,
            content: layui.$('#analyzeSocketTaskDialog'),
            area: ['700px', '300px'],
            btn: ['确定'],
            yes: function (index) {
                if (isBlank($scope.autoTaskUrl) && isBlank($scope.socketListString)) return layer.msg("路径或指令集合不能为空");
                $http.get(getUrlPath("taskAutoCreate"), {
                    params: {
                        "url": $scope.autoTaskUrl,
                        "socketListString": $scope.socketListString
                    }
                }).then(function (respData) {
                    layer.msg(respData.msg);
                    if (vailSuccess(respData)) {
                        $scope.reloadCurrentPage();
                    }
                })
            }
        });
    };
});

app.controller("taskAddOrEditController", function ($http, $scope, $rootScope, $routeParams, $timeout, ngInvoke) {

    $scope.taskInfoModel = {};
    $scope.formDataSubmitFlag = false;
    $scope.currentFormModel = {};
    $scope.batchImportModel = {};
    $scope.linkParamsModel = [];
    $scope.dataSourceModel = ['PARAM', 'RESPONSE', 'IGNORE'];
    $scope.doMode = '';
    $scope.protocolModel = ['HTTP', 'DUBBO', 'SOCKET', 'FILE'];


    $scope.taskInfo = function (id) {
        $http.get(getUrlPath("taskInfo"), {params: {"id": id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.taskInfoModel = respData.data;
                $scope.doMode = $scope.taskInfoModel.doMode;
                $scope.buildParamsPage(respData.data.listParamDeploy);
                $scope.buildOutputsPage(respData.data.listOutputDeploy);
            }
        })
    };

    $scope.initFn = function () {
        let id = $routeParams.id;
        if (isBlank(id)) {
            $scope.isEdit = false;
            return;
        } else {
            $scope.isEdit = true;
        }
        return $scope.taskInfo(id)
    }();

    $scope.buildParamsPage = function (data) {
        $scope.taskParamsModel = data;
        $rootScope.render();
    };

    $scope.buildOutputsPage = function (data) {
        $scope.taskOutputsModel = data;
        $rootScope.render();
    };

    $scope.getModelParam = function (id) {
        let modelParam = undefined;
        $scope.taskParamsModel.forEach(function (value) {
            if (isEquals(value.id, id)) {
                modelParam = value;
                return modelParam;
            }
        });
        return modelParam;
    };
    $scope.getModelOutput = function (id) {
        let modelOutput = undefined;
        $scope.taskOutputsModel.forEach(function (value) {
            if (isEquals(value.id, id)) {
                modelOutput = value;
                return modelOutput;
            }
        });
        return modelOutput;
    };


    $scope.doInputTaskParams = function (name, extra) {
        $http.post(getUrlPath("taskAddOrUpdateParam"), {
            "taskId": $scope.taskInfoModel.id,
            "name": name,
            "extra": extra
        }).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.refreshParamsData();
            }
        });
    };

    $scope.updateTaskParam = function (paramId) {
        let modelParam = $scope.getModelParam(paramId);
        if (modelParam == undefined) {
            layer.msg("参数异常");
            return;
        }
        $http.post(getUrlPath("taskAddOrUpdateParam"), {
            "id": paramId,
            "taskId": $scope.taskInfoModel.id,
            "name": modelParam.name,
            "extra": modelParam.extra

        }).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                return $scope.refreshParamsData();
            }
        });
    };


    $scope.refreshParamsData = function () {
        $http.get(getUrlPath("taskQueryParams"), {
            params: {
                "taskId": $scope.taskInfoModel.id
            }
        }).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.taskParamsModel = respData.data
            }
        });
    };

    $scope.refreshOutputsData = function () {
        $http.get(getUrlPath("taskQueryOutputs"), {
            params: {
                "taskId": $scope.taskInfoModel.id
            }
        }).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.taskOutputsModel = respData.data
            }
        });
    };

    $scope.taskAddOrUpdate = function (formData) {
        return ngInvoke.syncInvoke(getUrlPath("taskAddOrUpdate"), 'post', $scope.replaceProtocol(formData));
    };

    $scope.insertTaskParam = function () {
        if (isBlank($scope.inputParamName)) {
            layer.msg("参数不能为空");
            return;
        }
        let formData = $scope.refreshFormData();
        if (layui.$.isEmptyObject(formData)) return;
        if (isBlank($scope.taskInfoModel.id)) {
            let promise = $scope.taskAddOrUpdate(formData);
            promise.then(function (respData) {
                if (vailSuccess(respData)) {
                    $scope.taskInfoModel.id = respData.data;
                    return $scope.doInputTaskParams($scope.inputParamName, $scope.inputParamExtra);
                } else {
                    layer.msg(respData.msg);
                }
            });
        } else {
            return $scope.doInputTaskParams($scope.inputParamName, $scope.inputParamExtra);
        }
    };

    $scope.clearInputParam = function () {
        $scope.inputParamName = '';
        $scope.inputParamExtra = '';
    };


    $scope.deleteParam = function (id) {
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("taskParamDelete"), {params: {"id": id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.refreshParamsData();
                }
            });
            layer.close(index);
        });
    };

    $scope.deleteOutput = function (id) {
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("taskOutputDelete"), {params: {"id": id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.refreshOutputsData();
                }
            });
            layer.close(index);
        });
    };


    $scope.refreshFormData = function () {
        $scope.formDataSubmitFlag = true;
        $scope.taskInfoModel.isTarget ? layui.$("#taskTargetSubmit").click() : layui.$("#taskSubmit").click();
        $scope.formDataSubmitFlag = false;
        return $scope.currentFormModel;
    };

    $scope.changeSwitch = function (data) {
        data.isTarget = data.taskTarget && data.taskTarget === "on" ? true : false;
        data.doMode = data.doMode
        return data;
    };

    layui.form.on('submit(taskSubmit)', function (data) {
        if (!$scope.formDataSubmitFlag) {
            $scope.taskAddOrUpdate(data.field).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.goRoutePage("/task");
                }
            });
        } else {
            $scope.currentFormModel = $scope.changeSwitch(data.field);
        }
        return false;
    });

    //
    // layui.form.on('submit(taskTargetSubmit)', function (data) {
    //     if (!$scope.formDataSubmitFlag) {
    //         $scope.taskAddOrUpdate(data.field).then(function (respData) {
    //             layer.msg(respData.msg);
    //             if (vailSuccess(respData)) {
    //                 $scope.goRoutePage("/task");
    //             }
    //         });
    //     } else {
    //         $scope.currentFormModel = $scope.changeSwitch(data.field);
    //     }
    //     return false;
    // });

    $scope.taskAddOrUpdate = function (formData) {
        return ngInvoke.syncInvoke(getUrlPath("taskAddOrUpdate"), 'post', $scope.changeSwitch(formData));
    }

    $scope.doInputTaskOutputs = function (outputId) {
        ngInvoke.syncInvoke(getUrlPath("taskOutputInfo"), 'get', {"id": outputId}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.outputInfo = respData.data;

                $scope.temp = ["1"];

                layui.$("#source").val($scope.outputInfo.source);
                if ($scope.outputInfo.source == "PARAM") {
                    layui.$("#expression").val($scope.outputInfo.expr);
                }
                layui.$("#type").val($scope.outputInfo.type);
                $rootScope.render();
                layui.layer.open({
                    title: outputId == undefined ? "新增存储信息" : "修改存储信息",
                    type: 1,
                    zIndex: layer.zIndex,
                    shade: 0,
                    content: layui.$('#addOrEditOutputDialog'),
                    area: ['640px', '380px'],
                    btn: ['确定', '取消'],
                    yes: function (index) {
                        if (isBlank($scope.outputInfo.name)) {
                            layer.msg("存储列名不能为空");
                            return;
                        }
                        let expr = "";
                        if ($scope.outputInfo.source == "PARAM") {
                            if (layui.$("#expression").val() == null) {
                                layer.msg("请选择关联参数");
                                return;
                            }
                            expr = layui.$("#expression").val()
                        } else if ($scope.outputInfo.source == "RESPONSE") {
                            expr = layui.$("#jsonPath").val()
                        }
                        $http.post(getUrlPath("taskAddOrUpdateOutput"), $scope.requestOutputDeploy(outputId, expr)).then(function (respData) {
                            layer.msg(respData.msg);
                            if (vailSuccess(respData)) {
                                layer.close(index);
                                $scope.refreshOutputsData();
                            }
                        });
                    }
                });
            }
        });
    }


    $scope.addOrEditTaskOutput = function (outputId) {
        let formData = $scope.refreshFormData();
        if (layui.$.isEmptyObject(formData)) return;
        if (isBlank($scope.taskInfoModel.id)) {
            let promise = $scope.taskAddOrUpdate(formData);
            promise.then(function (respData) {
                if (vailSuccess(respData)) {
                    $scope.taskInfoModel.id = respData.data;
                    return $scope.doInputTaskOutputs(outputId);
                } else {
                    layer.msg(respData.msg);
                }
            });
        } else {
            return $scope.doInputTaskOutputs(outputId);
        }
    };

    $scope.requestOutputDeploy = function (outputId, expr) {
        return {
            "id": outputId,
            "taskId": $scope.taskInfoModel.id,
            "name": $scope.outputInfo.name,
            "source": $scope.outputInfo.source,
            "type": layui.$("#type").val(),
            "expr": expr
        }
    };

    $scope.$watch($scope.outputInfo, function () {
        console.log($scope.outputInfo);
    })
});