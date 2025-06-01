package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    /**
     * 发送注册验证码邮件
     */
    public void sendRegistrationCode(String to, String code) {
        log.info("准备发送验证码邮件 - 收件人: {}, 验证码: {}, 验证码长度: {}",
                to, code, code != null ? code.length() : 0);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("用户注册验证码");
        message.setText("您好！\n\n" +
                "您正在进行用户注册操作，验证码是：" + code + "\n\n" +
                "验证码有效期为5分钟，请尽快完成注册。\n\n" +
                "如果这不是您的操作，请忽略此邮件。\n\n" +
                "此邮件由系统自动发送，请勿回复。");

        mailSender.send(message);
        log.info("验证码邮件发送完成");
    }

    /**
     * 发送注册成功邮件
     */
    public void sendRegistrationSuccess(String to, String phoneNumber) {
        log.info("准备发送注册成功邮件 - 收件人: {}, 手机号: {}", to, phoneNumber);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("注册成功通知");
        message.setText("恭喜您！\n\n" +
                "您的账号（手机号：" + phoneNumber + "）已成功注册。\n\n" +
                "现在您可以使用手机号和密码登录系统。\n\n" +
                "感谢您的注册！\n\n" +
                "此邮件由系统自动发送，请勿回复。");

        mailSender.send(message);
        log.info("注册成功邮件发送完成");
    }

    /**
     * 发送客户信息报告
     */
    public void sendCustomerReport(String to, String content) {
        log.info("准备发送客户信息报告 - 收件人: {}", to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("客户信息定时报告");
        message.setText(content);
        mailSender.send(message);
        log.info("客户信息报告发送完成");
    }

    /**
     * 发送解锁邮件（保留原有方法）
     */
    public void sendUnlockEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("账号解锁验证码");
        message.setText("您的账号已成功注册，当前处于锁定状态。\n\n" +
                "您的解锁验证码是：" + code + "\n\n" +
                "验证码有效期为5分钟，请尽快使用。\n\n" +
                "如果这不是您的操作，请忽略此邮件。");

        mailSender.send(message);
    }
}