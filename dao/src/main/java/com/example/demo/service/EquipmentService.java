package com.example.demo.service;

import com.example.demo.dao.EquipmentDao;
import com.example.demo.model.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {
    @Autowired
    private EquipmentDao equipmentDao;

    public Equipment getRoomEquipments(String roomId) {
        return equipmentDao.getEquipmentById(roomId);
    }
}