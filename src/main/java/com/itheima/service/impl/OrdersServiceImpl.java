package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.bean.*;
import com.itheima.dao.OrdersDao;
import com.itheima.exception.CustomException;
import com.itheima.service.*;
import com.itheima.utils.BaseContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersDao, Orders> implements OrdersService {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public boolean submit(Orders orders) {

        //1.获取当前下单用户信息
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);

        //2.获取收货地址
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook==null){
            throw  new CustomException("用户地址为空，无法下单！");
        }

        //3.获取当前登录用户购物车中的商品信息
        LambdaQueryWrapper<ShoppingCart> shoppingCartWrapper = new LambdaQueryWrapper<>();
        shoppingCartWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartWrapper);
        if(shoppingCartList==null || shoppingCartList.size()==0){
            throw  new CustomException("购物车为空，无法下单！");
        }

        //4.将订单中商品信息保存到订单详情表中 orders_detail
        Long orderId = IdWorker.getId();  //订单id： 使用雪花算法生成一个id出来
        AtomicInteger sumAmount = new AtomicInteger(0);//总金额： 原子类 可以解决并发环境下原子性问题 防止线程不安全，保证金额累加不出现原子性问题

        List<OrderDetail> orderDetailList = new ArrayList<>();
        orderDetailList = shoppingCartList.stream().map((cart)->{
            //1.将shoppingCart对象中的商品信息属性赋值到OrderDetail对象中
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail,"id");
            //2.设置订单详情表中的商品关联的订单id
            orderDetail.setOrderId(orderId);
            //3.计算当前订单商品的总金额  BigDecimal:保证数据精度 防止丢失精度
            sumAmount.addAndGet((cart.getAmount().multiply(new BigDecimal(cart.getNumber()))).intValue());
            //4.返回orderDetail对象
            return  orderDetail;
        }).collect(Collectors.toList()); //5.收集订单详情信息到orderDetailList集合

        //4.6：批量保存订单详情信息到订单详情表中
        boolean flag1 = orderDetailService.saveBatch(orderDetailList);


        //5.将订单信息保存到订单表中 orders
        //id：订单id
        orders.setId(orderId);
        //number：订单号 String
        orders.setNumber(UUID.randomUUID().toString().replaceAll("-",""));
        //status：订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
        orders.setStatus(2);
        //下单用户id： userId
        orders.setUserId(userId);
        //地址id：addressBookId 【前端传递过来的有】
        //下单时间：orderTime
        orders.setOrderTime(LocalDateTime.now());
        //结账时间：checkoutTime
        orders.setCheckoutTime(LocalDateTime.now());
        //支付方式 1微信，2支付宝：payMethod; 【前端传递过来的有】
        //实收金额： amount
        orders.setAmount(new BigDecimal(sumAmount.intValue()));
        //备注： remark 【前端传递过来的有】
        //用户名：userName;
        orders.setUserName(user.getName());
        //手机号： phone;
        orders.setPhone(addressBook.getPhone());
        //地址：address;
        orders.setAddress(addressBook.getProvinceName()==null?"":addressBook.getProvinceName()+
                          addressBook.getCityName()==null?"":addressBook.getCityName()+
                          addressBook.getDistrictName()==null?"":addressBook.getDistrictName()+
                          addressBook.getDetail()==null?"":addressBook.getDetail()
                        );
        //收货人：consignee;
        orders.setConsignee(addressBook.getConsignee());

        boolean flag2 = this.save(orders);

        //6.清空用户购物车记录
        shoppingCartService.remove(shoppingCartWrapper);

        return flag1&&flag2;
    }
}
