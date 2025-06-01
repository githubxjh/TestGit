package com.example.demo.service;

import com.alibaba.fastjson2.JSON;
import com.example.demo.dto.RoomRecommendationDTO;
import com.example.demo.mapper.RoomFeaturesMapper;
import com.example.demo.mapper.RoomMapper;
import com.example.demo.mapper.UserProfileMapper;
import com.example.demo.model.Room;
import com.example.demo.model.RoomFeatures;
import com.example.demo.model.SearchIntent;
import com.example.demo.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 房间推荐引擎服务
 * 核心推荐算法实现
 */
@Service
public class RoomRecommendationEngineService {

    private static final Logger logger = LoggerFactory.getLogger(RoomRecommendationEngineService.class);

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomFeaturesMapper roomFeaturesMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 倒排索引存储
    private final Map<String, Set<String>> invertedIndex = new ConcurrentHashMap<>();

    // 缓存相关常量
    private static final String RECOMMENDATION_CACHE_KEY = "room:recommendation:";
    private static final String POPULAR_ROOMS_KEY = "popular_rooms";
    private static final int CACHE_EXPIRATION = 1800; // 30分钟缓存

    /**
     * 通用向量解析方法 - 将字符串或对象转换为Double数组
     */
    private Double[] parseVectorSafely(Object vectorData) {
        try {
            if (vectorData == null) {
                return getDefaultVector();
            }

            if (vectorData instanceof Double[]) {
                return (Double[]) vectorData;
            }

            if (vectorData instanceof String) {
                String vectorJson = (String) vectorData;
                if (vectorJson.trim().isEmpty()) {
                    return getDefaultVector();
                }
                return JSON.parseObject(vectorJson, Double[].class);
            }

            // 尝试转换为JSON字符串再解析
            String vectorJson = JSON.toJSONString(vectorData);
            return JSON.parseObject(vectorJson, Double[].class);

        } catch (Exception e) {
            logger.warn("解析向量数据失败，使用默认向量: {}", e.getMessage());
            return getDefaultVector();
        }
    }

    /**
     * 获取默认10维零向量
     */
    private Double[] getDefaultVector() {
        Double[] defaultVector = new Double[10];
        Arrays.fill(defaultVector, 0.0);
        return defaultVector;
    }

    /**
     * 解析房间特征向量字符串为Double数组
     */
    private Double[] parseFeatureVector(String vectorJson) {
        try {
            if (vectorJson == null || vectorJson.trim().isEmpty()) {
                // 返回默认10维零向量
                Double[] defaultVector = new Double[10];
                Arrays.fill(defaultVector, 0.0);
                return defaultVector;
            }

            // 使用fastjson解析JSON字符串为Double数组
            return JSON.parseObject(vectorJson, Double[].class);

        } catch (Exception e) {
            logger.warn("解析房间特征向量失败，使用默认向量: {}", e.getMessage());
            // 返回默认10维零向量
            Double[] defaultVector = new Double[10];
            Arrays.fill(defaultVector, 0.0);
            return defaultVector;
        }
    }

    /**
     * 系统启动时初始化索引
     */
    @jakarta.annotation.PostConstruct
    public void initializeIndex() {
        try {
            buildInvertedIndex();
            logger.info("推荐引擎初始化完成");
        } catch (Exception e) {
            logger.error("推荐引擎初始化失败", e);
        }
    }

