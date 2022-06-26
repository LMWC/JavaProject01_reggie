package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.bean.Category;
import com.itheima.bean.R;
import com.itheima.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/category")
@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //新增分类
    @PostMapping
    public R add(@RequestBody Category category){
        //1.调用Service完成新增
        boolean flag = categoryService.save(category);

        //2.返回处理结果
        return flag?R.success("新增分类成功！"):R.error("新增分类失败!");

    }

    //分类分页查询
    @GetMapping("/page")
    public R page(int page,int pageSize){
        //1.创建page对象 设置当前页码和每页显示条数
        Page<Category> categoryPage = new Page<Category>(page, pageSize);

        //2.设置查询条件【排序】
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);

        //3.调用service完成分页查询 返回分页数据
        categoryService.page(categoryPage,wrapper);

        return R.success(categoryPage);
    }

    //删除分类  普通实现
    /*@DeleteMapping
    public R delete(Long ids){

        //1.调用service完成根据分类id删除分类
        boolean flag = categoryService.removeById(ids);
        //2.响应处理结果
        if(flag){
            return R.success("分类删除成功！");
        }else{
            return R.error("分类删除失败！");
        }
    }*/

    //删除分类 Rest风格实现
    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id){
        log.info("分类删除-id：{}",id);
        //1.调用service完成根据分类id删除分类
        boolean flag = categoryService.removeById(id);
        //2.响应处理结果
        if(flag){
            return R.success("分类删除成功！");
        }else{
            return R.error("分类删除失败！");
        }
    }

    //修改分类
    @PutMapping
    public R update(@RequestBody Category category){
        //1.调用service完成分类修改
        boolean flag = categoryService.updateById(category);

        //2.响应处理结果
        if(flag){
            return R.success("分类修改成功！");
        }else{
            return R.error("分类修改失败！");
        }
    }

    //根据type查询分类列表
    @GetMapping("/list")
    public R list(Integer type){
        //1.设置查询条件 type  要查的是菜品分类 type=1 还是套餐分类 type=2 如果没有传递type，查询所有分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(type!=null,Category::getType,type);

        //2.调用Service完成分类列表查询
        List<Category> list = categoryService.list(wrapper);

        //3.响应处理结果
        return R.success(list);
    }
}
