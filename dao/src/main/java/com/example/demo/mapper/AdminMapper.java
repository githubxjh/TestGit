package com.example.demo.mapper;

import com.example.demo.model.Admin;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Mapper  // 确保有 @Mapper 注解
@Repository
public interface AdminMapper {

    /**
     * 根据条件查询管理员列表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param adminName 管理员名称
     * @return 管理员列表
     */
    @Select("<script>" +
            "SELECT * FROM admin WHERE 1=1" +
            "<if test='startDate != null'> AND gmt_create &gt;= #{startDate}</if>" +
            "<if test='endDate != null'> AND gmt_create &lt;= #{endDate}</if>" +
            "<if test='adminName != null and adminName != \"\"'> AND admin_name LIKE CONCAT('%', #{adminName}, '%')</if>" +
            " ORDER BY admin_id DESC" +
            "</script>")
    List<Admin> findAdminList(@Param("startDate") Timestamp startDate,
                              @Param("endDate") Timestamp endDate,
                              @Param("adminName") String adminName);

    /**
     * 根据ID获取管理员信息
     *
     * @param id 管理员ID
     * @return 管理员对象
     */
    @Select("SELECT * FROM admin WHERE admin_id = #{id}")
    Admin getAdminById(@Param("id") Long id);

    /**
     * 插入新管理员
     *
     * @param admin 管理员对象
     * @return 影响行数
     */
    @Insert("INSERT INTO admin (admin_name, admin_phone, admin_mailbox, admin_level, gmt_create, admin_status) " +
            "VALUES (#{adminName}, #{adminPhone}, #{adminMailbox}, #{adminLevel}, #{gmtCreate}, #{adminStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "adminId")
    int insertAdmin(Admin admin);

    /**
     * 更新管理员信息
     *
     * @param admin 管理员对象
     * @return 影响行数
     */
    @Update("UPDATE admin SET admin_name = #{adminName}, admin_phone = #{adminPhone}, " +
            "admin_mailbox = #{adminMailbox}, admin_level = #{adminLevel} " +
            "WHERE admin_id = #{adminId}")
    int updateAdmin(Admin admin);

    /**
     * 删除管理员
     *
     * @param id 管理员ID
     * @return 影响行数
     */
    @Delete("DELETE FROM admin WHERE admin_id = #{id}")
    int deleteAdmin(@Param("id") Long id);

    /**
     * 更新管理员状态
     *
     * @param id 管理员ID
     * @param status 状态值（1:启用, 0:禁用）
     * @return 影响行数
     */
    @Update("UPDATE admin SET admin_status = #{status} WHERE admin_id = #{id}")
    int updateAdminStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新密码
     *
     * @param id 管理员ID
     * @param password 新密码（已加密）
     * @return 影响行数
     */
    @Update("UPDATE admin SET admin_password = #{password} WHERE admin_id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    /**
     * 根据用户名查询管理员
     *
     * @param adminName 用户名
     * @return 管理员对象
     */
    @Select("SELECT * FROM admin WHERE admin_name = #{adminName}")
    Admin findByAdminName(@Param("adminName") String adminName);
}