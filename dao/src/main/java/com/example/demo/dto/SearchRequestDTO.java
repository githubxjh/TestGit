package com.example.demo.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 搜索请求DTO
 * 用于封装智能搜索推荐的请求参数
 */
public class SearchRequestDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "搜索关键词不能为空")
    private String keyword;

    @Min(value = 1, message = "限制数量不能小于1")
    @Max(value = 100, message = "限制数量不能大于100")
    private Integer limit = 20; // 默认值20

    /**
     * 默认构造函数
     */
    public SearchRequestDTO() {
    }

    /**
     * 全参构造函数
     */
    public SearchRequestDTO(Long userId, String keyword, Integer limit) {
        this.userId = userId;
        this.keyword = keyword;
        this.limit = limit;
    }

    /**
     * 简化构造函数，使用默认limit
     */
    public SearchRequestDTO(Long userId, String keyword) {
        this.userId = userId;
        this.keyword = keyword;
        this.limit = 20;
    }

    // getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * 获取处理后的关键词（去除前后空格，转小写）
     */
    public String getProcessedKeyword() {
        return keyword != null ? keyword.trim().toLowerCase() : "";
    }

    /**
     * 验证请求参数是否有效
     */
    public boolean isValid() {
        return userId != null &&
                keyword != null &&
                !keyword.trim().isEmpty() &&
                limit != null &&
                limit > 0 &&
                limit <= 100;
    }

    @Override
    public String toString() {
        return "SearchRequestDTO{" +
                "userId=" + userId +
                ", keyword='" + keyword + '\'' +
                ", limit=" + limit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchRequestDTO that = (SearchRequestDTO) o;

        if (!userId.equals(that.userId)) return false;
        if (!keyword.equals(that.keyword)) return false;
        return limit.equals(that.limit);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + keyword.hashCode();
        result = 31 * result + limit.hashCode();
        return result;
    }
}