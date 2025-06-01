package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.Order;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrderMapper extends BaseMapper<Order> {

    // 废弃原错误写法
    // @Select("SELECT ... FROM sspu.order WHERE car_id =#{car_id}")
    // Order getOrderByCarId(Integer car_id);
}

