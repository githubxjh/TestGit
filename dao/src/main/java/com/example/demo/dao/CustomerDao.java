package com.example.demo.dao;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.CustomerMapper;
import com.example.demo.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class CustomerDao {

    @Autowired
    CustomerMapper customerMapper;

    public List<Customer> getAllcustomersByStatus(Integer status) {
        QueryWrapper<Customer> customerQueryWrapper = new QueryWrapper<>();
        customerQueryWrapper.eq("customer_status", status);  // 查找 等级为2
        return customerMapper.selectList(customerQueryWrapper);
    }

    public Integer offlineCustomer(Long customerId) {
        QueryWrapper<Customer> customerQueryWrapper = new QueryWrapper<>();
        customerQueryWrapper.eq("customer_id", customerId);
        Customer customer = customerMapper.selectOne(customerQueryWrapper);
        if (customer == null) {
            System.out.println("Customer with id " + customerId + " not found.");
            return 0;
        }
        customer.setCustomerStatus(0);
        Timestamp currentTimestamp = Timestamp.valueOf(DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
        customer.setGmtModify(currentTimestamp);
        int result = customerMapper.update(customer, customerQueryWrapper);
        System.out.println("Rows affected: " + result);
        return result; // 只调用一次更新操作
    }

    public Customer getCustomerById(Long customerId) {
        QueryWrapper<Customer> customerQueryWrapper = new QueryWrapper<>();
        customerQueryWrapper.eq("customer_id", customerId);
        return customerMapper.selectOne(customerQueryWrapper);
    }
    public void updateCustomer(Customer customer) {
        customerMapper.updateById(customer);
    }


    public List<Customer> getAllCustomersByStatusAndConditions(Integer status, String datemin, String datemax, String searchKeyword) {
        QueryWrapper<Customer> customerQueryWrapper = new QueryWrapper<>();
        customerQueryWrapper.eq("customer_status", status);

        if (datemin != null && !datemin.isEmpty()) {
            customerQueryWrapper.ge("gmt_create", datemin);
        }
        if (datemax != null && !datemax.isEmpty()) {
            customerQueryWrapper.le("gmt_create", datemax);
        }
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            // 分割搜索关键词
            String[] keywords = searchKeyword.trim().split("\\s+");

            // 对每个关键词应用搜索条件
            for (String keyword : keywords) {
                String finalKeyword = keyword.trim();  // 创建新变量
                if (!finalKeyword.isEmpty()) {
                    customerQueryWrapper.and(wrapper -> wrapper
                            .like("customer_id", finalKeyword)
                            .or()
                            .like("customer_name", finalKeyword)
                            .or()
                            .like("customer_number", finalKeyword)
                            .or()
                            .like("customer_mailbox", finalKeyword));
                }
            }
        }

        return customerMapper.selectList(customerQueryWrapper);
    }
    public void addCustomer(Customer customer) {
        customerMapper.insert(customer);
    }
}

