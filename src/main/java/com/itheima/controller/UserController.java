package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.bean.R;
import com.itheima.bean.User;
import com.itheima.service.UserService;
import com.itheima.utils.SMSUtils;
import com.itheima.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /*
        可以使用map集合对象接收json格式参数 json对象的key作为map的key json对象的值就作为map的value
     */
    //1.发送手机短信
    @PostMapping("/sendMsg")
    public R sendMsg(HttpSession session, @RequestBody Map<String,Object> map){
        //1.获取手机号
        String phone = (String) map.get("phone");
        log.info("手机号：{}",phone);

        //2.生成验证码
        Integer validateCode = ValidateCodeUtils.generateValidateCode(4);

        log.info("验证码：{}",validateCode);

        //3.使用工具类SMSUtils发送短信
        //SMSUtils.sendMessage("瑞吉外卖","瑞吉外卖",phone,validateCode.toString());

        //4.将验证码存入到session中 方便在登录时和用户输入的验证码进行比对
        session.setAttribute("validateCode",validateCode);

        //5.响应处理结果
        //return R.success("验证码发送成功！");

        //获取验证码成功 将验证码响应给前端 避免频繁输入
        return R.success(validateCode);

    }

    //2.手机验证码登录
    @PostMapping("/login")
    public R login(HttpSession session,@RequestBody Map<String,Object> map){
        //1.获取用户输入的手机号和验证码
        String phone = map.get("phone").toString();
        //强转类型转换 1.数据类型要兼容【int long】  2.具有父子类关系
        //将一个数字存入到Map集合  int-->Integer-->Object -->
        String code = map.get("code").toString();

        //2.获取session中保存的验证码 和用户输入的验证码做比对
        Object validateCode = session.getAttribute("validateCode");

        if(validateCode!=null && validateCode.toString().equals(code)){
            //3.比对一致：登录成功  a：判断表中是否存在对应手机号的用户信息 如果没有 就存入进去 b：将登录成功的用户id存入session
            // a：判断表中是否存在对应手机号的用户信息 如果没有 就存入进去
            // 3.1:设置查询条件
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
            wrapper.eq(User::getPhone,phone);

            // 3.2：根据条件查询返回用户User对象
            User user = userService.getOne(wrapper);

            // 3.3：判断user==null，就将该手机号存入用户表中
            User userInfo = null;
            if(user==null){
                //当用户手机号保存到用户表的同时  会为该User对象生成并设置id
                userInfo = new User();
                userInfo.setPhone(phone);
                userService.save(userInfo);
            }


            // b：将登录成功的用户id存入session
            Long userId = user==null?userInfo.getId():user.getId();

            session.setAttribute("user",userId);

            return R.success("登录成功！");
        }else{
            //4.比对不一致：登录失败  验证码输入有误
            return R.error("验证码输入有误");
        }
    }
}
