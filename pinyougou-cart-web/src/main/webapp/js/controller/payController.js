app.controller('payController',function ($scope,$location,payService) {
    //1.写一个方法 ，当支付的页面一旦被加载 就应该调用该方法，发送请求
    // 获取到code_url
    // 然后利用QRious插件生成二维码，展示给用户

    $scope.createNative=function () {
        payService.createNative().success(
            function (response) {//Map   code_url,out_trade_no ,total_fee
                $scope.out_trade_no=response.out_trade_no;

                $scope.total_fee = (response.total_fee/100).toFixed(2);//分

                //生成二维码

                var qr = new QRious({
                    element:
                        document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.code_url
                });

                $scope.queryStatus();
            }
        )
    }


    //查询支付的状态
    $scope.queryStatus=function () {
        payService.queryStatus($scope.out_trade_no).success(
            function (response) {//result
                if(response.success){
                    //支付成功
                    alert("成功");
                  window.location.href="paysuccess.html#?money="+$scope.total_fee;
                }else{
                    //false 情况 现在有两种：1.支付失败  2.支付超时
                    if(response.message=='支付超时'){
                        //重新生成二维码
                        $scope.createNative();
                    }else{
                        alert(response.message);
                    }
                }
            }
        )
    }

    $scope.getMoney=function () {
        var money = $location.search()['money'];
       return money;
    }


})