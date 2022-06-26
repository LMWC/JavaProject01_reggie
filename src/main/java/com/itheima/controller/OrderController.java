package com.itheima.controller;

import com.itheima.bean.Orders;
import com.itheima.bean.R;
import com.itheima.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @PostMapping("/submit")
    public R submit(@RequestBody Orders orders){
        log.info("orderss:{}",orders);

        boolean flag = ordersService.submit(orders);

        return flag?R.success("下单成功！"):R.error("下单失败！");
    }
}
