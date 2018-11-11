app.controller('indexController',function ($scope,indexService) {

    $scope.contentList=[];

    //页面一加载就根据分类的ID 查询轮播图的列表 展示
    $scope.findContentList=function (categoryId) {
        indexService.findContentList(categoryId).success(
            function (response) {//List
                $scope.contentList[categoryId]=response;

            }
        )
    }
})