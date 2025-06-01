package com.example.demo.consumer;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.*;
import com.example.demo.model.Customer;
import com.example.demo.service.CustomerService;
import com.example.demo.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class RegistrationConsumer {

    private static final Logger log = LoggerFactory.getLogger(RegistrationConsumer.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private CustomerService customerService;

    /**
     * 处理邮件验证码发送
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_VERIFICATION_QUEUE)
    public void handleEmailVerification(EmailVerificationMessage message) {
        log.info("处理邮件验证码发送：{}", message);

        try {
            emailService.sendRegistrationCode(message.getEmail(), message.getCode());
            log.info("验证码发送成功：{}", message.getEmail());
        } catch (Exception e) {
            log.error("验证码发送失败", e);
        }
    }

    /**
     * 处理用户注册
     */
    @RabbitListener(queues = RabbitMQConfig.REGISTRATION_QUEUE)
    public void handleRegistration(RegistrationMessage message) {
        log.info("处理用户注册：{}", message);

        try {
            // 创建新用户
            Customer customer = new Customer();
            customer.setCustomerNumber(message.getPhoneNumber());
            customer.setCustomerPassword(message.getPassword());
            customer.setCustomerMailbox(message.getEmail());

            // 设置默认值
            customer.setCustomerName("用户" + message.getPhoneNumber().substring(7));
            customer.setCustomerSex(0);  // 默认未知
            customer.setCustomerLevel(1);  // 默认等级1
            customer.setCustomerStatus(1);  // 默认正常状态
            customer.setGmtCreate(new Timestamp(System.currentTimeMillis()));
            customer.setGmtModify(new Timestamp(System.currentTimeMillis()));

            // 保存到数据库
            customerService.addCustomer(customer);
            log.info("用户注册成功：{}", message.getPhoneNumber());

            // 发送注册成功邮件
            emailService.sendRegistrationSuccess(message.getEmail(), message.getPhoneNumber());

        } catch (Exception e) {
            log.error("用户注册失败", e);
        }
    }
}