package org.example.web.controller;

import com.example.demo.dto.*;
import com.example.demo.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FrontregisterController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * 发送验证码
     */
    @PostMapping("/register/send-code")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestBody VerificationCodeRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 简单验证
            if (request.getPhoneNumber() == null || request.getPhoneNumber().length() != 11) {
                response.put("success", false);
                response.put("message", "手机号格式不正确");
                return ResponseEntity.ok(response);
            }

            if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.put("success", false);
                response.put("message", "邮箱格式不正确");
                return ResponseEntity.ok(response);
            }

            boolean result = registrationService.sendVerificationCode(request.getEmail(), request.getPhoneNumber());
            if (result) {
                response.put("success", true);
                response.put("message", "验证码已发送到邮箱");
            } else {
                response.put("success", false);
                response.put("message", "验证码发送失败");
            }
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "系统错误，请稍后再试");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 提交注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegistrationConfirmDTO request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 简单验证
            if (request.getPhoneNumber() == null || request.getPhoneNumber().length() != 11) {
                response.put("success", false);
                response.put("message", "手机号格式不正确");
                return ResponseEntity.ok(response);
            }

            if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.put("success", false);
                response.put("message", "邮箱格式不正确");
                return ResponseEntity.ok(response);
            }

            if (request.getPassword() == null || request.getPassword().length() < 6) {
                response.put("success", false);
                response.put("message", "密码至少6位");
                return ResponseEntity.ok(response);
            }

            if (request.getVerificationCode() == null || request.getVerificationCode().length() != 6) {
                response.put("success", false);
                response.put("message", "验证码格式不正确");
                return ResponseEntity.ok(response);
            }

            boolean result = registrationService.register(request);
            if (result) {
                response.put("success", true);
                response.put("message", "注册成功");
            } else {
                response.put("success", false);
                response.put("message", "注册失败");
            }
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "系统错误，请稍后再试");
        }

        return ResponseEntity.ok(response);
    }
}
