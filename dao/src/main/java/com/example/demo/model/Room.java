
package com.example.demo.model;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.example.demo.dao.EquipmentDao;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Room实体类，增加设备相关字段
 * 修复基本类型与null比较的问题
 */
@Component
@TableName("room")
public class Room {
  @TableId(value = "room_id", type = IdType.AUTO)
  private String roomId;

  // 修改为包装类型，支持null值
  private Long category;
  private String roomName;
  private String starRating;
  private String roomImage; // 修正字段名称（原来是RoomImage）
  private String roomContent;
  private Double roomPrice; // 修改为Double包装类型
  private java.sql.Timestamp gmtCreate;
  private java.sql.Timestamp gmtModify;
  private Long roomStatus; // 修改为Long包装类型

  // 新增：设备相关字段（这些是临时字段，不存在于数据库中）
  @TableField(exist = false)  // 标记为非数据库字段
  private Equipment equipment;    // 关联的设备信息

  @TableField(exist = false)
  private String equipmentName;   // 设备名称（用于列表显示）

  @TableField(exist = false)
  private String categoryName;    // 设备类别名称（用于列表显示）

  @TableField(exist = false)
  private Integer quantity;       // 设备数量

  @TableField(exist = false)
  private Boolean isFree;         // 是否免费

  // 新增：支持多个设备
  @TableField(exist = false)
  private List<EquipmentDao.RoomEquipmentDetail> equipmentDetails; // 设备详情列表

  @TableField(exist = false)
  private String equipmentNames;  // 所有设备名称的拼接字符串（用于列表显示）

  // 推荐系统相关字段
  @TableField(exist = false)
  private Double similarity; // 相似度分数（用于推荐排序）

  @TableField(exist = false)
  private Double recommendScore; // 推荐分数

  // ==================== Getter和Setter方法 ====================

  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public Long getCategory() {
    return category;
  }

  public void setCategory(Long category) {
    this.category = category;
  }

  // 为了兼容原有代码，提供long类型的setter
  public void setCategory(long category) {
    this.category = (long) category;
  }

  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public String getStarRating() {
    return starRating;
  }

  public void setStarRating(String starRating) {
    this.starRating = starRating;
  }

  public String getRoomImage() {
    return roomImage;
  }

  public void setRoomImage(String roomImage) {
    this.roomImage = roomImage;
  }

  public String getRoomContent() {
    return roomContent;
  }

  public void setRoomContent(String roomContent) {
    this.roomContent = roomContent;
  }

  public Double getRoomPrice() {
    return roomPrice;
  }

  public void setRoomPrice(Double roomPrice) {
    this.roomPrice = roomPrice;
  }

  // 为了兼容原有代码，提供double类型的setter
  public void setRoomPrice(double roomPrice) {
    this.roomPrice = roomPrice;
  }

  public java.sql.Timestamp getGmtCreate() {
    return gmtCreate;
  }

  public void setGmtCreate(java.sql.Timestamp gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  public java.sql.Timestamp getGmtModify() {
    return gmtModify;
  }

  public void setGmtModify(java.sql.Timestamp gmtModify) {
    this.gmtModify = gmtModify;
  }

  public Long getRoomStatus() {
    return roomStatus;
  }

  public void setRoomStatus(Long roomStatus) {
    this.roomStatus = roomStatus;
  }

  // 为了兼容原有代码，提供long类型的setter
  public void setRoomStatus(long roomStatus) {
    this.roomStatus = roomStatus;
  }

  // ==================== 设备相关的Getter和Setter ====================

  public Equipment getEquipment() {
    return equipment;
  }

  public void setEquipment(Equipment equipment) {
    this.equipment = equipment;
  }

  public String getEquipmentName() {
    return equipmentName;
  }

  public void setEquipmentName(String equipmentName) {
    this.equipmentName = equipmentName;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public Boolean getIsFree() {
    return isFree;
  }

  public void setIsFree(Boolean isFree) {
    this.isFree = isFree;
  }

  public List<EquipmentDao.RoomEquipmentDetail> getEquipmentDetails() {
    return equipmentDetails;
  }

  public void setEquipmentDetails(List<EquipmentDao.RoomEquipmentDetail> equipmentDetails) {
    this.equipmentDetails = equipmentDetails;
  }

  public String getEquipmentNames() {
    return equipmentNames;
  }

  public void setEquipmentNames(String equipmentNames) {
    this.equipmentNames = equipmentNames;
  }

  public Boolean getFree() {
    return isFree;
  }

  public void setFree(Boolean free) {
    isFree = free;
  }

  // ==================== 推荐系统相关的Getter和Setter ====================

  public Double getSimilarity() {
    return similarity;
  }

  public void setSimilarity(Double similarity) {
    this.similarity = similarity;
  }

  public Double getRecommendScore() {
    return recommendScore;
  }

  public void setRecommendScore(Double recommendScore) {
    this.recommendScore = recommendScore;
  }

  // ==================== 辅助方法 ====================

  /**
   * 判断是否为特殊房间
   */
  public boolean isSpecialRoom() {
    return category != null && category == 0;
  }

  /**
   * 判断房间是否可用
   */
  public boolean isAvailable() {
    return roomStatus != null && roomStatus == 1;
  }

  /**
   * 获取价格等级（1-5）
   */
  public int getPriceLevel() {
    if (roomPrice == null) return 3;
    if (roomPrice < 200) return 1;
    if (roomPrice < 400) return 2;
    if (roomPrice < 600) return 3;
    if (roomPrice < 800) return 4;
    return 5;
  }

  /**
   * 获取格式化的价格字符串
   */
  public String getFormattedPrice() {
    return roomPrice != null ? String.format("¥%.2f", roomPrice) : "价格面议";
  }

  /**
   * 判断是否有设备信息
   */
  public boolean hasEquipment() {
    return equipmentNames != null && !equipmentNames.trim().isEmpty();
  }

  @Override
  public String toString() {
    return "Room{" +
            "roomId='" + roomId + '\'' +
            ", category=" + category +
            ", roomName='" + roomName + '\'' +
            ", starRating='" + starRating + '\'' +
            ", roomPrice=" + roomPrice +
            ", roomStatus=" + roomStatus +
            '}';
  }
}