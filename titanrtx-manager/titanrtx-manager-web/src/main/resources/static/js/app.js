var app = angular.module('app', ['ngRoute']).run(function ($rootScope, $location, $timeout) {
    $rootScope.$on('$routeChangeSuccess', function () {
        let pathArray = $location.url().split("/");
        let index = pathArray.indexOf("");
        if (index >= 0) {
            pathArray.remove(index);
        }
        let paramsPath = pathArray[0];
        if (paramsPath.indexOf("?") >= 0) {
            let paramsPathArray = paramsPath.split("?");
            $rootScope.path = paramsPathArray[0];
        } else {
            $rootScope.path = pathArray[0];
        }
        $rootScope.pageSize = 16;
        $rootScope.render();
    });
    $rootScope.render = function () {
        $timeout(function () {
            layui.form.render();
            layui.element.render();
            layui.table.render();
        }, 100)
    };
});

app.factory("vailLoginInterceptor", ["$q", function ($q) {
    return {
        response: function (response) {
            if (response.headers(0).redirect_url) {
                console.log("从响应头中获取到了重定向地址: ", response.headers(0).redirect_url);
                window.location = response.headers(0).redirect_url;
            }
            if (response.config.url.lastIndexOf(".query") !== -1 || response.config.url.lastIndexOf(".do") !== -1) {
                vailLogin(response.data);
                return response.data || $q.when(response.data);
            }
            return response || $q.when(response);
        }

    };
}]);

app.config(['$routeProvider', '$httpProvider', function ($routeProvider, $httpProvider) {
    $httpProvider.interceptors.push("vailLoginInterceptor");
    $httpProvider.defaults.cache = false;

    $routeProvider.when('/', {
        templateUrl: 'pages/overview/overview.html',
        controller: 'overviewController'
    }).when('/link', {
        templateUrl: 'pages/http/link/link_index.html',
        controller: 'linkController'
    }).when('/linkAddOrEdit', {
        templateUrl: 'pages/http/link/link_addOrEdit.html',
        controller: 'linkAddOrEditController'
    }).when('/linkParamOrder', {
        templateUrl: 'pages/http/link/big_link_order.html',
        controller: 'linkParamOrderController'
    }).when('/httpScene', {
        templateUrl: 'pages/http/scene/scene_index.html',
        controller: 'httpSceneController'
    }).when('/httpSceneAddOrEdit', {
        templateUrl: 'pages/http/scene/scene_addOrEdit.html',
        controller: 'httpSceneAddOrEditController'
    }).when('/httpReport', {
        templateUrl: 'pages/http/report/report_index.html',
        controller: 'httpReportController'
    }).when('/httpReportDetail', {
        templateUrl: 'pages/http/report/report_detail.html',
        controller: 'httpReportDetailController'
    }).when('/service', {
        templateUrl: 'pages/dubbo/service/service_index.html',
        controller: 'serviceController'
    }).when('/serviceAddOrEdit', {
        templateUrl: 'pages/dubbo/service/service_addOrEdit.html',
        controller: 'serviceAddOrEditController'
    }).when('/dubboScene', {
        templateUrl: 'pages/dubbo/scene/scene_index.html',
        controller: 'dubboSceneController'
    }).when('/dubboSceneAddOrEdit', {
        templateUrl: 'pages/dubbo/scene/scene_addOrEdit.html',
        controller: 'dubboSceneAddOrEditController'
    }).when('/dubboReport', {
        templateUrl: 'pages/dubbo/report/report_index.html',
        controller: 'dubboReportController'
    }).when('/dubboReportDetail', {
        templateUrl: 'pages/dubbo/report/report_detail.html',
        controller: 'dubboReportDetailController'
    }).when('/autoTestDeploy', {
        templateUrl: 'pages/auto/testDeploy_index.html',
        controller: 'autoTestDeployController'
    }).when('/topLink', {
        templateUrl: 'pages/auto/top/topLink_index.html',
        controller: 'topLinkController'
    }).when('/autoTop', {
        templateUrl: 'pages/auto/top/autoTop_index.html',
        controller: 'autoTopController'
    }).when('/blackGroup', {
        templateUrl: 'pages/auto/black_group/blackgroup_index.html',
        controller: 'blackGroupController'
    }).when('/blackGroupAddOrEdit', {
        templateUrl: 'pages/auto/black_group/blackgroup_addOrEdit.html',
        controller: 'blackGroupAddOrEditController'
    }).when('/whiteList', {
        templateUrl: 'pages/auto/white_list/whitelist_index.html',
        controller: 'whiteListController'
    }).when('/machine', {
        templateUrl: 'pages/system/machine/machine_index.html',
        controller: 'machineController'
    }).when('/machineMonitor', {
        templateUrl: 'pages/system/machine/machine_monitor.html',
        controller: 'machineMonitorController'
    }).when('/user', {
        templateUrl: 'pages/system/user/user_index.html',
        controller: 'userController'
    }).when('/userAdd', {
        templateUrl: 'pages/system/user/user_add.html',
        controller: 'userAddOrEditController'
    }).when('/userEdit', {
        templateUrl: 'pages/system/user/user_edit.html',
        controller: 'userAddOrEditController'
    }).when('/opsLog', {
        templateUrl: 'pages/system/opsLog/opsLog_index.html',
        controller: 'opsLogController'
    }).when('/rulesLink', {
        templateUrl: 'pages/system/rules/rulesLink_index.html',
        controller: 'rulesLinkController'
    }).when('/task', {
        templateUrl: 'pages/data/task/task_index.html',
        controller: 'taskController'
    }).when('/taskAddOrEdit', {
        templateUrl: 'pages/data/task/task_addOrEdit.html',
        controller: 'taskAddOrEditController'
    }).when('/batch', {
        templateUrl: 'pages/data/batch/batch_index.html',
        controller: 'batchController'
    }).when('/batchAddOrEdit', {
        templateUrl: 'pages/data/batch/batch_addOrEdit.html',
        controller: 'batchAddOrEditController'
    })
}]);

