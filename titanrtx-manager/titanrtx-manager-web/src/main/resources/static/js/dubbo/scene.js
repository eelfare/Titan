app.controller("dubboSceneController",function ($http,$scope,$rootScope) {

    $scope.initFn = function(){
        $scope.doInitFn(getUrlPath("dubboSceneList"),getUrlPath("dubboSceneSearch"));
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
            let lowerName = value.name.toLowerCase();
            let serviceIds = value.idsWeight;
            let enterLowerName  = searchKey.toLowerCase();
            if (lowerName.indexOf(enterLowerName) !== -1 || serviceIds.indexOf(enterLowerName) !== -1){
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push( $scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });


    $scope.sceneStart = function(id,$index){
        const now = new Date();
        const hours = now.getHours();
        if (hours >= 7 && hours <= 24) {
            layer.confirm('确定需要在白天进行压测吗?', {icon: 3, title: '提示'}, function (tips) {
                $scope.doScene(id,$index)
                layer.close(tips);
            });
        } else {
            $scope.doScene(id,$index)
        }
    };
    $scope.doScene = function(id,$index){
        layui.layer.load();
        $http.get(getUrlPath("dubboSceneStart"),{params :{"id":id}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.showModel[$index].status = 1;

            }
            layui.layer.closeAll('loading');
        })
    };

    $scope.sceneStop = function(id){
        layui.layer.load();
        $http.get(getUrlPath("dubboSceneStop"),{params :{"id":id}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)){
                $scope.routePage($rootScope.path);
            }
            layui.layer.closeAll('loading');
        })
    };

    $scope.showSceneServiceDialog = function(idsWeight){
        $http.get(getUrlPath("dubboSceneServices"),{params :{"serviceIds":idsWeight}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.showSceneServiceModel= respData.data;
                layui.layer.open({
                    title : "场景链路详情",
                    type: 1,
                    zIndex: layer.zIndex,
                    shade: 0,
                    content: layui.$('#sceneServicesDialog'),
                    area: ['1106px','620px']
                });
            }else {
                layer.msg(respData.msg);
            }
        })
    };
});

