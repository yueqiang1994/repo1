app.controller('userIndexController',function ($scope,userIndexService) {
    //写一个方法在页面初始化的时候调用 获取用户的信息
    $scope.findUserInfo=function () {
        userIndexService.findUserInfo().success(
            function (response) {//Map
                $scope.info = response;
            }
        )
    }
})