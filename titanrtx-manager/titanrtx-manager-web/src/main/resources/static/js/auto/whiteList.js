app.controller("whiteListController",function ($http,$scope,$rootScope,ngInvoke) {

    $scope.filterModel={};

    $scope.initFn = function(){
        $scope.doInitFn(getUrlPath("queryWhiteList"),'');
    }();

    $scope.deleteRecord = function (id) {
        layer.confirm('确定删除?',{icon: 3, title:'提示'}, function(index){
            $http.get(getUrlPath("deleteWhiteList"),{params:{"id":id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    return $scope.reloadCurrentPage();
                }
            });
            layer.close(index);
        });
    };


    $scope.addOrEditRecordDialog = function (id) {
        ngInvoke.syncInvoke(getUrlPath("whiteListInfo"),'get',{"id":id}).then(function (respData) {
            if (vailSuccess(respData)){
                $scope.recordModel = respData.data;
                layui.layer.open({
                    title : "白名单信息",
                    type: 1,
                    zIndex: 9999,
                    shade: 0,
                    content: layui.$('#addOrEditWhiteListDialog'),
                    area: ['1000px','680px'],
                    btn : ['保存'],
                    yes : function (index) {
                        ngInvoke.syncInvoke(getUrlPath("addOrUpdateWhiteList"),'post',$scope.recordModel).then(function (respData){
                            if (vailSuccess(respData)){
                                layer.close(index);
                                $scope.reloadCurrentPage();
                            }else{
                                layer.msg(respData.msg);
                            }
                        });
                    }
                });
            }
        });
    };

    $scope.$watch('searchModel', function() {
        let searchKey =  $scope.searchModel;
        if (isBlank(searchKey)){
            $scope.resetTablePage();
            return;
        }
        let totalRecodeIndexData=[];
        let targetIndex =[];
        $scope.searchModelTotalData.forEach(function (value,index) {
            let lowerDomain = value.domain.toLowerCase();
            let lowerPath = value.path.toLowerCase();
            let enterLowerName  = searchKey.toLowerCase();
            if (lowerDomain.indexOf(enterLowerName) !== -1 || lowerPath.indexOf(enterLowerName) !== -1){
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalRecodeIndexData.push( $scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalRecodeIndexData);
    });


});

