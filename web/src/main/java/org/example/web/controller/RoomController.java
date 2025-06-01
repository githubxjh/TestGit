package org.example.web.controller;

import com.example.demo.model.Room;
import com.example.demo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * 房间控制器
 * 处理所有与房间相关的HTTP请求
 */
@Controller
public class RoomController {
    @Autowired
    private RoomService roomService;

    /**
     * 显示房间列表页面
     * 此方法会加载所有活跃的房间，包括设备信息
     */
    @GetMapping("/room-brand")
    public String getAllRooms(Model model) {
        // 获取所有活跃状态（status=1）的房间列表，已包含设备信息
        List<Room> roomList = roomService.getAllRoomsByStatus(1);
        model.addAttribute("roomlist", roomList);
        model.addAttribute("total", roomList.size());
        return "room-brand";
    }

    /**
     * 搜索房间API
     * 返回符合条件的房间列表，包含设备信息
     */
    @GetMapping("/room-search")
    @ResponseBody
    public Map<String, Object> searchRooms(
            @RequestParam(value = "datemin", required = false) String datemin,
            @RequestParam(value = "datemax", required = false) String datemax,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "category", required = false) Long category) {

        // 根据条件搜索房间，已包含设备信息
        List<Room> roomList = roomService.getAllRoomsByStatusAndConditions(1, datemin, datemax, searchKeyword, category);

        // 构建返回数据，确保包含设备信息
        List<Map<String, Object>> simplifiedList = new ArrayList<>();
        for (Room room : roomList) {
            Map<String, Object> roomMap = new HashMap<>();
            // 基础房间信息
            roomMap.put("roomId", room.getRoomId());
            roomMap.put("category", room.getCategory());
            roomMap.put("roomName", room.getRoomName());
            roomMap.put("starRating", room.getStarRating());
            roomMap.put("roomImage", room.getRoomImage());
            roomMap.put("roomContent", room.getRoomContent());
            roomMap.put("roomPrice", room.getRoomPrice());
            roomMap.put("gmtCreate", room.getGmtCreate());
            roomMap.put("gmtModify", room.getGmtModify());

            // 新增：设备信息
            roomMap.put("equipmentNames", room.getEquipmentNames());  // 所有设备名称
            roomMap.put("equipmentName", room.getEquipmentName());   // 保留兼容
            roomMap.put("categoryName", room.getCategoryName());
            roomMap.put("quantity", room.getQuantity());
            roomMap.put("isFree", room.getIsFree());

            simplifiedList.add(roomMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("roomlist", simplifiedList);
        response.put("total", roomList.size());
        return response;
    }

    /**
     * 显示房间详情页面（包含所有设备详细信息）
     */
    @GetMapping("/room-detail/{roomid}")
    public String getRoomDetail(@PathVariable("roomid") String roomid, Model model) {
        Room room = roomService.getRoomById(roomid);
        model.addAttribute("room", room);
        return "room-detail";
    }

    /**
     * 显示下架页面
     * 包含房间和设备的详细信息
     */
    @GetMapping("/offlineroom/{roomid}")
    public String getOfflineRoomById(@PathVariable("roomid") String roomid, Model model) {
        // getRoomById方法已经包含了设备信息的获取
        Room room = roomService.getRoomById(roomid);
        model.addAttribute("room", room);
        return "offlineroom";
    }

    /**
     * 显示编辑页面
     * 包含房间和设备的详细信息
     */
    @GetMapping("/editroom/{roomid}")
    public String editRoomById(@PathVariable("roomid") String roomid, Model model) {
        // getRoomById方法已经包含了设备信息的获取
        Room room = roomService.getRoomById(roomid);
        model.addAttribute("room", room);
        return "editroom";
    }

    // 其他方法保持不变...
    @GetMapping("/room-refresh")
    @ResponseBody
    public Map<String, Object> refreshRoomCache() {
        roomService.refreshRoomCache(1);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "房间缓存已成功刷新");
        return response;
    }

    @PostMapping("/updateRoom")
    public String updateRoom(
            @RequestParam("roomId") String roomId,
            @RequestParam("category") Long category,
            @RequestParam("roomName") String roomName,
            @RequestParam("starRating") String starRating,
            @RequestParam(value = "roomImage", required = false) MultipartFile roomImage,
            @RequestParam("roomContent") String roomContent,
            @RequestParam("roomPrice") Double roomPrice,
            @RequestParam("roomStatus") Long roomStatus) {

        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            return "redirect:/error";
        }

        room.setCategory(category);
        room.setRoomName(roomName);
        room.setStarRating(starRating);
        room.setRoomContent(roomContent);
        room.setRoomPrice(roomPrice);
        room.setGmtModify(new Timestamp(System.currentTimeMillis()));

        if (roomImage != null && !roomImage.isEmpty()) {
            String newImagePath = saveImage(roomImage);
            if (newImagePath != null) {
                room.setRoomImage(newImagePath);
            }
        }

        roomService.updateRoom(room);
        return "redirect:/success";
    }

    @GetMapping("/offlineroom/offline_confirm")
    public String offlineConfirm(@RequestParam("roomid") String roomid) {
        System.out.println("offline_confirm:" + roomid);
        int result = roomService.offlineRoom(roomid);
        System.out.println("Update result: " + result);
        if (result == 1) {
            return "redirect:/success";
        } else {
            return "redirect:/error";
        }
    }

    @GetMapping("/room-add")
    public String getRoomAddPage() {
        return "room-add";
    }

    @PostMapping("/addRoom")
    @ResponseBody
    public Map<String, Object> addRoom(
            @RequestParam("category") Long category,
            @RequestParam("roomName") String roomName,
            @RequestParam("starRating") String starRating,
            @RequestParam(value = "roomImage", required = false) MultipartFile roomImage,
            @RequestParam("roomContent") String roomContent,
            @RequestParam("roomPrice") Double roomPrice) {

        Map<String, Object> response = new HashMap<>();

        try {
            Room room = new Room();
            room.setCategory(category);
            room.setRoomName(roomName);
            room.setStarRating(starRating);
            room.setRoomContent(roomContent);
            room.setRoomPrice(roomPrice);

            if (roomImage != null && !roomImage.isEmpty()) {
                String imagePath = saveImage(roomImage);
                room.setRoomImage(imagePath);
            } else {
                room.setRoomImage("/static/images/default-room.jpg");
            }

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            room.setGmtCreate(currentTime);
            room.setGmtModify(currentTime);
            room.setRoomStatus(1);

            roomService.addRoom(room);

            response.put("code", 0);
            response.put("msg", "添加房间成功");
        } catch (Exception e) {
            response.put("code", 1);
            response.put("msg", "添加房间失败: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/deleteRoom")
    @ResponseBody
    public Map<String, Object> deleteRoom(@RequestParam("roomid") String roomid) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = roomService.deleteRoom(roomid);
            if (result > 0) {
                response.put("code", 0);
                response.put("msg", "删除成功");
            } else {
                response.put("code", 1);
                response.put("msg", "删除失败，房间不存在");
            }
        } catch (Exception e) {
            response.put("code", 1);
            response.put("msg", "删除失败: " + e.getMessage());
        }
        return response;
    }

    private String saveImage(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String newFilename = UUID.randomUUID().toString() +
                    originalFilename.substring(originalFilename.lastIndexOf("."));

            String uploadDir = "src/main/resources/static/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File dest = new File(dir.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(dest);

            return "/static/uploads/" + newFilename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}