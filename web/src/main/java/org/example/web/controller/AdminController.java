package org.example.web.controller;

import com.example.demo.model.Admin;
import com.example.demo.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员列表页面
     */
    @GetMapping("/list")
    public String list() {
        return "admin-list";
    }

    /**
     * 获取管理员列表数据
     */
    @GetMapping("/listData")
    @ResponseBody
    public Map<String, Object> listData(@RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        @RequestParam(required = false) String adminName) {
        List<Admin> adminList = adminService.findAdminList(startDate, endDate, adminName);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "");
        result.put("count", adminList.size());
        result.put("data", adminList);
        return result;
    }

    /**
     * 添加管理员页面
     */
    @GetMapping("/add")
    public String add() {
        return "admin-add";
    }

    /**
     * 保存管理员
     */
    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> save(@RequestBody Admin admin) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 设置创建时间
            admin.setGmtCreate(new Timestamp(System.currentTimeMillis()));
            // 默认启用状态
            admin.setAdminStatus(1);
            adminService.saveAdmin(admin);
            result.put("code", 0);
            result.put("msg", "添加成功");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "添加失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 编辑管理员
     */
    @GetMapping("/edit/{id}")
    public String edit() {
        return "admin-add";
    }

    /**
     * 获取管理员详情
     */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public Map<String, Object> detail(@PathVariable("id") Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Admin admin = adminService.getAdminById(id);
            result.put("code", 0);
            result.put("msg", "");
            result.put("data", admin);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "获取数据失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新管理员
     */
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> update(@RequestBody Admin admin) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.updateAdmin(admin);
            result.put("code", 0);
            result.put("msg", "更新成功");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除管理员
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable("id") Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.deleteAdmin(id);
            result.put("code", 0);
            result.put("msg", "删除成功");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 启用/停用管理员
     */
    @PostMapping("/changeStatus")
    @ResponseBody
    public Map<String, Object> changeStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.changeAdminStatus(id, status);
            result.put("code", 0);
            result.put("msg", status == 1 ? "启用成功" : "停用成功");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "操作失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 修改密码页面
     */
    @GetMapping("/password")
    public String password() {
        return "admin-password-edit";
    }

    /**
     * 更新密码
     */
    @PostMapping("/updatePassword")
    @ResponseBody
    public Map<String, Object> updatePassword(@RequestParam("id") Long id,
                                              @RequestParam("oldPassword") String oldPassword,
                                              @RequestParam("newPassword") String newPassword) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = adminService.updatePassword(id, oldPassword, newPassword);
            if (success) {
                result.put("code", 0);
                result.put("msg", "密码修改成功");
            } else {
                result.put("code", 1);
                result.put("msg", "原密码不正确");
            }
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "修改失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 注册处理 - 自动锁定账号
     */
    @PostMapping("/register")
    @ResponseBody
    public Map<String, Object> register(@RequestBody Admin admin) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查用户名是否已存在
            if (adminService.checkAdminNameExists(admin.getAdminName())) {
                result.put("code", 1);
                result.put("msg", "用户名已存在");
                return result;
            }

            // 注册用户（会自动设置为锁定状态并发送验证码）
            adminService.registerAdmin(admin);

            result.put("code", 0);
            result.put("msg", "注册成功，账号已锁定，请查收邮件获取解锁验证码");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "注册失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 发送解锁验证码
     */
    @PostMapping("/sendUnlockCode")
    @ResponseBody
    public Map<String, Object> sendUnlockCode(@RequestParam String adminName) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.sendUnlockCode(adminName);
            result.put("code", 0);
            result.put("msg", "验证码已发送到您的邮箱");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "发送失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 验证解锁码
     */
    @PostMapping("/unlock")
    @ResponseBody
    public Map<String, Object> unlock(@RequestParam String adminName, @RequestParam String code) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = adminService.unlockAccount(adminName, code);
            if (success) {
                result.put("code", 0);
                result.put("msg", "账号解锁成功");
            } else {
                result.put("code", 1);
                result.put("msg", "验证码错误或已过期");
            }
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "解锁失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 锁定页面
     */
    @GetMapping("/locked")
    public String lockedPage() {
        return "locked";
    }

    /**
     * 登录处理 - 检查账号状态
     */
    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> login(@RequestParam("adminName") String adminName,
                                     @RequestParam("adminPassword") String adminPassword,
                                     HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证用户名和密码
            Admin admin = adminService.login(adminName, adminPassword);
            if (admin != null) {
                // 检查账号状态
                if (admin.getAdminStatus() == 0) {
                    // 账号锁定，跳转到锁定页面
                    result.put("code", 2);  // 特殊代码表示账号锁定
                    result.put("msg", "账号已锁定，请先解锁");
                    result.put("redirectUrl", "/admin/locked?adminName=" + adminName);
                } else {
                    // 登录成功，将管理员信息存入session
                    session.setAttribute("admin", admin);
                    result.put("code", 0);
                    result.put("msg", "登录成功");
                }
            } else {
                result.put("code", 1);
                result.put("msg", "用户名或密码错误");
            }
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "登录失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @ResponseBody
    public Map<String, Object> logout(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 清除session
            session.invalidate();
            result.put("code", 0);
            result.put("msg", "退出成功");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "退出失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 首页跳转（添加登录验证）
     */
    @GetMapping("/index")
    public String index(HttpSession session) {
        // 检查是否已登录
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        return "index";
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/getCurrentAdmin")
    @ResponseBody
    public Map<String, Object> getCurrentAdmin(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin != null) {
                result.put("code", 0);
                result.put("msg", "");
                result.put("data", admin);
            } else {
                result.put("code", 1);
                result.put("msg", "未登录");
            }
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "获取用户信息失败: " + e.getMessage());
        }
        return result;
    }
}