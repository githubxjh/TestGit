package org.example.web.controller;

import com.example.demo.dto.RoomRecommendationDTO;
import com.example.demo.dto.RoomSearchResultDTO;
import com.example.demo.dto.SmartTagDTO;
import com.example.demo.dto.UserBehaviorDTO;
import com.example.demo.dto.SearchRequestDTO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.model.Room;
import com.example.demo.model.UserProfile;
import com.example.demo.mapper.UserProfileMapper;
import com.example.demo.service.RoomRecommendationEngineService;
import com.example.demo.service.RoomService;
import com.example.demo.service.UserBehaviorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智能推荐API控制器
 */
@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class FrontRoomRecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(FrontRoomRecommendationController.class);

    @Autowired
    private RoomRecommendationEngineService recommendationEngine;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Autowired
    private UserProfileMapper userProfileMapper;

    /**
     * 智能搜索推荐接口
     */
    @PostMapping("/search-with-recommendation")
    public ApiResponse<List<RoomSearchResultDTO>> searchWithRecommendation(
            @RequestBody SearchRequestDTO request) {

        try {
            List<RoomRecommendationDTO> recommendations = recommendationEngine
                    .searchWithRecommendation(request.getUserId(), request.getKeyword(), request.getLimit());

            // 转换为前端需要的格式
            List<RoomSearchResultDTO> results = new ArrayList<>();
            for (RoomRecommendationDTO rec : recommendations) {
                Room room = roomService.getRoomById(rec.getRoomId());
                if (room != null) {
                    RoomSearchResultDTO dto = convertToSearchResult(room, rec);
                    results.add(dto);
                }
            }

            return ApiResponse.success(results);

        } catch (Exception e) {
            logger.error("智能搜索推荐失败", e);
            return ApiResponse.error("搜索失败，请重试");
        }
    }

    /**
     * 个性化推荐接口
     */
    @GetMapping("/personal-recommendations")
    public ApiResponse<List<RoomRecommendationDTO>> getPersonalRecommendations(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            List<RoomRecommendationDTO> recommendations =
                    recommendationEngine.recommendRooms(userId, limit);

            return ApiResponse.success(recommendations);

        } catch (Exception e) {
            logger.error("获取个性化推荐失败", e);
            return ApiResponse.error("推荐服务暂不可用");
        }
    }

    /**
     * 搜索建议接口
     */
    @GetMapping("/search-suggestions")
    public ApiResponse<List<String>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "8") int limit) {

        try {
            List<String> suggestions = generateSearchSuggestions(query, limit);
            return ApiResponse.success(suggestions);

        } catch (Exception e) {
            logger.error("获取搜索建议失败", e);
            return ApiResponse.error("建议服务暂不可用");
        }
    }

    /**
     * 智能标签推荐接口
     */
    @GetMapping("/smart-tags")
    public ApiResponse<List<SmartTagDTO>> getSmartTags(@RequestParam Long userId) {

        try {
            UserProfile profile = userProfileMapper.selectById(userId);
            List<SmartTagDTO> tags = new ArrayList<>();

            if (profile != null) {
                Map<String, Double> preferredTags = profile.getPreferredTagsMap();

                // 按权重排序并返回前5个
                tags = preferredTags.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0.3)
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .limit(5)
                        .map(entry -> new SmartTagDTO(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
            }

            // 如果用户标签不足，添加热门标签
            if (tags.size() < 3) {
                List<String> hotTags = Arrays.asList("豪华房间", "海景房", "商务房", "亲子房");
                for (String tag : hotTags) {
                    if (tags.stream().noneMatch(t -> t.getName().equals(tag))) {
                        tags.add(new SmartTagDTO(tag, 0.8));
                        if (tags.size() >= 5) break;
                    }
                }
            }

            return ApiResponse.success(tags);

        } catch (Exception e) {
            logger.error("获取智能标签失败", e);
            return ApiResponse.error("标签服务暂不可用");
        }
    }

    /**
     * 记录用户行为接口
     */
    @PostMapping("/record-user-behavior")
    public ApiResponse<Void> recordUserBehavior(@RequestBody UserBehaviorDTO behavior) {

        try {
            userBehaviorService.recordBehavior(behavior);
            return ApiResponse.success();

        } catch (Exception e) {
            logger.error("记录用户行为失败", e);
            return ApiResponse.error("记录失败");
        }
    }

    // ==================== 私有方法 ====================

    private RoomSearchResultDTO convertToSearchResult(Room room, RoomRecommendationDTO rec) {
        RoomSearchResultDTO dto = new RoomSearchResultDTO();
        dto.setRoomId(room.getRoomId());
        dto.setRoomName(room.getRoomName());
        dto.setRoomContent(room.getRoomContent());

        // 修复：Double到BigDecimal的转换
        if (room.getRoomPrice() != null) {
            dto.setRoomPrice(java.math.BigDecimal.valueOf(room.getRoomPrice()));
        }

        dto.setRoomImage(room.getRoomImage());

        // 修复：使用专门的方法处理设备名称
        dto.setEquipmentNames(parseEquipmentNames(room.getEquipmentNames()));

        dto.setRating(calculateRoomRating(room));
        dto.setScore(rec.getScore());
        dto.setSimilarity(rec.getSimilarity());
        dto.setReason(rec.getReason());

        // 设置额外信息
        dto.setIsRecommended(rec.getScore() > 0.5); // 得分大于0.5认为是推荐房间

        // 从房间的星级评分解析星级数字
        if (room.getStarRating() != null) {
            try {
                // 如果starRating是数字字符串，直接解析
                dto.setStarLevel(Integer.parseInt(room.getStarRating()));
            } catch (NumberFormatException e) {
                // 如果不是数字，尝试从文本中提取
                dto.setStarLevel(extractStarLevelFromText(room.getStarRating()));
            }
        }

        // 设置价格范围描述
        if (room.getRoomPrice() != null) {
            dto.setPriceRange(getPriceRangeDescription(room.getRoomPrice()));
        }

        return dto;
    }

    private List<String> generateSearchSuggestions(String query, int limit) {
        List<String> suggestions = new ArrayList<>();

        // 基于关键词匹配的建议生成
        if (query.contains("豪华")) {
            suggestions.addAll(Arrays.asList("豪华海景房", "豪华套房", "豪华商务房"));
        }
        if (query.contains("便宜") || query.contains("经济")) {
            suggestions.addAll(Arrays.asList("经济型房间", "标准间", "青年旅社"));
        }
        if (query.contains("海")) {
            suggestions.addAll(Arrays.asList("海景房", "海边度假村", "海景套房"));
        }
        if (query.contains("商务")) {
            suggestions.addAll(Arrays.asList("商务房", "商务套房", "会议室房间"));
        }
        if (query.contains("亲子")) {
            suggestions.addAll(Arrays.asList("亲子房", "家庭房", "儿童主题房"));
        }

        // 基于价格的建议
        if (query.matches(".*[0-9]+.*")) {
            suggestions.addAll(Arrays.asList("200-300元房间", "300-500元房间", "500-800元房间"));
        }

        // 移除重复并限制数量
        return suggestions.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Double calculateRoomRating(Room room) {
        // 这里可以基于真实的评价数据计算
        // 暂时返回模拟评分
        return 4.0 + Math.random() * 1.0; // 4.0-5.0之间的随机评分
    }

    /**
     * 从文本中提取星级数字
     */
    private Integer extractStarLevelFromText(String starRating) {
        if (starRating == null || starRating.trim().isEmpty()) {
            return 3; // 默认3星
        }

        // 尝试从文本中提取数字
        String text = starRating.toLowerCase();
        if (text.contains("五星") || text.contains("5星") || text.contains("★★★★★")) {
            return 5;
        } else if (text.contains("四星") || text.contains("4星") || text.contains("★★★★")) {
            return 4;
        } else if (text.contains("三星") || text.contains("3星") || text.contains("★★★")) {
            return 3;
        } else if (text.contains("二星") || text.contains("2星") || text.contains("★★")) {
            return 2;
        } else if (text.contains("一星") || text.contains("1星") || text.contains("★")) {
            return 1;
        }

        return 3; // 默认3星
    }

    /**
     * 处理设备名称字符串，支持多种分隔符
     */
    private List<String> parseEquipmentNames(String equipmentNames) {
        if (equipmentNames == null || equipmentNames.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 支持多种分隔符：逗号、分号、斜杠、竖线
        String[] separators = {",", ";", "/", "|", "、"};
        String normalizedNames = equipmentNames;

        // 统一替换为逗号分隔
        for (String separator : separators) {
            normalizedNames = normalizedNames.replace(separator, ",");
        }

        return java.util.Arrays.asList(normalizedNames.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct() // 去重
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取价格范围描述
     */
    private String getPriceRangeDescription(Double price) {
        if (price == null) {
            return "价格面议";
        }

        if (price < 200) {
            return "经济型 (￥0-200)";
        } else if (price < 400) {
            return "舒适型 (￥200-400)";
        } else if (price < 600) {
            return "高档型 (￥400-600)";
        } else if (price < 800) {
            return "豪华型 (￥600-800)";
        } else {
            return "奢华型 (￥800+)";
        }
    }
}