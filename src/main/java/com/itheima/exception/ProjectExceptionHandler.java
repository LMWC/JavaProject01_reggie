package com.itheima.exception;

import com.itheima.bean.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

//表示对实现Rest风格的Controller类中的方法进行增强
@Slf4j
@RestControllerAdvice
public class ProjectExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R doException(SQLIntegrityConstraintViolationException e){
        log.error("【全局异常处理器捕获异常：{}】",e.getMessage());
        //异常信息：Duplicate entry 'zhangsan' for key 'idx_username'
        String msg = e.getMessage();
        String username = msg.split(" ")[2];
        //响应处理 结果
        return R.error(username+"已存在！");
    }

    //捕获自定义CustomException异常 进行处理
    @ExceptionHandler(CustomException.class)
    public R doCustomException(CustomException e){
        log.error("【全局异常处理器捕获异常：{}】",e.getMessage());

        //响应结果
        return R.error(e.getMessage());
    }
}
