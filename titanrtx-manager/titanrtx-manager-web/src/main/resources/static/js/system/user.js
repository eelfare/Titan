app.controller("userController", function ($http, $scope) {

    $scope.initFn = function () {
        $scope.doInitFn(getUrlPath("userList"), '');
    }();

    $scope.$watch('searchModel', function () {
        let searchKey = $scope.searchModel;
        if (isBlank(searchKey)) {
            $scope.resetTablePage();
            return;
        }
        let totalFilterIndexData = [];
        let targetIndex = [];
        $scope.searchModelTotalData.forEach(function (value, index) {
            let enterLowerName = searchKey.toLowerCase();
            //增加 工号(userName)、用户名(nickName)、手机号(phone)的查看
            let lowerUserName = value.userName.toLowerCase();
            if (lowerUserName.indexOf(enterLowerName) !== -1) {
                targetIndex.push(index);
                return;
            }
            let lowerNickName = value.nickName;
            if (lowerNickName != null) {
                if (lowerNickName.indexOf(enterLowerName) !== -1) {
                    targetIndex.push(index);
                    return;
                }
            }
            let phone = value.phone;
            if (phone.indexOf(enterLowerName) !== -1) {
                targetIndex.push(index);
            }
        });
        targetIndex.forEach(function (value) {
            totalFilterIndexData.push($scope.searchModelTotalData[value]);
        });
        return $scope.buildTablePageBar(totalFilterIndexData);
    });


    $scope.userEnable = function (id) {
        $scope.doAble(getUrlPath("userEnable"), id);
    };
    $scope.userDisable = function (id) {
        $scope.doAble(getUrlPath("userDisable"), id);
    };
    $scope.doAble = function (url, id) {
        $http.get(url, {params: {"id": id}}).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.reloadCurrentPage();
            }
        })
    }


});

app.controller("userAddOrEditController", function ($http, $scope, $routeParams, $rootScope) {

    $scope.selectedPermissions = [];

    $scope.extraPermissions = [];

    $scope.userInfo = function (id) {
        $http.get(getUrlPath("userInfo"), {params: {"id": id}}).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.userBo = respData.data.user;
                $scope.selectedPermissions = $scope.selectedPermissions.concat(respData.data.hasPermission);
                $scope.extraPermissions = $scope.extraPermissions.concat(respData.data.extraPermissions);
                $rootScope.render();
            }
        })
    };

    $scope.permissionList = function () {
        $http.get(getUrlPath("permissionList")).then(function (respData) {
            if (vailSuccess(respData)) {
                $scope.extraPermissions = respData.data;
            }
        })
    };

    $scope.initFn = function () {
        isBlank($routeParams.id) ? $scope.permissionList() : $scope.userInfo($routeParams.id);
    }();


    layui.form.on('submit(userSubmit)', function (data) {
        let selectPermissionIds = [];
        $scope.selectedPermissions.forEach(function (value) {
            selectPermissionIds.push(value.id);
        });
        let formData = data.field;
        formData.pathIds = selectPermissionIds.join();
        $http.post(getUrlPath("userAddOrUpdate"), formData).then(function (respData) {
            layer.msg(respData.msg);
            if (vailSuccess(respData)) {
                $scope.goRoutePage("/user", {});
            }
        });
        return false;
    });


    layui.form.on('checkbox(hasAllPermissionCheckBox)', function (data) {
        let permission = JSON.parse(data.value);
        $scope.extraPermissions.push(permission);
        let newSelectedPermissions = [];
        $scope.selectedPermissions.forEach(function (value) {
            if (!isEquals(value.id, permission.id)) {
                newSelectedPermissions.push(value);
            }
        });
        $scope.selectedPermissions = newSelectedPermissions;
        $scope.$apply();
        $rootScope.render();
    });

    layui.form.on('checkbox(extraAllPermissionCheckBox)', function (data) {
        let permission = JSON.parse(data.value);
        $scope.selectedPermissions.push(permission);
        let newExtraPermissions = [];
        $scope.extraPermissions.forEach(function (value) {
            if (!isEquals(value.id, permission.id)) {
                newExtraPermissions.push(value);
            }
        });
        $scope.extraPermissions = newExtraPermissions;
        $scope.$apply();
        $rootScope.render();
    });

    layui.form.on('checkbox(hasSinglePermissionCheckBox)', function (data) {
        if (data.elem.checked && $scope.selectedPermissions.length > 0) {
            $scope.extraPermissions = $scope.extraPermissions.concat($scope.selectedPermissions);
            $scope.selectedPermissions = [];
            $scope.$apply();
            $rootScope.render();
        }
    });

    layui.form.on('checkbox(extraSinglePermissionCheckBox)', function (data) {
        if (data.elem.checked && $scope.extraPermissions.length > 0) {
            $scope.selectedPermissions = $scope.selectedPermissions.concat($scope.extraPermissions);
            $scope.extraPermissions = [];
            $scope.$apply();
            $rootScope.render();
        }
    });
});