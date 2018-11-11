app.controller('cartController',function ($scope,cartService) {
    //写一个方法 当页面一旦被加载就调用 获取购物车列表 展示到页面中

    $scope.findCartList=function () {
        cartService.findCartList().success(
            function (response) {//List<cart>
                $scope.cartList = response;
                //定义两个变量
                $scope.totalNum=0;
                $scope.totalMoney=0;
                for (var i=0;i<response.length;i++){
                    var cart = response[i];

                    for(var j=0;j<cart.orderItemList.length;j++){
                        var orderItem = cart.orderItemList[j];
                        $scope.totalNum+=orderItem.num;
                        $scope.totalMoney+=orderItem.totalFee;
                    }

                }
            }
        )
    };

    $scope.addItemCart=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {//Result
                if(response.success){
                    //刷新列表
                    $scope.findCartList();
                }else{
                    alert(response.message);
                }
            }
        )
    }

    //方法 一旦被页面加载就应该调用将地址的列表查找出来 并且展示到页面中
    $scope.findAddressList=function () {
        cartService.findAddressList().success(
            function (response) {//List<tbaddress>
                $scope.addressList = response;
                //赋值 你猜 给当前的对象address

                for(var i=0;i<response.length;i++){
                    var a = response[i];//{"address":"你猜","alias":"家里","cityId":null,"contact":"尼采","createDate":null,"id":65,"isDefault":"1","mobile":"13888888888","notes":null,"provinceId":null,"townId":null,"userId":"zhangsanfeng"}
                    if(a.isDefault=='1'){
                        $scope.address=a;
                        break;
                    }
                }

            }
        )
    }

    //1.定义一个变量 用于存储地址的对象
    $scope.address={};

    //2.定义一个方法 在点击的时候调用 需要改变当前的变量的值
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }
    //3.定义一个方法用于判断 当前的循环到的对象是否不是是当前变量的对象，如果是 说明是同一个对象，返回true,
    $scope.isSelected=function (address) {
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }

    $scope.order={paymentType:'1'};//用于将来点击提交订单的时候 整个页面作为一个表单（一个对象：对应于数据库的tb_order表====tbOrder pojo）
    
    $scope.changePaymentType=function (type) {
        $scope.order.paymentType=type;
    }

    $scope.submitOrder=function () {
        //将选中的地址的信息 赋予到order变量中
        $scope.order.receiver=$scope.address.contact;
        $scope.order.receiverMobile=$scope.address.mobile;
        $scope.order.receiverAreaName=$scope.address.address;
        cartService.submitOrder($scope.order).success(
            function (response) {//result
                if(response.success){
                    //跳转到支付的页面
                    alert("微信支付");
                }else{
                    alert(response.message);
                }
            }
        )
    }
    
})