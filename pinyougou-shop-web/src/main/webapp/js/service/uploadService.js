app.service('uploadService',function ($http) {
    this.uploadFile=function () {
        var formData = new FormData();//创建一个H5的form表单对象
        //添加一个字段  <input type="file" name=file id="file">
        // append中的第一个file 就是name的名称
        //append中的第二个file 就是id=file  files 代表的就是选中的文件列表  获取一个就可以了：files[0]
        formData.append("file",file.files[0]);
       // formData.append("name","213");//<input type="text" name=name>
        return $http({
            method:'POST',
            url:"../upload.do",
            data: formData,
            headers: {'Content-Type':undefined},//设置contenttype:undefined 表示会使用多媒体类型的content-type:multpart/form-data
            transformRequest: angular.identity//设置angularjs的传递流的方式   会自动添加分割线 允许添加多个参数。
        });
    }
})