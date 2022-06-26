package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.bean.Dish;
import com.itheima.bean.DishFlavor;
import com.itheima.dao.DishDao;
import com.itheima.dto.DishDto;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishDao, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    //保存菜品及其口味信息
    /*
        优化1：当菜品没有口味信息时  不再进行口味信息的批量添加
        优化2：使用Stream流遍历菜品口味信息，为每个口味设置对应的菜品id
        优化3：添加事务管理
            事务打在接口上：对该接口所有实现类中的所有方法都添加事务管理
            事务打在类上：对该类中的所有方法都添加事务管理
            事务打在接口方法上：对该接口所有实现类中的这个方法都添加事务管理
            事务打在实现类方法上：对该类中的这个方法添加事务管理
            实现步骤：
                1.在方法上打上@Transactional注解
                2.在启动类上打上@EnableTransactionManagement 开启事务
     */
    @Transactional
    @Override
    public boolean saveWithFlavor(DishDto dishDto) {
        //1.向菜品表中插入菜品的基本信息  调用MyBatisPlus提供的save方法
        boolean flag1 = this.save(dishDto);

        //2.向口味表中插入菜品口味的基本信息  需要管理菜品id
        //2.1：需要从DishDto中得到口味信息列表
        List<DishFlavor> flavors = dishDto.getFlavors();

        //id=null, dishId=null, name=甜味, value=["无糖","少糖","半糖","多糖","全糖"]
        //2.2:设置每个口味对应的菜品id
        //直接从DishDto中获取到菜品id 一旦菜品基本信息新增成功 MyBatisPlus就帮我们将新增成功的菜品id设置到了DishDto对象中【返回新增数据的主键id】
        Long dishId = dishDto.getId();

        //方式一：for循环遍历
        /*for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }*/

        //方式二：使用JDK8 Stream流
        flavors = flavors.stream().map((flavor)->{
            flavor.setDishId(dishId);
            return flavor;
        }).collect(Collectors.toList());


        log.info("菜品口味信息：{}",flavors);


        //2.3：批量新增口味信息到口味表
        boolean flag2 = true; //默认添加口味成功  防止菜品没有口味信息时  批量插入返回false
        if(flavors!=null&&flavors.size()>0){
            flag2 = dishFlavorService.saveBatch(flavors);
        }
        return flag1&&flag2;
    }

    //根据id查询菜品及其口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //0.创建DishDto对象封装返回的数据【菜品及其口味信息】
        DishDto dishDto = new DishDto();

        //1.根据id查询菜品信息封装到DishDto对象中
        Dish dish = this.getById(id);
        BeanUtils.copyProperties(dish,dishDto);

        //2.根据菜品id查询该菜品的口味信息 封装到DishDto对象中
        //2.1：设置查询条件
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId,id);
        //2.2：查询该菜品对应的口味信息
        List<DishFlavor> dishFlavors = dishFlavorService.list(wrapper);
        //2.3：封装菜品口味信息列表到DishDto对象中
        dishDto.setFlavors(dishFlavors);

        //3.返回DishDto对象
        return dishDto;
    }

    //修改菜品及其口味信息
    @Transactional
    @Override
    public boolean updateWithFlavor(DishDto dishDto) {
        //1.修改菜品基本信息
        boolean flag1 = this.updateById(dishDto);

        //2.修改菜品口味信息
        //获取当前菜品id
        Long dishId =  dishDto.getId();

        //2.1：删除当前菜品的原来的所有口味信息 【根据菜品id删除】
        //设置删除where条件
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId,dishId);
        dishFlavorService.remove(wrapper);

        //2.2：重新添加当前菜品的口味信息到菜品口味表
        //a:获取当前菜品的口味信息列表
        List<DishFlavor> flavors = dishDto.getFlavors();
        //b：设置口味信息对应的菜品id
        flavors = flavors.stream().map((flavor)->{
            //设置新增的口味信息id为null 查询时自动生成  防止和前面逻辑删除的口味信息id产生重复冲突
            flavor.setId(null);
            //设置口味信息对应的菜品id
            flavor.setDishId(dishId);
            return flavor;
        }).collect(Collectors.toList());
        //c：批量添加菜品口味信息到口味表中
        boolean flag2 = true;
        if(flavors!=null&&flavors.size()>0){
            flag2 = dishFlavorService.saveBatch(flavors);
        }

        //3.菜品基本信息和菜品口味信息同时修改成功  表示菜品修改成功
        return flag1&&flag2;
    }
}
