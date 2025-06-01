package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.EquipmentCategory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface EquipmentCategoryMapper extends BaseMapper<EquipmentCategory> {
    // 基本的CRUD操作由BaseMapper提供
}
