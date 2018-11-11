var app = angular.module('pinyougou', []);//定义模块

app.filter('trustHtml',function ($sce) {
    return function (data) {//data 就是没有解释的html的标签的文本
        return $sce.trustAsHtml(data);
    }
})