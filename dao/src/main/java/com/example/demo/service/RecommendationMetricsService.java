package com.example.demo.service;

// ==================== 正确的import ====================
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.springframework.stereotype.Component;

/**
 * 推荐系统性能监控服务
 * 负责记录推荐系统的各种性能指标
 */
@Component
public class RecommendationMetricsService {

    private final MeterRegistry meterRegistry;
    private final Counter searchCounter;
    private final Counter recommendationCounter;
    private final Counter clickCounter;
    private final Counter conversionCounter;
    private final Timer searchTimer;
    private final Timer recommendationTimer;

    public RecommendationMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 搜索相关指标
        this.searchCounter = Counter.builder("recommendation.search.total")
                .description("Total number of searches")
                .tag("service", "recommendation")
                .register(meterRegistry);

        // 推荐生成指标
        this.recommendationCounter = Counter.builder("recommendation.generated.total")
                .description("Total number of recommendations generated")
                .tag("service", "recommendation")
                .register(meterRegistry);

        // 点击相关指标
        this.clickCounter = Counter.builder("recommendation.clicks.total")
                .description("Total number of recommendation clicks")
                .tag("service", "recommendation")
                .register(meterRegistry);

        // 转化相关指标
        this.conversionCounter = Counter.builder("recommendation.conversions.total")
                .description("Total number of booking conversions from recommendations")
                .tag("service", "recommendation")
                .register(meterRegistry);

        // 时间相关指标
        this.searchTimer = Timer.builder("recommendation.search.duration")
                .description("Search request duration")
                .tag("service", "recommendation")
                .register(meterRegistry);

        this.recommendationTimer = Timer.builder("recommendation.generation.duration")
                .description("Recommendation generation duration")
                .tag("service", "recommendation")
                .register(meterRegistry);
    }

    // ==================== 基础计数方法 ====================

    /**
     * 记录搜索次数
     */
    public void recordSearch() {
        searchCounter.increment();
    }

    /**
     * 记录搜索次数（带标签）
     */
    public void recordSearch(String searchType) {
        Counter.builder("recommendation.search.total")
                .tag("type", searchType)
                .tag("service", "recommendation")
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录推荐生成次数
     */
    public void recordRecommendation(int count) {
        recommendationCounter.increment(count);
    }

    /**
     * 记录推荐点击
     */
    public void recordClick() {
        clickCounter.increment();
    }

    /**
     * 记录推荐点击（带位置信息）
     */
    public void recordClick(int position) {
        Counter.builder("recommendation.clicks.total")
                .tag("position", String.valueOf(position))
                .tag("service", "recommendation")
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录转化（预订）
     */
    public void recordConversion() {
        conversionCounter.increment();
    }

    /**
     * 记录转化（带来源信息）
     */
    public void recordConversion(String source) {
        Counter.builder("recommendation.conversions.total")
                .tag("source", source)
                .tag("service", "recommendation")
                .register(meterRegistry)
                .increment();
    }

    // ==================== 时间测量方法 ====================

    /**
     * 开始搜索计时
     */
    public Timer.Sample startSearchTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 开始推荐生成计时
     */
    public Timer.Sample startRecommendationTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 记录搜索耗时
     */
    public void recordSearchDuration(Timer.Sample sample) {
        sample.stop(searchTimer);
    }

    /**
     * 记录推荐生成耗时
     */
    public void recordRecommendationDuration(Timer.Sample sample) {
        sample.stop(recommendationTimer);
    }

    // ==================== 高级指标方法 ====================

    /**
     * 记录缓存命中率
     */
    public void recordCacheHit(boolean hit) {
        Counter.builder("recommendation.cache.total")
                .tag("result", hit ? "hit" : "miss")
                .tag("service", "recommendation")
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录推荐质量分数
     */
    public void recordRecommendationQuality(double score) {
        meterRegistry.gauge("recommendation.quality.score", score);
    }

    /**
     * 记录用户满意度
     */
    public void recordUserSatisfaction(double rating) {
        meterRegistry.gauge("recommendation.user.satisfaction", rating);
    }

    /**
     * 记录错误次数
     */
    public void recordError(String errorType) {
        Counter.builder("recommendation.errors.total")
                .tag("type", errorType)
                .tag("service", "recommendation")
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录API响应状态
     */
    public void recordApiResponse(int statusCode) {
        Counter.builder("recommendation.api.responses.total")
                .tag("status", String.valueOf(statusCode))
                .tag("service", "recommendation")
                .register(meterRegistry)
                .increment();
    }

    // ==================== 业务指标方法 ====================

    /**
     * 记录推荐算法使用情况
     */
    public void recordAlgorithmUsage(String algorithmType) {
        Counter.builder("recommendation.algorithm.usage.total")
                .tag("algorithm", algorithmType)
                .tag("service", "recommendation")
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录用户行为
     */
    public void recordUserBehavior(String behaviorType) {
        Counter.builder("recommendation.user.behavior.total")
                .tag("behavior", behaviorType)
                .tag("service", "recommendation")
                .register(meterRegistry)
                .increment();
    }

    /**
     * 计算并记录点击率
     */
    public void calculateAndRecordCTR(int clicks, int impressions) {
        if (impressions > 0) {
            double ctr = (double) clicks / impressions;
            meterRegistry.gauge("recommendation.ctr", ctr);
        }
    }

    /**
     * 计算并记录转化率
     */
    public void calculateAndRecordConversionRate(int conversions, int clicks) {
        if (clicks > 0) {
            double conversionRate = (double) conversions / clicks;
            meterRegistry.gauge("recommendation.conversion.rate", conversionRate);
        }
    }

    // ==================== 获取指标方法 ====================

    /**
     * 获取搜索总数
     */
    public double getSearchCount() {
        return searchCounter.count();
    }

    /**
     * 获取推荐总数
     */
    public double getRecommendationCount() {
        return recommendationCounter.count();
    }

    /**
     * 获取点击总数
     */
    public double getClickCount() {
        return clickCounter.count();
    }

    /**
     * 获取转化总数
     */
    public double getConversionCount() {
        return conversionCounter.count();
    }

    /**
     * 获取平均搜索耗时（毫秒）
     */
    public double getAverageSearchDuration() {
        return searchTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 获取平均推荐生成耗时（毫秒）
     */
    public double getAverageRecommendationDuration() {
        return recommendationTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}