package com.example.demo.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.EquipmentMapper;
import com.example.demo.mapper.RoomEquipmentMapper;
import com.example.demo.mapper.EquipmentCategoryMapper;
import com.example.demo.model.Equipment;
import com.example.demo.model.RoomEquipment;
import com.example.demo.model.EquipmentCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

/**
 * 设备数据访问对象
 * 处理设备相关的数据库操作
 */
@Service
public class EquipmentDao {
    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private RoomEquipmentMapper roomEquipmentMapper;

    @Autowired
    private EquipmentCategoryMapper equipmentCategoryMapper;

    /**
     * 根据房间ID获取房间设备关联信息
     * @param roomId 房间ID
     * @return 房间设备关联信息
     */
    public RoomEquipment getRoomEquipmentByRoomId(String roomId) {
        QueryWrapper<RoomEquipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", roomId);
        return roomEquipmentMapper.selectOne(queryWrapper);
    }

    /**
     * 根据房间ID获取所有相关设备
     * @param roomId 房间ID
     * @return 设备列表
     */
    public List<RoomEquipment> getRoomEquipmentsByRoomId(String roomId) {
        QueryWrapper<RoomEquipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", roomId);
        return roomEquipmentMapper.selectList(queryWrapper);
    }

    /**
     * 根据房间ID获取设备详细信息（包括设备信息和分类信息）
     * @param roomId 房间ID
     * @return 设备信息
     */
    public Equipment getEquipmentById(String roomId) {
        RoomEquipment roomEquipment = getRoomEquipmentByRoomId(roomId);
        if (roomEquipment == null) {
            return null;
        }

        Equipment equipment = equipmentMapper.selectById(roomEquipment.getEquipmentId());
        return equipment;
    }

    /**
     * 获取设备类别信息
     * @param categoryId 类别ID
     * @return 类别信息
     */
    public EquipmentCategory getEquipmentCategory(Integer categoryId) {
        return equipmentCategoryMapper.selectById(categoryId);
    }

    /**
     * 获取房间的设备详细信息，包括数量和是否免费
     * @param roomId 房间ID
     * @return 包含完整信息的对象
     */
    public RoomEquipmentDetail getRoomEquipmentDetail(String roomId) {
        RoomEquipment roomEquipment = getRoomEquipmentByRoomId(roomId);
        if (roomEquipment == null) {
            return null;
        }

        Equipment equipment = equipmentMapper.selectById(roomEquipment.getEquipmentId());
        if (equipment == null) {
            return null;
        }

        EquipmentCategory category = null;
        if (equipment.getCategoryId() != null) {
            category = equipmentCategoryMapper.selectById(equipment.getCategoryId());
        }

        RoomEquipmentDetail detail = new RoomEquipmentDetail();
        detail.setEquipment(equipment);
        detail.setCategory(category);
        detail.setQuantity(roomEquipment.getQuantity());
        detail.setIsFree(roomEquipment.getFree());

        return detail;
    }

    /**
     * 获取房间的所有设备详细信息（支持多个设备）
     * @param roomId 房间ID
     * @return 设备详情列表
     */
    public List<RoomEquipmentDetail> getAllRoomEquipmentDetails(String roomId) {
        List<RoomEquipment> roomEquipments = getRoomEquipmentsByRoomId(roomId);
        List<RoomEquipmentDetail> details = new ArrayList<>();

        for (RoomEquipment roomEquipment : roomEquipments) {
            Equipment equipment = equipmentMapper.selectById(roomEquipment.getEquipmentId());
            if (equipment != null) {
                EquipmentCategory category = null;
                if (equipment.getCategoryId() != null) {
                    category = equipmentCategoryMapper.selectById(equipment.getCategoryId());
                }

                RoomEquipmentDetail detail = new RoomEquipmentDetail();
                detail.setEquipment(equipment);
                detail.setCategory(category);
                detail.setQuantity(roomEquipment.getQuantity());
                detail.setIsFree(roomEquipment.getFree());

                details.add(detail);
            }
        }

        return details;
    }

    /**
     * 内部类：用于返回房间设备的完整信息
     */
    public static class RoomEquipmentDetail {
        private Equipment equipment;
        private EquipmentCategory category;
        private Integer quantity;
        private Boolean isFree;

        // getter和setter
        public Equipment getEquipment() {
            return equipment;
        }

        public void setEquipment(Equipment equipment) {
            this.equipment = equipment;
        }

        public EquipmentCategory getCategory() {
            return category;
        }

        public void setCategory(EquipmentCategory category) {
            this.category = category;
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
    }
}