app.controller("dubboReportController",function ($http,$scope,$rootScope) {

    $scope.selectInputModel=[];

    $scope.initFn = function(){
        $scope.doInitFn(getUrlPath("dubboReportList"),getUrlPath("dubboReportSearch"));
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
            let lowerName = value.dubboSceneEntity.name.toLowerCase();
            let sceneId = value.dubboSceneEntity.id;
            let enterLowerName  = searchKey.toLowerCase();
            if (lowerName.indexOf(enterLowerName) !== -1 || isEquals(sceneId,enterLowerName)){
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push( $scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });

    $scope.export = function(id){
        window.open(getUrlPath("reportExport")+"?ids="+id+"&type=DUBBO");
    };

    $scope.batchExport = function(){
        if ($scope.selectInputModel.length === 0){
            layer.msg("请选择需要导出的场景");
            return;
        }
        let ids = $scope.selectInputModel.join();
        $scope.export(ids);
        $scope.reloadCurrentPage();
    };
    layui.form.on('checkbox(allReportChoose)',function(data){
        let groupCheckbox = layui.$("input[name='singleReportId']");
        $scope.selectInputModel=[];
        if (data.elem.checked){
            for (i = 0; i < groupCheckbox.length; i++) {
                $scope.selectInputModel.push(groupCheckbox[i].value);
                groupCheckbox[i].checked=true;
            }
        }else{
            for (i = 0; i < groupCheckbox.length; i++) {
                groupCheckbox[i].checked=false;
            }
        }
        $rootScope.render();
    });
    layui.form.on('checkbox(singleReportChoose)',function(data){
        if (data.elem.checked){
            $scope.selectInputModel.push(data.value);
        }else{
            $scope.selectInputModel.removeS(data.value);
        }
    });

});

app.controller("dubboReportDetailController",function ($http,$scope,$routeParams) {
    $scope.detailReport = function () {
        $http.get(getUrlPath("dubboReportDetail"),{params :{"id":$routeParams.id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.reportDetailModel = respData.data;
            }
        })
    }();

});