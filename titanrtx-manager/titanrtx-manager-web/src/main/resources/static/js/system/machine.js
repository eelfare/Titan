app.controller("machineController",function ($http,$scope) {

    $scope.initFn = function(){
        $scope.doInitFn(getUrlPath("machineList"),'');
    }();

    $scope.$watch('searchModel', function() {
        let searchKey =  $scope.searchModel;
        if (isBlank(searchKey)){
            $scope.resetTablePage();
            return;
        }
        let totalFilterIndexData=[];
        let targetIndex =[];
        $scope.searchModelTotalData.forEach(function (value,index) {
            let instantsId = value.id.toLowerCase();
            let lowerAddress = value.address.toLowerCase();
            let enterLowerName  = searchKey.toLowerCase();
            if (instantsId.indexOf(enterLowerName) !== -1 || lowerAddress.indexOf(enterLowerName) !== -1){
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push( $scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });

    $scope.machineEnable = function (address) {
        $scope.doAble(getUrlPath("machineEnable"),address);
    };

    $scope.machineDisable = function (address) {
        $scope.doAble(getUrlPath("machineDisable"),address);
    };
    $scope.doAble = function (url,address) {
        $http.get(url,{params:{"address": address}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.reloadCurrentPage();
            }
        })
    };

    $scope.showBatchHostsDialog=function () {
            layui.layer.open({
                title : "批量修改HOSTS信息",
                type: 1,
                zIndex: layer.zIndex,
                shade: 0,
                content: layui.$('#batchHostsDialog'),
                area: ['1040px','580px'],
                btn :['保存','取消'],
                yes: function (index) {
                    let hostsModel = $scope.batchHostsDialog;
                    $http.get(getUrlPath("machineBatchModify"),{params:{"content": hostsModel}}).then(function (respData) {
                        layer.msg(respData.msg);
                        if (vailSuccess(respData)) {
                            layer.close(index);
                        }
                    });
                },
                btn2:function (index) {
                    layer.close(index);
                }
            });
    };



    $scope.showHostsDialog=function (address) {
        $http.get(getUrlPath("machineHosts"),{params:{"address": address}}).then(function (respData) {
            if (vailSuccess(respData)) {
                let showStr='';
                respData.data.forEach(function (value) {
                    showStr += value + "\n";
                });
                $scope.hostsModel = showStr;
                layui.layer.open({
                    title : "域名信息",
                    type: 1,
                    zIndex: layer.zIndex,
                    shade: 0,
                    content: layui.$('#hostsDialog'),
                    area: ['1040px','580px'],
                    btn :['保存','取消'],
                    yes: function (index) {
                        let hostsModel = $scope.hostsModel;
                        $http.get(getUrlPath("machineModifyHosts"),{params:{"address":address,"content": hostsModel}}).then(function (respData) {
                            layer.msg(respData.msg);
                            if (vailSuccess(respData)) {
                                layer.close(index);
                            }
                        });
                    },
                    btn2:function (index) {
                        layer.close(index);
                    }
                });
            }else {
                layer.msg(respData.msg);
            }
        });
    };


});
app.controller("machineMonitorController",function ($http,$scope,$rootScope,$routeParams,$interval) {

    $scope.initFn = function(){
        layui.laydate.render({elem: '#startTime', type: 'datetime', value:getNowPreMinuter(5)});
        layui.laydate.render({elem: '#endTime', type: 'datetime', value:getNowPreMinuter(0)});
        $rootScope.render();
    }();


    let netInChart = echarts.init(document.getElementById('netInChart'));
    let netInChartOption ={
        tooltip: {
            trigger: 'axis'
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: []
        },
        yAxis: {
            type: 'value' ,
            splitLine: {show: true}
        },
        grid:{
            top : 10,
            left : 50,
            right : 50,
            bottom: 20
        },
        series: [{
            name:'网络流入速率',
            type: 'line',
            smooth:true,
            showSymbol: false,
            data: [],
            lineStyle:{
                normal:{
                    color:'#EE82EE'
                }
            }
        }]
    };
    netInChart.setOption(netInChartOption);

    let netOutChart = echarts.init(document.getElementById('netOutChart'));
    let netOutChartOption ={
        tooltip: {
            trigger: 'axis'
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: []
        },
        yAxis: {
            type: 'value' ,
            splitLine: {show: true}
        },
        grid:{
            top : 10,
            left : 50,
            right : 50,
            bottom: 20
        },
        series: [{
            name:'网络流出速率',
            type: 'line',
            smooth:true,
            showSymbol: false,
            data: [],
            lineStyle:{
                normal:{
                    color:'#4169E1'
                }
            }
        }]
    };
    netOutChart.setOption(netOutChartOption);

    let cpuChart = echarts.init(document.getElementById('cpuChart'));
    let cpuChartOption ={
        tooltip: {
            trigger: 'axis'
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: []
        },
        yAxis: {
            type: 'value' ,
            splitLine: {show: true}
        },
        grid:{
            top : 10,
            left : 50,
            right : 50,
            bottom: 20
        },
        series: [{
            name:'CPU使用率',
            type: 'line',
            smooth:true,
            showSymbol: false,
            data: [],
            lineStyle:{
                normal:{
                    color:'#3BC0FF'
                }
            }
        }]
    };
    cpuChart.setOption(cpuChartOption);


    let memoryChart = echarts.init(document.getElementById('memoryChart'));
    let memoryChartOption = {
        tooltip: {
            trigger: 'axis'
        },
        yAxis: {
            type: 'value' ,
            splitLine: {show: true}
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: []
        },
        grid:{
            top : 10,
            left : 50,
            right : 50,
            bottom: 20
        },
        series: [{
            name:'内存使用率',
            type: 'line',
            smooth:true,
            showSymbol: false,
            data: [],
            lineStyle:{
                normal:{
                    color:'#55B802'
                }
            }
        }]
    };
    memoryChart.setOption(memoryChartOption);


    $scope.machineMonitor = function (id,start,end) {
        layui.layer.load();
        $http.get(getUrlPath("machineMonitor"),{params:{"id":id,"start":start,"end": end}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.buildChart(respData.data);
            }
            layui.layer.closeAll('loading');
        })
    };

    $scope.monitorInit = function(){
       $scope.machineMonitor($routeParams.id,layui.$("#startTime").val(),layui.$("#endTime").val());
    }();

    $scope.buildNetInChart= function(data){
        let xData = [];
        let xAxis =[];
        data.forEach(function (value) {
            xData.push(value.value);
            xAxis.push(formatDate(parseInt(value.timestamp)));
        });
        netInChartOption.xAxis.data = xAxis;
        netInChartOption.series[0].data =xData;
        netInChart.setOption(netInChartOption);
    };
    $scope.buildNetOutChart= function(data){
        let xData = [];
        let xAxis =[];
        data.forEach(function (value) {
            xData.push(value.value);
            xAxis.push(formatDate(parseInt(value.timestamp)));
        });
        netOutChartOption.xAxis.data = xAxis;
        netOutChartOption.series[0].data =xData;
        netOutChart.setOption(netOutChartOption);
    };
    $scope.buildCpuChart= function(data){
        let xData = [];
        let xAxis =[];
        data.forEach(function (value) {
            xData.push(value.value);
            xAxis.push(formatDate(parseInt(value.timestamp)));
        });
        cpuChartOption.xAxis.data = xAxis;
        cpuChartOption.series[0].data =xData;
        cpuChart.setOption(cpuChartOption);
    };
    $scope.buildMemoryChart= function(data){
        let xData = [];
        let xAxis =[];
        data.forEach(function (value) {
            xData.push(value.value);
            xAxis.push(formatDate(parseInt(value.timestamp)));
        });
        memoryChartOption.xAxis.data = xAxis;
        memoryChartOption.series[0].data =xData;
        memoryChart.setOption(memoryChartOption);
    };

    $scope.buildChart = function (data) {
        $scope.buildNetInChart(data.netIn);
        $scope.buildNetOutChart(data.netOut);
        $scope.buildCpuChart(data.cpu);
        $scope.buildMemoryChart(data.memory);
    };

    $scope.intervalMonitor = $interval(function () {
        let startDate = formatMonitorDate(new Date(layui.$("#startTime").val()).getTime()+ 15000);
        let endDate = formatMonitorDate(new Date(layui.$("#endTime").val()).getTime() + 15000);
        layui.$("#startTime").val(startDate);
        layui.$("#endTime").val(endDate);
        $rootScope.render();
        $scope.machineMonitor($routeParams.id,startDate,endDate);
    },15000);

    $scope.$on("$destroy", function () {
        $interval.cancel($scope.intervalMonitor);
    });

    $scope.queryMonitor= function () {
        $scope.machineMonitor($routeParams.id,layui.$("#startTime").val(),layui.$("#endTime").val());
        if ($scope.intervalMonitor !== undefined){
            $interval.cancel($scope.intervalMonitor);
        }
    }

});