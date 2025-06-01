package com.example.demo.dao;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.RoomMapper;
import com.example.demo.model.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class RoomDao {
    @Autowired
    RoomMapper roomMapper;

    public List<Room> getAllRoomsByStatus(Integer status) {
        QueryWrapper<Room> roomQueryWrapper = new QueryWrapper<>();
        roomQueryWrapper.eq("room_status", status);
        return roomMapper.selectList(roomQueryWrapper);
    }

    public List<Room> getAllRoomsByStatusAndConditions(Integer status, String datemin, String datemax, String searchKeyword, Long category) {
        QueryWrapper<Room> roomQueryWrapper = new QueryWrapper<>();
        roomQueryWrapper.eq("room_status", status);

        if (datemin != null && !datemin.isEmpty()) {
            roomQueryWrapper.ge("gmt_create", datemin);
        }
        if (datemax != null && !datemax.isEmpty()) {
            roomQueryWrapper.le("gmt_create", datemax);
        }
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            roomQueryWrapper.and(wrapper -> wrapper
                    .like("room_id", searchKeyword)
                    .or()
                    .like("room_name", searchKeyword)
                    .or()
                    .like("room_content", searchKeyword));
        }
        if (category != null) {
            roomQueryWrapper.eq("category", category);
        }

        return roomMapper.selectList(roomQueryWrapper);
    }

    // 修改getRoomById方法
    public Room getRoomById(String roomId) {
        QueryWrapper<Room> roomQueryWrapper = new QueryWrapper<>();
        roomQueryWrapper.eq("room_id", roomId);
        return roomMapper.selectOne(roomQueryWrapper);
    }

    // 修改offlineRoom方法
    public Integer offlineRoom(String roomId) {
        QueryWrapper<Room> roomQueryWrapper = new QueryWrapper<>();
        roomQueryWrapper.eq("room_id", roomId);
        Room room = roomMapper.selectOne(roomQueryWrapper);
        if (room == null) {
            System.out.println("Room with id " + roomId + " not found.");
            return 0;
        }
        room.setRoomStatus(0);
        Timestamp currentTimestamp = Timestamp.valueOf(DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
        room.setGmtModify(currentTimestamp);
        int result = roomMapper.update(room, roomQueryWrapper);
        System.out.println("Rows affected: " + result);
        return result;
    }

    public void updateRoom(Room room) {
        // 使用update方法而不是updateById，指定更新条件
        QueryWrapper<Room> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", room.getRoomId());
        roomMapper.update(room, queryWrapper);

        // 如果依然想使用updateById，确保主键正确设置
        // 并且Room类和RoomMapper都正确配置
        // roomMapper.updateById(room);
    }

    public void addRoom(Room room) {
        roomMapper.insert(room);
    }

    public Integer deleteRoom(String roomId) {
        return roomMapper.deleteById(roomId);
    }
}