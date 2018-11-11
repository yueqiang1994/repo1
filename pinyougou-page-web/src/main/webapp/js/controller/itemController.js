app.controller('itemController',function($scope,$http){
	$scope.num=1;
	$scope.add=function(num){
//		console.log(num);
//		console.log(isNaN(num));
		if(isNaN(num)){
			
			//不是数字
			alert("请输入数字");
			return;
		}
		
		var x = parseInt(num);
		$scope.num=$scope.num+x;
		if($scope.num<=0){
			$scope.num=1;
		}
	}
	
	//定义一个变量 用于存储 当前的规格数据（点击的规格的数据）
	
	//skuList[0]={"id":1,"title":"手机 16G 移动4G","price":1,"spec":{"网络":"移动4G","机身内存":"32G"}}
	
	$scope.specificationItems=angular.fromJson(angular.toJson(skuList[0].spec));  //angular.fromJson()
	
	$scope.sku=skuList[0];
	
	//定义方法 用于点击的时候影响变量的值
	
	$scope.selectSpecification=function(key,value){
		$scope.specificationItems[key]=value;
		$scope.skuChange();
	}
	
	
	
	//写一个方法 用于判断 当前的变量 是否有 你点击的规格的数据         如果 有 说明要选中，如果没有不选中
	
	$scope.isSelected=function(key,value){
		if($scope.specificationItems[key]==value){
			return true;	
		}else{
			return false;
		}
		
		
	}
	
	$scope.skuChange=function(){
		for(var i=0;i<skuList.length;i++){
			
			if(angular.toJson($scope.specificationItems)==angular.toJson(skuList[i].spec)){
				$scope.sku=skuList[i];
				break;
			}
			
		}
		
	}

	//添加购物车
	$scope.addGoodsToCartList=function () {
			$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+"&num="+$scope.num,{'withCredentials':true}).success(
				function (response) {//Result
					if(response.success){
						//跳转到购物车的列表
						window.location.href="http://localhost:9107/cart.html";
					}else{
						alert(response.message);
					}
                }
			);
    }

	
	
	
	
	
	
	
})