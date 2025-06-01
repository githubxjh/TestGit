package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.UserProfile;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 用户画像数据访问层
 * 负责用户偏好、特征向量等数据的CRUD操作
 */
@Mapper
@Repository
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据用户ID查询用户画像（带缓存优化）
     */
    @Select("SELECT * FROM user_profile WHERE user_id = #{userId}")
    UserProfile selectByUserId(@Param("userId") Long userId);

    /**
     * 批量查询用户画像
     */
    @Select("<script>" +
            "SELECT * FROM user_profile WHERE user_id IN " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "</script>")
    List<UserProfile> selectByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 查询活跃用户画像（最近30天有更新的用户）
     */
    @Select("SELECT * FROM user_profile WHERE last_updated >= DATE_SUB(NOW(), INTERVAL 30 DAY) ORDER BY last_updated DESC")
    List<UserProfile> selectActiveProfiles();

    /**
     * 查询新用户（无画像数据的用户）
     */
    @Select("SELECT u.user_id FROM users u LEFT JOIN user_profile up ON u.user_id = up.user_id WHERE up.user_id IS NULL")
    List<Long> selectNewUsers();

    // ==================== 用户偏好相关方法 ====================

    /**
     * 更新用户标签偏好
     */
    @Update("UPDATE user_profile SET preferred_tags = #{preferredTags}, last_updated = NOW() WHERE user_id = #{userId}")
    int updatePreferredTags(@Param("userId") Long userId, @Param("preferredTags") String preferredTags);

    /**
     * 更新用户价格偏好
     */
    @Update("UPDATE user_profile SET price_range = #{priceRange}, last_updated = NOW() WHERE user_id = #{userId}")
    int updatePriceRange(@Param("userId") Long userId, @Param("priceRange") String priceRange);

    /**
     * 更新用户星级偏好
     */
    @Update("UPDATE user_profile SET star_preference = #{starPreference}, last_updated = NOW() WHERE user_id = #{userId}")
    int updateStarPreference(@Param("userId") Long userId, @Param("starPreference") Integer starPreference);

    /**
     * 更新用户特征向量
     */
    @Update("UPDATE user_profile SET profile_vector = #{profileVector}, last_updated = NOW() WHERE user_id = #{userId}")
    int updateProfileVector(@Param("userId") Long userId, @Param("profileVector") String profileVector);

    /**
     * 批量更新用户画像
     */
    @Update("<script>" +
            "<foreach collection='profiles' item='profile' separator=';'>" +
            "UPDATE user_profile SET " +
            "preferred_tags = #{profile.preferredTags}, " +
            "price_range = #{profile.priceRange}, " +
            "star_preference = #{profile.starPreference}, " +
            "profile_vector = #{profile.profileVector}, " +
            "last_updated = NOW() " +
            "WHERE user_id = #{profile.userId}" +
            "</foreach>" +
            "</script>")
    int batchUpdateProfiles(@Param("profiles") List<UserProfile> profiles);

    // ==================== 统计分析方法 ====================

    /**
     * 统计用户偏好标签分布
     */
    @Select("SELECT " +
            "JSON_UNQUOTE(JSON_EXTRACT(preferred_tags, CONCAT('$.', tags.tag_name))) as tag_weight, " +
            "tags.tag_name, " +
            "COUNT(*) as user_count " +
            "FROM user_profile, " +
            "JSON_TABLE(preferred_tags, '$.*' COLUMNS (tag_name VARCHAR(100) PATH '$')) as tags " +
            "WHERE JSON_UNQUOTE(JSON_EXTRACT(preferred_tags, CONCAT('$.', tags.tag_name))) > 0 " +
            "GROUP BY tags.tag_name " +
            "ORDER BY user_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectPopularTags(@Param("limit") int limit);

    /**
     * 统计价格偏好分布
     */
    @Select("SELECT price_range, COUNT(*) as count FROM user_profile " +
            "WHERE price_range IS NOT NULL GROUP BY price_range ORDER BY count DESC")
    List<Map<String, Object>> selectPriceRangeDistribution();

    /**
     * 统计星级偏好分布
     */
    @Select("SELECT star_preference, COUNT(*) as count FROM user_profile " +
            "WHERE star_preference IS NOT NULL GROUP BY star_preference ORDER BY star_preference")
    List<Map<String, Object>> selectStarPreferenceDistribution();

    /**
     * 查询相似用户（基于特征向量相似度）
     * 注意：这是一个简化的相似度查询，实际生产中建议使用专门的向量数据库
     */
    @Select("SELECT user_id, preferred_tags, profile_vector FROM user_profile " +
            "WHERE user_id != #{userId} AND profile_vector IS NOT NULL " +
            "ORDER BY (CASE WHEN profile_vector IS NOT NULL THEN 1 ELSE 0 END) DESC " +
            "LIMIT #{limit}")
    List<UserProfile> selectSimilarUsers(@Param("userId") Long userId, @Param("limit") int limit);

    // ==================== 数据维护方法 ====================

    /**
     * 清理过期的用户画像数据（超过1年未更新）
     */
    @Delete("DELETE FROM user_profile WHERE last_updated < DATE_SUB(NOW(), INTERVAL 1 YEAR)")
    int cleanExpiredProfiles();

    /**
     * 重置用户画像数据
     */
    @Update("UPDATE user_profile SET " +
            "preferred_tags = '{}', " +
            "price_range = '2,4', " +
            "star_preference = 3, " +
            "profile_vector = '[0,0,0,0,0,0,0,0,0,0]', " +
            "last_updated = NOW() " +
            "WHERE user_id = #{userId}")
    int resetUserProfile(@Param("userId") Long userId);

    /**
     * 检查用户画像是否存在
     */
    @Select("SELECT COUNT(*) FROM user_profile WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 获取用户画像创建时间
     */
    @Select("SELECT last_updated FROM user_profile WHERE user_id = #{userId}")
    Timestamp selectLastUpdated(@Param("userId") Long userId);

    // ==================== 复合查询方法 ====================

    /**
     * 根据标签偏好查找相似用户
     */
    @Select("<script>" +
            "SELECT * FROM user_profile WHERE user_id != #{userId} " +
            "<if test='tags != null and tags.size() > 0'>" +
            "AND (" +
            "<foreach collection='tags' item='tag' separator=' OR '>" +
            "JSON_EXTRACT(preferred_tags, CONCAT('$.', #{tag})) > #{threshold}" +
            "</foreach>" +
            ")" +
            "</if>" +
            "ORDER BY last_updated DESC LIMIT #{limit}" +
            "</script>")
    List<UserProfile> selectUsersByTags(@Param("userId") Long userId,
                                        @Param("tags") List<String> tags,
                                        @Param("threshold") double threshold,
                                        @Param("limit") int limit);

    /**
     * 根据价格和星级偏好查找用户
     */
    @Select("SELECT * FROM user_profile WHERE " +
            "price_range = #{priceRange} AND star_preference = #{starPreference} " +
            "ORDER BY last_updated DESC LIMIT #{limit}")
    List<UserProfile> selectUsersByPriceAndStar(@Param("priceRange") String priceRange,
                                                @Param("starPreference") Integer starPreference,
                                                @Param("limit") int limit);

    // ==================== 性能优化相关方法 ====================

    /**
     * 分页查询用户画像
     */
    @Select("SELECT * FROM user_profile " +
            "ORDER BY last_updated DESC " +
            "LIMIT #{offset}, #{size}")
    List<UserProfile> selectProfilesWithPaging(@Param("offset") int offset, @Param("size") int size);

    /**
     * 统计用户画像总数
     */
    @Select("SELECT COUNT(*) FROM user_profile")
    long countAllProfiles();

    /**
     * 统计最近N天活跃用户数
     */
    @Select("SELECT COUNT(*) FROM user_profile WHERE last_updated >= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    long countActiveUsers(@Param("days") int days);

    // ==================== 自定义SQL方法 ====================

    /**
     * 创建用户画像时的插入或更新操作
     */
    @Insert("INSERT INTO user_profile (user_id, preferred_tags, price_range, star_preference, profile_vector, last_updated) " +
            "VALUES (#{userId}, #{preferredTags}, #{priceRange}, #{starPreference}, #{profileVector}, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "preferred_tags = VALUES(preferred_tags), " +
            "price_range = VALUES(price_range), " +
            "star_preference = VALUES(star_preference), " +
            "profile_vector = VALUES(profile_vector), " +
            "last_updated = NOW()")
    int insertOrUpdateProfile(UserProfile profile);

    /**
     * 根据用户行为增量更新标签权重
     */
    @Update("UPDATE user_profile SET " +
            "preferred_tags = JSON_SET(COALESCE(preferred_tags, '{}'), CONCAT('$.', #{tag}), " +
            "COALESCE(JSON_EXTRACT(preferred_tags, CONCAT('$.', #{tag})), 0) + #{increment}), " +
            "last_updated = NOW() " +
            "WHERE user_id = #{userId}")
    int incrementTagWeight(@Param("userId") Long userId,
                           @Param("tag") String tag,
                           @Param("increment") double increment);

    /**
     * 批量增量更新标签权重
     */
    @Update("<script>" +
            "<foreach collection='updates' item='update' separator=';'>" +
            "UPDATE user_profile SET " +
            "preferred_tags = JSON_SET(COALESCE(preferred_tags, '{}'), CONCAT('$.', #{update.tag}), " +
            "COALESCE(JSON_EXTRACT(preferred_tags, CONCAT('$.', #{update.tag})), 0) + #{update.increment}), " +
            "last_updated = NOW() " +
            "WHERE user_id = #{update.userId}" +
            "</foreach>" +
            "</script>")
    int batchIncrementTagWeights(@Param("updates") List<TagWeightUpdate> updates);

    // ==================== 内部类：标签权重更新DTO ====================

    class TagWeightUpdate {
        private Long userId;
        private String tag;
        private double increment;

        public TagWeightUpdate(Long userId, String tag, double increment) {
            this.userId = userId;
            this.tag = tag;
            this.increment = increment;
        }

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }

        public double getIncrement() { return increment; }
        public void setIncrement(double increment) { this.increment = increment; }
    }
}