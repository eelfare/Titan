app.controller("opsLogController",function ($http,$scope) {

    $scope.initFn = function(){
        $scope.doInitFn(getUrlPath("opsLogList"),getUrlPath("opsLogSearch"));
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
            let lowerUserName = value.userName.toLowerCase();
            let enterLowerName  = searchKey.toLowerCase();
            if (lowerUserName.indexOf(enterLowerName) !== -1){
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push( $scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });
});