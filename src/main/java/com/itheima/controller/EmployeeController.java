package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.bean.Employee;
import com.itheima.bean.R;
import com.itheima.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@RequestMapping("/employee")
@RestController
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /*
         //1.接收到用户登录输入的用户名和密码，封装到了emp对象中 对密码进行MD5加密
        //2.根据用户名查询数据库，返回Employee对象
        //3.判断Employee对象是否为null  null：员工不存在
        //4.比对密码是否一致           不一致：员工账号和密码不匹配
        //5.查询员工状态是否禁用        禁用 0：员工已禁用
        //6.登录成功，将员工id存入Session，返回成功结果【员工对象】
     */
    //登录
    @PostMapping("/login")
    public R login(HttpSession session, @RequestBody Employee emp){

        //emp是用来封装前端用户登录时输入的用户名和密码  这里id为null
        log.info("【登录员工信息：{}】",emp);
        //原来：根据用户名查询用户是否存在  用户存在，再比对密码是否一致
        //现在：
        //1.接收到用户登录输入的用户名和密码，封装到了emp对象中 对密码进行MD5加密
        String password = DigestUtils.md5DigestAsHex(emp.getPassword().getBytes());

        //2.根据用户名查询数据库，返回Employee对象
        //2.1：创建LambdaQueryWrapper对象
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        //2.2：设置查询条件
        wrapper.eq(Employee::getUsername,emp.getUsername());
        //2.3：执行查询方法 传入wrapper对象
        Employee employee = employeeService.getOne(wrapper);        //employee 从数据库查询出来的，里面的信息完整

        //3.判断Employee对象是否为null  null：员工不存在
        if(employee==null){
            return R.error("员工不存在！");
        }

        //4.比对密码是否一致           不一致：员工账号和密码不匹配
        if(password==null || !password.equals(employee.getPassword())){
            return R.error("员工账号和密码不匹配！");
        }

        //5.查询员工状态是否禁用        禁用 0：员工已禁用
        if(employee.getStatus()==0){
            return R.error("员工已禁用！");
        }

        //6.登录成功，将员工id存入Session，返回成功结果【员工对象】
        session.setAttribute("employee",employee.getId());
        return R.success(employee);
    }

    //退出登录
    @PostMapping("/logout")
    public R logout(HttpSession  session){
        //1.清理session中用户信息
        //session.removeAttribute("employee");  //可以 不推荐  表示当前用户退出登录了 只清除了session中的一个数据
        session.invalidate();       //直接让当前这个用户关联的session对象立即失效  其中保存的数据也被删掉了
        //2.返回处理结果
        return R.success("退出成功！");
    }

    //新增员工
    @PostMapping
    public R add(HttpSession session, @RequestBody Employee emp){
        log.info("【EmployeeController add-线程id：{}】",Thread.currentThread().getId());

        //0.补全emp实体对象字段数据
        //password、status
        emp.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        emp.setStatus(1);

        //公共字段：使用MyBatisPlus自动填充
        /*
        //createTime、updateTime
        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateTime(LocalDateTime.now());

        //createUser、updateUser
        Long employeeId = (Long) session.getAttribute("employee");
        emp.setCreateUser(employeeId);
        emp.setUpdateUser(employeeId);
        */

        //1.调用service完成新增
        boolean flag = employeeService.save(emp);

        //2.返回处理结果
        return flag?R.success(true):R.error("新增员工失败！");
    }

    //分页查询
    @GetMapping("/page")
    public R page(Integer page,Integer pageSize,String name){
        //1.配置MyBatisPlus分页拦截器
        //2.接收前端传递的分页相关参数
        //3.通过page对象设置分页条件【当前页码 每页显示条数】
        Page<Employee> employeePage = new Page<>(page, pageSize);

        //4.通过LambdaQueryWrapper设置分页查询条件【根据name进行模糊查询】
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name!=null && name.length()>0,Employee::getName,name);

        //5.调用MyBatisPlus分页查询方法进行分页查询  得到分页相关数据
        employeeService.page(employeePage,wrapper);

        log.info("分页查询数据：{}",employeePage);

        //6.后台将分页查询数据响应给前端展示
        return R.success(employeePage);
    }

    //启用禁用员工状态|编辑员工信息
    @PutMapping
    public R update(HttpSession session, @RequestBody Employee emp){

        log.info("【EmployeeController update-线程id：{}】",Thread.currentThread().getId());

        //公共字段：使用MyBatisPlus自动填充
        /*
        //0.更新时 需要设置更新时间和更新人
        emp.setUpdateTime(LocalDateTime.now());
        emp.setUpdateUser((Long) session.getAttribute("employee"));
        */

        //1.调用service完成更新
        boolean flag = employeeService.updateById(emp);

        //2.返回处理结果
        if(flag){
            return R.success("员工信息修改成功！");
        }else{
            return R.error("员工信息修改失败！");
        }
    }

    //根据id查询员工信息
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        //1.调用Service处理 根据id查询员工信息
        Employee employee = employeeService.getById(id);

        //2.返回处理结果
        return employee!=null?R.success(employee):R.error("查询失败，请重试！");
    }

}
