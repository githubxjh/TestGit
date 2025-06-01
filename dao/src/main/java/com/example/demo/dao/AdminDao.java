package com.example.demo.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 管理员数据访问层，使用 MyBatis-Plus 进行增强
 */
@Mapper  // 确保添加 @Mapper 注解
@Repository
public interface AdminDao extends BaseMapper<Admin> {

    /**
     * 通过自定义查询条件查询管理员列表
     *
     * @param queryWrapper 查询条件包装器
     * @return 管理员列表
     */
    default List<Admin> selectWithCustomCondition(QueryWrapper<Admin> queryWrapper) {
        return this.selectList(queryWrapper);
    }

    /**
     * 通过管理员级别查询管理员列表
     *
     * @param adminLevel 管理员级别
     * @return 管理员列表
     */
    default List<Admin> selectByAdminLevel(Long adminLevel) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("admin_level", adminLevel);
        return this.selectList(queryWrapper);
    }

    /**
     * 通过状态查询管理员列表
     *
     * @param status 状态值（1:启用, 0:禁用）
     * @return 管理员列表
     */
    default List<Admin> selectByStatus(Long status) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("admin_status", status);
        return this.selectList(queryWrapper);
    }
}