    /**
     * 主推荐接口
     */
    public List<RoomRecommendationDTO> recommendRooms(Long userId, int limit) {
        String cacheKey = RECOMMENDATION_CACHE_KEY + userId;

        try {
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<RoomRecommendationDTO> cachedResult =
                    (List<RoomRecommendationDTO>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedResult != null && !cachedResult.isEmpty()) {
                return cachedResult.stream().limit(limit).collect(Collectors.toList());
            }

            // 获取用户画像
            UserProfile userProfile = userProfileMapper.selectById(userId);
            if (userProfile == null) {
                userProfile = createDefaultProfile(userId);
            }

            // 候选房间召回
            Set<String> candidateRoomIds = recallCandidates(userProfile);

            // 排序和推荐
            List<RoomRecommendationDTO> recommendations = rankAndRecommend(
                    candidateRoomIds, userProfile, limit * 2
            );

            // 缓存结果
            if (!recommendations.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, recommendations,
                        CACHE_EXPIRATION, TimeUnit.SECONDS);
            }

            return recommendations.stream().limit(limit).collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("生成推荐失败，用户ID: {}", userId, e);
            return getFallbackRecommendations(limit);
        }
    }

    /**
     * 基于搜索关键词的智能推荐
     */
    public List<RoomRecommendationDTO> searchWithRecommendation(Long userId, String keyword, int limit) {
        try {
            // 解析搜索意图
            SearchIntent intent = parseSearchIntent(keyword);

            // 基于意图的候选召回
            Set<String> candidateRoomIds = recallByIntent(intent);

            // 获取用户画像进行个性化排序
            UserProfile userProfile = userProfileMapper.selectById(userId);
            if (userProfile == null) {
                userProfile = createDefaultProfile(userId);
            }

            return rankAndRecommend(candidateRoomIds, userProfile, limit);

        } catch (Exception e) {
            logger.error("搜索推荐失败，用户ID: {}, 关键词: {}", userId, keyword, e);
            return searchFallback(keyword, limit);
        }
    }

    /**
     * 构建倒排索引
     */
    private void buildInvertedIndex() {
        try {
            List<RoomFeatures> allRoomFeatures = roomFeaturesMapper.selectList(null);
            invertedIndex.clear();

            for (RoomFeatures room : allRoomFeatures) {
                List<String> tags = room.getTagsList();
                for (String tag : tags) {
                    invertedIndex.computeIfAbsent(tag, k -> ConcurrentHashMap.newKeySet())
                            .add(room.getRoomId());
                }

                // 价格等级索引
                String priceTag = "price_level_" + room.getPriceLevel();
                invertedIndex.computeIfAbsent(priceTag, k -> ConcurrentHashMap.newKeySet())
                        .add(room.getRoomId());

                // 星级索引
                String starTag = "star_" + room.getStarLevel();
                invertedIndex.computeIfAbsent(starTag, k -> ConcurrentHashMap.newKeySet())
                        .add(room.getRoomId());
            }

            logger.info("倒排索引构建完成，包含 {} 个标签", invertedIndex.size());

        } catch (Exception e) {
            logger.error("构建倒排索引失败", e);
        }
    }

    /**
     * 候选房间召回
     */
    private Set<String> recallCandidates(UserProfile userProfile) {
        Set<String> candidates = new HashSet<>();
        Map<String, Double> preferredTags = userProfile.getPreferredTagsMap();

        try {
            // 基于标签召回
            for (Map.Entry<String, Double> entry : preferredTags.entrySet()) {
                String tag = entry.getKey();
                Double weight = entry.getValue();

                if (weight > 0.3) { // 权重阈值过滤
                    Set<String> tagRooms = invertedIndex.get(tag);
                    if (tagRooms != null) {
                        candidates.addAll(tagRooms);
                    }
                }
            }

            // 基于价格偏好召回
            if (userProfile.getPriceRange() != null) {
                String[] range = userProfile.getPriceRange().split(",");
                int minLevel = Integer.parseInt(range[0]);
                int maxLevel = Integer.parseInt(range[1]);

                for (int level = minLevel; level <= maxLevel; level++) {
                    Set<String> priceRooms = invertedIndex.get("price_level_" + level);
                    if (priceRooms != null) {
                        candidates.addAll(priceRooms);
                    }
                }
            }

            // 如果候选太少，添加流行房间
            if (candidates.size() < 20) {
                candidates.addAll(getPopularRooms(50));
            }

        } catch (Exception e) {
            logger.error("候选召回失败，用户ID: {}", userProfile.getUserId(), e);
            candidates.addAll(getPopularRooms(50));
        }

        return candidates;
    }

    /**
     * 基于搜索意图召回候选房间
     */
    private Set<String> recallByIntent(SearchIntent intent) {
        Set<String> candidates = new HashSet<>();

        try {
            // 基于设施召回
            for (String facility : intent.getFacilities()) {
                Set<String> facilityRooms = invertedIndex.get(facility);
                if (facilityRooms != null) {
                    candidates.addAll(facilityRooms);
                }
            }

            // 基于房型召回
            if (intent.getRoomType() != null) {
                Set<String> typeRooms = invertedIndex.get(intent.getRoomType());
                if (typeRooms != null) {
                    candidates.addAll(typeRooms);
                }
            }

            // 基于价格召回
            if (intent.getPriceHint() != null) {
                int priceLevel = calculatePriceLevel(intent.getPriceHint().doubleValue());
                Set<String> priceRooms = invertedIndex.get("price_level_" + priceLevel);
                if (priceRooms != null) {
                    candidates.addAll(priceRooms);
                }
            }

            // 如果候选太少，使用热门房间补充
            if (candidates.size() < 10) {
                candidates.addAll(getPopularRooms(30));
            }

        } catch (Exception e) {
            logger.error("基于意图召回失败", e);
            candidates.addAll(getPopularRooms(30));
        }

        return candidates;
    }

    /**
     * 排序和推荐
     */
    private List<RoomRecommendationDTO> rankAndRecommend(
            Set<String> candidateRoomIds, UserProfile userProfile, int limit) {

        if (candidateRoomIds.isEmpty()) {
            return getFallbackRecommendations(limit);
        }

        try {
            List<RoomFeatures> candidates = roomFeaturesMapper.selectByRoomIds(candidateRoomIds);
            // 直接获取用户画像向量（已经是Double[]类型）
            Double[] userVector = userProfile.getProfileVector();
            if (userVector == null) {
                userVector = getDefaultVector();
            }

            // 将userVector声明为final，以便在lambda中使用
            final Double[] finalUserVector = userVector;

            return candidates.stream()
                    .map(room -> {
                        // 获取房间特征向量 - 使用RoomFeatures提供的方法
                        Double[] roomVector = room.getFeatureVectorArray();
                        if (roomVector == null || roomVector.length == 0) {
                            roomVector = getDefaultVector();
                        }
                        double similarity = calculateCosineSimilarity(finalUserVector, roomVector);

                        // 添加业务规则加权
                        double businessScore = calculateBusinessScore(room, userProfile);
                        double finalScore = similarity * 0.7 + businessScore * 0.3;

                        return new RoomRecommendationDTO(
                                room.getRoomId(),
                                finalScore,
                                similarity,
                                "基于您的偏好推荐"
                        );
                    })
                    .filter(dto -> dto.getScore() > 0.1) // 过滤低分推荐
                    .sorted(Comparator.comparingDouble(RoomRecommendationDTO::getScore).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("排序推荐失败", e);
            return getFallbackRecommendations(limit);
        }
    }

    /**
     * 解析用户画像向量字符串为Double数组
     */
    private Double[] parseProfileVector(String vectorJson) {
        try {
            if (vectorJson == null || vectorJson.trim().isEmpty()) {
                // 返回默认10维零向量
                Double[] defaultVector = new Double[10];
                Arrays.fill(defaultVector, 0.0);
                return defaultVector;
            }

            // 使用fastjson解析JSON字符串为Double数组
            return JSON.parseObject(vectorJson, Double[].class);

        } catch (Exception e) {
            logger.warn("解析用户画像向量失败，使用默认向量: {}", e.getMessage());
            // 返回默认10维零向量
            Double[] defaultVector = new Double[10];
            Arrays.fill(defaultVector, 0.0);
            return defaultVector;
        }
    }

    /**
     * 余弦相似度计算
     */
    private double calculateCosineSimilarity(Double[] vectorA, Double[] vectorB) {
        if (vectorA == null || vectorB == null ||
                vectorA.length == 0 || vectorB.length == 0) {
            return 0.0;
        }

        int minLength = Math.min(vectorA.length, vectorB.length);
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < minLength; i++) {
            double a = vectorA[i] != null ? vectorA[i] : 0.0;
            double b = vectorB[i] != null ? vectorB[i] : 0.0;

            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 业务规则评分
     */
    private double calculateBusinessScore(RoomFeatures room, UserProfile userProfile) {
        double score = 0.0;

        try {
            // 星级匹配度
            if (userProfile.getStarPreference() != null) {
                int starDiff = Math.abs(room.getStarLevel() - userProfile.getStarPreference());
                score += Math.max(0, 1.0 - starDiff * 0.2);
            }

            // 价格匹配度
            if (userProfile.getPriceRange() != null) {
                String[] range = userProfile.getPriceRange().split(",");
                int minLevel = Integer.parseInt(range[0]);
                int maxLevel = Integer.parseInt(range[1]);

                if (room.getPriceLevel() >= minLevel && room.getPriceLevel() <= maxLevel) {
                    score += 0.5;
                }
            }

        } catch (Exception e) {
            logger.warn("计算业务规则评分失败", e);
        }

        return Math.min(1.0, score);
    }

    /**
     * 解析搜索意图
     */
    private SearchIntent parseSearchIntent(String keyword) {
        SearchIntent intent = new SearchIntent();
        intent.setOriginalQuery(keyword);

        try {
            String lowerKeyword = keyword.toLowerCase();

            // 价格意图识别
            Pattern pricePattern = Pattern.compile("([0-9]+)");
            Matcher matcher = pricePattern.matcher(keyword);
            if (matcher.find()) {
                int price = Integer.parseInt(matcher.group(1));
                intent.setPriceHint(price);
            }

            // 设施意图识别
            String[] facilityKeywords = {"wifi", "早餐", "健身", "游泳", "停车", "海景", "市景", "空调"};
            List<String> detectedFacilities = new ArrayList<>();
            for (String facility : facilityKeywords) {
                if (lowerKeyword.contains(facility)) {
                    detectedFacilities.add(facility);
                }
            }
            intent.setFacilities(detectedFacilities);

            // 房型意图识别
            String[] roomTypeKeywords = {"标准", "豪华", "套房", "单人", "双人", "亲子", "商务", "电竞"};
            for (String roomType : roomTypeKeywords) {
                if (lowerKeyword.contains(roomType)) {
                    intent.setRoomType(roomType);
                    break;
                }
            }

        } catch (Exception e) {
            logger.warn("解析搜索意图失败，关键词: {}", keyword, e);
        }

        return intent;
    }

    /**
     * 获取热门房间
     */
    private Set<String> getPopularRooms(int limit) {
        try {
            @SuppressWarnings("unchecked")
            Set<String> popularRooms = (Set<String>) redisTemplate.opsForValue()
                    .get(POPULAR_ROOMS_KEY);

            if (popularRooms == null || popularRooms.isEmpty()) {
                // 从数据库获取活跃房间作为备选
                List<Room> activeRooms = roomMapper.selectList(null);
                popularRooms = activeRooms.stream()
                        .filter(room -> room.getRoomStatus() != null && room.getRoomStatus() == 1)
                        .map(Room::getRoomId)
                        .limit(limit)
                        .collect(Collectors.toSet());

                // 存入缓存
                if (!popularRooms.isEmpty()) {
                    redisTemplate.opsForValue().set(POPULAR_ROOMS_KEY, popularRooms,
                            3600, TimeUnit.SECONDS);
                }
            }

            return popularRooms.stream().limit(limit).collect(Collectors.toSet());

        } catch (Exception e) {
            logger.error("获取热门房间失败", e);
            return new HashSet<>();
        }
    }

    /**
     * 创建默认用户画像
     */
    private UserProfile createDefaultProfile(Long userId) {
        try {
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            profile.setPreferredTags("{}");
            profile.setPriceRange("2,4"); // 默认中等价位偏好
            profile.setStarPreference(3); // 默认3星偏好

            // 初始化10维特征向量为Double数组
            Double[] vector = new Double[10];
            Arrays.fill(vector, 0.0);
            profile.setProfileVector(vector);
            profile.setLastUpdated(new Timestamp(System.currentTimeMillis()));

            userProfileMapper.insert(profile);
            logger.info("为用户 {} 创建默认画像", userId);

            return profile;

        } catch (Exception e) {
            logger.error("创建默认用户画像失败，用户ID: {}", userId, e);
            // 返回临时画像避免空指针
            UserProfile tempProfile = new UserProfile();
            tempProfile.setUserId(userId);
            tempProfile.setPreferredTags("{}");
            tempProfile.setPriceRange("2,4");
            tempProfile.setStarPreference(3);
            // 为临时画像设置默认向量
            tempProfile.setProfileVector(getDefaultVector());
            return tempProfile;
        }
    }

    /**
     * 备用推荐策略
     */
    private List<RoomRecommendationDTO> getFallbackRecommendations(int limit) {
        try {
            Set<String> popularRoomIds = getPopularRooms(limit * 2);

            return popularRoomIds.stream()
                    .limit(limit)
                    .map(roomId -> new RoomRecommendationDTO(
                            roomId,
                            0.5, // 默认分数
                            0.0,
                            "热门推荐"
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("获取备用推荐失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 搜索备用策略
     */
    private List<RoomRecommendationDTO> searchFallback(String keyword, int limit) {
        try {
            // 简单的关键词匹配
            List<Room> allRooms = roomMapper.selectList(null); // 这里需要添加null参数
            String lowerKeyword = keyword.toLowerCase();

            return allRooms.stream()
                    .filter(room -> room.getRoomName() != null &&
                            room.getRoomName().toLowerCase().contains(lowerKeyword))
                    .limit(limit)
                    .map(room -> new RoomRecommendationDTO(
                            room.getRoomId(),
                            0.6, // 匹配分数
                            0.0,
                            "关键词匹配"
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("搜索备用策略失败", e);
            return Collections.emptyList();
        }
    }
    /**
     * 计算价格等级
     */
    private int calculatePriceLevel(double price) {
        if (price < 200) return 1;
        if (price < 400) return 2;
        if (price < 600) return 3;
        if (price < 800) return 4;
        return 5;
    }

    /**
     * 刷新推荐缓存
     */
    public void refreshRecommendationCache(Long userId) {
        try {
            String cacheKey = RECOMMENDATION_CACHE_KEY + userId;
            redisTemplate.delete(cacheKey);
            logger.debug("清除用户 {} 的推荐缓存", userId);
        } catch (Exception e) {
            logger.warn("清除推荐缓存失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 获取推荐引擎状态
     */
    public Map<String, Object> getEngineStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            status.put("indexSize", invertedIndex.size());
            status.put("indexKeys", invertedIndex.keySet().size());
            status.put("status", "运行中");

            // 统计各类标签数量
            long priceTagCount = invertedIndex.keySet().stream()
                    .filter(key -> key.startsWith("price_level_"))
                    .count();
            long starTagCount = invertedIndex.keySet().stream()
                    .filter(key -> key.startsWith("star_"))
                    .count();

            status.put("priceTagCount", priceTagCount);
            status.put("starTagCount", starTagCount);

        } catch (Exception e) {
            status.put("status", "异常");
            status.put("error", e.getMessage());
        }

        return status;
    }
}