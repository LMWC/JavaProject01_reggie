package com.itheima.controller;

import com.itheima.bean.R;
import com.itheima.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequestMapping("/common")
@RestController
public class CommonController {

    @Value("${reggie.upload}")
    private String uploadPath;

    /*
        文件上传：
            1.定义一个方法接收请求处理
            2.设置方法参数类型为MultipartFile类型 参数名称和文件上传表单项name属性值一致
            3.在方法中完成IO读写 将上传的文件保存到服务器硬盘指定位置
     */
    @PostMapping("/upload")
    public R upload(MultipartFile file){

        //注意：文件上传下载功能测试时，一定要先登录
        //优化1：文件名不要写死 如果一旦文件名固定了 ，会导致用户先上传的文件被后上传的覆盖
        //优化2：文件上传服务端保存地址不要写死了 硬编码问题  以后不能修改  希望文件上传服务器保存地址是可以配置的，进行修改的
        //优化3：如果服务器保存上传文件的路径不存在  需要使用代码手动创建 否则会报错

        //1.重新设置上传的文件名称 文件名称+后缀名
        //1.0：用户上传的文件原始名称
        String filename = file.getOriginalFilename();  //a.jpg
        log.info("原文件名：{}",filename);
        //1.1：获取用户上传文件的后缀名
        String suffix = filename.substring(filename.lastIndexOf(".")); //.jpg
        //1.2：设置新的文件名 使用UUID
        String newFileName = UUID.randomUUID().toString()+suffix;
        log.info("新文件名：{}",newFileName);

        //2.判断文件上传保存目录是否存在，如果不存在 则自动创建
        File dirPath = new File(uploadPath);
        if(!dirPath.exists()){
            dirPath.mkdirs();
        }

        //3.设置服务器保存上传文件的硬盘地址
        String path = uploadPath+newFileName;

        //4.完成文件保存
        try {
            file.transferTo(new File(path));
        } catch (IOException e) {
            log.error("文件删除出现异常：{}",e.getMessage());
            throw new CustomException("文件上传失败！");
        }

        //5.响应处理结果
        return R.success(newFileName);
    }

    //文件下载
    @GetMapping("/download")
    public void download(HttpServletResponse response, String name){
        //IO流的读写 将服务器上的文件写给客户端浏览器

        try {
            //1.获取要下载文件的输入流对象
            String downloadFile = uploadPath+name;
            FileInputStream is = new FileInputStream(downloadFile);

            //2.获取输出流对象
            ServletOutputStream os = response.getOutputStream();

            //3.循环读写 完成将文件写给客户端浏览器
            byte[] b = new byte[1024];
            int len = -1;
            while((len=is.read(b))!=-1){
                os.write(b,0,len);
            }

            //4.关闭流
            os.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("找不到要下载的文件！");
        }
    }

}
