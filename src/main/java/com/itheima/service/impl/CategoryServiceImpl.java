package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.bean.Category;
import com.itheima.bean.Dish;
import com.itheima.bean.Setmeal;
import com.itheima.dao.CategoryDao;
import com.itheima.exception.CustomException;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    //扩展removeById的方法功能【重写removeById方法】
    @Override
    public boolean removeById(Serializable id) {

        //1.判断当前分类下是否存在菜品信息【根据分类id统计菜品表中对应的菜品数量  如果数量大于0】
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishWrapper);

        if(count1>0){
            //return false;
            throw new CustomException("该分类下存在菜品信息，不能删除！");
        }

        //2.判断当前分类下是否存在套餐信息【根据分类id统计套餐表中对应的套餐数量  如果数量大于0】
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealWrapper);

        if (count2>0){
            //return false;
            throw new CustomException("该分类下存在套餐信息，不能删除！");
        }

        //3.如果都不存在则删除  【调用父类的方法完成删除】
        return super.removeById(id);
    }
}
