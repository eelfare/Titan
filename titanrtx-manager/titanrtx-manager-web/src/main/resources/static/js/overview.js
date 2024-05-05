app.controller("overviewController",function ($http,$scope,$interval) {

    $scope.overviewModel={};

    $scope.overviewInfo = function () {
        $http.get(getUrlPath("overviewInfo")).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.overviewModel.infoModel= respData.data;
                return $scope.buildAgentChart(respData.data.agentInfo);
            }
            layer.msg(respData.msg);
        })
    }();
    $scope.buildAgentChart = function (agentInfo) {
        $scope.overviewAgentChart(agentInfo,"agentInfoChart");
    };
    $scope.overviewAgentChart= function(agentInfo,selectorID) {
        let totalNum = agentInfo.total.length;
        let runningNum = agentInfo.running.length;
        let availableNum = agentInfo.available.length;
        let disableNum = agentInfo.disable.length;
        let totalIp = agentInfo.total;
        let runningIp = agentInfo.running;
        let availableIp = agentInfo.available;
        let disableIp = agentInfo.disable;

        let rows = 10; // 节点行数,列数根据该值计算
        let bottomHeight = 45; // 底部高度
        let nodeText = [ '已用', '可用', '禁用','总数' ];
        let nodeValue = [0,1,2,3]; // 2个值分别表示已用、可用的类型值
        // 颜色值,依次表示已用节点、可用节点、总节点、占位节点、分割节点线、节点边框
        let nodeTableColor = [ '#DEDE6B', '#7BC96F','#FF0000', '#87CEEB', '#EBEDF0','#FFFFFF', '#FFFFFF' ];
        // 初始化echarts实例
        let myChart = echarts.init(document.getElementById(selectorID));
        let myChartWidth = myChart.getWidth();
        let myChartHeight = myChart.getHeight();
        let cells = Math.ceil(myChartWidth / ((myChartHeight - bottomHeight) / rows)); // 列数
        // 计算默认行数是否足够,不够则增加
        while ((rows * cells) < totalNum) {
            rows = rows + 1;
            cells = Math.ceil(myChartWidth / ((myChartHeight - bottomHeight) / rows)); // 列数
        }
        // 获取值为空的指定长度数组
        let getEmptyArr = function(n) {
            let arr = new Array(n);
            for (let i = 0; i < n; i++) {
                arr[i] = '';
            }
            return arr;
        };
        // 组装节点数据,参数依次是:节点值、节点数、数据数组
        let packageNodeData = function(value, nodeNum, dataArr) {
            let data = [], x, y;
            for (var i = 0; i < nodeNum; i++) {
                if (value == nodeValue[0]) {
                    x = parseInt((availableNum+ i) / rows);
                    y = rows - 1 - (availableNum + i) % rows;
                }else if (value == nodeValue[1]){
                    x = parseInt((disableNum+ i) / rows);
                    y = rows - 1 - (disableNum + i) % rows;
                } else {
                    x = parseInt(i / rows);
                    y = rows - 1 - i % rows;
                }
                data.push({
                    label : dataArr[i],
                    value : [ x, y, value ]
                });
            }
            return data;
        };

        splitLine = {
            show : true,
            lineStyle : {
                color : nodeTableColor[5],
                width : 2
            }
        };
        itemStyle = {
            normal : {
                borderColor : nodeTableColor[5],
                borderWidth : 2,
            },
            emphasis : {
                shadowBlur : 1,
                shadowColor : 'rgba(0, 0, 0, 0.5)',
            }
        };
        option = {
            tooltip : {
                formatter : function(obj) {
                    return '<div>' + obj.data.label + '</div>';
                },
                backgroundColor:'rgba(50,50,50,0.5)'
            },
            grid : {
                top : 15,
                left : 15,
                right : 15,
                bottom : bottomHeight,
                show : true,
                backgroundColor : nodeTableColor[4]
            },
            xAxis : {
                data : getEmptyArr(cells),
                splitLine : splitLine,
                axisLine : {
                    show : false
                },
                axisTick : {
                    show : false
                }
            },
            yAxis : {
                data : getEmptyArr(rows),
                splitLine : splitLine,
                axisLine : {
                    show : false
                },
                axisTick : {
                    show : false
                }
            },
            visualMap : [ {
                pieces : [ {
                    value : nodeValue[0],
                    label : nodeText[0] + ': ' + runningNum
                }, {
                    value : nodeValue[1],
                    label : nodeText[1] + ': ' + availableNum
                } , {
                    value : nodeValue[2],
                    label : nodeText[2] + ': ' + disableNum
                },{
                    value : nodeValue[3],
                    label : nodeText[3] + ': ' + totalNum
                }],
                orient : 'horizontal',
                itemGap : 10,
                right : 10,
                bottom : 8,
                color : [nodeTableColor[3], nodeTableColor[2],nodeTableColor[1],nodeTableColor[0]]
            } ],
            series : [{
                name : nodeText[0],
                type : 'heatmap',
                data : packageNodeData(nodeValue[0],runningNum,runningIp),
                itemStyle : itemStyle
                },
                {
                    name : nodeText[1],
                    type : 'heatmap',
                    data : packageNodeData(nodeValue[1],availableNum,availableIp),
                    itemStyle : itemStyle
                },
                {
                    name : nodeText[2],
                    type : 'heatmap',
                    data : packageNodeData(nodeValue[2],disableNum,disableIp),
                    itemStyle : itemStyle
                }]
        };
        myChart.setOption(option);
    };

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

    $scope.netRateMonitor = function () {
        $http.get(getUrlPath("netRateMonitor")).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.buildChart(respData.data);
            }
        })
    };
    $scope.netRateMonitor();

    $scope.buildChart = function (data) {
        $scope.buildNetInChart(data.netIn);
        $scope.buildNetOutChart(data.netOut);
    };
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


    $scope.intervalMonitor = $interval(function () {
        $scope.netRateMonitor();
    },15000);

    $scope.$on("$destroy", function () {
        $interval.cancel($scope.intervalMonitor);
    });


    $scope.restartAll = function(){
        $http.get(getUrlPath("restartAll")).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)){
                $scope.reloadCurrentPage();
            }
        })
    };
    $scope.resetAll = function () {
        $http.get(getUrlPath("resetAll")).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)){
                $scope.reloadCurrentPage();
            }
        })
    }

});