package com.example.demo.service;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.*;
import com.example.demo.model.Customer;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.CustomerMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class RegistrationService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * 发送验证码
     */
    public boolean sendVerificationCode(String email, String phoneNumber) {
        try {
            // 检查手机号是否已注册
            QueryWrapper<Customer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("customer_number", phoneNumber);
            Customer existingCustomer = customerMapper.selectOne(queryWrapper);

            if (existingCustomer != null) {
                throw new RuntimeException("该手机号已注册");
            }

            // 检查邮箱是否已注册
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("customer_mailbox", email);
            existingCustomer = customerMapper.selectOne(queryWrapper);

            if (existingCustomer != null) {
                throw new RuntimeException("该邮箱已注册");
            }

            // 生成6位验证码
            String code = generateVerificationCode();

            // 添加日志检查验证码长度
            System.out.println("生成的验证码: " + code + ", 长度: " + code.length());

            // 存储到Redis，有效期5分钟
            String key = "verification:code:" + phoneNumber;
            stringRedisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

            // 存储邮箱信息，用于验证时校验
            String emailKey = "verification:email:" + phoneNumber;
            stringRedisTemplate.opsForValue().set(emailKey, email, 5, TimeUnit.MINUTES);

            // 发送消息到RabbitMQ队列
            EmailVerificationMessage message = new EmailVerificationMessage();
            message.setEmail(email);
            message.setCode(code);
            message.setPhoneNumber(phoneNumber);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.USER_EXCHANGE,
                    RabbitMQConfig.EMAIL_VERIFICATION_ROUTING_KEY,
                    message
            );

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 注册用户
     */
    public boolean register(RegistrationConfirmDTO request) {
        try {
            // 验证验证码
            String key = "verification:code:" + request.getPhoneNumber();
            String storedCode = stringRedisTemplate.opsForValue().get(key);

            if (storedCode == null || !storedCode.equals(request.getVerificationCode())) {
                throw new RuntimeException("验证码错误或已过期");
            }

            // 验证邮箱是否一致
            String emailKey = "verification:email:" + request.getPhoneNumber();
            String storedEmail = stringRedisTemplate.opsForValue().get(emailKey);

            if (storedEmail == null || !storedEmail.equals(request.getEmail())) {
                throw new RuntimeException("邮箱不匹配");
            }

            // 再次检查手机号是否已注册（防止并发）
            QueryWrapper<Customer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("customer_number", request.getPhoneNumber());
            Customer existingCustomer = customerMapper.selectOne(queryWrapper);

            if (existingCustomer != null) {
                throw new RuntimeException("该手机号已注册");
            }

            // 删除验证码
            stringRedisTemplate.delete(key);
            stringRedisTemplate.delete(emailKey);

            // 发送注册消息到RabbitMQ队列
            RegistrationMessage message = new RegistrationMessage();
            message.setPhoneNumber(request.getPhoneNumber());
            message.setPassword(request.getPassword());
            message.setEmail(request.getEmail());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.USER_EXCHANGE,
                    RabbitMQConfig.REGISTRATION_ROUTING_KEY,
                    message
            );

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 生成6位随机验证码（改进版本）
     */
    private String generateVerificationCode() {
        // 方法1：使用Math.random()
        int code = (int) ((Math.random() * 9 + 1) * 100000);  // 生成100000-999999之间的数

        System.out.println("生成的验证码: " + code );
        return String.valueOf(code);


        // 方法2：使用Random确保6位
        // Random random = new Random();
        // int code = random.nextInt(900000) + 100000;  // 生成100000-999999之间的数
        // return String.valueOf(code);

        // 方法3：逐位生成
        // StringBuilder code = new StringBuilder();
        // Random random = new Random();
        // code.append(random.nextInt(9) + 1);  // 第一位不能是0
        // for (int i = 0; i < 5; i++) {
        //     code.append(random.nextInt(10));
        // }
        // return code.toString();
    }
}