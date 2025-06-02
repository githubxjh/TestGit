package com.example.demo.service;// ==================== 用户行为服务 ====================

import com.alibaba.fastjson.JSON;
import com.example.demo.dto.UserBehaviorDTO;
import com.example.demo.mapper.UserBehaviorMapper;
import com.example.demo.mapper.UserProfileMapper;
import com.example.demo.model.Room;
import com.example.demo.model.UserBehavior;
import com.example.demo.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class UserBehaviorService {

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

//    @Autowired
//    private RoomRecommendationEngineService recommendationEngineService;
    @Autowired
    private RoomService roomService;
    public void recordBehavior(UserBehaviorDTO behavior) {
        // 记录用户行为到数据库
        UserBehavior record = new UserBehavior();
        record.setUserId(behavior.getUserId());
        record.setRoomId(behavior.getRoomId());
        record.setBehaviorType(behavior.getBehaviorType());
        record.setSearchKeyword(behavior.getSearchKeyword());
        record.setClickPosition(behavior.getClickPosition());
        record.setRecommendScore(behavior.getRecommendScore());
        record.setTimestamp(new Timestamp(System.currentTimeMillis()));

        userBehaviorMapper.insert(record);

        // 异步更新用户画像
        CompletableFuture.runAsync(() -> {
            updateUserProfileFromBehavior(behavior);
        });
    }

    private void updateUserProfileFromBehavior(UserBehaviorDTO behavior) {
        // 基于用户行为更新用户画像
        // 这里可以实现复杂的机器学习算法来更新用户特征向量

        if ("click".equals(behavior.getBehaviorType())) {
            // 点击行为：增加对应房间类型的偏好权重
            Room room = roomService.getRoomById(behavior.getRoomId());
            if (room != null) {
                updateUserPreferenceWeights(behavior.getUserId(), room);
            }
        }
    }

    private void updateUserPreferenceWeights(Long userId, Room room) {
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null) return;

        Map<String, Double> currentTags = profile.getPreferredTagsMap();

        // 基于点击的房间特征更新用户偏好
        if (room.getEquipmentNames() != null) {
            String[] equipments = room.getEquipmentNames().split(",");
            for (String equipment : equipments) {
                String trimmed = equipment.trim();
                currentTags.put(trimmed, currentTags.getOrDefault(trimmed, 0.0) + 0.05);
            }
        }

        // 基于价格等级更新偏好
        int priceLevel = calculatePriceLevel(room.getRoomPrice());
        String priceTag = "price_level_" + priceLevel;
        currentTags.put(priceTag, currentTags.getOrDefault(priceTag, 0.0) + 0.03);

        // 基于房间类型更新偏好
        String categoryTag = room.getCategory() == 1 ? "普通房间" : "特殊房间";
        currentTags.put(categoryTag, currentTags.getOrDefault(categoryTag, 0.0) + 0.02);

        // 权重衰减，防止某些标签权重过高
        currentTags.replaceAll((k, v) -> Math.min(1.0, v * 0.95));

        profile.setPreferredTags(JSON.toJSONString(currentTags));
        profile.setLastUpdated(new Timestamp(System.currentTimeMillis()));

        userProfileMapper.updateById(profile);
    }

    private int calculatePriceLevel(Double price) {
        if (price == null) return 3;
        if (price < 200) return 1;
        if (price < 400) return 2;
        if (price < 600) return 3;
        if (price < 800) return 4;
        return 5;
    }
}