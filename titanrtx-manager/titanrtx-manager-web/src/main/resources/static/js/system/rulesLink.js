app.controller("rulesLinkController",function ($http,$scope,$rootScope,ngInvoke) {

    $scope.rulesModel={};

    $scope.initFn = function(){
        $scope.doInitFn(getUrlPath("rulesLinkRules"),'');
    }();

    $scope.deleteRules = function (id) {
        layer.confirm('确定删除?',{icon: 3, title:'提示'}, function(index){
            $http.get(getUrlPath("rulesDeleteRules"),{params:{"id":id}}).then(function (respData) {
                layer.msg(respData.msg);
                if (vailSuccess(respData)) {
                    return $scope.reloadCurrentPage();
                }
            });
            layer.close(index);
        });
    };


    $scope.addOrEditRulesDialog = function (id) {
        ngInvoke.syncInvoke(getUrlPath("rulesInfo"),'get',{"id":id}).then(function (respData) {
            if (vailSuccess(respData)){
                $scope.rulesModel = respData.data;
                layui.layer.open({
                    title : "规则信息",
                    type: 1,
                    zIndex: 9999,
                    shade: 0,
                    content: layui.$('#addOrEditRulesDialog'),
                    area: ['1000px','680px'],
                    btn : ['保存'],
                    yes : function (index) {
                        ngInvoke.syncInvoke(getUrlPath("rulesAddRules"),'post',$scope.rulesModel).then(function (respData){
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

    layui.form.on('select(rulesTypeFilter)',function(elem){
        $scope.rulesModel.rulesType = elem.value;
        $rootScope.render();
    });

    $scope.$watch('searchModel', function() {
        let searchKey =  $scope.searchModel;
        if (isBlank(searchKey)){
            $scope.resetTablePage();
            return;
        }
        let totalFilterIndexData=[];
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
            totalFilterIndexData.push( $scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });


});

