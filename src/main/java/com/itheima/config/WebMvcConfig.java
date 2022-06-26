package com.itheima.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

//lombok使用 idea2020.3以下的版本需要安装插件并开启注解处理器，否则就会在使用lombok时出现红色波浪线提示
//这个注解是由lombok提供的  打上这个注解之间  可以直接使用log对象 进行日志记录
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    //配置静态资源映射
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {

        log.info("开启静态资源映射...");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    //扩展消息转换器
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");

        //1.创建消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        //2.设置消息转换器所使用的具体对象映射器
        converter.setObjectMapper(new JacksonObjectMapper());

        //3.设置索引，将我们自己的转换器 放在最前面，不要用默认的转换器
        converters.add(0,converter);
    }
}