/**
 * 数组扩展方法，移除数组中某一元素或某一段元素
 * @param from 需要移除元素的索引开始值（只传一个参数表示单独移除该元素）
 * @param to 需要移除元素的索引结束值
 * @returns {*}
 */
Array.prototype.remove = function (from, to) {
    let rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};
Array.prototype.removeS = function (val) {
    let index = this.indexOf(val);
    if (index > -1) {
        this.splice(index, 1);
    }
};


/**
 * 根据元素值查询数组中元素的索引
 * @param val
 * @returns {number}
 */
Array.prototype.indexOf = function (val) {
    for (let i = 0; i < this.length; i++) {
        if (this[i] == val) return i;
    }
    return -1;
};

app.filter('convertStatus', function () {
    return function (input) {
        if (input == 'IDLE') {
            return '空闲';
        } else if (input == 'RUNNING') {
            return '运行';
        }
        return '禁用';
    }
});
app.filter('converStrategy', function () {
    return function (input) {
        if (isBlank(input)) {
            return '尖峰';
        } else if (isEquals(input, 'peak')) {
            return '尖峰';
        } else if (isEquals(input, 'gently')) {
            return '平缓';
        } else {
            return '固定';
        }
    }
});

app.filter('converRulesType', function () {
    return function (input) {
        if (isEquals(input, 'FILTER')) {
            return '过滤';
        } else {
            return '加密';
        }
    }
});


app.filter('parseIds', function () {
    return function (input) {
        if (isBlank(input)) return;
        let ids = [];
        let pair = input.split(",");
        for (let i = 0; i < pair.length; i++) {
            if (ids.length > 4) {
                ids[i] = "...";
                break;
            }
            ids[i] = pair[i].split("_")[0];
        }
        return ids.join();
    }
});

