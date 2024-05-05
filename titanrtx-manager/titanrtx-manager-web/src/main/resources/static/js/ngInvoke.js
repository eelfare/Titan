app.factory('ngInvoke', ['$http', '$q', function ($http, $q) {
    return {
        syncInvoke : function(url,type,requestdata) {
            let deferred = $q.defer();
            if (isEquals('get',type)){
                $http.get(url,{params:requestdata}).then(function (respData) {
                    deferred.resolve(respData);
                })
            }else{
                $http.post(url,requestdata).then(function (respData) {
                    deferred.resolve(respData);
                });
            }
            return deferred.promise;
        },

    };
}]);