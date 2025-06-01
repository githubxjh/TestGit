package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.RoomEquipment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface RoomEquipmentMapper extends BaseMapper<RoomEquipment> {
    // 所有基本的CRUD操作将由BaseMapper提供
}