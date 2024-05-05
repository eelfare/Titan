app.controller("linkController", function ($http, $scope) {

    $scope.initFn = function () {
        $scope.doInitFn(getUrlPath("linkList"), getUrlPath("linkSearch"));
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
            let lowerUrl = value.url.toLowerCase();
            let linkId = value.id;
            let enterLowerName = searchKey.toLowerCase();
            if (lowerName.indexOf(enterLowerName) !== -1 || lowerUrl.indexOf(enterLowerName) !== -1 || isEquals(linkId, enterLowerName)) {
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push($scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });

});

app.controller("linkAddOrEditController", function ($http, $scope, $rootScope, $routeParams, $timeout, ngInvoke) {

    $scope.linkInfoModel = {};
    $scope.formDataSubmitFlag = false;
    $scope.currentFormModel = {};
    $scope.batchImportModel = {};
    $scope.linkParamsModel = [];


    $scope.linkInfo = function (id) {
        $http.get(getUrlPath("linkInfo"), {params: {"id": id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.linkInfoModel = respData.data.link;
                $scope.buildTablePageBar(respData.data.params);
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
        return $scope.linkInfo(id)
    }();

    $scope.refreshParamsData = function (currentPage) {
        $http.get(getUrlPath("linkQueryParams"), {
            params: {
                "linkId": $scope.linkInfoModel.id,
                "currentPage": currentPage
            }
        }).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.buildTablePageBar(respData.data);
            }
        });
    };

    $scope.buildTablePageBar = function (data) {
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
                if (!first) {
                    $scope.refreshParamsData(obj.curr);
                }
            }
        })
    };

    $scope.getModelParam = function (id) {
        let modelParam = undefined;
        $scope.linkParamsModel.forEach(function (value) {
            if (isEquals(value.id, id)) {
                modelParam = value.param;
                return modelParam;
            }
        });
        return modelParam;
    };

    $scope.doParamCase = function (requestParam) {
        let formData = $scope.refreshFormData();
        if (layui.$.isEmptyObject(formData)) return;
        $scope.doRequestCase(getUrlPath("doHttpParamCase"), {"link": formData, "requestParam": requestParam});
    };

    $scope.doPreParamCase = function (paramId) {
        return $scope.doParamCase($scope.getModelParam(paramId));
    };
    $scope.doInputParamCase = function () {
        return $scope.doParamCase($scope.inputParamModel);
    };


    $scope.doInputLinkParams = function (params, index) {
        $http.post(getUrlPath("linkAddParams"), {
            "id": $scope.linkInfoModel.id,
            "params": params
        }).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.refreshParamsData($scope.currentPage);
                if (isNotBlank(index)) {
                    layer.close(index);
                }
            }
        });
    };

    $scope.updateHttpParam = function (paramId) {
        let modelParam = $scope.getModelParam(paramId);
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
                return $scope.refreshParamsData($scope.currentPage);
            }
        });
    };

    $scope.insertOrAddLink = function (formData) {
        return ngInvoke.syncInvoke(getUrlPath("linkAddOrUpdate"), 'post', $scope.replaceProtocol(formData));
    };

    $scope.insertLinkParam = function () {
        if (isBlank($scope.inputParamModel)) {
            layer.msg("参数不能为空");
            return;
        }
        let formData = $scope.refreshFormData();
        if (layui.$.isEmptyObject(formData)) return;
        if (isBlank($scope.linkInfoModel.id)) {
            let promise = $scope.insertOrAddLink(formData);
            promise.then(function (respData) {
                if (vailSuccess(respData)) {
                    $scope.linkInfoModel.id = respData.data;
                    return $scope.doInputLinkParams($scope.inputParamModel);
                } else {
                    layer.msg(respData.msg);
                }
            });
        } else {
            return $scope.doInputLinkParams($scope.inputParamModel);
        }
    };

    $scope.clearInputParam = function () {
        $scope.inputParamModel = '';
    };


    $scope.deleteParam = function (id) {
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("linkParamDelete"), {params: {"id": id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.refreshParamsData($scope.currentPage);
                }
            });
            layer.close(index);
        });
    };


    $scope.batchDeleteParams = function (linkId) {
        layer.confirm('确定删除全部参数?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("linkClearParam"), {params: {"linkId": linkId}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.refreshParamsData($scope.currentPage);
                }
            });
            layer.close(index);
        });
    };

    $scope.refreshFormData = function () {
        $scope.formDataSubmitFlag = true;
        layui.$("#linkSubmit").click();
        $scope.formDataSubmitFlag = false;
        return $scope.currentFormModel;
    };

    $scope.replaceProtocol = function (data) {
        data.url = data.url.trim().replace("http://", "");
        return data;
    };

    layui.form.on('submit(linkSubmit)', function (data) {
        if (!$scope.formDataSubmitFlag) {
            $scope.insertOrAddLink(data.field).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.goRoutePage("/link");
                }
            });
        } else {
            $scope.currentFormModel = $scope.replaceProtocol(data.field);
        }
        return false;
    });

    $scope.batchDoParamCase = function () {
        //参数为空时 默认提供测试
        if ($scope.linkParamsModel.length === 0) {
            return $scope.doInputParamCase();
        }
        for (let i = 0; i < $scope.linkParamsModel.length; i++) {
            $timeout(function () {
                layer.msg("ID:" + ($scope.linkParamsModel[i].id) + "响应结果:");
                $scope.doParamCase($scope.linkParamsModel[i].param)
            }, i * 3000);
        }
    };


    $scope.batchImportParams = function () {
        let formData = $scope.refreshFormData();
        if (layui.$.isEmptyObject(formData)) return;
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
                /**
                 * 当隐藏域ID为空时需要先提交当前链路信息保存返回id作为凭证
                 */
                let id = $scope.linkInfoModel.id;
                if (isBlank(id)) {
                    $scope.insertOrAddLink(formData).then(function (respData) {
                        if (vailSuccess(respData)) {
                            $scope.linkInfoModel.id = respData.data;
                            return $scope.preDoInputLinkParams(index);
                        } else {
                            layer.msg(respData.msg);
                        }
                    });
                } else {
                    return $scope.preDoInputLinkParams(index);
                }
            },
            end: function () {
                $scope.batchImportModel = {};
            }
        });
    };

    $scope.preDoInputLinkParams = function (index) {
        let inputData = $scope.batchImportModel.inputData;
        if (isNotBlank($scope.batchImportModel.template)) {
            inputData = formatDataHelper($scope.batchImportModel.template, inputData);
        }
        return $scope.doInputLinkParams(inputData, index);
    };


    $scope.pullHttParams = function () {
        let formData = $scope.refreshFormData();
        if (layui.$.isEmptyObject(formData)) return;
        let fullUrl = formData.protocol + "://" + formData.url;
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
                        let id = $scope.linkInfoModel.id;
                        if (isBlank(id)) {
                            $scope.insertOrAddLink(formData).then(function (respData) {
                                if (vailSuccess(respData)) {
                                    $scope.linkInfoModel.id = respData.data;
                                    return $scope.doPullParams(fullUrl, index)
                                } else {
                                    layer.msg(respData.msg);
                                }
                            });
                        } else {
                            return $scope.doPullParams(fullUrl, index)
                        }
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
    $scope.doPullParams = function (fullUrl, index) {
        $http.get(getUrlPath("topLinkPullParams"), {
            params: {
                "id": $scope.linkInfoModel.id,
                "url": fullUrl,
                "size": $scope.inputPullParamsSizeModel
            }
        }).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                layer.close(index);
                $scope.refreshParamsData($scope.currentPage);
            }
        });
    }

});


app.controller("linkParamOrderController", function ($http, $scope) {

    $scope.initFn = function () {
        $scope.doInitFn(getUrlPath("linkParamOrder"));
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
            let lowerUrl = value.url.toLowerCase();
            let linkId = value.id;
            let count = value.count;
            let enterLowerName = searchKey.toLowerCase();
            if (lowerName.indexOf(enterLowerName) !== -1 || lowerUrl.indexOf(enterLowerName) !== -1 || isEquals(linkId, enterLowerName)) {
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push($scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });

});