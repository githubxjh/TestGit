package com.example.demo.service;

// ==================== 特征向量初始化服务 ====================

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.config.SchedulingProperties;
import com.example.demo.mapper.RoomFeaturesMapper;
import com.example.demo.mapper.RoomMapper;
import com.example.demo.mapper.UserProfileMapper;
import com.example.demo.model.Room;
import com.example.demo.model.RoomFeatures;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeatureInitializationService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomFeaturesMapper roomFeaturesMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private SchedulingProperties schedulingProperties;

    private static final Logger logger = LoggerFactory.getLogger(FeatureInitializationService.class);

    /**
     * 初始化所有房间的特征向量
     * 现在受配置控制，只有在配置允许时才执行
     */
    @PostConstruct
    public void initializeRoomFeatures() {
        // 检查配置是否允许执行初始化
        if (!schedulingProperties.shouldExecuteRoomFeaturesInit()) {
            logger.info("房间特征向量初始化已禁用。当前状态: {}",
                    schedulingProperties.getStatusDescription());
            return;
        }

        logger.info("开始初始化房间特征向量...");

        try {
            List<Room> allRooms = roomMapper.selectList(null);
            int processed = 0;

            for (Room room : allRooms) {
                RoomFeatures features = generateRoomFeatures(room);

                // 检查是否已存在
                RoomFeatures existing = roomFeaturesMapper.selectById(room.getRoomId());
                if (existing != null) {
                    features.setRoomId(room.getRoomId());
                    roomFeaturesMapper.updateById(features);
                } else {
                    features.setRoomId(room.getRoomId());
                    roomFeaturesMapper.insert(features);
                }

                processed++;
                if (processed % 100 == 0) {
                    logger.info("已处理 {} 个房间特征", processed);
                }
            }

            logger.info("房间特征向量初始化完成，共处理 {} 个房间", processed);

        } catch (Exception e) {
            logger.error("初始化房间特征向量失败", e);
        }
    }

    /**
     * 生成单个房间的特征向量
     */
    public RoomFeatures generateRoomFeatures(Room room) {
        RoomFeatures features = new RoomFeatures();

        // 1. 生成基础标签
        List<String> tags = generateRoomTags(room);
        features.setTags(JSON.toJSONString(tags));

        // 2. 计算价格等级
        features.setPriceLevel(calculatePriceLevel(room.getRoomPrice()));

        // 3. 设置星级
        features.setStarLevel(parseStarRating(room.getStarRating()));

        // 4. 生成特征向量 (10维向量)
        Double[] featureVector = generateFeatureVector(room, tags);
        features.setFeatureVector(JSON.toJSONString(featureVector));

        // 5. 设置地理位置 (这里使用模拟数据，实际应用中应该从真实的地理信息获取)
        features.setLongitude(generateMockLongitude());
        features.setLatitude(generateMockLatitude());

        return features;
    }

    /**
     * 生成房间标签
     */
    private List<String> generateRoomTags(Room room) {
        List<String> tags = new ArrayList<>();

        // 基于房间名称生成标签（增加空值检查）
        if (room.getRoomName() != null) {
            String roomName = room.getRoomName().toLowerCase();
            if (roomName.contains("豪华")) tags.add("豪华");
            if (roomName.contains("标准")) tags.add("标准");
            if (roomName.contains("套房")) tags.add("套房");
            if (roomName.contains("商务")) tags.add("商务");
            if (roomName.contains("亲子")) tags.add("亲子");
            if (roomName.contains("海景")) tags.add("海景");
            if (roomName.contains("山景")) tags.add("山景");
            if (roomName.contains("电竞")) tags.add("电竞");
        }

        // 基于房间描述生成标签
        if (room.getRoomContent() != null && !room.getRoomContent().trim().isEmpty()) {
            String content = room.getRoomContent().toLowerCase();
            if (content.contains("wifi") || content.contains("无线网络")) tags.add("wifi");
            if (content.contains("早餐")) tags.add("早餐");
            if (content.contains("停车")) tags.add("停车");
            if (content.contains("健身")) tags.add("健身");
            if (content.contains("游泳")) tags.add("游泳池");
            if (content.contains("空调")) tags.add("空调");
            if (content.contains("浴缸")) tags.add("浴缸");
        }

        // 基于设备信息生成标签
        if (room.getEquipmentNames() != null && !room.getEquipmentNames().trim().isEmpty()) {
            String[] equipments = room.getEquipmentNames().split(",");
            for (String equipment : equipments) {
                String trimmedEquipment = equipment.trim();
                if (!trimmedEquipment.isEmpty()) {
                    tags.add(trimmedEquipment);
                }
            }
        }

        // 基于价格生成标签
        if (room.getRoomPrice() != null) {
            if (room.getRoomPrice() < 200) tags.add("经济型");
            else if (room.getRoomPrice() < 500) tags.add("中档");
            else tags.add("高档");
        }

        // 基于房间类型生成标签
        if (room.getCategory() != null) {
            tags.add(room.getCategory() == 1 ? "普通房间" : "特殊房间");
        }

        return tags.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 生成10维特征向量
     * 每一维代表不同的特征：
     * [0] 价格归一化值 (0-1)
     * [1] 星级归一化值 (0-1)
     * [2] 豪华程度 (0-1)
     * [3] 商务友好度 (0-1)
     * [4] 家庭友好度 (0-1)
     * [5] 设施丰富度 (0-1)
     * [6] 位置便利度 (0-1)
     * [7] 房间大小 (0-1)
     * [8] 现代化程度 (0-1)
     * [9] 特色程度 (0-1)
     */
    private Double[] generateFeatureVector(Room room, List<String> tags) {
        Double[] vector = new Double[10];

        // [0] 价格归一化 (假设最高价格2000) - 修复空值问题
        vector[0] = (room.getRoomPrice() != null) ? Math.min(room.getRoomPrice() / 2000.0, 1.0) : 0.5;

        // [1] 星级归一化
        int starLevel = parseStarRating(room.getStarRating());
        vector[1] = starLevel / 5.0;

        // [2] 豪华程度
        vector[2] = calculateLuxuryScore(room, tags);

        // [3] 商务友好度
        vector[3] = calculateBusinessScore(room, tags);

        // [4] 家庭友好度
        vector[4] = calculateFamilyScore(room, tags);

        // [5] 设施丰富度
        vector[5] = calculateAmenityScore(room, tags);

        // [6] 位置便利度 (模拟值)
        vector[6] = 0.5 + Math.random() * 0.5;

        // [7] 房间大小 (基于价格和类型推断)
        vector[7] = calculateRoomSizeScore(room, tags);

        // [8] 现代化程度
        vector[8] = calculateModernityScore(room, tags);

        // [9] 特色程度
        vector[9] = calculateUniquenessScore(room, tags);

        return vector;
    }

    private double calculateLuxuryScore(Room room, List<String> tags) {
        double score = 0.0;

        if (tags.contains("豪华")) score += 0.4;
        if (tags.contains("套房")) score += 0.3;
        if (tags.contains("高档")) score += 0.2;
        if (tags.contains("海景")) score += 0.1;

        // 基于价格调整 - 修复空值问题
        if (room.getRoomPrice() != null && room.getRoomPrice() > 800) {
            score += 0.2;
        }

        return Math.min(score, 1.0);
    }

    private double calculateBusinessScore(Room room, List<String> tags) {
        double score = 0.0;

        if (tags.contains("商务")) score += 0.5;
        if (tags.contains("wifi")) score += 0.2;
        if (tags.contains("会议室")) score += 0.3;

        return Math.min(score, 1.0);
    }

    private double calculateFamilyScore(Room room, List<String> tags) {
        double score = 0.0;

        if (tags.contains("亲子")) score += 0.5;
        if (tags.contains("家庭")) score += 0.4;
        if (tags.contains("游泳池")) score += 0.1;

        return Math.min(score, 1.0);
    }

    private double calculateAmenityScore(Room room, List<String> tags) {
        // 设施数量越多分数越高
        return Math.min(tags.size() / 10.0, 1.0);
    }

    private double calculateRoomSizeScore(Room room, List<String> tags) {
        double score = 0.5; // 默认中等大小

        if (tags.contains("套房")) score += 0.3;
        if (tags.contains("豪华")) score += 0.2;

        // 基于价格推断 - 修复空值问题
        if (room.getRoomPrice() != null) {
            if (room.getRoomPrice() > 600) score += 0.2;
            else if (room.getRoomPrice() < 300) score -= 0.2;
        }

        return Math.max(0.0, Math.min(score, 1.0));
    }

    private double calculateModernityScore(Room room, List<String> tags) {
        double score = 0.5; // 默认值

        if (tags.contains("wifi")) score += 0.1;
        if (tags.contains("智能")) score += 0.2;
        if (tags.contains("现代")) score += 0.2;
        if (tags.contains("电竞")) score += 0.3;

        return Math.min(score, 1.0);
    }

    private double calculateUniquenessScore(Room room, List<String> tags) {
        double score = 0.0;

        if (tags.contains("电竞")) score += 0.4;
        if (tags.contains("主题")) score += 0.3;
        if (tags.contains("特色")) score += 0.2;
        // 修复空值问题
        if (room.getCategory() != null && room.getCategory() == 0) score += 0.1; // 特殊房间

        return Math.min(score, 1.0);
    }

    private int calculatePriceLevel(Double price) {
        if (price == null) return 3;
        if (price < 200) return 1;
        if (price < 400) return 2;
        if (price < 600) return 3;
        if (price < 800) return 4;
        return 5;
    }

    private int parseStarRating(String starRating) {
        if (starRating == null || starRating.trim().isEmpty()) return 3;

        try {
            // 提取数字部分
            String numberPart = starRating.replaceAll("[^0-9]", "");
            if (!numberPart.isEmpty()) {
                int rating = Integer.parseInt(numberPart);
                // 确保星级在1-5范围内
                return Math.max(1, Math.min(5, rating));
            }
        } catch (NumberFormatException e) {
            logger.warn("解析星级失败: {}", starRating, e);
        }

        return 3; // 默认3星
    }

    private double generateMockLongitude() {
        // 模拟深圳地区的经度范围: 113.8-114.8
        return 113.8 + Math.random() * 1.0;
    }

    private double generateMockLatitude() {
        // 模拟深圳地区的纬度范围: 22.4-22.8
        return 22.4 + Math.random() * 0.4;
    }

    /**
     * 批量更新房间特征向量
     * 现在使用配置的cron表达式，并受开关控制
     */
    @Scheduled(cron = "#{@schedulingProperties.cron.roomFeaturesUpdate}") // 动态cron表达式
    public void scheduledUpdateRoomFeatures() {
        // 检查定时任务是否启用
        if (!schedulingProperties.shouldExecuteRoomFeaturesUpdate()) {
            logger.debug("房间特征更新定时任务已禁用。当前状态: {}",
                    schedulingProperties.getStatusDescription());
            return;
        }

        logger.info("开始定时更新房间特征向量...");

        try {
            // 获取最近24小时内修改的房间
            Timestamp yesterday = new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000);

            QueryWrapper<Room> queryWrapper = new QueryWrapper<>();
            queryWrapper.ge("gmt_modify", yesterday);
            List<Room> modifiedRooms = roomMapper.selectList(queryWrapper);

            int processedCount = 0;
            int errorCount = 0;

            for (Room room : modifiedRooms) {
                try {
                    RoomFeatures features = generateRoomFeatures(room);
                    features.setRoomId(room.getRoomId());

                    RoomFeatures existing = roomFeaturesMapper.selectById(room.getRoomId());
                    if (existing != null) {
                        roomFeaturesMapper.updateById(features);
                    } else {
                        roomFeaturesMapper.insert(features);
                    }
                    processedCount++;
                } catch (Exception e) {
                    errorCount++;
                    logger.error("更新房间 {} 特征失败", room.getRoomId(), e);
                }
            }

            logger.info("定时更新完成，处理了 {} 个房间，成功: {}, 失败: {}",
                    modifiedRooms.size(), processedCount, errorCount);

        } catch (Exception e) {
            logger.error("定时更新房间特征向量失败", e);
        }
    }

    /**
     * 手动触发特征向量更新（用于调试和维护）
     */
    public void manualUpdateRoomFeatures() {
        logger.info("手动触发房间特征向量更新...");
        initializeRoomFeatures();
    }

    /**
     * 更新单个房间的特征向量
     */
    public boolean updateSingleRoomFeatures(String roomId) {
        try {
            Room room = roomMapper.selectById(roomId);
            if (room == null) {
                logger.warn("房间不存在: {}", roomId);
                return false;
            }

            RoomFeatures features = generateRoomFeatures(room);
            features.setRoomId(room.getRoomId());

            RoomFeatures existing = roomFeaturesMapper.selectById(room.getRoomId());
            if (existing != null) {
                roomFeaturesMapper.updateById(features);
            } else {
                roomFeaturesMapper.insert(features);
            }

            logger.info("房间 {} 特征向量更新成功", roomId);
            return true;

        } catch (Exception e) {
            logger.error("更新房间 {} 特征向量失败", roomId, e);
            return false;
        }
    }

    /**
     * 获取特征向量初始化统计信息
     */
    public java.util.Map<String, Object> getInitializationStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        try {
            long totalRooms = roomMapper.selectCount(null);
            long featuresCount = roomFeaturesMapper.selectCount(null);

            stats.put("totalRooms", totalRooms);
            stats.put("featuresCount", featuresCount);
            stats.put("initializationRate", totalRooms > 0 ? (double) featuresCount / totalRooms : 0.0);
            stats.put("missingFeatures", totalRooms - featuresCount);

            // 添加配置状态信息
            stats.put("schedulingStatus", schedulingProperties.getStatusDescription());
            stats.put("schedulingConfig", schedulingProperties.toString());

        } catch (Exception e) {
            logger.error("获取初始化统计信息失败", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * 动态更新定时任务配置（用于运行时控制）
     */
    public void updateSchedulingConfig(boolean enabled, boolean roomFeaturesInit,
                                       boolean roomFeaturesUpdate, boolean userProfileUpdate) {
        schedulingProperties.setEnabled(enabled);
        schedulingProperties.setRoomFeaturesInit(roomFeaturesInit);
        schedulingProperties.setRoomFeaturesUpdate(roomFeaturesUpdate);
        schedulingProperties.setUserProfileUpdate(userProfileUpdate);

        logger.info("定时任务配置已更新: {}", schedulingProperties.getStatusDescription());
    }
}