package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.bean.Orders;

public interface OrdersService extends IService<Orders> {

    //用户下单：保存订单信息到订单表和订单详情表中
    boolean submit(Orders orders);

    /**
     * 再来一单
     * @param orders
     */
    void again(Orders orders);
}
