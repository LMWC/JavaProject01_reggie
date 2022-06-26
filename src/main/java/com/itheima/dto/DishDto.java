package com.itheima.dto;

import com.itheima.bean.Dish;
import com.itheima.bean.DishFlavor;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 *  封装新增菜品前端提交的数据
 */
//设置toString方法 在打印当前对象时 将父类中的属性数据也打印出来
@ToString(callSuper = true)
@Data
public class DishDto extends Dish {

    //菜品口味列表
    private List<DishFlavor> flavors = new ArrayList<>();

    //菜品分类名称
    private String categoryName;

    private Integer copies;
}
