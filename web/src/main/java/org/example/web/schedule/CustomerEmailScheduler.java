package org.example.web.schedule;

import com.example.demo.model.Customer;
import com.example.demo.service.CustomerService;
import com.example.demo.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CustomerEmailScheduler {

    private static final Logger log = LoggerFactory.getLogger(CustomerEmailScheduler.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmailService emailService;

    private static final String TARGET_EMAIL = "18370814757@139.com";

    /**
     * 每1分钟执行一次
     * cron表达式: 秒 分 时 日 月 周
     * "0 0/1 * * * ?" 表示每分钟的第0秒执行
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void scanAndSendCustomerInfo() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("========== 开始扫描客户信息 {} ==========", currentTime);

        try {
            // 获取状态为1的客户（正常状态）
            List<Customer> customers = customerService.getAllCustomersByStatus(1);

            if (customers == null || customers.isEmpty()) {
                log.info("没有找到客户数据");
                return;
            }

            // 只取前5条数据
            List<Customer> topFiveCustomers = customers.size() > 5
                    ? customers.subList(0, 5)
                    : customers;

            // 构建邮件内容
            StringBuilder emailContent = new StringBuilder();
            emailContent.append("客户信息报告 - ").append(currentTime).append("\n\n");
            emailContent.append("以下是前5条客户信息：\n\n");

            // 打印到控制台并构建邮件内容
            for (int i = 0; i < topFiveCustomers.size(); i++) {
                Customer customer = topFiveCustomers.get(i);
                String customerInfo = formatCustomerInfo(customer, i + 1);

                // 打印到控制台
                System.out.println(customerInfo);
                log.info("客户信息: {}", customerInfo);

                // 添加到邮件内容
                emailContent.append(customerInfo).append("\n\n");
            }

            // 发送邮件
            sendCustomerReport(emailContent.toString());

            log.info("成功扫描并发送 {} 条客户信息", topFiveCustomers.size());

        } catch (Exception e) {
            log.error("扫描客户信息时发生错误", e);
        }
    }

    /**
     * 格式化客户信息
     */
    private String formatCustomerInfo(Customer customer, int index) {
        return String.format("【客户 %d】\n" +
                        "ID: %d\n" +
                        "姓名: %s\n" +
                        "电话: %s\n" +
                        "邮箱: %s\n" +
                        "性别: %s\n" +
                        "等级: %d\n" +
                        "状态: %s\n" +
                        "创建时间: %s",
                index,
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getCustomerNumber(),
                customer.getCustomerMailbox(),
                getGenderText(customer.getCustomerSex()),
                customer.getCustomerLevel(),
                getStatusText(customer.getCustomerStatus()),
                customer.getGmtCreate() != null ? customer.getGmtCreate().toString() : "未知"
        );
    }

    /**
     * 获取性别文本
     */
    private String getGenderText(long sex) {
        switch ((int) sex) {
            case 0: return "未知";
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(long status) {
        return status == 1 ? "正常" : "禁用";
    }

    /**
     * 发送客户报告邮件
     */
    private void sendCustomerReport(String content) {
        try {
            // 创建一个新的邮件发送方法
            emailService.sendCustomerReport(TARGET_EMAIL, content);
            log.info("客户信息报告已发送到: {}", TARGET_EMAIL);
        } catch (Exception e) {
            log.error("发送邮件失败", e);
        }
    }
}