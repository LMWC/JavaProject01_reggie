package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.bean.Setmeal;
import com.itheima.bean.SetmealDish;
import com.itheima.dao.CategoryDao;
import com.itheima.dao.SetmealDao;
import com.itheima.dto.SetmealDto;
import com.itheima.exception.CustomException;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealDao, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /*@Autowired
    private CategoryDao categoryDao;*/
    //private CategoryService categoryService;


    //保存套餐及其菜品信息
    @Override
    @Transactional
    public boolean saveWithDish(SetmealDto setmealDto) {
        //1.保存套餐基本信息到setmeal表
        boolean flag1= this.save(setmealDto);

        //2.保存套餐下的菜品信息到setmeal_dish表中
        //2.1:获取当前套餐的id    【MyBatisPlus操作套餐信息添加到setmeal表中成功之后 就会自动将套餐id赋值到setmealDto的id属性上】
        Long setmealId = setmealDto.getId();

        //2.2:设置菜品所属的套餐id
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());

        //2.3:批量保存套餐菜品信息到setmeal_dish表
        boolean flag2 = setmealDishService.saveBatch(setmealDishes);

        return flag1&&flag2;
    }

    //删除套餐
    @Override
    @Transactional
    public boolean removeWithDish(List<Long> ids) {

        //1.根据套餐id删除套餐表setmeal中的套餐记录
        //1.1：判断当前套餐是否在售  0:停售  1：启售
        List<Setmeal> setmealList = this.listByIds(ids);

        //1.2：如果在售 则不能删除
        for (Setmeal setmeal : setmealList) {
            if(setmeal.getStatus()==1){
                throw new CustomException("当前"+setmeal.getName()+"在售，无法删除！");
            }
        }

        //1.3：如果停售 则可以正常删除
        //批量删除
        boolean flag1 = this.removeByIds(ids);

        //2.根据套餐id删除套餐菜品关系表setmeal_dish中的套餐菜品记录
        //2.1：设置删除条件
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        //wrapper.eq(SetmealDish::getSetmealId,ids);    //setmeal_id=1,2
        wrapper.in(SetmealDish::getSetmealId,ids);    //setmeal_id IN (?,?)

        //2.2：使用SetmealDishService完成根据套餐id删除套餐菜品信息
        boolean flag2 = setmealDishService.remove(wrapper);

        return flag1&&flag2;
    }

    @Override
    public boolean updateWithStatus(Integer status, List<Long> ids) {

        //新建一个集合
        List<Setmeal> list = new ArrayList<>();

        //遍历ids 将id、status设置到list中
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);

            list.add(setmeal);
        }

        boolean flag = this.updateBatchById(list);

        return flag;

    }

}
