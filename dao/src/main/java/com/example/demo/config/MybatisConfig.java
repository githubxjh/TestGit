package com.example.demo.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * 简化配置，避免与MyBatis-Plus自动配置冲突
 */
@Configuration
@MapperScan(basePackages = "com.example.demo.mapper")
public class MybatisConfig {
    // 移除自定义SqlSessionFactory配置，让MyBatis-Plus自动配置处理
    // 这样可以避免配置冲突导致的Mapper重复扫描问题
}