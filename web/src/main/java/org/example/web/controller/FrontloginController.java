package org.example.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.CustomerMapper;
import com.example.demo.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class FrontloginController {

    @Autowired
    private CustomerMapper customerMapper;

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginMessage) {
        Map<String, Object> response = new HashMap<>();
        String username = loginMessage.get("username");
        String password = loginMessage.get("password");

        System.out.println("收到登录请求: username=" + username + ", password=" + password);

        try {
            // 使用用户名（电话号码）查询用户
            QueryWrapper<Customer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("customer_number", username); // 使用电话号码登录
            Customer customer = customerMapper.selectOne(queryWrapper);

            // 如果找不到，尝试用用户名查找
            if (customer == null) {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("customer_name", username);
                customer = customerMapper.selectOne(queryWrapper);
            }

            // 验证用户和密码
            if (customer != null && customer.getCustomerPassword().equals(password)) {
                // 检查用户状态（假设1为正常状态）
                if (customer.getCustomerStatus() != 1) {
                    response.put("success", false);
                    response.put("message", "账号已被禁用");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                // 登录成功
                response.put("success", true);
                response.put("message", "登录成功");

                // 生成token
                String token = UUID.randomUUID().toString();
                response.put("token", token);

                // 返回用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("customerId", customer.getCustomerId());
                userInfo.put("customerName", customer.getCustomerName());
                userInfo.put("customerNumber", customer.getCustomerNumber());
                userInfo.put("customerLevel", customer.getCustomerLevel());
                response.put("userInfo", userInfo);

                System.out.println("用户登录成功: " + customer.getCustomerName());
                System.out.println("生成的token: " + token);

                return ResponseEntity.ok(response);
            } else {
                // 登录失败
                response.put("success", false);
                response.put("message", "用户名或密码错误");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            System.err.println("登录处理出错: " + e.getMessage());
            response.put("success", false);
            response.put("message", "登录失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        if (token != null && !token.isEmpty()) {
            response.put("valid", true);
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}