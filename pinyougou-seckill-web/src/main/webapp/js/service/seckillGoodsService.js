app.service('seckillGoodsService',function ($http) {
    this.findList=function () {
        return $http.get('/seckillGoods/findList.do');
    }
    this.findOne=function (id) {
        return $http.get('/seckillGoods/findOne.do?id='+id);
    }

    /**
     *
     * @param id  商品的ID
     */
    this.submitOrder=function (id) {
        return $http.get('/seckillOrder/submitOrder.do?id='+id);
    }

    this.queryStatus=function () {
        return $http.get('/seckillOrder/queryStatus.do');
    }
})