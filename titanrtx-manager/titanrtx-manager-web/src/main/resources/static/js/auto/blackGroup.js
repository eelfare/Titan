app.controller("blackGroupController", function ($http, $scope, $rootScope, ngInvoke) {

    $scope.filterModel = {};

    $scope.initFn = function () {
        $scope.doInitFn(getUrlPath("queryBlackGroup"), getUrlPath("blackGroupSearch"));
    }();

    $scope.deleteGroup = function (id) {
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("deleteBlackGroup"), {params: {"id": id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    return $scope.reloadCurrentPage();
                }
            });
            layer.close(index);
        });
    };


    $scope.addOrEditFilterDialog = function (id) {
        ngInvoke.syncInvoke(getUrlPath("blackGroupInfo"), 'get', {"id": id}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.blackGroupModel = respData.data;
                layui.layer.open({
                    title: "过滤信息",
                    type: 1,
                    zIndex: 9999,
                    shade: 0,
                    content: layui.$('#addOrEditFilterDialog'),
                    area: ['1000px', '680px'],
                    btn: ['保存'],
                    yes: function (index) {
                        ngInvoke.syncInvoke(getUrlPath("addOrUpdateBlackGroup"), 'post', $scope.blackGroupModel).then(function (respData) {
                            if (vailSuccess(respData)) {
                                layer.close(index);
                                $scope.reloadCurrentPage();
                            } else {
                                layer.msg(respData.msg);
                            }
                        });
                    }
                });
            }
        });
    };

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
            let enterLowerName = searchKey.toLowerCase();
            if (lowerName.indexOf(enterLowerName) !== -1) {
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push($scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });


});

app.controller("blackGroupAddOrEditController", function ($http, $scope, $rootScope, $routeParams, ngInvoke, $timeout) {
    $scope.linkListModel = [];
    $scope.selectFilterModel = [];
    $scope.linkParamsModel = [];
    $scope.batchImportModel = {};

    $scope.isModify = false;


    $scope.sceneInfo = function (id) {
        $http.get(getUrlPath("blackGroupInfo"), {params: {"id": id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.showGroupInfoModel = respData.data.blackGroupEntity;
                console.log($scope.showGroupInfoModel);
                respData.data.list.forEach(function (value) {
                    $scope.selectFilterModel.push(value);
                });
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
        return $scope.sceneInfo(id)
    }();


    layui.form.on('submit(groupSubmit)', function (data) {
        let formData = data.field;
        formData.id = $scope.showGroupInfoModel.id;
        console.log(formData);
        $http.post(getUrlPath("addOrUpdateBlackGroup"), formData).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.goRoutePage("/blackGroup", {});
            }
        });
        return false;
    });


    $scope.deleteFilter = function (id) {
        console.log(id)
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("deleteFilter"), {params: {"id": id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    return $scope.reloadCurrentPage();
                }
            });
            layer.close(index);
        });
    };


    $scope.addOrEditRecordDialog = function (id) {
        if ($scope.showGroupInfoModel === undefined) {
            layer.msg("请先输入分组名称");
            return;
        } else if ($scope.showGroupInfoModel.name === undefined) {
            layer.msg("请先输入分组名称");
            return;
        }
        ngInvoke.syncInvoke(getUrlPath("blackListInfo"), 'get', {
            "id": id,
            "groupId": $scope.showGroupInfoModel.id,
            "groupName": $scope.showGroupInfoModel.name
        }).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.recordModel = respData.data;
                console.log(respData.data);
                layui.layer.open({
                    title: "黑名单信息",
                    type: 1,
                    zIndex: 9999,
                    shade: 0,
                    content: layui.$('#addOrEditBlackListDialog'),
                    area: ['1000px', '680px'],
                    btn: ['保存'],
                    yes: function (index) {
                        ngInvoke.syncInvoke(getUrlPath("addOrUpdateBlackList"), 'post', $scope.recordModel).then(function (respData) {
                            console.log($scope.showGroupInfoModel);
                            if (vailSuccess(respData)) {
                                layer.close(index);
                                $scope.goRoutePage('/blackGroupAddOrEdit', {'id': respData.data})
                                $scope.reloadCurrentPage();
                            } else {
                                layer.msg(respData.msg);
                            }
                        });
                    }
                });
            }
        });
    };
});