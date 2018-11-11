app.service('payService',function ($http) {
    this.createNative=function () {
        return $http.get('/pay/createNative.do');
    }
    this.queryStatus=function (out_trade_no) {
        return $http.get('/pay/queryStatus.do?out_trade_no='+out_trade_no);
    }
})