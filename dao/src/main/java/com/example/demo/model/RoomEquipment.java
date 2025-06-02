package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import org.springframework.stereotype.Component;

@Component
@TableName("room_equipment")
public class RoomEquipment {
    // 在 MyBatis-Plus 中，对于复合主键，你需要在一个字段上使用 @TableId
    // 在另一个字段上使用 @TableField，或者实现 KeySequence
    @TableId(value = "room_id")
    private String roomId;

    @TableField("equipment_id")
    private Long equipmentId;

    private Integer quantity;
    private Boolean isFree;

    // getter 和 setter 方法

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getFree() {
        return isFree;
    }

    public void setFree(Boolean free) {
        isFree = free;
    }
}