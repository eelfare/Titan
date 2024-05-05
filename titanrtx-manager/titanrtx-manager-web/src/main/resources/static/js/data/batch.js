app.controller("batchController", function ($http, $scope, $rootScope) {
    $scope.gradingListModel = [
        {"id": "PRECISE", "name": "精确"},
        {"id": "EVERYDAY", "name": "每天"},
        {"id": "EVERYHOUR", "name": "每小时"},
        {"id": "EVERYMINUTE", "name": "每分钟"}
    ]

    $scope.selectedGrading = {"id": "PRECISE", "name": "精确"};

    $scope.initFn = function () {
        $scope.doInitFn(getUrlPath("batchList"), getUrlPath("batchSearch"));
        layui.laydate.render({
            elem: '#batchDispatchTime',
            type: 'datetime',
            min: getBeginToday(),
            value: getNowPreMinuter(0)
        });
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
            let batchId = value.id + "";
            let enterLowerName = searchKey.toLowerCase();
            if (lowerName.indexOf(enterLowerName) !== -1 || isEquals(batchId, enterLowerName)) {
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push($scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });


    $scope.batchStart = function (id, index) {
        layui.layer.load();
        $http.get(getUrlPath("batchStart"), {params: {"id": id}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.showModel[index].status = 1;

            }
            layui.layer.closeAll('loading');
        })
    };

    $scope.batchStop = function (id) {
        layui.layer.load();
        $http.get(getUrlPath("batchStop"), {params: {"id": id}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.routePage($rootScope.path);
            }
            layui.layer.closeAll('loading');
        })
    };

    // 导出数据
    $scope.exportParams = function (id, index) {
        layui.layer.load();
        $http.get(getUrlPath("batchExport"), {params: {"batchId": id}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.showModel[index].status = 5;
                console.log(respData)
            }
            layui.layer.closeAll('loading');
        });
    }
    // 重置批次
    $scope.resetBatch = function (id, index) {
        layer.confirm('确定重置当前批次信息?', {icon: 3, title: '提示'}, function (index) {
            layui.layer.load();
            $http.get(getUrlPath("resetBatch"), {params: {"batchId": id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.reloadCurrentPage();
                }
                layui.layer.closeAll('loading');
            });
            layer.close(index);
        });
    }

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
    // 调度
    $scope.addDispatch = function (batch) {
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
                    "businessId": batch.id,
                    "type":2,
                    "name": batch.name,
                    "time": $scope.selectedGrading.id == "PRECISE"?
                        layui.$("#executeTimePrecise").val():($scope.selectedGrading.id == "EVERYDAY"?
                            layui.$("#executeTimeEveryday").val():($scope.selectedGrading.id == "EVERYHOUR"?
                                layui.$("#executeTimeEveryHour").val():0)),
                    "grading": $scope.selectedGrading.id
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

app.controller("batchAddOrEditController", function ($http, $scope, $rootScope, $routeParams, $timeout, ngInvoke) {

    $scope.batchInfoModel = {};
    $scope.formDataSubmitFlag = false;
    $scope.currentFormModel = {};
    $scope.tastEntitiesModel = [];
    $scope.taskListModel = [];
    $scope.targetTaskModel = {};
    $scope.batchOwnTaskModel = {};
    $scope.templateParamModel = [];


    $scope.batchInfo = function (id) {
        $http.get(getUrlPath("batchInfo"), {params: {"id": id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.batchInfoModel = respData.data.batchEntity;
                $scope.targetTaskModel = respData.data.targetTask;
                $scope.buildTaskEntitiesPage(respData.data.taskEntities);
            } else if (respData.code == 10021) {
                layer.msg(respData.msg);
                $scope.batchInfoModel = respData.data.batchEntity;
                $scope.targetTaskModel = respData.data.targetTask;
                $scope.buildTaskEntitiesPage(respData.data.taskEntities);
            }
        })
    };


    $scope.taskList = function () {
        $http.get(getUrlPath("taskList"), {}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.taskListModel = respData.data;
            }
        })
    };

    $scope.initFn = function () {
        // 获取所有的任务
        $scope.taskList();
        let id = $routeParams.id;
        if (isBlank(id)) {
            $scope.isEdit = false;
            return;
        } else {
            $scope.isEdit = true;
        }
        return $scope.batchInfo(id)
    }();

    $scope.buildTaskEntitiesPage = function (data) {
        $scope.tastEntitiesModel = data;
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

    $scope.insertOrAddTask = function (formData) {
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
            let promise = $scope.insertOrAddTask(formData);
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


    $scope.refreshFormData = function () {
        $scope.formDataSubmitFlag = true;
        layui.$("#batchSubmit").click();
        $scope.formDataSubmitFlag = false;
        return $scope.currentFormModel;
    };

    $scope.getFormData = function (formData) {
        let taskIds = []
        if ($scope.targetTaskModel && $scope.targetTaskModel.id) {
            formData.target = $scope.targetTaskModel.id
        }
        $scope.tastEntitiesModel.forEach(function (value) {
            taskIds.push(value.id);
        });
        if (taskIds.length === 0) {
            layer.msg("批次中需要最少一个任务");
            return;
        }
        formData.tasks = taskIds.join();
        return formData;
    };

    layui.form.on('submit(batchSubmit)', function (data) {
        if (!$scope.formDataSubmitFlag) {
            $scope.batchAddOrUpdate(data.field).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.goRoutePage("/batch");
                }
            });
        } else {
            $scope.currentFormModel = $scope.getFormData(data.field)
        }
        return false;
    });

    $scope.doQueryTaskParamSource = function (taskId) {
        ngInvoke.syncInvoke(getUrlPath("queryTaskParamSource"), 'get', {
            "batchId": $scope.batchInfoModel.id,
            "taskId": taskId
        }).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.batchWithTaskModel = respData.data.taskEntity;
                $scope.paramSourceBosModel = respData.data.paramSourceBos;
                $scope.batchOwnTaskModel = respData.data.batchOwnTask;

                layui.$("#targetBatchTask").val($scope.batchWithTaskModel.id);
                $rootScope.render();
                layui.layer.open({
                    title: "配置任务参数映射",
                    type: 1,
                    zIndex: layer.zIndex,
                    shade: 0,
                    content: layui.$('#addOrEditTaskParamSourceDialog'),
                    area: ['1240px', '680px'],
                    btn: ['确定', '取消'],
                    yes: function (index) {
                        let params = {}
                        params.id = respData.data.id;
                        params.batchId = $scope.batchInfoModel.id;
                        params.taskId = taskId;
                        let paramsSource = [];
                        let error = false;
                        $scope.paramSourceBosModel.filter(function (param) {
                            let has = false;
                            $scope.batchWithTaskModel.listParamDeploy.forEach((value => {
                                if (isEquals(param.paramName, value.name)) {
                                    has = true;
                                }
                            }))
                            return has;
                        }).forEach(function (value) {
                            if (error) {
                                return;
                            }
                            let temp = value.paramName + "&"
                            if (layui.$("#paramType" + $scope.paramSourceBosModel.indexOf(value)).val()) {
                                temp = temp + layui.$("#paramType" + $scope.paramSourceBosModel.indexOf(value)).val() + "&"
                            } else {
                                layer.msg("请设置参数" + value.paramName + "的使用方式");
                                error = true;
                                return;
                            }
                            if (!value.sqlDeployBo) {
                                layer.msg("请设置参数" + value.paramName + "MySQL对应配置信息");
                                error = true;
                                return;
                            }

                            console.log($scope.selectAllColumn)

                            let selectAll = value.sqlDeployBo.selectAll ? '1&' : '0&'
                            temp = temp + selectAll

                            if (!value.sqlDeployBo.columnOutput) {
                                layer.msg("请设置参数" + value.paramName + "关联" + value.sqlDeployBo.taskEntity.name + "任务对应的数据表的列");
                                error = true;
                                return;
                            }
                            temp = temp + value.sqlDeployBo.taskEntity.id + "&" + value.sqlDeployBo.columnOutput.name

                            if (value.sqlDeployBo.filter) {
                                temp = temp + "&" + value.sqlDeployBo.filter.name
                            }
                            paramsSource.push(temp);
                        });
                        if (!error) {
                            params.paramsSource = paramsSource.join()
                            console.log(params);
                            $http.post(getUrlPath("batchAddOrUpdateParamSource"), params).then(function (respData) {
                                layer.msg(respData.msg);
                                if (vailSuccess(respData)) {
                                    layer.close(index);
                                }
                            });
                        }
                    }
                });
            }
        });
    }


    $scope.addOrEditTaskParamSource = function (taskId) {
        let formData = $scope.refreshFormData();
        if (layui.$.isEmptyObject(formData)) return;
        let promise = $scope.batchAddOrUpdate(formData);
        promise.then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.batchInfoModel.id = respData.data;
                $scope.doQueryTaskParamSource(taskId);
            }
        });
    };

    $scope.moveTask = function (taskId, type) {
        $http.get(getUrlPath("moveTask"), {params: {"batchId":$routeParams.id, "taskId": taskId,"type": type}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.reloadCurrentPage();
            }
        });
    };

    $scope.deleteBatchWithTask = function (task) {
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            if ($scope.targetTaskModel && task.id == $scope.targetTaskModel.id) {
                $scope.targetTaskModel = undefined;
            }
            $scope.tastEntitiesModel.removeS(task);
            $scope.render();
            layer.close(index);
        });
    };

    $scope.requestOutputDeploy = function (outputId, expr) {
        return {
            "id": outputId,
            "taskId": $scope.taskInfoModel.id,
            "name": $scope.outputInfo.name,
            "source": layui.$("#source").val(),
            "type": layui.$("#type").val(),
            "expr": expr
        }
    };
    $scope.onchange = function (data) {
        console.log(data)
    }

    $scope.enterEvent = function (e) {
        let keycode = window.event ? e.keyCode : e.which;
        if (keycode === 13) {
            let searchKey = $scope.searchTaskMode;
            if (isBlank(searchKey)) return;
            $http.get(getUrlPath("taskSearch"), {params: {"key": searchKey}}).then(function (respData) {
                if (vailSuccess(respData)) {
                    $scope.showTaskModel = respData.data;
                    layui.$(".searchTaskNameSelect").show();
                }
            })
        }
    };


    $scope.$watch('searchTaskMode', function () {
        let searchKey = $scope.searchTaskMode;
        if (isNotBlank(searchKey)) {
            let showServiceArray = $scope.searchTaskHelper($scope.taskListModel, searchKey);
            if (showServiceArray.length > 0) {
                $scope.showTaskModel = showServiceArray;
                layui.$(".searchTaskNameSelect").show();
            } else {
                layui.$(".searchTaskNameSelect").hide();
            }
        } else {
            layui.$(".searchTaskNameSelect").hide();
        }
    });


    $scope.searchTaskHelper = function (data, name) {
        let showArray = [];
        let trimName = name.trim().toLowerCase();
        for (let j = 0; j < data.length; j++) {
            let taskModel = data[j];
            let lowerName = taskModel.name.toLowerCase();
            let id = taskModel.id;
            let lowerTableName = taskModel.tableName.toLowerCase();
            if (isEquals(id, name) || lowerName.indexOf(trimName) !== -1 || lowerTableName.indexOf(trimName) !== -1) {
                if (showArray.length <= 10) {
                    showArray.push(taskModel);
                } else {
                    break;
                }
            }
        }
        return showArray;
    };

    $scope.selectTask = function (taskModel) {
        for (let i = 0; i < $scope.tastEntitiesModel.length; i++) {
            if ($scope.tastEntitiesModel[i].id === taskModel.id) {
                return layer.msg("任务已存在列表中");
            }
        }
        if (taskModel.isTarget) {
            $scope.targetTaskModel = taskModel
        }
        $scope.tastEntitiesModel.push(taskModel);
        layui.$(".searchTaskNameSelect").hide();
    };

    $scope.batchAddOrUpdate = function (formData) {
        let taskIds = []
        if ($scope.targetTaskModel && $scope.targetTaskModel.id) {
            formData.target = $scope.targetTaskModel.id
        }
        $scope.tastEntitiesModel.forEach(function (value) {
            taskIds.push(value.id);
        });
        if (taskIds.length === 0) {
            layer.msg("批次中需要最少一个任务");
            return;
        }
        formData.tasks = taskIds.join();
        return $http.post(getUrlPath("batchAddOrUpdate"), formData);
    }
});