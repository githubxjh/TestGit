package com.example.demo.config;

// ==================== 系统健康检查 ====================

import com.example.demo.dto.RoomRecommendationDTO;
import com.example.demo.service.RoomRecommendationEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 推荐系统健康检查组件
 * 用于监控推荐系统的运行状态
 */
@Component
public class RecommendationHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationHealthIndicator.class);

    @Autowired
    private RoomRecommendationEngineService recommendationEngineService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            // 检查推荐引擎状态
            Map<String, Object> engineStatus = recommendationEngineService.getEngineStatus();

            // 执行简单的推荐测试
            List<RoomRecommendationDTO> testRecommendations = null;
            try {
                testRecommendations = recommendationEngineService.recommendRooms(1L, 1);
            } catch (Exception e) {
                logger.warn("推荐引擎测试失败，但系统仍可运行", e);
            }

            // 检查Redis连接
            boolean redisHealthy = checkRedisConnection();

            // 综合判断健康状态
            boolean isHealthy = engineStatus.get("status").equals("运行中") && redisHealthy;

            Health.Builder healthBuilder = isHealthy ? Health.up() : Health.down();

            return healthBuilder
                    .withDetail("recommendation-engine", engineStatus.get("status"))
                    .withDetail("redis-connection", redisHealthy ? "UP" : "DOWN")
                    .withDetail("index-size", engineStatus.get("indexSize"))
                    .withDetail("test-recommendation-count",
                            testRecommendations != null ? testRecommendations.size() : 0)
                    .withDetail("price-tags", engineStatus.get("priceTagCount"))
                    .withDetail("star-tags", engineStatus.get("starTagCount"))
                    .build();

        } catch (Exception e) {
            logger.error("健康检查执行失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("error-type", e.getClass().getSimpleName())
                    .build();
        }
    }

    /**
     * 检查Redis连接状态
     */
    private boolean checkRedisConnection() {
        try {
            // 执行简单的Redis操作来检查连接
            redisTemplate.opsForValue().set("health-check", "test", 10, java.util.concurrent.TimeUnit.SECONDS);
            String result = (String) redisTemplate.opsForValue().get("health-check");
            redisTemplate.delete("health-check");

            return "test".equals(result);

        } catch (Exception e) {
            logger.warn("Redis连接检查失败", e);
            return false;
        }
    }

    /**
     * 获取详细的健康信息
     */
    public Map<String, Object> getDetailedHealthInfo() {
        Map<String, Object> healthInfo = new java.util.HashMap<>();

        try {
            // 推荐引擎状态
            Map<String, Object> engineStatus = recommendationEngineService.getEngineStatus();
            healthInfo.put("engine", engineStatus);

            // Redis状态
            healthInfo.put("redis", Map.of(
                    "connected", checkRedisConnection(),
                    "ping", pingRedis()
            ));

            // 系统资源信息
            Runtime runtime = Runtime.getRuntime();
            healthInfo.put("system", Map.of(
                    "totalMemory", runtime.totalMemory(),
                    "freeMemory", runtime.freeMemory(),
                    "usedMemory", runtime.totalMemory() - runtime.freeMemory(),
                    "maxMemory", runtime.maxMemory(),
                    "availableProcessors", runtime.availableProcessors()
            ));

            // 当前时间戳
            healthInfo.put("timestamp", System.currentTimeMillis());
            healthInfo.put("status", "healthy");

        } catch (Exception e) {
            healthInfo.put("status", "unhealthy");
            healthInfo.put("error", e.getMessage());
        }

        return healthInfo;
    }

    /**
     * Ping Redis服务器
     */
    private String pingRedis() {
        try {
            return redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
        } catch (Exception e) {
            return "FAILED: " + e.getMessage();
        }
    }
}