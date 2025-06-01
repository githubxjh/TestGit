package org.example.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.demo.mapper") // 修复：只扫描Mapper所在的具体包
@ComponentScan({"org.example.web", "com.example.demo"}) // 保持原有的组件扫描
@EnableCaching // 修复：添加缺失的注解
@EnableScheduling // 修复：将这个注解单独一行
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}