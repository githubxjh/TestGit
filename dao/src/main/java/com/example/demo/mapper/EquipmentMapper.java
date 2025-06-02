package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.Equipment;
import com.example.demo.model.Room;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface EquipmentMapper extends BaseMapper<Equipment> {
    // 所有基本的CRUD操作将由BaseMapper提供
    // 如果需要自定义方法，可以在这里添加
}