app.filter('to_trusted', ['$sce', function ($sce) {
    return function (text) {
        if (isBlank(text)) return;
        return $sce.trustAsHtml(syntaxHighlight(text));
    };
}]);

function syntaxHighlight(json) {
    if (isJsonFormat(json)) {
        json = JSON.stringify(JSON.parse(json), undefined, 2);
    }
    json = json.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        let cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}

function isJsonFormat(str) {
    try {
        layui.$.parseJSON(str);
    } catch (e) {
        return false;
    }
    return true;
}

app.directive("selectPlus", selectPlus);

function selectPlus() {
    let directive = {
        restrict: "E",
        template: ' <select lay-search   \n' +
            '                                ng-model="selectId"\n' +
            '                                >\n' +
            '<option></option>' +
            ' <option ng-repeat="item in listModel" value="{{item.id}}">{{item.name}}</option>\n' +
            '                        </select>',
        replace: true,
        scope: {
            listModel: "=",
            yjModel: "="

        },
        link: function ($scope, $element, attr) {
            setTimeout(function () {
                let layfilter = attr['layFilter']
                $scope.selectId = $scope.yjModel && $scope.yjModel.id ? $scope.yjModel.id + "" : ''

                layui.form.on('select(' + layfilter + ')', function (data) {
                    // $scope.data = $element.val()
                    // $scope[attr['change']]($element.val())
                    $scope.selectId = data.value
                    console.log(data)
                    let selectItemArr = $scope.listModel.filter(function (item) {
                        return item.id == data.value
                    })
                    if (selectItemArr.length) {
                        let selectItem = selectItemArr[0]
                        $scope.yjModel = selectItem
                    } else {
                        $scope.yjModel = null
                    }
                    setTimeout(function () {
                        layui.form.render()
                    }, 50)
                    $scope.$apply();
                })
                setTimeout(function () {
                    layui.form.render()
                }, 50)
                $scope.$apply();
            }, 100)
        }
    }
    return directive;
}


app.directive("selectStaticPlus", selectStaticPlus);

function selectStaticPlus() {
    let directive = {
        restrict: "E",
        template: ' <select lay-search   \n' +
            '                                ng-model="selected"\n' +
            '                                >\n' +
            ' <option ng-repeat="item in listModel" value="{{item}}">{{item}}</option>\n' +
            '                        </select>',
        replace: true,
        scope: {
            listModel: "=",
            yjModel: "="

        },
        link: function ($scope, $element, attr) {
            setTimeout(function () {
                let layfilter = attr['layFilter']
                $scope.selected = $scope.yjModel ? $scope.yjModel + "" : ''

                layui.form.on('select(' + layfilter + ')', function (data) {
                    // $scope.data = $element.val()
                    // $scope[attr['change']]($element.val())
                    $scope.selected = data.value
                    console.log(data)
                    let selectItemArr = $scope.listModel.filter(function (item) {
                        return item == data.value
                    })
                    if (selectItemArr.length) {
                        let selectItem = selectItemArr[0]
                        $scope.yjModel = selectItem
                    }
                    setTimeout(function () {
                        layui.form.render()
                    }, 100)
                    $scope.$apply();
                })
                setTimeout(function () {
                    layui.form.render()
                }, 200)
                $scope.$apply();
            }, 200)
        }
    }
    return directive;
}


app.directive("switchPlus", switchPlus);

function switchPlus() {
    let directive = {
        restrict: "E",
        template: ' <input ng-init="yjSwitchModel" ng-model="yjSwitchModel" type="checkbox" lay-skin="switch"\n' +
            '                           lay-text="ON|OFF">',
        replace: true,
        scope: {
            yjSwitchModel: "="

        },
        link: function ($scope, $element, attr) {
            let layfilter = attr['layFilter']

            layui.form.on('switch(' + layfilter + ')', function (data) {
                $scope.yjSwitchModel = this.checked
                setTimeout(function () {
                    layui.form.render()
                }, 200)
                $scope.$apply();
            })
        }
    }
    return directive;
}