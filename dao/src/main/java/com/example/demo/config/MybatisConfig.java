package com.example.demo.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * 注册自定义TypeHandler
 */
@Configuration
@MapperScan(basePackages = "com.example.demo.mapper")
public class MybatisConfig {

    /**
     * 注册自定义TypeHandler
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            // 注册Double[]数组的TypeHandler
            typeHandlerRegistry.register(Double[].class, DoubleArrayTypeHandler.class);
        };
    }
}