package com.itheima.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.bean.R;
import com.itheima.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter("/*")
public class LoginCheckFilter implements Filter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        log.info("【LoginCheckFilter-线程id：{}】",Thread.currentThread().getId());


        //0.强转两个对象
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        //1.获取当前请求URI
        String uri = req.getRequestURI();
        //log.info("【LoginCheckFilter-本次请求地址：{}】",uri);

        /*=====================================================传统方式实现判断===================================================*/
        //2.判断当前请求地址是否需要拦截
        //3.不需要拦截 直接放行
        /*if(uri.contains("/backend") ||  uri.contains("/front") || uri.contains("/login") || uri.contains("/logout")){
            chain.doFilter(req,resp);
            //控制代码不再往下执行了
            return;
        }*/


        /*=====================================================使用Spring提供的路径匹配器===================================================*/
        //a.声明不需要拦截的路径地址数组
        String[] paths = {"/backend/**","/front/**","/employee/login","/employee/logout","/user/sendMsg","/user/login"};
        //b.遍历不需要进行拦截的路径地址  和当前请求地址进行匹配
        boolean flag = false;
        for (String path : paths) {
            if(PATH_MATCHER.match(path,uri)){
                flag = true;
                break;
            }
        }
        //c.匹配通过true 不进行过滤拦截
        if(flag){
            chain.doFilter(req,resp);
            //控制代码不再往下执行了
            return;
        }

        log.info("【LoginCheckFilter-本次拦截请求地址：{}】",uri);
        //4.需要拦截，判断管理端用户是否登录 已登录：直接放行
        //4.1：获取session中存储的员工id
        Object employee = req.getSession().getAttribute("employee");
        //4.2：如果employee为null 说明未登录  不为null：登录
        if(employee!=null){
            //将当前登录的员工id存入ThreadLocal中 在一次线程间共享
            BaseContext.setCurrentId((Long) employee);
            //请求之前的设置
            chain.doFilter(req,resp);
            //请求处理完毕 响应的设置
            return;
        }

        //4.需要拦截，判断移动端用户是否登录 已登录：直接放行
        Object user = req.getSession().getAttribute("user");
        if(user!=null){
            //将当前登录的用户id存入ThreadLocal中 在一次线程间共享
            BaseContext.setCurrentId((Long) user);
            //放行请求
            chain.doFilter(req,resp);
            //执行完本次请求 回到过滤器这里 直接return 跳出过滤器方法执行 代码不再继续向下执行了
            return;
        }


        //5.未登录：返回结果  由前端收到处理结果完成页面的跳转
        //注意：需要将处理结果封装到R对象中进行返回  code=1
        response.getWriter().print(JSON.toJSONString(R.error("NOTLOGIN")));

    }

    @Override
    public void init(FilterConfig config) throws ServletException {

    }

}