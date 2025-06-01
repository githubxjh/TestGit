package com.example.demo.dto;

public class RoomRecommendationDTO {
    private String roomId;
    private Double score;
    private Double similarity;
    private String reason;

    public RoomRecommendationDTO() {}

    public RoomRecommendationDTO(String roomId, Double score, Double similarity, String reason) {
        this.roomId = roomId;
        this.score = score;
        this.similarity = similarity;
        this.reason = reason;
    }

    // getters and setters...
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Double getSimilarity() { return similarity; }
    public void setSimilarity(Double similarity) { this.similarity = similarity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}