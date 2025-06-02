package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 定时任务配置属性类
 * 用于控制各种定时任务的开关
 */
@Configuration
@ConfigurationProperties(prefix = "app.scheduling")
public class SchedulingProperties {

    /**
     * 定时任务总开关
     */
    private boolean enabled = false;

    /**
     * 房间特征初始化开关
     */
    private boolean roomFeaturesInit = false;

    /**
     * 房间特征更新开关
     */
    private boolean roomFeaturesUpdate = false;

    /**
     * 用户画像更新开关
     */
    private boolean userProfileUpdate = false;

    /**
     * 定时任务执行时间配置
     */
    private Cron cron = new Cron();

    // ==================== Getter和Setter方法 ====================

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRoomFeaturesInit() {
        return roomFeaturesInit;
    }

    public void setRoomFeaturesInit(boolean roomFeaturesInit) {
        this.roomFeaturesInit = roomFeaturesInit;
    }

    public boolean isRoomFeaturesUpdate() {
        return roomFeaturesUpdate;
    }

    public void setRoomFeaturesUpdate(boolean roomFeaturesUpdate) {
        this.roomFeaturesUpdate = roomFeaturesUpdate;
    }

    public boolean isUserProfileUpdate() {
        return userProfileUpdate;
    }

    public void setUserProfileUpdate(boolean userProfileUpdate) {
        this.userProfileUpdate = userProfileUpdate;
    }

    public Cron getCron() {
        return cron;
    }

    public void setCron(Cron cron) {
        this.cron = cron;
    }

    // ==================== 便利方法 ====================

    /**
     * 检查是否应该执行房间特征初始化
     */
    public boolean shouldExecuteRoomFeaturesInit() {
        return enabled && roomFeaturesInit;
    }

    /**
     * 检查是否应该执行房间特征更新
     */
    public boolean shouldExecuteRoomFeaturesUpdate() {
        return enabled && roomFeaturesUpdate;
    }

    /**
     * 检查是否应该执行用户画像更新
     */
    public boolean shouldExecuteUserProfileUpdate() {
        return enabled && userProfileUpdate;
    }

    /**
     * 获取当前配置状态的描述
     */
    public String getStatusDescription() {
        if (!enabled) {
            return "定时任务已全局禁用";
        }

        StringBuilder sb = new StringBuilder("定时任务状态: ");
        sb.append("房间特征初始化=").append(roomFeaturesInit ? "启用" : "禁用");
        sb.append(", 房间特征更新=").append(roomFeaturesUpdate ? "启用" : "禁用");
        sb.append(", 用户画像更新=").append(userProfileUpdate ? "启用" : "禁用");

        return sb.toString();
    }

    // ==================== 内部类：定时任务时间配置 ====================

    public static class Cron {
        /**
         * 房间特征更新的cron表达式
         */
        private String roomFeaturesUpdate = "0 0 2 * * ?"; // 默认凌晨2点

        /**
         * 用户画像更新的cron表达式
         */
        private String userProfileUpdate = "0 0 3 * * ?"; // 默认凌晨3点

        public String getRoomFeaturesUpdate() {
            return roomFeaturesUpdate;
        }

        public void setRoomFeaturesUpdate(String roomFeaturesUpdate) {
            this.roomFeaturesUpdate = roomFeaturesUpdate;
        }

        public String getUserProfileUpdate() {
            return userProfileUpdate;
        }

        public void setUserProfileUpdate(String userProfileUpdate) {
            this.userProfileUpdate = userProfileUpdate;
        }
    }

    @Override
    public String toString() {
        return "SchedulingProperties{" +
                "enabled=" + enabled +
                ", roomFeaturesInit=" + roomFeaturesInit +
                ", roomFeaturesUpdate=" + roomFeaturesUpdate +
                ", userProfileUpdate=" + userProfileUpdate +
                ", cron=" + cron +
                '}';
    }
}
