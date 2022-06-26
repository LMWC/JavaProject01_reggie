package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.bean.Dish;
import com.itheima.dto.DishDto;

public interface DishService extends IService<Dish> {

    /**
     * 保存菜品及其口味信息
     * @param dishDto
     * @return
     */
    boolean saveWithFlavor(DishDto dishDto);

    /**
     * 根据id查询菜品基本信息及其口味信息返回
     * @param id
     * @return
     */
    DishDto getByIdWithFlavor(Long id);

    /**
     * 修改菜品及其口味信息
     * @param dishDto
     * @return
     */
    boolean updateWithFlavor(DishDto dishDto);
}
