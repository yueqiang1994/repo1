app.controller('seckillGoodsController',function ($scope,$location,$interval,seckillGoodsService) {
    //页面一旦加载就应该查询所有的商品的列表 循环展示到页面中

    $scope.findList=function () {
        seckillGoodsService.findList().success(
            function (response) {//List<tbseckillgoods>
                $scope.list=response;
            }
        )
    }
    //是要从url中获取id的参数的值，用$location
    $scope.findOne=function () {
        var id = $location.search()['id'];
        seckillGoodsService.findOne(id).success(
            function (response) {//tbseckillgoods
                $scope.entity = response;


                //先获取到数据库中的结束时间
                var enddate = new Date($scope.entity.endTime).getTime();//表示从1970年到 $scope.entity.endTime时间的毫秒数
                //获取到当前的时间
                var currentdate = new Date().getTime();//表示从1970年到 当前时间的毫秒数
                // 获取剩余的秒数
                var allsecond = Math.floor((enddate- currentdate)/1000);
                time= $interval(function(){
                    $scope.second=convertTimeString(allsecond);
                    if(allsecond>0){
                        allsecond =allsecond-1;
                    }else{
                        $interval.cancel(time);
                        alert("秒杀服务已结束");
                    }
                },1000);
            }
        )
    }

    //转换秒为   天小时分钟秒格式  XXX天 10:22:33
    convertTimeString=function(allsecond){
        var days= Math.floor( allsecond/(60*60*24));//天数
        var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小时数
        var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
        var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        if(days>0){
            days=days+"天 ";
        }
        if(hours<10){
            hours="0"+hours;
        }
        if(minutes<10){
            minutes="0"+minutes;
        }
        if(seconds<10){
            seconds="0"+seconds;
        }
        return days+hours+":"+minutes+":"+seconds;
    }

    //提交订单 抢购

    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function (response) {//result
                if(response.success){//true  就代表一定是订单创建成功吗？
                    //调用查询的订单的状态的方法 每隔三秒钟查询
                    var allsecond=100;// 定时5分钟 每隔三秒查询一次
                    time= $interval(function(){

                        if(allsecond>0){
                            allsecond=allsecond-1;
                            seckillGoodsService.queryStatus().success(
                                function (response) {//result
                                    if(response.success){
                                        alert("chenggong");
                                        $interval.cancel(time);
                                    }else{
                                        //是false的情况：要登录，提示在排队，创建订单失败

                                        if(response.message=='401'){
                                            //要登录了

                                        }else{
                                            alert(response.message);
                                        }
                                    }
                                }
                            );
                        }else{
                            $interval.cancel(time);
                            alert("超时");
                        }
                    },3000);
                }else{
                    if(response.message=='401'){
                        //要登录了
                        //获取当前浏览器的URL:http://localhost:9109/seckill-item.html#?id=5
                        var url = window.location.href;
                        window.location.href="/page/login.do?url="+encodeURIComponent(url);// /page/login.do要做的事是后台接收，并且重定向到url中
                    }else{
                        alert(response.message);
                    }
                }
            }
        );
    }

})