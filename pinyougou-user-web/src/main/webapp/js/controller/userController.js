app.controller('userController',function ($scope,userService) {
    //写一个方法 在用户点击完成注册的按钮的时候调用
    //定义一个变量用于绑定的所有的属性 $scope.entity={'username':"",password:"1"}

    $scope.register=function () {

        //先判断是否密码一致
        if($scope.entity.password!=$scope.confirmpassword){
            alert("密码不一致");
            return;
        }
        userService.add($scope.entity,$scope.code).success(
            function (response) {//result
                if(response.success){
                    //跳转到登录的页面
                    alert("zhuce成功");
                }else{
                    alert(response.message);
                }

            }
        )
    }
    // return $http.get('/user/createSms.do?phone='+phone);

    $scope.createSms=function () {
        userService.createSms($scope.entity.phone).success(
            function (repsonse) {
                alert(repsonse.message);
            }
        )
    }
})