package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.bean.Category;
import com.itheima.bean.R;
import com.itheima.bean.Setmeal;
import com.itheima.dto.SetmealDto;
import com.itheima.exception.CustomException;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequestMapping("/setmeal")
@RestController
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    //新增套餐
    @PostMapping
    public R add(@RequestBody SetmealDto setmealDto){
        log.info("新增套餐-套餐信息：{}",setmealDto);

        //1.调用service完成新增套餐
        //由于直接 调用save方法只能保存套餐的基本信息  无法保存套餐的菜品信息 因此  需要扩展save方法
        boolean flag = setmealService.saveWithDish(setmealDto);
        //2.返回处理结果
        return flag?R.success("新增套餐成功！"):R.error("新增套餐失败！");
    }

    //套餐分页展示
    @GetMapping("/page")
    public R page(Integer page,Integer pageSize,String name){
        //1.创建Page对象 设置分页参数【当前页码 每页显示条数】
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);

        //2.设置查询条件 【套餐名称 name】
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name!=null&&name.length()>0,Setmeal::getName,name);

        //3.调用service中page方法【MyBatisPlus提供的ServiceImpl中】
        setmealService.page(setmealPage,wrapper);

        /*
            分析：
                1.Page<Setmeal> setmealPage  -->  Page<SetmealDto> setmealDtoPage
                2.直接复制分页的相关数据到setmealDtoPage中
                3.重新封装当前页数据records   到setmealDtoPage中
         */
        //3.1：创建setmealDtoPage对象
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //3.2：封装总条数、每页显示条数、当前页码这些数据到setmealDtoPage对象中  忽略records属性的值不进行复制
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        //3.3：封装当前页数据集合到setmealDtoPage对象中
        List<Setmeal> setmealList = setmealPage.getRecords();
        List<SetmealDto> setmealDtoList = new ArrayList<>();
        for (Setmeal setmeal : setmealList) {
            //a:将setmeal对象中的数据都赋值到setmealDto对象中
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);
            //b:设置setmealDto的categoryName属性值
            Category category = categoryService.getById(setmeal.getCategoryId());
            setmealDto.setCategoryName(category.getName());
            //c:将setmealDto对象放到setmealDtoList集合中
            setmealDtoList.add(setmealDto);
        }

        setmealDtoPage.setRecords(setmealDtoList);

        //4.返回分页查询数据
        //return R.success(setmealPage);
        return R.success(setmealDtoPage);
    }

    //单个删除和批量删除
    /*
        单个删除和批量删除 都是传递一个参数，只是参数值不一样 ，所以此时定义List集合接收请求参数
        由于list集合并不能直接使用接收请求参数  所以，需要在参数前打上@RequestParam注解 将请求参数映射封装到List集合对象中
     */
    @DeleteMapping
    public R delete(@RequestParam List<Long> ids){

        boolean flag = setmealService.removeWithDish(ids);


        return flag?R.success("套餐删除成功！"):R.error("套餐删除失败！");
    }

    @GetMapping("/list")
    public R list(Long categoryId,Integer status){
        //1.调用service根据分类id和套餐状态查询指定分类下的套餐列表信息
        //1.1：设置查询条件
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId!=null,Setmeal::getCategoryId,categoryId);
        wrapper.eq(status!=null,Setmeal::getStatus,status);

        //1.2：调用方法查询
        List<Setmeal> list = setmealService.list(wrapper);

        //2.返回处理结果
        return R.success(list);
    }
}
