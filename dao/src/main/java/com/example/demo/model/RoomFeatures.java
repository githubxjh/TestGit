package com.example.demo.model;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
@TableName("room_features")
public class RoomFeatures {
    @TableId(value = "room_id")
    private String roomId;
    private String featureVector; // JSON格式存储特征向量
    private String tags; // 房间标签
    private Double longitude; // 经度
    private Double latitude; // 纬度
    private Integer priceLevel; // 价格等级 1-5
    private Integer starLevel; // 星级

    public Double[] getFeatureVectorArray() {
        try {
            return JSON.parseObject(featureVector, Double[].class);
        } catch (Exception e) {
            return new Double[0];
        }
    }

    public List<String> getTagsList() {
        try {
            return JSON.parseArray(tags, String.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getFeatureVector() {
        return featureVector;
    }

    public void setFeatureVector(String featureVector) {
        this.featureVector = featureVector;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(Integer priceLevel) {
        this.priceLevel = priceLevel;
    }

    public Integer getStarLevel() {
        return starLevel;
    }

    public void setStarLevel(Integer starLevel) {
        this.starLevel = starLevel;
    }
}
