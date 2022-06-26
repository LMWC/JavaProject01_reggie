package com.itheima.dto;


import com.itheima.bean.Setmeal;
import com.itheima.bean.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;//套餐菜品信息

    private String categoryName;    //套餐分类名称
}
