package com.example.demo.service;

import com.example.demo.dao.CustomerDao;
import com.example.demo.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerDao customerDao;
    public List<Customer> getAllCustomersByStatus(Integer status){
        return customerDao.getAllcustomersByStatus(status);
    }
    public Integer offlineCustomer(Long customerid){
        return customerDao.offlineCustomer(customerid);
    }
    public Customer getCustomerById(Long customerId) {
        return customerDao.getCustomerById(customerId);}
    @Transactional
    public void updateCustomer(Customer customer) {
        customerDao.updateCustomer(customer);
    }
    @Transactional
    public List<Customer> getAllCustomersByStatusAndConditions(Integer status, String datemin, String datemax, String searchKeyword) {
        return customerDao.getAllCustomersByStatusAndConditions(status, datemin, datemax, searchKeyword);
    }
    @Transactional
    public void addCustomer(Customer customer) {
        customerDao.addCustomer(customer);
    }
}
