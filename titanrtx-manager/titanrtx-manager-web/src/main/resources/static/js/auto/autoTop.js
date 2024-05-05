app.controller("autoTopController", function ($http, $scope, $rootScope, ngInvoke) {

    $scope.selectInputModel = [];
    $scope.selectScaleModel = [];

    $scope.sourceContent = "";

    $scope.initFn = function () {
        $rootScope.pageSize = 500;

        $http.get(getUrlPath("autoTop300StressStatus"), null).then(function (respData) {
            let result = respData.data;
            console.log("top stress status : " + result);
            $scope.top300Switch = result;
        });

        // 查询是否需要有人工操作的top link
        $scope.doInitFn(getUrlPath("autoTopLinkList"), null, null);
    }();

    $scope.doRequestBt = function () {
        $scope.selectInputModel = [];
        $scope.selectScaleModel = [];
        console.log($scope.requestParams());
        $scope.doInitFn(getUrlPath("topLinkList"), getUrlPath("linkSearch"), $scope.requestParams());
    };

    // 监听开关的状态
    $scope.top300Switch = false;
    $scope.$watch('top300Switch', function () {
        $http.get(getUrlPath("autoTop300StressSwitch"), {
            params: {
                "topSwitch": $scope.top300Switch
            }
        }).then(function (respData) {
            if (!vailSuccess(respData)) {
                layer.msg(respData.msg);
            }
        });
    });

    layui.form.on('checkbox(allTopChoose)', function (data) {
        let groupCheckbox = layui.$("input[name='singleTopId']");
        $scope.selectedNum = 300;
        $scope.selectInputModel = [];
        $scope.selectScaleModel = [];
        let sum = 0;

        if (data.elem.checked) {
            if ($scope.selectedNum > 0) {
                for (let i = 0; i < groupCheckbox.length; i++) {
                    if (!groupCheckbox[i].disabled) {
                        if (sum < $scope.selectedNum) {
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
        } else {
            for (i = 0; i < groupCheckbox.length; i++) {
                if (!groupCheckbox[i].disabled) {
                    groupCheckbox[i].checked = false;
                }
            }
        }
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

    $scope.confirmAutoTopLink = function () {
        if ($scope.selectInputModel.length <= 0) {
            layer.msg("请选择需要压测的TOP链路");
            return;
        }
        let params = {}
        params.url = $scope.selectInputModel;
        if ($scope.selectInputModel.length == 300) {
            $http.post(getUrlPath("autoTopLinkConfirm"), params).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    return $scope.reloadCurrentPage();
                }
            });
        } else {
            layer.confirm('确定不满足TOP300条链路也进行压测？', {icon: 3, title: '提示'}, function (index) {
                $http.post(getUrlPath("autoTopLinkConfirm"), params).then(function (respData) {
                    layer.msg(respData.msg);
                    if (vailSuccess(respData)) {
                        return $scope.reloadCurrentPage();
                    }
                });
                layer.close(index);
            });
        }
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

    $scope.monkey = function () {
        console.log("asdf");
    }

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

    $scope.autoTopLinkAddBlack = function (domain, path) {
        layer.confirm('确定将该链路添加到默认的黑名单分组中？', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("autoTopLinkAddBlack"), {
                params: {
                    "path": path,
                    "domain": domain
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

    $scope.autoTopLinkAddWhite = function (domain, path) {
        layer.confirm('确定将该链路添加到白名单中?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("autoTopLinkAddWhite"), {
                params: {
                    "path": path,
                    "domain": domain
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

    $scope.autoTopLinkRemove = function (domain, path) {
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath("autoTopLinkRemove"), {
                params: {
                    "path": path,
                    "domain": domain
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
                $scope.sourceContent = e.target.result.replace(/\ +/g, "").replace(/\ +/g, "");
                console.log($scope.sourceContent);
                $scope.searchNum = $scope.sourceContent.split("\n").length;
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


