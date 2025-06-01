package com.example.demo.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;

import com.example.demo.config.DoubleArrayTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户画像模型
 * 增加了正确的TypeHandler配置
 */
@Component
@TableName("user_profile")
public class UserProfile {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    private String preferredTags; // JSON格式存储标签权重
    private String priceRange; // 价格偏好区间
    private Integer starPreference; // 星级偏好
    private String locationPreference; // 地理位置偏好

    /**
     * 用户特征向量 - 使用自定义TypeHandler处理Double[]与数据库的转换
     */
    @TableField(value = "profile_vector",
            typeHandler = DoubleArrayTypeHandler.class,
            jdbcType = JdbcType.VARCHAR)
    private Double[] profileVector;

    private Timestamp lastUpdated;

    /**
     * 默认构造函数
     */
    public UserProfile() {
    }

    /**
     * 带参构造函数
     */
    public UserProfile(Long userId) {
        this.userId = userId;
        this.preferredTags = "{}";
        this.priceRange = "2,4";
        this.starPreference = 3;
        this.profileVector = getDefaultProfileVector();
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }

    /**
     * 解析偏好标签
     */
    public Map<String, Double> getPreferredTagsMap() {
        try {
            if (preferredTags == null || preferredTags.trim().isEmpty()) {
                return new HashMap<>();
            }
            return JSON.parseObject(preferredTags, new TypeReference<Map<String, Double>>(){});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 设置偏好标签
     */
    public void setPreferredTagsMap(Map<String, Double> tagsMap) {
        if (tagsMap == null || tagsMap.isEmpty()) {
            this.preferredTags = "{}";
        } else {
            this.preferredTags = JSON.toJSONString(tagsMap);
        }
    }

    /**
     * 获取默认的特征向量（10维零向量）
     */
    private Double[] getDefaultProfileVector() {
        Double[] defaultVector = new Double[10];
        for (int i = 0; i < 10; i++) {
            defaultVector[i] = 0.0;
        }
        return defaultVector;
    }

    /**
     * 安全获取特征向量（确保不为null）
     */
    public Double[] getProfileVectorSafe() {
        return profileVector != null ? profileVector : getDefaultProfileVector();
    }

    /**
     * 更新特征向量中的某个维度
     */
    public void updateVectorDimension(int index, Double value) {
        if (profileVector == null) {
            profileVector = getDefaultProfileVector();
        }
        if (index >= 0 && index < profileVector.length && value != null) {
            profileVector[index] = value;
        }
    }

    /**
     * 获取特征向量的维度
     */
    public int getVectorDimension() {
        return profileVector != null ? profileVector.length : 10;
    }

    // ==================== Getter和Setter方法 ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPreferredTags() {
        return preferredTags;
    }

    public void setPreferredTags(String preferredTags) {
        this.preferredTags = preferredTags;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public Integer getStarPreference() {
        return starPreference;
    }

    public void setStarPreference(Integer starPreference) {
        this.starPreference = starPreference;
    }

    public String getLocationPreference() {
        return locationPreference;
    }

    public void setLocationPreference(String locationPreference) {
        this.locationPreference = locationPreference;
    }

    public Double[] getProfileVector() {
        return profileVector;
    }

    public void setProfileVector(Double[] profileVector) {
        this.profileVector = profileVector;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断用户画像是否已初始化
     */
    public boolean isInitialized() {
        return profileVector != null && profileVector.length > 0;
    }

    /**
     * 重置为默认画像
     */
    public void resetToDefault() {
        this.preferredTags = "{}";
        this.priceRange = "2,4";
        this.starPreference = 3;
        this.profileVector = getDefaultProfileVector();
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }

    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "userId=" + userId +
                ", priceRange='" + priceRange + '\'' +
                ", starPreference=" + starPreference +
                ", locationPreference='" + locationPreference + '\'' +
                ", vectorDimension=" + getVectorDimension() +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProfile that = (UserProfile) o;

        return userId != null ? userId.equals(that.userId) : that.userId == null;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}