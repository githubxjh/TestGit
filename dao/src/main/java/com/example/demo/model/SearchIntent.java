package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

// ==================== 搜索意图类 ====================
public class SearchIntent {
    private String originalQuery;
    private Integer priceHint;
    private List<String> facilities = new ArrayList<>();
    private String roomType;
    private String location;

    // getters and setters
    public String getOriginalQuery() { return originalQuery; }
    public void setOriginalQuery(String originalQuery) { this.originalQuery = originalQuery; }

    public Integer getPriceHint() { return priceHint; }
    public void setPriceHint(Integer priceHint) { this.priceHint = priceHint; }

    public List<String> getFacilities() { return facilities; }
    public void setFacilities(List<String> facilities) { this.facilities = facilities; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}