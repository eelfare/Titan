app.controller("autoTestDeployController", function ($http, $scope, $rootScope, ngInvoke) {

    $scope.testDeployModel = {};

    $scope.gradingListModel = [
        {"id": "PRECISE", "name": "精确"},
        {"id": "EVERYDAY", "name": "每天"},
        {"id": "EVERYHOUR", "name": "每小时"},
        {"id": "EVERYMINUTE", "name": "每分钟"}
    ]

    $scope.selectedGrading = {"id": "PRECISE", "name": "精确"};

    $scope.initFn = function () {
        $scope.doInitFn(getUrlPath("autoDeployList"), '');
    }();

    $scope.deleteDeploy = function (id, history) {
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("autoTestDeployDelete"), {
                params: {
                    "id": id,
                    "history": history
                }
            }).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    return $scope.reloadCurrentPage();
                }
            });
            layer.close(index);
        });
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
    $scope.editAutoTest = function (deploy) {
        $scope.everyDay = true;
        $scope.concurrentDeploy = deploy;
        layui.laydate.render({
            elem: '#executeTimePrecise',
            type: 'datetime',
            min: getBeginToday(),
            value: getNowPreMinuter(0)
        });
        layui.layer.open({
            title: "修改调度配置",
            type: 1,
            zIndex: 9999,
            shade: 0,
            content: layui.$('#editDeployDialog'),
            area: ['600px', '400px'],
            btn: ['保存'],
            yes: function (index) {
                $http.post(getUrlPath("autoDeployAddOrEdit"), {
                    "id": deploy.id,
                    "businessId": deploy.batch ? deploy.batchId : deploy.sceneId,
                    "name": deploy.name,
                    "time": $scope.selectedGrading.id == "PRECISE"?
                        layui.$("#executeTimePrecise").val():($scope.selectedGrading.id == "EVERYDAY"?
                            layui.$("#executeTimeEveryday").val():($scope.selectedGrading.id == "EVERYHOUR"?
                                layui.$("#executeTimeEveryHour").val():0)),
                    "grading": $scope.selectedGrading.id,
                    "continuousTime": deploy.batch ? 0 : deploy.continuousTime,
                    "type": deploy.batch ? 2 : (deploy.commonStress ? 0 : 1),
                }).then(function (respData) {
                    layer.msg(respData.msg);
                    if (vailSuccess(respData)) {
                        layer.close(index);
                        $scope.reloadCurrentPage();
                    }
                })
            }
        });
    };

    layui.form.on('select(rulesTypeFilter)', function (elem) {
        $scope.rulesModel.rulesType = elem.value;
        $rootScope.render();
    });

    $scope.$watch('searchModel', function () {
        let searchKey = $scope.searchModel;
        if (isBlank(searchKey)) {
            $scope.resetTablePage();
            return;
        }
        let totalFilterIndexData = [];
        let targetIndex = [];
        $scope.searchModelTotalData.forEach(function (value, index) {
            let lowerDomain = value.domain.toLowerCase();
            let lowerPath = value.path.toLowerCase();
            let enterLowerName = searchKey.toLowerCase();
            if (lowerDomain.indexOf(enterLowerName) !== -1 || lowerPath.indexOf(enterLowerName) !== -1) {
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push($scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });


});

