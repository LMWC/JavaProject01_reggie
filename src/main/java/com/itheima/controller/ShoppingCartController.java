package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.bean.R;
import com.itheima.bean.ShoppingCart;
import com.itheima.service.ShoppingCartService;
import com.itheima.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/shoppingCart")
@RestController
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    //1.添加菜品或套餐到购物车shopping_cart表中
    /*@PostMapping("/add")
    public R add(@RequestBody ShoppingCart cart){
        //1.获取当前登录的用户id，设置到cart对象的userId属性上
        cart.setUserId(BaseContext.getCurrentId());

        //2.调用service完成添加

        //2.1：判断当前要添加的是菜品还是套餐
        //2.2：判断当前用户 购物车中是否已经存在这个菜品或套餐
        Long dishId = cart.getDishId();

        if(dishId!=null){
            //添加的是菜品
            //根据用户id、菜品id、菜品口味查询当前菜品在购物车中是否存在
            //提取公共代码到外面
            LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShoppingCart::getUserId,cart.getUserId());
            wrapper.eq(ShoppingCart::getDishId,cart.getDishId());
            wrapper.eq(ShoppingCart::getDishFlavor,cart.getDishFlavor());
            ShoppingCart dishOne = shoppingCartService.getOne(wrapper);
            //设置当前加入购物车的菜品数量
            cart = dishOne==null?cart:dishOne;
            cart.setNumber(dishOne==null?1:dishOne.getNumber()+1);

        }else{
            //添加的是套餐
            //根据用户id、套餐id查询当前菜品在购物车中是否存在
            LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShoppingCart::getUserId,cart.getUserId());
            wrapper.eq(ShoppingCart::getSetmealId,cart.getSetmealId());
            ShoppingCart setmealOne = shoppingCartService.getOne(wrapper);
            //设置当前加入购物车的套餐数量
            cart = setmealOne==null?cart:setmealOne;
            cart.setNumber(setmealOne==null?1:setmealOne.getNumber()+1);
        }

        //2.3：完成菜品或套餐的添加 根据数量进行判断完成新增或修改
        boolean flag = shoppingCartService.saveOrUpdate(cart);

        //3.返回处理结果
        return flag?R.success("添加购物车成功！"):R.error("添加购物车失败！");
    }*/

    //代码优化后的代码实现：
    @PostMapping("/add")
    public R add(@RequestBody ShoppingCart cart){
        //1.获取当前登录的用户id，设置到cart对象的userId属性上
        cart.setUserId(BaseContext.getCurrentId());

        //2.调用service完成添加
        //2.1：判断当前要添加的是菜品还是套餐
        Long dishId = cart.getDishId();
        //2.2：判断当前用户 购物车中是否已经存在这个菜品或套餐
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,cart.getUserId());
        if(dishId!=null){
            //添加的是菜品
            //根据用户id、菜品id、菜品口味查询当前菜品在购物车中是否存在
            wrapper.eq(ShoppingCart::getDishId,cart.getDishId());
            wrapper.eq(ShoppingCart::getDishFlavor,cart.getDishFlavor());
        }else{
            //添加的是套餐
            //根据用户id、套餐id查询当前套餐在购物车中是否存在
            wrapper.eq(ShoppingCart::getSetmealId,cart.getSetmealId());
        }
        //根据条件查询购物车中是否存在对应的菜品或套餐记录
        ShoppingCart shoppingCart = shoppingCartService.getOne(wrapper);
        //设置当前加入购物车的菜品数量 根据数量进行判断完成新增或修改
        cart = shoppingCart==null?cart:shoppingCart;
        cart.setNumber(shoppingCart==null?1:shoppingCart.getNumber()+1);

        //2.3：完成菜品或套餐的添加
        boolean flag = shoppingCartService.saveOrUpdate(cart);

        //3.返回处理结果
        return flag?R.success("添加购物车成功！"):R.error("添加购物车失败！");
    }

    //2.查询当前登录用户购物车中的商品列表信息
    @GetMapping("/list")
    public R list(){
        //1.获取当前登录用户的id
        Long userId = BaseContext.getCurrentId();

        //2.调用service根据当前用户id查询购物车中的商品列表
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> list = shoppingCartService.list(wrapper);

        //3.返回处理结果
        return R.success(list);
    }

    //3.清空当前登录用户购物车中的商品信息
    @DeleteMapping("/clean")
    public R delete(){
        //1.获取当前登录用户的id
        Long userId = BaseContext.getCurrentId();

        //2.调用service根据当前登录的用户id完成删除当前用户购物车中所有数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);
        boolean flag = shoppingCartService.remove(wrapper);

        //3.返回处理结果
        return flag?R.success("清空购物车成功！"):R.error("清空购物车失败！");
    }

}
