package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.bean.OrderDetail;
import com.itheima.bean.Orders;
import com.itheima.bean.R;
import com.itheima.dto.OrdersDto;
import com.itheima.service.OrderDetailService;
import com.itheima.service.OrdersService;
import com.itheima.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 订单提交
     * @param orders    订单
     * @return          响应信息
     */
    @PostMapping("/submit")
    public R submit(@RequestBody Orders orders){
        log.info("orderss:{}",orders);

        boolean flag = ordersService.submit(orders);

        return flag?R.success("下单成功！"):R.error("下单失败！");
    }

    /**
     * 用户端 展示订单详情
     * @param page      页面
     * @param pageSize  页面大小
     * @return          响应信息
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {

        //分页构造器对象
        Page<Orders> ordersPage = new Page<>(page, pageSize);

        //获取当前用户ID
        Long userID = BaseContext.getCurrentId();

        //查询用户的订单
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.in(userID != 0, Orders::getUserId, userID);
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //此页面没有订单详情信息，需要封装到dishDto中
        ordersService.page(ordersPage, queryWrapper);

        //创建空Dto用来承接dtoPage的分页数据
        Page<OrdersDto> dtoPage = new Page<>();
        //除PageInfo内Orders的数据，将orderPage中的全部拷贝到dtoPage中
        BeanUtils.copyProperties(ordersPage, dtoPage, "records");//records添加与否好像无影响???

        //用stream流对 orderPage 内的每一条 records 进行处理
        List<OrdersDto> OrdersDtoList = ordersPage.getRecords().stream().map(orders -> {
            //把 orders 中records数据拷贝到 OrdersDto 中
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(orders, ordersDto);

            //新建一个订单详情类的选择器，用订单id进行绑定
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(OrderDetail::getOrderId, orders.getId());

            //收集到一个list中
            List<OrderDetail> list = orderDetailService.list(wrapper);

            //用该list为orderDto赋值
            ordersDto.setOrderDetails(list);

            return ordersDto;
        }).collect(Collectors.toList());

        //条件筛选后的 OrdersDtoList 作为记录设置到 dtoPage 中
        dtoPage.setRecords(OrdersDtoList);

        return R.success(dtoPage);
    }

    /**
     * 后端 展示某一个时间段订单明细
     * @param page          页面
     * @param pageSize      页面大小
     * @return              响应信息
     */
    @GetMapping("/page")
    public R orderListByTime(Integer page, Integer pageSize,String beginTime, String endTime) {

        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();

        //查询用户的订单
        if (beginTime == null || endTime == null) {
            wrapper.orderByDesc(Orders::getOrderTime);
        } else {
            wrapper.orderByDesc(Orders::getOrderTime);
            wrapper.lt(Orders::getOrderTime, endTime);
            wrapper.gt(Orders::getOrderTime, beginTime);

            //wrapper.between(Orders::getOrderTime, beginTime, endTime);
        }

        ordersService.page(pageInfo, wrapper);

        return R.success(pageInfo);
    }

    /**
     * 订单派送
     * @param orders  订单信息
     * @return   响应信息
     */
    @PutMapping
    public R send(@RequestBody Orders orders) {

        Integer status = orders.getStatus();

        //订单派送 -> 订单完成
        orders.setStatus(status);
        ordersService.updateById(orders);


        return R.success("订单派送成功！");
    }

    /**
     * 再来一单
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){

        ordersService.again(orders);
        return R.success("相同菜品已加入购物车！");
    }
}
