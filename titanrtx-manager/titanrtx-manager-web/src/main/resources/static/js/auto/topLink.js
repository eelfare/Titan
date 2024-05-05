app.controller("topLinkController", function ($http, $scope, $rootScope, ngInvoke) {

    $scope.selectInputModel = [];
    $scope.selectScaleModel = [];

    $scope.requestParams = function () {
        let number = layui.$("#topNumber").val();
        if (isBlank(number)) {
            layer.msg("请输入TOP数量");
            return;
        }
        $rootScope.pageSize = number;

        return {
            "params": {
                "domain": layui.$("#domainName").val(),
                "blackGroupId": layui.$("#blackGroupId").val(),
                "startTime": layui.$("#topLinkStartTime").val(),
                "endTime": layui.$("#topLinkEndTime").val(),
                "number": number,
                "fileName": $scope.fileName
            }
        }
    };

    $scope.initFn = function () {
        console.log("toplink initFn")
        // layui.laydate.render({elem:'#topLinkStartTime', type: 'datetime',min: getBeginToday(), value:getBeginToday()});
        layui.laydate.render({
            elem: '#topLinkStartTime',
            type: 'datetime',
            min: getBeginToday(),
            value: getNowPreMinuter(120),
            min: '2018-1-1',
            max: getNowPreMinuter(0),
        });
        layui.laydate.render({
            elem: '#topLinkEndTime',
            type: 'datetime',
            min: getBeginToday(),
            value: getNowPreMinuter(0),
            min: '2018-1-1',
            max: getNowPreMinuter(0),
        });
        // 根据新需求，关闭进入排行榜就去搜索的功能
        // $scope.doInitFn(getUrlPath("topLinkList"),null,$scope.requestParams());
        $http.get(getUrlPath("topLinkDomains")).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.topLinkDomainModel = respData.data;
                $rootScope.render();
            }
        });
        // 获取过滤分组
        $http.get(getUrlPath("queryBlackGroup")).then(function (respData) {
            if (vailSuccess(respData)) {
                console.log(respData);
                $scope.blackGroupModel = respData.data;
                $rootScope.render();
            }
        });
        $scope.isUseSearch = false;
        $scope.searchNum = 50;
    }();

    $scope.doRequestBt = function () {
        $scope.selectInputModel = [];
        $scope.selectScaleModel = [];
        console.log($scope.requestParams());
        $scope.doInitFn(getUrlPath("topLinkList"), getUrlPath("linkSearch"), $scope.requestParams());
    };


    layui.form.on('checkbox(allTopChoose)', function (data) {
        let groupCheckbox = layui.$("input[name='singleTopId']");
        $scope.selectInputModel = [];
        $scope.selectReqNum = [];
        $scope.selectedNum = 0;
        if (data.elem.checked) {
            for (i = 0; i < groupCheckbox.length; i++) {
                if (!groupCheckbox[i].disabled) {
                    let splitVal = groupCheckbox[i].value.split(",")
                    $scope.selectInputModel.push(splitVal[0]);
                    $scope.selectScaleModel.push(splitVal[1]);
                    groupCheckbox[i].checked = true;
                    $scope.selectedNum++;
                }
            }
        } else {
            for (i = 0; i < groupCheckbox.length; i++) {
                if (!groupCheckbox[i].disabled) {
                    groupCheckbox[i].checked = false;
                }
            }
        }
        console.log($scope.selectedNum);
        $rootScope.render();
    });
    layui.form.on('checkbox(singleTopChoose)', function (data) {
        console.log(data)
        let splitVal = data.value.split(",")
        if (data.elem.checked) {
            $scope.selectInputModel.push(splitVal[0]);
            $scope.selectScaleModel.push(splitVal[1]);
            $scope.selectedNum++;
        } else {
            $scope.selectInputModel.removeS(splitVal[0]);
            $scope.selectScaleModel.removeS(splitVal[1]);
            $scope.selectedNum--;
        }
        console.log($scope.selectedNum);
        $rootScope.render();
    });

    $scope.batchTurnScene = function () {
        if ($scope.selectInputModel.length <= 0) {
            layer.msg("请选择链路");
            return;
        }
        let linkParamsNum = layui.$("#linkParamsNum").val();
        let linkParamsTreaty = layui.$("#linkTreaty").val();
        $scope.inputSceneNameDialogDialog($scope.selectInputModel, $scope.selectScaleModel, linkParamsNum, linkParamsTreaty);
    };

    $scope.inputSceneNameDialogDialog = function (urlArray, scaleArray, linkParamsNum, linkParamsTreaty) {
        layui.layer.open({
            title: "场景名称",
            type: 1,
            zIndex: 9999,
            shade: 0,
            content: layui.$('#inputSceneNameDialog'),
            area: ['420px', '200px'],
            btn: ['保存'],
            yes: function (index) {
                if (isBlank($scope.inputSceneNameModel)) {
                    layer.msg("场景名不能为空");
                    return;
                }
                $scope.doTurnScene(urlArray, scaleArray, linkParamsNum, linkParamsTreaty, $scope.inputSceneNameModel, index);
            }
        });
    };

    $scope.turnScene = function (url) {
        let urlArray = [];
        urlArray.push(url);
        $scope.inputSceneNameDialogDialog(urlArray);
    };

    $scope.doTurnScene = function (urlArray, scaleArray, linkParamsNum, linkParamsTreaty, sceneName, index) {
        layui.layer.load();
        ngInvoke.syncInvoke(getUrlPath("topTurnSceneByUrl"), 'post', {
            "url": urlArray,
            "scale": scaleArray,
            "linkParamsNum": linkParamsNum,
            "linkParamsTreaty": linkParamsTreaty,
            "name": sceneName
        }).then(function (respData) {
            layui.layer.closeAll('loading');
            if (vailSuccess(respData)) {
                layer.close(index);
                let id = respData.data;
                $scope.goRoutePage("httpSceneAddOrEdit", {"id": id});
            } else {
                layer.msg(respData.msg)
            }
        });
    };


    $scope.topLinkRemove = function (path) {
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("topLinkRemove"), {params: {"path": path}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    return $scope.reloadCurrentPage();
                }
            });
            layer.close(index);
        });
    };

    $scope.useSearch = function () {
        $scope.isUseSearch = true;
    }

    $scope.$watch('searchModel', function () {
        let searchKey = $scope.searchModel;
        if (isBlank(searchKey)) {
            if (!$scope.isUseSearch) { // 这里需要特殊处理一下
                $scope.$parent.searchModelTotalData = [];
            }
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

    $scope.selectFile = function (files) {
        if (files.length) {
            let file = files[0];
            $scope.fileName = '已选中文件：' + files[0].name;
            let reader = new FileReader();
            reader.readAsText(file, 'UTF-8');
            reader.onload = function (e) {
                $scope.fileName = file.name;
                let sourceContent = e.target.result.replace(/\ +/g, "").replace(/\ +/g, "");
                console.log(sourceContent);
                $scope.searchNum = sourceContent.split("\n").length;

                $http.post(getUrlPath("topLinkUpload"), {"sourceContent": sourceContent}).then(function (respData) {
                });
            }
        }
        $rootScope.render();
    }

    // 计算有效链接数
    $scope.$on('ngRepeatFinished', function () {
        let size = $scope.showModel.length;
        $scope.selectedNum = 0;
        $scope.availabilityLength = 0;
        for (let i = 0; i < size; i++) {
            if (!$scope.showModel[i].filter) {
                $scope.availabilityLength++;
            }
        }
    });

    $scope.changeSelected = function (selectedNum) {
        console.log(selectedNum);
        let groupCheckbox = layui.$("input[name='singleTopId']");
        if (selectedNum == undefined) {
            layer.msg("超出有效链路数");
            for (let i = 0; i < groupCheckbox.length; i++) {
                if (!groupCheckbox[i].disabled) {
                    groupCheckbox[i].checked = false;
                }
            }
            $rootScope.render();
            return;
        }
        $scope.selectedNum = selectedNum;
        $scope.selectInputModel = [];
        $scope.selectScaleModel = [];
        let sum = 0;
        if (selectedNum > 0) {
            for (let i = 0; i < groupCheckbox.length; i++) {
                if (!groupCheckbox[i].disabled) {
                    if (sum < selectedNum) {
                        let splitVal = groupCheckbox[i].value.split(",")
                        $scope.selectInputModel.push(splitVal[0]);
                        $scope.selectScaleModel.push(splitVal[1]);
                        groupCheckbox[i].checked = true;
                        sum++;
                    } else {
                        groupCheckbox[i].checked = false;
                    }
                }
            }
        } else {
            for (let i = 0; i < groupCheckbox.length; i++) {
                if (!groupCheckbox[i].disabled) {
                    groupCheckbox[i].checked = false;
                }
            }
        }
        $rootScope.render();
    }
});

app.directive('onFinishRender', function ($timeout) {
    return {
        restrict: 'A',
        link: function (scope, element, attr) {
            if (scope.$last === true) {
                $timeout(function () {
                    scope.$emit('ngRepeatFinished');
                });
            }
        }
    }
});


