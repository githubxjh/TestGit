package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

// ==================== 用户行为数据模型 ====================

@Component
@TableName("user_behavior")
public class UserBehavior {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String roomId;
    private String behaviorType;
    private String searchKeyword;
    private Integer clickPosition;
    private Double recommendScore;
    private Timestamp timestamp;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}

