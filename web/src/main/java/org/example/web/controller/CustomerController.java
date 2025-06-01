package org.example.web.controller;

import com.example.demo.model.Customer;
import com.example.demo.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/customer-list")
    public String getAllCustomer(Model model,
                                 @RequestParam(value = "datemin", required = false) String datemin,
                                 @RequestParam(value = "datemax", required = false) String datemax,
                                 @RequestParam(value = "searchKeyword", required = false) String searchKeyword) {
        List<Customer> customerList = customerService.getAllCustomersByStatusAndConditions(1, datemin, datemax, searchKeyword);
        model.addAttribute("customerlist", customerList);
        model.addAttribute("total", customerList.size());
        return "customer-list";
    }


    @GetMapping("/offlinecustomer/{customerid}")
    public String getOfflineCarById(@PathVariable("customerid") Long customerid, Model model) {

        Customer customer = customerService.getCustomerById(customerid);
        model.addAttribute("customer", customer);
        return "offlinecustomer";
    }

    @GetMapping("/editcustomer/{customerid}")
    public String editCustomerById(@PathVariable("customerid")Long customerid,Model model){
        Customer customer= customerService.getCustomerById(customerid);
        model.addAttribute("customer",customer);
        return "editcustomer";
    }
    @PostMapping("/updateCustomer")
    public String updateUser(@RequestParam("customerId") Long customerId,
                             @RequestParam("customerName") String customerName,
                             @RequestParam("customerPassword") String customerPassword,
                             @RequestParam("customerSex") Long customerSex,
                             @RequestParam("customerNumber") String customerNumber,
                             @RequestParam("customerMailbox") String customerMailbox,
                             @RequestParam("customerLevel") Long customerLevel,
                             @RequestParam("gmtCreate") String gmtCreate,
                             @RequestParam("gmtModify") String gmtModify,
                             @RequestParam("customerStatus") Long customerStatus) {
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            return "redirect:/error";
        }

        customer.setCustomerName(customerName);
        customer.setCustomerSex(customerSex);
        customer.setCustomerNumber(customerNumber);
        customer.setCustomerMailbox(customerMailbox);
        customer.setCustomerLevel(customerLevel);
        customer.setGmtModify(new Timestamp(System.currentTimeMillis()));

        customerService.updateCustomer(customer);
        return "redirect:/success";
    }
    @GetMapping("/offlinecustomer/offline_confirm")
    public String offlineConfirm(@RequestParam("customerid") Long customerid) {
        System.out.println("offline_confirm:" + customerid);
        int result = customerService.offlineCustomer(customerid);
        System.out.println("Update result: " + result); // 添加日志
        if (result == 1) {
            return "redirect:/success";
        } else {
            return "redirect:/error";
        }
    }
    @GetMapping("/error")
    public String handleError() {
        return "error"; // 返回一个名为 "error" 的视图
    }
    @GetMapping("/success")
    public String closePage() {
        return "success";
    }
    @GetMapping("/customer-search")
    @ResponseBody
    public Map<String, Object> searchCustomers(
            @RequestParam(value = "datemin", required = false) String datemin,
            @RequestParam(value = "datemax", required = false) String datemax,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword) {
        List<Customer> customerList = customerService.getAllCustomersByStatusAndConditions(1, datemin, datemax, searchKeyword);

        // 如果需要简化返回的数据，可以创建一个轻量级DTO对象
        List<Map<String, Object>> simplifiedList = new ArrayList<>();
        for (Customer customer : customerList) {
            Map<String, Object> customerMap = new HashMap<>();
            customerMap.put("customerId", customer.getCustomerId());
            customerMap.put("customerName", customer.getCustomerName());
            customerMap.put("customerPassword", customer.getCustomerPassword());
            customerMap.put("customerSex", customer.getCustomerSex());
            customerMap.put("customerNumber", customer.getCustomerNumber());
            customerMap.put("customerMailbox", customer.getCustomerMailbox());
            customerMap.put("customerLevel", customer.getCustomerLevel());
            customerMap.put("gmtCreate", customer.getGmtCreate());
            customerMap.put("gmtModify", customer.getGmtModify());
            // 可以选择不包含不必要的大字段

            simplifiedList.add(customerMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("customerlist", simplifiedList);
        response.put("total", customerList.size());
        return response;
    }

    // 如需要，还可以添加分页功能
    @GetMapping("/customer-search-paged")
    @ResponseBody
    public Map<String, Object> searchCustomersPaged(
            @RequestParam(value = "datemin", required = false) String datemin,
            @RequestParam(value = "datemax", required = false) String datemax,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        List<Customer> allCustomers = customerService.getAllCustomersByStatusAndConditions(1, datemin, datemax, searchKeyword);

        // 手动分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, allCustomers.size());
        List<Customer> pagedCustomers = allCustomers.subList(start, end);

        // 转换为简化的数据结构
        List<Map<String, Object>> simplifiedList = new ArrayList<>();
        for (Customer customer : pagedCustomers) {
            Map<String, Object> customerMap = new HashMap<>();
            // 填充数据...
            simplifiedList.add(customerMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("customerlist", simplifiedList);
        response.put("total", allCustomers.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) allCustomers.size() / size));

        return response;
    }
    @GetMapping("/customer-add")
    public String getCustomerAddPage() {
        return "customer-add";
    }

    @PostMapping("/addCustomer")
    @ResponseBody
    public Map<String, Object> addCustomer(
            @RequestParam("customerName") String customerName,
            @RequestParam("customerPassword") String customerPassword,
            @RequestParam("customerSex") Long customerSex,
            @RequestParam("customerNumber") String customerNumber,
            @RequestParam("customerMailbox") String customerMailbox,
            @RequestParam("customerLevel") Long customerLevel) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 创建新的Customer对象
            Customer customer = new Customer();
            customer.setCustomerName(customerName);
            customer.setCustomerPassword(customerPassword);
            customer.setCustomerSex(customerSex);
            customer.setCustomerNumber(customerNumber);
            customer.setCustomerMailbox(customerMailbox);
            customer.setCustomerLevel(customerLevel);

            // 设置创建时间和修改时间
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            customer.setGmtCreate(currentTime);
            customer.setGmtModify(currentTime);

            // 设置用户状态为活跃(1)
            customer.setCustomerStatus(1L);

            // 保存用户
            customerService.addCustomer(customer);

            response.put("code", 0);
            response.put("msg", "添加用户成功");
        } catch (Exception e) {
            response.put("code", 1);
            response.put("msg", "添加用户失败: " + e.getMessage());
            // 记录完整的堆栈跟踪以进行调试
            e.printStackTrace();
        }

        return response;
    }}














