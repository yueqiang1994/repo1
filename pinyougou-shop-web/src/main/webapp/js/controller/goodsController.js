 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,typeTemplateService,itemCatService,goodsService,uploadService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id = $location.search()['id'];
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//将entity中的介绍信息存入到富文本中
				editor.html($scope.entity.goodsDesc.introduction);
				$scope.entity.goodsDesc.itemImages=angular.fromJson($scope.entity.goodsDesc.itemImages);
                $scope.entity.goodsDesc.customAttributeItems=angular.fromJson( $scope.entity.goodsDesc.customAttributeItems);
                $scope.entity.goodsDesc.specificationItems=angular.fromJson( $scope.entity.goodsDesc.specificationItems);
                for(var i=0;i<$scope.entity.itemList.length;i++){
                    var obj = $scope.entity.itemList[i];
                    obj.spec = angular.fromJson(obj.spec);
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
                if(response.success){
                    $scope.entity={};
                    editor.html('');
                    //跳转到管理页面
					window.location.href="goods.html";

                }else{
                    alert(response.message);
                }
			}		
		);				
	}

    $scope.entity={goods:{},goodsDesc:{itemImages:[],customAttributeItems:[],specificationItems:[]},itemList:[]};

    $scope.add=function () {
        $scope.entity.goodsDesc.introduction=editor.html();
        goodsService.add( $scope.entity  ).success(
            function (response) {//result
                if(response.success){
                    alert("成功");
                    $scope.entity={};
                    editor.html('');

                }else{
                    alert(response.message);
                }
            }
        )
    }
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//文件上传的访问，点击按钮的时候调用

	$scope.uploadFile=function () {
		uploadService.uploadFile().success(
			function (response) {//Result  包含了message属性 里面的值就是URL
				if(response.success){
					$scope.image_entity.url=response.message
					//$scope.url=response.message;
				}else{
					alert(response.message);
				}
            }
		)
    }
    //点击保存的时候将图片的颜色 和URL 存到js对象中  将js对象存入到数组中。

	$scope.addTableRow=function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //查询商品一级分类列表  页面初始化的时候就要调用
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(
			function (response) {//LIst<tbitemcat>
				$scope.itemCat1List=response;
            }
		)
    }

    //监听一级分类 查询二级分类

	$scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
		if(newValue!=undefined){
			itemCatService.findByParentId(newValue).success(
				function (response) {//LIst<tbitemcat>
					$scope.itemCat2List=response;
				}
			)
		}
    });
	//监听二级分类 查询三级分类
    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        if(newValue!=undefined){
            itemCatService.findByParentId(newValue).success(
                function (response) {//LIst<tbitemcat>
                    $scope.itemCat3List=response;
                }
            )
        }
    });

    //监听三级分类 查询模板
    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        if(newValue!=undefined){
            itemCatService.findOne(newValue).success(
                function (response) {//tbitemcat
                    $scope.entity.goods.typeTemplateId=response.typeId;
                }
            )
        }
    });

    //监听模板的Id的变化  查询模板的数据（目的是要模板数据中的品牌列表,）
	//监听模板的ID 的变化 查询模板的数据 （目的是获取模班中规格的数据，不够，还需要拼接 返回页面数据：
	/* [
    {"id":27,"text":"网络",options:[{optionName:'移动4G'},{}]},
    {"id":32,"text":"机身内存"}
	]*/
	// ）
    $scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
        if(newValue!=undefined){
            typeTemplateService.findOne(newValue).success(
                function (response) {//typeTemplate
                   $scope.typeTemplate = response;//这里有模板关联的品牌列表
                    $scope.typeTemplate.brandIds=angular.fromJson( $scope.typeTemplate.brandIds);

                    //如果是添加就要从模板中获取
					//如果是编辑 不要从模板中获取
					if($location.search()['id']!=undefined && $location.search()['id']!=null){
						//要编辑
					}else{
                   		 $scope.entity.goodsDesc.customAttributeItems=angular.fromJson($scope.typeTemplate.customAttributeItems);
					}
                }
            );

            //发送请求获取规格（包含规格的选项）的列表
			typeTemplateService.findSpecList(newValue).success(
				function (response) {
					$scope.specList=response;
                }
			)
        }
    });


    /**
	 * 这个方法是用于在点击的时候调用去影响变量的值
     */
    /**
	 * [
     {"attributeValue":["移动3G","移动4G"],"attributeName":"网络"},
     {"attributeValue":["16G","32G"],"attributeName":"机身内存"}
     ]
	 *
     * @param specName 网络
     * @param specValue  移动3G
     */
    //$scope.entity.goodsDesc.specificationItems=[];
    $scope.updateSpecAttribute=function (event,specName,specValue) {
		//1.先判断数组中是否有对象  (传递过来的被点击的选项所在的对象)
		//{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"}
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,specName,"attributeName");
		if(object!=null){
			//2.有  添加选项的值
			if(event.target.checked){
				//勾选
                object.attributeValue.push(specValue);
			}else{
				//取消勾选
                object.attributeValue.splice(object.attributeValue.indexOf(specValue),1);

                if(object.attributeValue.length==0){
                	//删除对象
                    $scope.entity.goodsDesc.specificationItems.splice( $scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}


		}else{
			//3.没有 添加对象
			$scope.entity.goodsDesc.specificationItems.push({"attributeValue":[specValue],"attributeName":specName});
		}


    }

    //这个方法在点击选项的时候都要调用
	//循环遍历$scope.entity.goodsDesc.specificationItems  重新构建SKU列表的变量值

       /*

       $scope.entity.goodsDesc.specificationItems=[
        {"attributeValue":["移动3G","移动4G"],"attributeName":"网络"},
            {"attributeValue":["16G","32G"],"attributeName":"机身内存"}
        ]

        */
    $scope.createItemList=function () {
    	//初始化
		$scope.entity.itemList=[{'spec':{},'price':0,'num':999,'status':'0','isDefault':'0'}];
		//循环遍历
		var speificationItems=$scope.entity.goodsDesc.specificationItems;

		for(var i=0;i<speificationItems.length;i++){
			var object=speificationItems[i];//{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"}
			//将规格的名称  和对应的规格的选项的值 拼接起来   'spec':{'网络':'移动3G','机身内存':'16G'}  存入到itemList
            $scope.entity.itemList=addColumn($scope.entity.itemList,object.attributeName,object.attributeValue);
		}

    }

    //循环遍历 重新生成一个SKU的列表
    /**
	 *
     * @param list   [{'spec':{},'price':0,'num':999,'status':'0','isDefault':'0'}];
     * @param columnName 网络
     * @param columnValues  [移动3G ,移动4G]
     * @returns {Array}
     */
    addColumn=function (list,columnName,columnValues) {
    	var newList=[];

    	for(var i=0;i<list.length;i++){
    		var oldRow = list[i];//{'spec':{},'price':0,'num':999,'status':'0','isDefault':'0'}

			for(var j =0;j<columnValues.length;j++){
				//获取 移动3G
				//深克隆
				var newRow=angular.fromJson(angular.toJson(oldRow));
                newRow.spec[columnName]=columnValues[j];//{'spec':{网络:移动3G},'price':0,'num':999,'status':'0','isDefault':'0'}
				newList.push(newRow);
			}
		}
    	return newList;
    }




    //===================================start=============

	$scope.status=['未审核','已审核','审核未通过','已关闭'];



    //1.写一个方法 查询所有的商品分类数据

	$scope.categoryList=[,,,,,,,,,,,,,"手机"];

	$scope.findAllCategoryList=function () {
        itemCatService.findAll().success(
        	function (response) {//List<tbitemcat>
					for(var i=0;i<response.length;i++){
                        $scope.categoryList[response[i].id]=response[i].name;
					}
            }
		)
    }

    //用于判断 页面显示的选项的值是否在数组中存储，如果存在就勾选
	//数组：
	// "specificationItems":[{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"},
	//                       {"attributeValue":["16G","32G"],"attributeName":"机身内存"}]
    $scope.checkAttributeValue=function (specName,specValue) {
		//从数组中获取对象  如果对象存在 说明 有对象 再寻找值
        var obj = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,specName,'attributeName');
        if(obj!=null){
            if(obj.attributeValue.indexOf(specValue)!=-1){
            	//找到
				return true;
			}
		}
		return false;
    }












});	
