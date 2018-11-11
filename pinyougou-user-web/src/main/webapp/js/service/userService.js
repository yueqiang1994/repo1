app.service('userService',function ($http) {
    this.add=function (user,code) {
        return $http.post('/user/add.do?code='+code,user);
    }
    this.createSms=function (phone) {
        return $http.get('/user/createSms.do?phone='+phone);
    }

})