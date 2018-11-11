app.controller('searchController',function ($scope,$location,searchService) {
    //写一个方法 当点击搜索的按钮的时候要调用发送请求将数据查询出来 展示到页面中

    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sortType':'','sortField':''};

    $scope.search=function () {
        searchService.search($scope.searchMap).success(
            function (response) {//Map
                $scope.resultMap=response;
                buildPageLable();
            }
        )
    }
    //方法需要在页面初始化的时候调用
    $scope.searchByPortalkeyword=function () {
        //1.获取页面中传递过来的搜索的关键字
        var keywords = $location.search()['keywords'];
        if(keywords!=undefined && keywords!=null){
            $scope.searchMap.keywords=keywords;
            //2.调用搜索的方法来查询
            $scope.search();
        }

    }

    //方法用于影响变量searchMap的值 在点击分类的时候  在点击 品牌的时候  在点击规格的选项的时候调用
    $scope.addSearchItem=function (key,value) {
        if(key=='category' || key=='brand'|| key=='price'){
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;//spec:{}
        }

        $scope.search();
    }
    //方法用于影响变量searchMap的值 在点击分类的时候  在点击 品牌的时候  在点击规格的选项的时候调用 移除值
    $scope.removeSearchItem=function (key) {//category
        if(key=='category' || key=='brand' || key=='price'){
            $scope.searchMap[key]='';
        }else{
            delete $scope.searchMap.spec[key];
        }
    }
    //构建分页标签栏位

    buildPageLable=function () {
        var totalPages = $scope.resultMap.totalPages;//总页数
        $scope.pageLable=[];
        var firstPage=1;
        var lastPage=$scope.resultMap.totalPages;


        $scope.dottedPre=false;//用做显示前面的点 false  就是不显示
        $scope.dottedNext=false;//用作显示后面的点 false  就是不显示

        //中间这里要进行逻辑判断
        if(totalPages>5){
            //判断如果当前页 < 3 就要显示前5页
            if($scope.searchMap.pageNo<3){
                firstPage=1;
                lastPage=5;
                $scope.dottedPre=false;//用做显示前面的点 false  就是不显示
                $scope.dottedNext=true;//用作显示后面的点 false  就是不显示

            }else if($scope.searchMap.pageNo>(totalPages-2)){
                //如果当前页 > 总页数-2  就要显示后5页
                lastPage=totalPages;
                firstPage=$scope.resultMap.totalPages-4;

                $scope.dottedPre=true;//用做显示前面的点 false  就是不显示
                $scope.dottedNext=false;//用作显示后面的点 false  就是不显示

            }else {
                firstPage= $scope.searchMap.pageNo-2;  //  4 5 6 7 8
                lastPage=$scope.searchMap.pageNo+2;
                $scope.dottedPre=true;//用做显示前面的点 false  就是不显示
                $scope.dottedNext=true;//用作显示后面的点 false  就是不显示
            }
        }else{
            //显示前5页
            $scope.dottedPre=false;//用做显示前面的点  false  就是不显示
            $scope.dottedNext=false;//用作显示后面的点 false  就是不显示
        }
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLable.push(i);
        }
    }

    //点击的时候调用该方法
    $scope.queryByPage=function (pageNo) {
        //将传递过来的页码 影响变量的值

        //判断 pageno是否是一个数字
        console.log(isNaN(pageNo));

        if(isNaN(pageNo)==false){//如果是false 表示的是数字类型的字符串

            if($scope.searchMap.pageNo>$scope.resultMap.totalPages){
                $scope.searchMap.pageNo=$scope.resultMap.totalPages;
            }else if($scope.searchMap.pageNo<=0){
                $scope.searchMap.pageNo=1;
            }else{
                $scope.searchMap.pageNo=parseInt(pageNo);;
            }
            $scope.search();
        }else{
            alert("请输入数字");
        }
    }
    

    //清空
    $scope.clear=function () {
        $scope.searchMap={'keywords':$scope.searchMap.keywords,'category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sortType':'','sortField':''};
    }
    //该方法要在点击价格/或者其他的排序的条件的时候调用
    $scope.sortByFieldAndType=function (sortType,sortField) {
        //改变 searchMap的值
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sortType=sortType;
        //查询
        $scope.search();

    }

    /**
     * 判断 关键字是否就是品牌 如果是 返回true  如果不是返回false
     * @returns {boolean}
     */
    $scope.keywordsIsBrand=function () {
        //循环遍历品牌列表
        for(var i=0;i< $scope.resultMap.brandList.length;i++){
            var brand = $scope.resultMap.brandList[i];
            //如果关键字中包含品牌的字存在就表示 是要显示品牌
            if($scope.searchMap.keywords.indexOf(brand.text)!=-1){
                $scope.searchMap.brand=brand.text;
                return true;
            }
        }
        return false;
    }

})
