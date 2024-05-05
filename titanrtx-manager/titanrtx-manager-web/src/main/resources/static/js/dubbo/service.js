app.controller("serviceController",function ($http,$scope) {

    $scope.initFn = function(){
        $scope.doInitFn(getUrlPath("serviceList"),getUrlPath("serviceSearch"));
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
            let serviceId = value.id;
            let aliasName = value.aliasName.toLowerCase();
            let applicationName = value.applicationName.toLowerCase();
            let serviceName = value.serviceName.toLowerCase();
            let methodName = value.methodName.toLowerCase();
            let enterLowerName  = searchKey.toLowerCase();
            if (isEquals(serviceId,enterLowerName) || aliasName.indexOf(enterLowerName) !== -1 || applicationName.indexOf(enterLowerName) !== -1 || serviceName.indexOf(enterLowerName) !== -1 || methodName.indexOf(enterLowerName) !== -1){
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push( $scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });
});

app.controller("serviceAddOrEditController",function ($http,$scope,$routeParams,$rootScope,$timeout,ngInvoke) {

    $scope.serviceInfoModel={};
    $scope.formDataSubmitFlag = false;
    $scope.serviceAddressModel=[];
    $scope.currentFormModel={};
    $scope.batchImportModel={};
    $scope.serviceParamsModel=[];


    $scope.applicationNames = function(){
        $http.get(getUrlPath("applicationNames")).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.applicationNamesModel = respData.data;
                $rootScope.render();
            }else {
                layer.msg(respData.msg);
            }
        })
    };

    $scope.serviceInfo = function(id){
        $http.get(getUrlPath("serviceInfo"),{params :{"id":id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.serviceInfoModel = respData.data.service;
                $scope.buildTablePageBar(respData.data.params);
            }
        })
    };

    $scope.initFun = function(){
        let id = $routeParams.id;
        $scope.applicationNames();
        if (isBlank(id)){
            $scope.isEdit = false;
            return;
        }else {
            $scope.isEdit = true;
        }
        return $scope.serviceInfo(id)
    }();


    $scope.buildTablePageBar = function(data){
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
                if (!first){
                    $scope.refreshParamsData(obj.curr);
                }
            }
        })
    };

    $scope.refreshParamsData = function(currentPage){
        $http.get(getUrlPath("serviceQueryParams"),{params:{"id":$scope.serviceInfoModel.id,"currentPage":currentPage}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.buildTablePageBar(respData.data);
            }
        });
    };

    $scope.getModelParam = function(id){
        let modelParam = undefined;
        $scope.serviceParamsModel.forEach(function (value) {
            if (isEquals(value.id,id)){
                modelParam = value.param;
                return modelParam;
            }
        });
        return modelParam;
    };

    $scope.doInputServiceParams=function(params,index){
        $http.post(getUrlPath("serviceAddParams"),{"id":$scope.serviceInfoModel.id,"params":params}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.refreshParamsData(1);
                if (isNotBlank(index)){
                    layer.close(index);
                }
            }
        });
    };

    $scope.updateHttpParam = function(paramId){
        let modelParam = $scope.getModelParam(paramId);
        if (isBlank(modelParam)){
            layer.msg("参数异常");
            return;
        }
        $http.get(getUrlPath("serviceUpdateParam"),{params:{"paramId":paramId,"param":modelParam}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                return $scope.refreshParamsData(1);
            }
        });
    };


    $scope.insertServiceParam = function(){
        if (isBlank($scope.inputParamModel)){
            layer.msg("参数不能为空");
            return;
        }
        let formData = $scope.refreshFormData();
        if (layui.$.isEmptyObject(formData)) return;
        if (isBlank($scope.serviceInfoModel.id)) {
            $scope.insertOrAddService(formData).then(function (respData) {
                if (vailSuccess(respData)){
                    $scope.serviceInfoModel.id = respData.data;
                    return $scope.doInputServiceParams($scope.inputParamModel);
                }else{
                    layer.msg(respData.msg);
                }
            });
        }else{
            return $scope.doInputServiceParams($scope.inputParamModel);
        }
    };

    $scope.doParamCase = function (requestParam) {
        let formData = $scope.refreshFormData();
        if (layui.$.isEmptyObject(formData)) return;
        $scope.doRequestCase(getUrlPath("doDubboParamCase"),{"service": formData,"requestParam": requestParam});
    };

    $scope.doPreParamCase= function(paramId){
        return $scope.doParamCase($scope.getModelParam(paramId));
    };
    $scope.doInputParamCase = function(){
        return $scope.doParamCase($scope.inputParamModel);
    };

    $scope.clearInputParam = function(){
        $scope.inputParamModel = '';
    };


    $scope.deleteParam = function(id){
        layer.confirm('确定删除?',{icon: 3, title:'提示'}, function(index){
            $http.get(getUrlPath("serviceParamDelete"),{params :{"id":id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.refreshParamsData($scope.currentPage);
                }
            });
            layer.close(index);
        });
    };


    $scope.batchDeleteParams = function(serviceId){
        layer.confirm('确定删除全部参数?',{icon: 3, title:'提示'}, function(index){
            $http.get(getUrlPath("serviceClearParam"),{params : {"id":serviceId}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.refreshParamsData($scope.currentPage);
                }
            });
            layer.close(index);
        });
    };

    $scope.batchDoParamCase=function(){
        //参数为空时 默认提供测试
        if ($scope.serviceParamsModel.length === 0){
            return $scope.doInputParamCase();
        }
        for (let i = 0; i < $scope.serviceParamsModel.length; i ++){
            $timeout(function () {
                layer.msg("ID:"+($scope.serviceParamsModel[i].id)+"响应结果:");
                $scope.doParamCase($scope.serviceParamsModel[i].param)
            },i * 3000);
        }
    };


    layui.form.on('select(applicationNameFilter)',function(elem){
        $scope.serviceUrls(elem.value);
        $scope.serviceInfoModel.applicationName = elem.value;

    });

    $scope.serviceUrls = function (applicationName) {
        $http.get(getUrlPath("serviceUrls"),{params:{"application":applicationName}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.serviceUrlsModel = respData.data;
                $rootScope.render();
            }
        })
    };

    layui.form.on('select(serviceFilter)',function(elem){
        $scope.serviceMethods(elem.value);
        $scope.serviceInfoModel.serviceName = elem.value;
    });

    layui.form.on('select(methodFilter)',function (elem) {
        $scope.serviceInfoModel.methodName = elem.value;
        $scope.$apply();
    });
    layui.form.on('select(addressFilter)',function (elem) {
        $scope.serviceInfoModel.address = elem.value;
        $scope.$apply();
    });

    $scope.serviceMethods = function (serviceName) {
        $http.get(getUrlPath("serviceMethods"),{params:{"service":serviceName}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.serviceMethodsModel = respData.data;
                $rootScope.render();
            }
        });
        $scope.serviceAddress(serviceName);
    };

    $scope.serviceAddress = function(serviceName){
         $http.get(getUrlPath("serviceAddress"),{params:{"service":serviceName}}).then(function (respData) {
           if (vailSuccess(respData)) {
               $scope.serviceAddressModel = respData.data;
               $rootScope.render();
           }
       })
    };

    $scope.batchImportParams=function () {
        let formData = $scope.refreshFormData();
        if (layui.$.isEmptyObject(formData)) return;
        layui.layer.open({
            title : "批量导入参数",
            type: 1,
            zIndex: layer.zIndex,
            shade: 0,
            content: layui.$('#batchImportParamsDialog'),
            area: ['1106px','620px'],
            btn :['确定'],
            yes :function (index) {
                if (isBlank($scope.batchImportModel.inputData)){
                    layer.msg("参数不能为空");
                    return;
                }
                /**
                 * 当隐藏域ID为空时需要先提交当前链路信息保存返回id作为凭证
                 */
                let id = $scope.serviceInfoModel.id;
                if (isBlank(id)){
                    $scope.insertOrAddService(formData).then(function (respData) {
                        if (vailSuccess(respData)) {
                            $scope.serviceInfoModel.id = respData.data;
                            return $scope.preDoInputServiceParams(index)
                        }else{
                            layer.msg(respData.msg);
                        }
                    });
                }else{
                    return $scope.preDoInputServiceParams(index)
                }
            },
            end : function () {
                $scope.batchImportModel={};
            }
        });
    };

    $scope.preDoInputServiceParams = function(index){
        let inputData = $scope.batchImportModel.inputData;
        if (isNotBlank($scope.batchImportModel.template)){
            inputData = formatDataHelper($scope.batchImportModel.template,inputData);
        }
        return $scope.doInputServiceParams(inputData,index);
    };


    $scope.refreshFormData= function(){
        $scope.formDataSubmitFlag = true;
        layui.$("#serviceSubmit").click();
        $scope.formDataSubmitFlag = false;
        return $scope.currentFormModel;
    };

    $scope.insertOrAddService = function(formData){
        return ngInvoke.syncInvoke(getUrlPath("serviceAddOrUpdate"),'post',formData);
    };

    layui.form.on('submit(serviceSubmit)', function(data){
        if ( !$scope.formDataSubmitFlag ){
            $scope.insertOrAddService(data.field).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    $scope.goRoutePage("/service");
                }
            });
        }else{
            $scope.currentFormModel = data.field;
        }
        return false;
    });


    $scope.pullHttParams= function () {
        layer.msg("暂未支持服务获取参数");
    }

});