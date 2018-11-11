app.service('userIndexService',function ($http) {
    this.findUserInfo=function () {
        return $http.get('/user/login/info.do');
    }
})