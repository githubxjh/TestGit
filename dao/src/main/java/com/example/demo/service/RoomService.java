package com.example.demo.service;

import com.example.demo.dao.RoomDao;
import com.example.demo.dao.EquipmentDao;
import com.example.demo.dao.EquipmentDao.RoomEquipmentDetail;
import com.example.demo.model.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 房间服务类
 * 负责房间相关的业务逻辑处理，包括获取房间信息、设备信息等
 */
@Service
public class RoomService {
    @Autowired
    private RoomDao roomDao;

    @Autowired
    private EquipmentDao equipmentDao;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ROOM_LIST_KEY = "room:list:status:";
    private static final String ROOM_DETAIL_KEY = "room:detail:";
    private static final long CACHE_EXPIRATION = 30; // 缓存过期时间（分钟）

    /**
     * 获取指定状态的所有房间（包含设备信息）
     * @param status 房间状态（1:活跃，0:下架）
     * @return 房间列表
     */
    public List<Room> getAllRoomsByStatus(Integer status) {
        String cacheKey = ROOM_LIST_KEY + status;

        // 首先尝试从Redis获取数据
        @SuppressWarnings("unchecked")
        List<Room> cachedRooms = (List<Room>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedRooms != null) {
            return cachedRooms;
        }

        // 如果缓存中没有，则从数据库获取
        List<Room> rooms = roomDao.getAllRoomsByStatus(status);

        // 为每个房间加载设备信息
        for (Room room : rooms) {
            try {
                // 获取房间对应的所有设备详细信息
                List<RoomEquipmentDetail> details = equipmentDao.getAllRoomEquipmentDetails(room.getRoomId());

                if (details != null && !details.isEmpty()) {
                    room.setEquipmentDetails(details);

                    // 拼接所有设备名称用于列表显示
                    String equipmentNames = details.stream()
                            .map(d -> d.getEquipment().getEquipmentName())
                            .collect(Collectors.joining(", "));
                    room.setEquipmentNames(equipmentNames);

                    // 为了兼容旧代码，保留第一个设备的信息
                    RoomEquipmentDetail firstDetail = details.get(0);
                    room.setEquipment(firstDetail.getEquipment());
                    room.setEquipmentName(firstDetail.getEquipment().getEquipmentName());
                    if (firstDetail.getCategory() != null) {
                        room.setCategoryName(firstDetail.getCategory().getCategoryName());
                    }
                    room.setQuantity(firstDetail.getQuantity());
                    room.setIsFree(firstDetail.getIsFree());
                }
            } catch (Exception e) {
                // 如果获取设备信息失败，记录日志但不影响房间列表的显示
                System.err.println("获取房间 " + room.getRoomId() + " 的设备信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 存入缓存并设置过期时间
        redisTemplate.opsForValue().set(cacheKey, rooms, CACHE_EXPIRATION, TimeUnit.MINUTES);

        return rooms;
    }

    /**
     * 根据条件搜索房间（包含设备信息）
     */
    public List<Room> getAllRoomsByStatusAndConditions(Integer status, String datemin, String datemax,
                                                       String searchKeyword, Long category) {
        // 由于参数多且动态，此方法不使用缓存
        List<Room> rooms = roomDao.getAllRoomsByStatusAndConditions(status, datemin, datemax, searchKeyword, category);

        // 为搜索结果添加设备信息
        for (Room room : rooms) {
            try {
                List<RoomEquipmentDetail> details = equipmentDao.getAllRoomEquipmentDetails(room.getRoomId());

                if (details != null && !details.isEmpty()) {
                    room.setEquipmentDetails(details);

                    // 拼接所有设备名称
                    String equipmentNames = details.stream()
                            .map(d -> d.getEquipment().getEquipmentName())
                            .collect(Collectors.joining(", "));
                    room.setEquipmentNames(equipmentNames);

                    // 兼容旧代码
                    RoomEquipmentDetail firstDetail = details.get(0);
                    room.setEquipment(firstDetail.getEquipment());
                    room.setEquipmentName(firstDetail.getEquipment().getEquipmentName());
                    if (firstDetail.getCategory() != null) {
                        room.setCategoryName(firstDetail.getCategory().getCategoryName());
                    }
                    room.setQuantity(firstDetail.getQuantity());
                    room.setIsFree(firstDetail.getIsFree());
                }
            } catch (Exception e) {
                System.err.println("获取房间 " + room.getRoomId() + " 的设备信息失败: " + e.getMessage());
            }
        }

        return rooms;
    }

    /**
     * 根据ID获取房间详情（包含完整的设备信息）
     */
    public Room getRoomById(String roomId) {
        String cacheKey = ROOM_DETAIL_KEY + roomId;

        // 尝试从Redis获取
        Room cachedRoom = (Room) redisTemplate.opsForValue().get(cacheKey);

        if (cachedRoom != null) {
            return cachedRoom;
        }

        // 如果缓存中没有，则从数据库获取
        Room room = roomDao.getRoomById(roomId);

        // 获取设备详细信息
        if (room != null) {
            try {
                List<RoomEquipmentDetail> details = equipmentDao.getAllRoomEquipmentDetails(roomId);

                if (details != null && !details.isEmpty()) {
                    room.setEquipmentDetails(details);

                    // 拼接所有设备名称
                    String equipmentNames = details.stream()
                            .map(d -> d.getEquipment().getEquipmentName())
                            .collect(Collectors.joining(", "));
                    room.setEquipmentNames(equipmentNames);

                    // 兼容旧代码
                    RoomEquipmentDetail firstDetail = details.get(0);
                    room.setEquipment(firstDetail.getEquipment());
                    room.setEquipmentName(firstDetail.getEquipment().getEquipmentName());
                    if (firstDetail.getCategory() != null) {
                        room.setCategoryName(firstDetail.getCategory().getCategoryName());
                    }
                    room.setQuantity(firstDetail.getQuantity());
                    room.setIsFree(firstDetail.getIsFree());
                }
            } catch (Exception e) {
                System.err.println("获取房间 " + roomId + " 的设备信息失败: " + e.getMessage());
            }

            // 存入缓存并设置过期时间
            redisTemplate.opsForValue().set(cacheKey, room, CACHE_EXPIRATION, TimeUnit.MINUTES);
        }

        return room;
    }

    // 其他方法保持不变...
    @Transactional
    public Integer offlineRoom(String roomId) {
        Integer result = roomDao.offlineRoom(roomId);

        if (result > 0) {
            // 清除列表缓存（活跃和非活跃状态都清除）
            redisTemplate.delete(ROOM_LIST_KEY + "0");
            redisTemplate.delete(ROOM_LIST_KEY + "1");

            // 清除详情缓存
            redisTemplate.delete(ROOM_DETAIL_KEY + roomId);
        }

        return result;
    }

    @Transactional
    public Integer deleteRoom(String roomId) {
        Room room = roomDao.getRoomById(roomId);
        if (room != null) {
            Integer result = roomDao.deleteRoom(roomId);

            if (result > 0) {
                String listCacheKey = ROOM_LIST_KEY + room.getRoomStatus();
                redisTemplate.delete(listCacheKey);

                String detailCacheKey = ROOM_DETAIL_KEY + roomId;
                redisTemplate.delete(detailCacheKey);
            }

            return result;
        }

        return 0;
    }

    @Transactional
    public void updateRoom(Room room) {
        roomDao.updateRoom(room);

        String detailCacheKey = ROOM_DETAIL_KEY + room.getRoomId();
        String listCacheKey = ROOM_LIST_KEY + room.getRoomStatus();

        redisTemplate.delete(detailCacheKey);
        redisTemplate.delete(listCacheKey);
    }

    @Transactional
    public void addRoom(Room room) {
        roomDao.addRoom(room);

        String listCacheKey = ROOM_LIST_KEY + room.getRoomStatus();
        redisTemplate.delete(listCacheKey);
    }

    public void refreshRoomCache(Integer status) {
        List<Room> rooms = roomDao.getAllRoomsByStatus(status);

        String cacheKey = ROOM_LIST_KEY + status;
        redisTemplate.opsForValue().set(cacheKey, rooms, CACHE_EXPIRATION, TimeUnit.MINUTES);
    }
}