package com.itheima.utils;

public class BaseContext {
    private static final ThreadLocal<Long> THREAD_LOCAL_CURRENTID = new ThreadLocal<>();

    //获取当前登录的用户id
    public static Long getCurrentId(){
        return THREAD_LOCAL_CURRENTID.get();
    }

    //设置当前登录的用户id 存入ThreadLocal中 在一个线程间进行数据共享
    public static void setCurrentId(Long id){
        THREAD_LOCAL_CURRENTID.set(id);
    }
}
