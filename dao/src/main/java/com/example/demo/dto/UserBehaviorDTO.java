package com.example.demo.dto;

public class UserBehaviorDTO {
    private Long userId;
    private String roomId;
    private String behaviorType; // click, view, search, book
    private String searchKeyword;
    private Integer clickPosition;
    private Double recommendScore;

    // getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getBehaviorType() { return behaviorType; }
    public void setBehaviorType(String behaviorType) { this.behaviorType = behaviorType; }

    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }

    public Integer getClickPosition() { return clickPosition; }
    public void setClickPosition(Integer clickPosition) { this.clickPosition = clickPosition; }

    public Double getRecommendScore() { return recommendScore; }
    public void setRecommendScore(Double recommendScore) { this.recommendScore = recommendScore; }
}
