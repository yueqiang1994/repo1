package com.pinyougou.shop.controller;

import com.pinyougou.common.util.FastDFSClient;
import entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.shop.controller
 * @since 1.0
 */
@RestController
public class UploadController {

    //
    @RequestMapping("/upload")
    public Result uploadFile(MultipartFile file){
        try {
            //1.获取文件的字节流对象  获取原文件的扩展名
            byte[] bytes=file.getBytes();
            String originalFilename = file.getOriginalFilename();
            //不带点
            String extName=originalFilename.substring(originalFilename.lastIndexOf(".")+1);
            //2.调用fastdfs的客户端的代码上传
            FastDFSClient client = new FastDFSClient("classpath:config/fastdfs_client.conf");

            String path= client.uploadFile(bytes, extName);//    group1/M00/00/04/wKgZhVurCCWAddhCAAB5y7m37kU087.jpg
            //拼接URL
            String realPath = "http://192.168.25.133/"+path;
            //3.返回页面一个Result 包含路径
            return new Result(true,realPath);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