app.controller("dubboSceneAddOrEditController",function ($http,$scope,$rootScope,$routeParams,ngInvoke,$timeout) {

    $scope.serviceListModel = [];
    $scope.selectServiceModel=[];


    $scope.isModify = false;

    $scope.serviceList = function () {
        $http.get(getUrlPath("serviceList")).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.serviceListModel= respData.data;
            }
        })
    }();

    $scope.sceneMeta = function(){
        $http.get(getUrlPath("dubboSceneMeta")).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.sceneMetaModel= respData.data;
            }
        })
    }();

    $scope.sceneInfo = function(id){
        $http.get(getUrlPath("dubboSceneInfo"),{params :{"id":id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.showSceneInfoModel= respData.data.dubboSceneEntity;
                respData.data.services.forEach(function (value) {
                    $scope.selectServiceModel.push(value);
                });
            }
        })
    };

    $scope.initFun = function(){
        let id = $routeParams.id;
        if (isBlank(id)){
            $scope.isEdit = false;
            return;
        }else {
            $scope.isEdit = true;
        }
        return $scope.sceneInfo(id)
    }();


    $scope.$watch('searchServiceMode', function() {
        let searchKey = $scope.searchServiceMode;
        if (isNotBlank(searchKey)){
            let showServiceArray = $scope.searchServiceHelper($scope.serviceListModel,searchKey);
            if (showServiceArray.length > 0){
                $scope.showServiceModel=showServiceArray;
                layui.$(".searchServiceNameSelect").show();
            }else{
                layui.$(".searchServiceNameSelect").hide();
            }
        }else{
            layui.$(".searchServiceNameSelect").hide();
        }
    });

    $scope.searchServiceHelper=function(data,name){
        let showArray=[];
        let trimName = name.trim().toLowerCase();
        for (let j = 0; j <   data.length ; j ++){
            let serviceModel = data[j];
            let lowerName = serviceModel.name.toLowerCase();
            let id   = serviceModel.id;
            let lowerServiceName  = serviceModel.serviceName.toLowerCase();
            if (isEquals(id,name) || lowerName.indexOf(trimName) !== -1 || lowerServiceName.indexOf(trimName) !== -1){
                if (showArray.length <= 10 ){
                    showArray.push(serviceModel);
                }else {
                    break;
                }
            }
        }
        return showArray;
    };

    $scope.enterEvent = function (e) {
        let keycode = window.event ? e.keyCode : e.which;
        if( keycode === 13 ){
            let searchKey = $scope.searchServiceMode;
            if (isBlank(searchKey))return;
            $http.get(getUrlPath("serviceSearch"),{params:{"key":searchKey}}).then(function (respData) {
                if (vailSuccess(respData)) {
                    $scope.showServiceModel = respData.data;
                    layui.$(".searchServiceNameSelect").show();
                }
            })
        }
    };


    $scope.selectServiceName = function (serviceModel) {
        for (let i = 0 ; i < $scope.selectServiceModel.length ; i ++){
            if ($scope.selectServiceModel[i].id === serviceModel.id){
                return layer.msg("服务已存在列表中");
            }
        }
        $scope.selectServiceModel.push(serviceModel);
        layui.$(".searchServiceNameSelect").hide();
    };

    $scope.removeService = function ($index) {
        $scope.selectServiceModel.remove($index,$index);
    };

    layui.form.on('submit(sceneSubmit)', function(data){
        let serviceIdsWeight = [];
        $scope.selectServiceModel.forEach(function (value) {
            serviceIdsWeight.push(value.id+"_"+value.weight);
        });
        if (serviceIdsWeight.length ===0){
            layer.msg("场景需要最少一个服务");
            return;
        }
        let formData = data.field;
        formData.idsWeight = serviceIdsWeight.join();
        $http.post(getUrlPath("dubboSceneAddOrUpdate"),formData).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.goRoutePage("/dubboScene",{});
            }
        });
        return false;
    });

    $scope.refreshParamsData = function(id,currentPage){
        $http.get(getUrlPath("serviceQueryParams"),{params:{"id":id,"currentPage":currentPage}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.buildTablePageBar(id,respData.data);
            }
        });
    };

    $scope.buildTablePageBar = function(id,data){
        $scope.serviceParamsModel = data.list;
        $rootScope.render();
        layui.laypage.render({
            elem : 'paramsModelPage',
            theme: '#5FB878',
            layout: ['count', 'prev', 'page', 'next'],
            count : data.total,
            curr : data.pageNum,
            limit : 10,
            jump: function (obj,first) {
                $scope.currentPage = obj.curr;
                if (!first){
                    $scope.refreshParamsData(id,obj.curr);
                }
            }
        })
    };

    $scope.refreshSingleLink = function(id){
        $http.get(getUrlPath("serviceFind"),{params :{"id":id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                let refreshLink = respData.data;
                let refreshModel =[];
                for (let i = 0 ; i < $scope.selectServiceModel.length ; i ++){
                    if ($scope.selectServiceModel[i].id !== refreshLink.id){
                        refreshModel.push($scope.selectServiceModel[i]);
                    }else {
                        refreshModel.push(refreshLink);
                    }
                }
                $scope.selectServiceModel = refreshModel;
            }
        })
    };


    $scope.paramDialog = function (id) {
        $scope.refreshParamsData(id,1);
        $scope.currentServiceId = id;
        layui.layer.open({
            title : "参数详情",
            type: 1,
            zIndex: 9999,
            shade: 0,
            content: layui.$("#paramDialog"),
            area: ['1106px','620px'],
            end : function () {
                $scope.refreshSingleLink(id);
            }
        });
    };


    $scope.queryServiceInfo = function(id){
        return ngInvoke.syncInvoke(getUrlPath("serviceInfo"),'get',{"id":id});
    };

    $scope.getParamModel = function(id){
        let mp = undefined;
        $scope.serviceParamsModel.forEach(function (value) {
            if (isEquals(value.id,id)){
                mp = value.param;
                return mp;
            }
        });
        return mp;
    };

    $scope.updateParam = function(serviceId,paramId){
        let modelParam = $scope.getParamModel(paramId);
        if (isBlank(modelParam)){
            layer.msg("参数异常");
            return;
        }
        $http.get(getUrlPath("serviceUpdateParam"),{params:{"paramId":paramId,"param":modelParam}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                return $scope.refreshParamsData(serviceId,$scope.currentPage);
            }
        });
    };

    $scope.doParamCase = function (id,requestParam) {
        $scope.queryServiceInfo(id).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.doRequestCase(getUrlPath("doDubboParamCase"),{"service": respData.data.service,"requestParam": requestParam});
            }else{
                layer.msg(respData.msg);
            }
        });
    };

    $scope.doPreParamCase= function(id,paramId){
        return $scope.doParamCase(id,$scope.getParamModel(paramId));
    };

    $scope.insertParam = function(id){
        if (isBlank($scope.inputParamModel)){
            layer.msg("参数不能为空");
            return;
        }
        return $scope.doInputParams(id,$scope.inputParamModel);
    };

    $scope.doInputParams=function(id,params,index){
        $http.post(getUrlPath("serviceAddParams"),{"id":id,"params":params}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.refreshParamsData(id,$scope.currentPage);
                if (isNotBlank(index)){
                    layer.close(index);
                }
            }
        });
    };

    $scope.doInputParamCase = function(id){
        return $scope.doParamCase(id,$scope.inputParamModel);
    };

    $scope.clearInputParam = function(){
        $scope.inputParamModel = '';
    };

    $scope.batchDeleteParams = function(id){
        layer.confirm('确定删除全部参数?',{icon: 3, title:'提示'}, function(index){
            $http.get(getUrlPath("serviceClearParam"),{params:{"id":id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.refreshParamsData(1);
                }
            });
            layer.close(index);
        });
    };

    $scope.deleteParam = function (id,paramId,url) {
        layer.confirm('确定删除?',{icon: 3, title:'提示'}, function(index){
            $http.get(getUrlPath(url),{params :{"id":paramId}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.refreshParamsData(id,$scope.currentPage);
                }
            });
            layer.close(index);
        });
    };


    $scope.batchDoParamCase=function(id){
        if ($scope.serviceParamsModel.length === 0){
            return $scope.doInputParamCase();
        }
        for (let i = 0; i < $scope.serviceParamsModel.length; i ++){
            $timeout(function () {
                layer.msg("ID:"+($scope.serviceParamsModel[i].id)+"响应结果:");
                $scope.doParamCase(id,$scope.serviceParamsModel[i].param)
            },i * 3000);
        }
    };


    $scope.batchImportParams=function (id) {
        layui.layer.open({
            title : "批量导入参数",
            type: 1,
            zIndex: layer.zIndex,
            shade: 0,
            content: layui.$('#batchImportParamsDialog'),
            area: ['1040px','580px'],
            btn :['确定'],
            yes :function (index) {
                if (isBlank($scope.batchImportModel.inputData)){
                    layer.msg("参数不能为空");
                    return;
                }
                let inputData = $scope.batchImportModel.inputData;
                if (isNotBlank($scope.batchImportModel.template)){
                    inputData = formatDataHelper($scope.batchImportModel.template,inputData);
                }
                return $scope.doInputParams(id,inputData,index);
            },
            end : function () {
                $scope.batchImportModel={};
                $scope.refreshSingleLink(id);
            }
        });
    };



    $scope.pullHttParams= function (linkId) {
      layer.msg("暂未支持服务获取参数");
    };



});