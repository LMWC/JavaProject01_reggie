package com.itheima.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.itheima.bean.Employee;
import com.itheima.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    //添加web起步依赖后，在IOC容器中已经管理了HttpSession对象 可以直接注入使用
   /* @Autowired
    private HttpSession session;*/

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("【MyMetaObjectHandler insert-线程id：{}】",Thread.currentThread().getId());

        //获取当前登录的用户id
        //Long employeeId = (Long) session.getAttribute("employee");
        Long employeeId = BaseContext.getCurrentId();

        log.info("公共字段自动填充【insert】");
        //createTime、createUser、updateTime、updateUser
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("createUser", employeeId);

        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", employeeId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("【MyMetaObjectHandler update-线程id：{}】",Thread.currentThread().getId());

        //获取当前登录的用户id
        //Long employeeId = (Long) session.getAttribute("employee");
        Long employeeId = BaseContext.getCurrentId();

        log.info("公共字段自动填充【update】");
        //updateTime、updateUser
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", employeeId);
    }
}
