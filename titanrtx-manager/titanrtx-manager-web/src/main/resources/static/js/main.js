app.controller("mainController", function ($http, $scope, $rootScope, $route, $location, $interval) {

    $scope.domainModel = '';
    $scope.searchModelTotalData = [];
    $scope.searchUrl = '';
    $scope.linkParamsModel = [];

    $scope.initUserInfo = function () {
        $http.get(getUrlPath("userSession")).then(function (respData) {
            $scope.userSession = respData.data;
        });
    }();

    $scope.doListenerStatus = function (domain) {
        if (typeof (WebSocket) === "undefined") {
            layer.msg("你的浏览器不支持webSocket.......");
            return;
        }

        var url = new URL(window.location);
        var protocol = url.protocol
        var wsProtocal = "ws://"
        if (protocol == "https:") {
            wsProtocal = "wss://"
        }

        $scope.socket = new WebSocket(wsProtocal + domain + "/status");
        $scope.socket.onopen = function () {
            $scope.heartCheckStart();
            $scope.aggFlag = true;
            $scope.$apply();
        };
        $scope.socket.onmessage = function (msg) {
            $scope.heartCheckReset();
            if ("TITAN_PONG" == msg.data) {
                return;
            }
            let curPath = $rootScope.path;
            layer.msg(msg.data, {time: 5000, offset: 'rt'});
            if (isEquals(curPath, 'httpScene') || isEquals(curPath, 'dubboScene') || isEquals(curPath, 'batch')) {
                $scope.routePage($rootScope.path);
            }
        };
        $scope.socket.onclose = function () {
            $scope.heartCheckClose();
            $scope.aggFlag = false;
            $scope.$apply();
        };
        $scope.socket.onerror = function () {
            $scope.heartCheckClose();
            $scope.aggFlag = false;
            $scope.$apply();
        };
    };

    $scope.reloadRoutePage = function (url) {
        let curPath = $rootScope.path;
        if (isEquals(url, curPath)) {
            $scope.routePage(url)
        }
    };

    $scope.socketConnect = function () {
        if (!$scope.aggFlag) {
            if (isBlank($scope.domainModel)) {
                $http.get(getUrlPath("queryDomain")).then(function (respData) {
                    if (vailSuccess(respData)) {
                        $scope.domainModel = respData.data;
                        $scope.doListenerStatus(respData.data);
                    }
                });
            } else {
                $scope.doListenerStatus($scope.domainModel);
            }
        }
    };
    $scope.socketConnect();

    $scope.timeout = 30000;
    $scope.timeoutObj = null
    $scope.heartCheckReset = function () {
        clearTimeout(this.timeoutObj);
        $scope.heartCheckStart();
    };
    $scope.heartCheckStart = function () {
        this.timeoutObj = setInterval(function () {
            $scope.socket.send("TITAN_PING");
        }, this.timeout);
    };
    $scope.heartCheckClose = function () {
        if (this.timeoutObj != null) {
            clearTimeout(this.timeoutObj)
        }
    };

    $scope.routePage = function (url) {
        $route.reload(url);
        // $scope.socketConnect();
    };

    $scope.reloadCurrentPage = function () {
        $scope.reloadRoutePage($rootScope.path);
        // $scope.socketConnect();
    };

    $scope.goRoutePage = function (url, params) {
        if (layui.$.isEmptyObject(params)) {
            $location.path(url);
        } else {
            $location.path(url).search(params);
        }
        // $scope.socketConnect();
    };


    $scope.doInitFn = function (listUrl, searchUrl, params) {
        $scope.searchUrl = searchUrl;
        $scope.doLoadList(listUrl, params);
    };

    $scope.doLoadList = function (listUrl, params) {
        layui.layer.load();
        if (params == undefined) {
            params = {"params": {}}
        }
        $http.get(listUrl, params, {timeout: 5000}).error(function () {
            layui.layer.closeAll('loading');
        }).then(function (respData) {
            console.log(respData)
            if (vailSuccess(respData)) {
                $scope.searchModelTotalData = respData.data;
                console.log($scope.searchModelTotalData)
                $scope.buildTablePageBar(respData.data);
            } else {
                layer.msg(respData.msg)
            }
            layui.layer.closeAll('loading');
        });
    };

    $scope.resetTablePage = function () {
        $scope.buildTablePageBar($scope.searchModelTotalData);
    };

    $scope.tableShowData = function (data, curr) {
        let needShowData = [];
        let pageNum = (curr - 1) * $rootScope.pageSize;
        if (data.length > pageNum) {
            needShowData = data.slice(pageNum, curr * $rootScope.pageSize);
        } else {
            needShowData = data.slice(pageNum, data.length);
        }
        return needShowData;
    };

    $scope.buildTablePageBar = function (data, curr) {
        layui.laypage.render({
            elem: 'indexModelPage',
            theme: '#5FB878',
            layout: ['count', 'prev', 'page', 'next'],
            count: data.length,
            curr: curr === undefined ? 1 : count,
            limit: $rootScope.pageSize,
            jump: function (obj) {
                $scope.currentPage = obj.curr;
                $scope.showModel = $scope.tableShowData(data, obj.curr);
                $rootScope.render();
            }
        })
    };

    $scope.enterEvent = function (e, tis) {
        let keycode = window.event ? e.keyCode : e.which;
        if (keycode === 13) {
            let searchKey = tis.searchModel;
            if (isBlank(searchKey) && $scope.searchModelTotalData.length === 0) return;
            $http.get($scope.searchUrl, {params: {"key": searchKey}}).then(function (respData) {
                if (vailSuccess(respData)) {
                    if (respData.data.length > 0) {
                        $scope.searchModelTotalData = respData.data;
                    }
                    $scope.buildTablePageBar(respData.data);
                }
            })
        }
    };

    $scope.delete = function (id, url) {
        layer.confirm('确定删除?', {icon: 3, title: '提示'}, function (index) {
            $http.get(getUrlPath(url), {params: {"id": id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.reloadCurrentPage();
                }
            });
            layer.close(index);
        });
    };


    $scope.doRequestCase = function (url, submitData) {
        layui.layer.load();
        $http.post(url, submitData, {timeout: 15000})
            .error(function () {
                layui.layer.closeAll('loading');
                layer.msg("请求出错,请检查测试信息");
            })
            .then(function (respData) {
                layui.layer.closeAll('loading');
                if (vailSuccess(respData)) {
                    $scope.paramsCasRespDialog(respData.data);
                } else {
                    layer.msg(respData.msg);
                }
            });
    };


    $scope.paramsCasRespDialog = function (msg) {
        $scope.paramsCasRespMsg = msg;
        layui.layer.open({
            title: "响应内容",
            type: 1,
            zIndex: 9999,
            shade: 0,
            content: layui.$("#paramsCasRespDialog"),
            area: ['820px', '500px']
        });
    };

    $scope.describeParamsDialog = function (layId) {
        layui.layer.open({
            title: "参数格式说明",
            type: 1,
            zIndex: 9999,
            content: layui.$("#describeParamsDialog"),
            area: ['1106px', '620px'],
            success: function () {
                layui.element.tabChange('describeParamsFilter', layId);
            }
        });
    };


    $scope.progressDialog = function (id, type) {
        $http.get(getUrlPath("progressState"), {params: {"id": id, "type": type}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.showProgressDialog(id, type);
                $rootScope.render();
            } else {
                layer.msg(respData.msg);
            }
        });
    };

    $scope.showProgressDialog = function (id, type) {
        let progressChart = echarts.init(document.getElementById('progressChart'));
        progressChartOption.series[0].data[0].value = 0;
        progressChartOption.series[1].data[0].value = 0;
        progressChart.setOption(progressChartOption);
        /**
         * 防止多次点击定时器生成多个
         */
        if ($scope.intervalPro !== undefined) {
            $interval.cancel($scope.intervalPro);
        }
        $scope.intervalPro = $interval(function () {
            $http.get(getUrlPath("progressState"), {params: {"id": id, "type": type}}).then(function (respData) {
                if (vailSuccess(respData)) {
                    if (respData.data !== undefined) {
                        progressChartOption.series[0].data[0].value = respData.data.requestRate;
                        progressChartOption.series[1].data[0].value = respData.data.waitResponseRate;
                        progressChart.setOption(progressChartOption);
                    }
                } else {
                    layer.closeAll();
                    $interval.cancel($scope.intervalPro);
                }
            });
        }, 3000);

        layui.layer.open({
            title: "压测进度详情",
            type: 1,
            zIndex: layer.zIndex,
            shade: 0,
            content: layui.$('#progressDialog'),
            area: ['1106px', '620px'],
            end: function () {
                $interval.cancel($scope.intervalPro);
            }
        });
    };


    $scope.layout = function () {
        $http.get(getUrlPath("layout")).then(function (respData) {
            if (vailSuccess(respData)) {
                window.location.href = "/login.html"
            }
        });
    };


    let progressChartOption = {
        backgroundColor: '#1b1b1b',
        tooltip: {
            formatter: "{a} <br/>{c} {b}"
        },
        series: [
            {
                name: '请求进度',
                type: 'gauge',
                min: 0,
                max: 100,
                splitNumber: 10,
                center: ['25%', '50%'],
                radius: '80%',
                axisLine: {            // 坐标轴线
                    lineStyle: {       // 属性lineStyle控制线条样式
                        color: [[0.09, 'lime'], [0.82, '#1e90ff'], [1, '#ff4500']],
                        width: 3,
                        shadowColor: '#fff', //默认透明
                        shadowBlur: 10
                    }
                },
                axisLabel: {            // 坐标轴小标记
                    textStyle: {       // 属性lineStyle控制线条样式
                        fontWeight: 'bolder',
                        color: '#fff',
                        shadowColor: '#fff', //默认透明
                        shadowBlur: 10
                    }
                },
                axisTick: {            // 坐标轴小标记
                    length: 15,        // 属性length控制线长
                    lineStyle: {       // 属性lineStyle控制线条样式
                        color: 'auto',
                        shadowColor: '#fff', //默认透明
                        shadowBlur: 10
                    }
                },
                splitLine: {           // 分隔线
                    length: 25,         // 属性length控制线长
                    lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
                        width: 3,
                        color: '#fff',
                        shadowColor: '#fff', //默认透明
                        shadowBlur: 10
                    }
                },
                pointer: {           // 分隔线
                    shadowColor: '#fff', //默认透明
                    shadowBlur: 5
                },
                title: {
                    textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
                        fontWeight: 'bolder',
                        fontSize: 20,
                        fontStyle: 'italic',
                        color: '#fff',
                        shadowColor: '#fff', //默认透明
                        shadowBlur: 10
                    }
                },
                detail: {
                    backgroundColor: 'rgba(30,144,255,0.8)',
                    borderWidth: 1,
                    borderColor: '#fff',
                    shadowColor: '#fff', //默认透明
                    shadowBlur: 5,
                    offsetCenter: [0, '50%'],       // x, y，单位px
                    textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
                        fontWeight: 'bolder',
                        color: '#fff'
                    }
                },
                data: [{value: 0, name: 'req/r'}]
            },
            {
                name: '响应进度',
                type: 'gauge',
                min: 0,
                max: 100,
                splitNumber: 10,
                center: ['75%', '50%'],
                radius: '80%',
                axisLine: {            // 坐标轴线
                    lineStyle: {       // 属性lineStyle控制线条样式
                        color: [[0.09, 'lime'], [0.82, '#1e90ff'], [1, '#ff4500']],
                        width: 3,
                        shadowColor: '#fff', //默认透明
                        shadowBlur: 10
                    }
                },
                axisLabel: {            // 坐标轴小标记
                    textStyle: {       // 属性lineStyle控制线条样式
                        fontWeight: 'bolder',
                        color: '#fff',
                        shadowColor: '#fff', //默认透明
                        shadowBlur: 10
                    }
                },
                axisTick: {            // 坐标轴小标记
                    length: 15,        // 属性length控制线长
                    lineStyle: {       // 属性lineStyle控制线条样式
                        color: 'auto',
                        shadowColor: '#fff', //默认透明
                        shadowBlur: 10
                    }
                },
                splitLine: {           // 分隔线
                    length: 25,         // 属性length控制线长
                    lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
                        width: 3,
                        color: '#fff',
                        shadowColor: '#fff', //默认透明
                        shadowBlur: 10
                    }
                },
                pointer: {           // 分隔线
                    shadowColor: '#fff', //默认透明
                    shadowBlur: 5
                },
                title: {
                    textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
                        fontWeight: 'bolder',
                        fontSize: 20,
                        fontStyle: 'italic',
                        color: '#fff',
                        shadowColor: '#fff', //默认透明
                        shadowBlur: 10
                    }
                },
                detail: {
                    backgroundColor: 'rgba(30,144,255,0.8)',
                    borderWidth: 1,
                    borderColor: '#fff',
                    shadowColor: '#fff', //默认透明
                    shadowBlur: 5,
                    offsetCenter: [0, '50%'],       // x, y，单位px
                    textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
                        fontWeight: 'bolder',
                        color: '#fff'
                    }
                },
                data: [{value: 0, name: 'rsp/r'}]
            }
        ]
    };


});