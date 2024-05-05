app.controller("httpReportController", function ($http, $scope, $rootScope) {

    $scope.selectInputModel = [];

    $scope.initFn = function () {
        $scope.doInitFn(getUrlPath("httpReportList"), getUrlPath("httpReportSearch"));
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
            let lowerName = value.httpSceneEntity.name.toLowerCase();
            let sceneId = value.httpSceneEntity.id;
            let enterLowerName = searchKey.toLowerCase();
            if (lowerName.indexOf(enterLowerName) !== -1 || isEquals(sceneId, enterLowerName)) {
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push($scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });

    $scope.export = function (id) {
        window.open(getUrlPath("reportExport") + "?ids=" + id + "&type=HTTP");
    };

    $scope.batchExport = function () {
        if ($scope.selectInputModel.length === 0) {
            layer.msg("请选择需要导出的场景");
            return;
        }
        let ids = $scope.selectInputModel.join();
        $scope.export(ids);
        $scope.reloadCurrentPage();
    };

    layui.form.on('checkbox(allReportChoose)', function (data) {
        let groupCheckbox = layui.$("input[name='singleReportId']");
        $scope.selectInputModel = [];
        if (data.elem.checked) {
            for (i = 0; i < groupCheckbox.length; i++) {
                $scope.selectInputModel.push(groupCheckbox[i].value);
                groupCheckbox[i].checked = true;
            }
        } else {
            for (i = 0; i < groupCheckbox.length; i++) {
                groupCheckbox[i].checked = false;
            }
        }
        $rootScope.render();
    });
    layui.form.on('checkbox(singleReportChoose)', function (data) {
        if (data.elem.checked) {
            $scope.selectInputModel.push(data.value);
        } else {
            $scope.selectInputModel.removeS(data.value);
        }
    });
});

app.controller("httpReportDetailController", function ($http, $scope, $routeParams) {
    $scope.detailReport = function () {
        $http.get(getUrlPath("httpReportDetail"), {params: {"id": $routeParams.id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.reportDetailModel = respData.data;
            }
        })
    }();

    $scope.showLinkDialog = function (id) {
        for (let i = 0; i < $scope.reportDetailModel.bulletEntity.length; i++) {
            if (id === $scope.reportDetailModel.bulletEntity[i].id) {
                $scope.showLinkModel = $scope.reportDetailModel.bulletEntity[i];
                break;
            }
        }
        console.log($scope.showLinkModel)
        layui.layer.open({
            title: "链路信息",
            type: 1,
            zIndex: layer.zIndex,
            shade: 0,
            content: layui.$('#linkDialog'),
            area: ['1106px', '170px']
        });
    };

    // 设置基线
    $scope.setBaseLine = function (bo) {
        $http.get(getUrlPath("httpBaseLineAddOrUpdate"), {
            params: {
                "id": $routeParams.id,
                "linkId": bo.id,
                "rt": bo.avgRt,
                "tps": bo.avgDisposeCount
            }
        }).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.reloadCurrentPage();
            }
        })
    }
});