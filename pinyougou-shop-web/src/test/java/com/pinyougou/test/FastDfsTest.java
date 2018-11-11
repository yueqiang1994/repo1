package com.pinyougou.test;

import com.pinyougou.common.util.FastDFSClient;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyou.test
 * @since 1.0
 */

public class FastDfsTest {
    //上传文件 1.加入依赖
    //2使用客户端代码来实现文件上传
    @Test
    public void testUpload() throws  Exception{
        //1.创建一个配置文件 文件中需要配置服务器的ip地址和端口  加载该文件
        ClientGlobal.init("C:\\Users\\ThinkPad\\IdeaProjects\\37\\pinyougou-parent\\pinyougou-shop-web\\src\\main\\resources\\config\\fastdfs_client.conf");

        //2.先创一个tracker client对象
        TrackerClient trackerClient = new TrackerClient();

        //3.创建一个tracker server
        TrackerServer trackerServer = trackerClient.getConnection();
        //4.创建一个storageClient 来直接上传文件
        StorageClient storageClient = new StorageClient(trackerServer,null);

        //5.上传图片
        /**
         * 参数1 文件路径
         * 参数2 扩展名  不要带点
         * 参数3 元数据   包括图片的像素 高度 作者 时间戳....
         */
        String[] jpgs = storageClient.upload_file("C:\\Users\\Public\\Pictures\\Sample Pictures\\Koala.jpg", "jpg", null);
        for (String jpg : jpgs) {
            System.out.println(jpg);
        }
    }

    @Test
    public void testFastClientTest() throws Exception{
        FastDFSClient client =  new FastDFSClient("C:\\Users\\ThinkPad\\IdeaProjects\\37\\pinyougou-parent\\pinyougou-shop-web\\src\\main\\resources\\config\\fastdfs_client.conf");
        String jpg = client.uploadFile("C:\\Users\\Public\\Pictures\\Sample Pictures\\10301142N-3.jpg", "jpg");
        System.out.println(jpg);
    }
}
