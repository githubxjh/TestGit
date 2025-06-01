package com.example.demo.config;


import com.example.demo.interceptor.RoomCacheInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RoomCacheInterceptor roomCacheInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/welcome").setViewName("welcome");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加缓存拦截器
        registry.addInterceptor(roomCacheInterceptor)
                .addPathPatterns("/room-brand", "/offlineroom/**", "/editroom/**")
                .excludePathPatterns("/static/**", "/lib/**");

        // 登录拦截器（注释掉的原始代码）
//        registry.addInterceptor(new LoginInterceptor())
//                .addPathPatterns("/**") // 拦截所有请求
//                .excludePathPatterns("/admin/login", "/admin/register") // 排除登录和注册请求
//                .excludePathPatterns("/static/**", "/lib/**", "/*.js", "/*.css", "/*.ico")
//                .excludePathPatterns("/admin/login", "/admin/doLogin", "/admin/register", "/admin/doRegister"); // 排除登录和注册相关路径
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加自定义资源处理
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/");
    }
}