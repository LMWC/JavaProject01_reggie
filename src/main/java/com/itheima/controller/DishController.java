package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.bean.Category;
import com.itheima.bean.Dish;
import com.itheima.bean.DishFlavor;
import com.itheima.bean.R;
import com.itheima.dto.DishDto;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/dish")
@RestController
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    //新增菜品
    @PostMapping
    public R add(@RequestBody DishDto dishDto){
        log.info("新增菜品：DishDto={}",dishDto);

        //1.调用service完成菜品新增
        boolean flag = dishService.saveWithFlavor(dishDto);

        //2.返回处理结果
        return flag?R.success("新增菜品成功！"):R.error("新增菜品失败！");
    }

    //分页查询
    @GetMapping("/page")
    public R page(Integer page,Integer pageSize,String name){

        //1.调用service完成分页查询
        //1.1：创建page对象 设置分页参数【当前页码、每页显示条数】
        Page<Dish> dishPage = new Page<>(page, pageSize);

        //1.2：设置查询条件 【根据菜品名称模糊查询 如果没有菜品名称 默认查询所有菜品进行进行分页】
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name!=null&&name.length()>0,Dish::getName,name);

        //1.3：调用page方法查询 传入page对象和wrapper对象
        dishService.page(dishPage,wrapper);

        /*
            问题：响应分页数据给前端 具体页面展示的每条菜品信息记录都是一个Dish对象  dish对象中只有分类的id，没有分类名称 所以导致页面上菜品分类没有显示
            解决：
                需要响应给前端页面展示的菜品数据，除了菜品的基本信息之外，还需要包含菜品的所属分类名称categoryName
                具体实现：需要自定义一个类，封装菜品的基本信息字段和菜品分类名称categoryName字段    前面刚好有一个类DishDto
                        将查询出来的每条菜品信息dish对象 封装到DishDto对象中 【菜品基本信息+菜品分类名称】
                        设置：
                            1.需要设置响应给前端的当前页数据List集合，应该是List<DishDto>
                            2.需要设置总条数响应给前端
                            3.创建Page<DishDto>对象  将新的每页展示的数据集合和总条数设置处理
                            4.响应给前端的数据 就是Page<DishDto>对象
                        实现方式一：
                            1.创建Page<DishDto>对象  Page<DishDto> dishDtoPage = new Page<DishDto>();
                            2.设置dishDtoPage的total属性【总条数】
                            3.设置dishDtoPage的records属性【当前页展示的数据集合】 List<DishDto>
                                3.1：创建List<DishDto>对象  List<DishDto> dishDtoList = new ArrayList<>();
                                3.2: 得到当前页菜品列表基本信息 List<Dish> dishList = dishPage.getRecords();
                                3.3：遍历dishList集合 将每一条菜品信息dish对象封装到dishDto对象中
                                    a：使用BeanUtils.copyProperties(dish,dishDto),将菜品的基本信息封装到dishDto对象中
                                    b：根据菜品分类id categoryId查询出categoryName封装到dishDto对象的categoryName属性中
                                    c: 将每一个dishDto对象存入到dishDtoList集合中
                            4.响应dishDtoPage对象给前端

                        实现方式二：自定义SQL语句，多表连接查询 设置返回值类型为DishDto 将查询结果封装到DishDto对象中
         */

        //完善分页功能：设置响应数据中要包含菜品分类名称  响应List<DishDto>
        //查询出来的每条菜品分类信息  Dish-->DishDto
        List<Dish> dishList = dishPage.getRecords();

        //2.1:创建Page<DishDto>
        Page<DishDto> dishDtoPage = new Page<>();
        //2.2:遍历当前页数据 将dish对象中的数据封装到DishDto中
        List<DishDto> dishDtoList = new ArrayList<>();

        for (Dish dish : dishList) {
            DishDto dishDto = new DishDto();
            //使用BeanUtils工具类 实现将一个对象中的属性数据 复制到另一个对象中【只要属性名称相同就可以赋值成功】
            //1.设置菜品基本信息到DishDto
            BeanUtils.copyProperties(dish,dishDto);
            //2.设置菜品分类名称到DishDto
            Category category = categoryService.getById(dish.getCategoryId());
            dishDto.setCategoryName(category.getName());
            //3.将每一条菜品记录DishDto再存入集合中
            dishDtoList.add(dishDto);
        }

        //2.3:将DishDtoList对象封装到DishDtoPage对象中
        dishDtoPage.setRecords(dishDtoList);        //设置当前页数据
        dishDtoPage.setTotal(dishPage.getTotal());  //设置总条数


        //3.响应处理结果
        //return R.success(dishPage);
        return R.success(dishDtoPage);
    }

    //根据id查询菜品信息（菜品基本信息&口味信息）
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){

        //1.调用service完成菜品信息查询
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        //2.响应处理结果
        return R.success(dishDto);
    }

    //修改菜品
    @PutMapping
    public R update(@RequestBody DishDto dishDto){
        //1.调用service完成修改菜品
        boolean flag = dishService.updateWithFlavor(dishDto);

        //2.响应处理结果
        return flag?R.success("修改菜品成功！"):R.error("修改菜品失败！");
    }

    //根据分类id查询菜品列表
    @GetMapping("/list")
    public R list(Long categoryId){
        //1.设置查询条件
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId,categoryId);
        //2.调用service查询
        List<Dish> list = dishService.list(wrapper);

        //改进：封装list中的每一个dish对象到DishDto对象中 然后将DishDto对象存入到DishDtolist集合中返回
        //a:创建一个DishDtolist集合
        List<DishDto> dishDtoList = new ArrayList<>();
        //b：遍历list集合 将每一个dish对象中的数据封装到dishDto对象中
        dishDtoList = list.stream().map(dish->{
            //1.创建dishDto对象
            DishDto dishDto = new DishDto();
            //2.将dish对象中的数据复制到dishDto对象中
            BeanUtils.copyProperties(dish,dishDto);
            //3.查询该菜品的口味信息，设置dishDto的flavors属性值
            //根据菜品id 去到菜品口味表中查询其对应的口味信息列表
            LambdaQueryWrapper<DishFlavor> flavorWrapper = new LambdaQueryWrapper<>();
            flavorWrapper.eq(DishFlavor::getDishId,dish.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(flavorWrapper);

            dishDto.setFlavors(dishFlavors);
            //4.返回dishDto对象
            return dishDto;
        }).collect(Collectors.toList());//5.收集每一个dishDto对象存入到dishDtoList集合中


        log.info("dishDtoList：{}",dishDtoList);
        //3.返回查询结果
        //return R.success(list);
        return R.success(dishDtoList);
    }

    /**
     * 批量修改菜品状态
     * @param status     菜品状态
     * @param ids        菜品id
     * @return           响应信息
     */
    @PostMapping("/status/{status}")
    public R status(@PathVariable Integer status, @RequestParam List<Long> ids) {

        boolean flag = dishService.updateWithStatus(status, ids);

        return flag ? R.success("修改状态成功") : R.error("修改状态失败");

    }

    /**
     * 批量删除菜品
     * @param ids    菜品id
     * @return       响应信息
     */
    @DeleteMapping
    public R delete(@RequestParam List<Long> ids) {

        boolean flag = dishService.removeByIds(ids);

        return  flag ? R.success("删除成功") : R.error("删除失败");
    }
}

