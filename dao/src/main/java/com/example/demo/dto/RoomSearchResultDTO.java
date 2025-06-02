package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房间搜索结果DTO
 * 用于封装智能搜索推荐的房间结果
 */
public class RoomSearchResultDTO {

    private String roomId;
    private String roomName;
    private String roomContent;
    private BigDecimal roomPrice;
    private String roomImage;
    private List<String> equipmentNames;
    private Double rating; // 房间评分
    private Double score; // 推荐得分
    private Double similarity; // 相似度
    private String reason; // 推荐理由
    private String priceRange; // 价格区间描述
    private Integer starLevel; // 星级
    private String location; // 位置信息
    private Boolean isRecommended; // 是否为推荐房间
    private Integer bookingCount; // 预订次数（可选）

    /**
     * 默认构造函数
     */
    public RoomSearchResultDTO() {
    }

    /**
     * 基础构造函数
     */
    public RoomSearchResultDTO(String roomId, String roomName, BigDecimal roomPrice) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomPrice = roomPrice;
    }

    // getters and setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomContent() {
        return roomContent;
    }

    public void setRoomContent(String roomContent) {
        this.roomContent = roomContent;
    }

    public BigDecimal getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(BigDecimal roomPrice) {
        this.roomPrice = roomPrice;
    }

    public String getRoomImage() {
        return roomImage;
    }

    public void setRoomImage(String roomImage) {
        this.roomImage = roomImage;
    }

    public List<String> getEquipmentNames() {
        return equipmentNames;
    }

    public void setEquipmentNames(List<String> equipmentNames) {
        this.equipmentNames = equipmentNames;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public Integer getStarLevel() {
        return starLevel;
    }

    public void setStarLevel(Integer starLevel) {
        this.starLevel = starLevel;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getIsRecommended() {
        return isRecommended;
    }

    public void setIsRecommended(Boolean isRecommended) {
        this.isRecommended = isRecommended;
    }

    public Integer getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(Integer bookingCount) {
        this.bookingCount = bookingCount;
    }

    /**
     * 获取格式化的价格字符串
     */
    public String getFormattedPrice() {
        if (roomPrice == null) {
            return "价格面议";
        }
        return "¥" + roomPrice.toString();
    }

    /**
     * 获取格式化的评分字符串
     */
    public String getFormattedRating() {
        if (rating == null) {
            return "暂无评分";
        }
        return String.format("%.1f分", rating);
    }

    /**
     * 获取格式化的推荐得分字符串
     */
    public String getFormattedScore() {
        if (score == null) {
            return "0%";
        }
        return String.format("%.0f%%", score * 100);
    }

    /**
     * 获取星级描述
     */
    public String getStarDescription() {
        if (starLevel == null || starLevel < 1) {
            return "经济型";
        }
        switch (starLevel) {
            case 1: return "一星级";
            case 2: return "二星级";
            case 3: return "三星级";
            case 4: return "四星级";
            case 5: return "五星级";
            default: return "豪华型";
        }
    }

    /**
     * 判断是否为高分推荐
     */
    public boolean isHighScore() {
        return score != null && score > 0.8;
    }

    /**
     * 判断是否为高评分房间
     */
    public boolean isHighRating() {
        return rating != null && rating >= 4.5;
    }

    @Override
    public String toString() {
        return "RoomSearchResultDTO{" +
                "roomId='" + roomId + '\'' +
                ", roomName='" + roomName + '\'' +
                ", roomPrice=" + roomPrice +
                ", rating=" + rating +
                ", score=" + score +
                ", similarity=" + similarity +
                ", reason='" + reason + '\'' +
                ", starLevel=" + starLevel +
                ", isRecommended=" + isRecommended +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomSearchResultDTO that = (RoomSearchResultDTO) o;

        return roomId != null ? roomId.equals(that.roomId) : that.roomId == null;
    }

    @Override
    public int hashCode() {
        return roomId != null ? roomId.hashCode() : 0;
    }
}