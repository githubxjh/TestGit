package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.dao.AdminDao;
import com.example.demo.mapper.AdminMapper;
import com.example.demo.model.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class AdminService {
    @Autowired(required = false)
    private AdminMapper adminMapper;

    @Autowired(required = false)
    private AdminDao adminDao;

    // 添加EmailService和RedisService的注入
    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisService redisService;

    /**
     * 根据条件查询管理员列表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param adminName 管理员名称
     * @return 管理员列表
     */
    public List<Admin> findAdminList(String startDate, String endDate, String adminName) {
        try {
            Timestamp startTimestamp = null;
            Timestamp endTimestamp = null;

            if (!StringUtils.isEmpty(startDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date start = sdf.parse(startDate);
                startTimestamp = new Timestamp(start.getTime());
            }

            if (!StringUtils.isEmpty(endDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date end = sdf.parse(endDate);
                endTimestamp = new Timestamp(end.getTime() + 24 * 60 * 60 * 1000 - 1); // 设置为当天结束时间
            }

            if (adminMapper != null) {
                return adminMapper.findAdminList(startTimestamp, endTimestamp, adminName);
            } else if (adminDao != null) {
                // 如果 adminMapper 不可用但 adminDao 可用，使用 adminDao 进行查询
                // 这里使用 MyBatis-Plus 的 QueryWrapper 构建查询条件
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Admin> queryWrapper =
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

                if (startTimestamp != null) {
                    queryWrapper.ge("gmt_create", startTimestamp);
                }
                if (endTimestamp != null) {
                    queryWrapper.le("gmt_create", endTimestamp);
                }
                if (!StringUtils.isEmpty(adminName)) {
                    queryWrapper.like("admin_name", adminName);
                }
                queryWrapper.orderByDesc("admin_id");

                return adminDao.selectList(queryWrapper);
            } else {
                throw new RuntimeException("数据访问组件未正确配置");
            }
        } catch (Exception e) {
            throw new RuntimeException("查询管理员列表失败", e);
        }
    }

    /**
     * 获取管理员详情
     *
     * @param id 管理员ID
     * @return 管理员对象
     */
    public Admin getAdminById(Long id) {
        if (adminMapper != null) {
            return adminMapper.getAdminById(id);
        } else if (adminDao != null) {
            return adminDao.selectById(id);
        } else {
            throw new RuntimeException("数据访问组件未正确配置");
        }
    }

    /**
     * 保存管理员
     *
     * @param admin 管理员对象
     */
    @Transactional
    public void saveAdmin(Admin admin) {
        if (adminMapper != null) {
            adminMapper.insertAdmin(admin);
        } else if (adminDao != null) {
            adminDao.insert(admin);
        } else {
            throw new RuntimeException("数据访问组件未正确配置");
        }
    }

    /**
     * 更新管理员信息
     *
     * @param admin 管理员对象
     */
    @Transactional
    public void updateAdmin(Admin admin) {
        if (adminMapper != null) {
            adminMapper.updateAdmin(admin);
        } else if (adminDao != null) {
            adminDao.updateById(admin);
        } else {
            throw new RuntimeException("数据访问组件未正确配置");
        }
    }

    /**
     * 删除管理员
     *
     * @param id 管理员ID
     */
    @Transactional
    public void deleteAdmin(Long id) {
        if (adminMapper != null) {
            adminMapper.deleteAdmin(id);
        } else if (adminDao != null) {
            adminDao.deleteById(id);
        } else {
            throw new RuntimeException("数据访问组件未正确配置");
        }
    }

    /**
     * 修改管理员状态（启用/禁用）
     *
     * @param id 管理员ID
     * @param status 状态值（1:启用, 0:禁用）
     */
    @Transactional
    public void changeAdminStatus(Long id, Integer status) {
        if (adminMapper != null) {
            adminMapper.updateAdminStatus(id, status);
        } else if (adminDao != null) {
            Admin admin = adminDao.selectById(id);
            if (admin != null) {
                admin.setAdminStatus(status.longValue());
                adminDao.updateById(admin);
            }
        } else {
            throw new RuntimeException("数据访问组件未正确配置");
        }
    }

    /**
     * 更新密码
     *
     * @param id 管理员ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    @Transactional
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        // 在实际应用中，应该从数据库获取管理员信息并验证旧密码
        // 这里简化处理，假设成功

        // 1. 加密新密码
        String encryptedPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());

        // 2. 更新密码
        if (adminMapper != null) {
            adminMapper.updatePassword(id, encryptedPassword);
        } else if (adminDao != null) {
            // 由于 AdminDao 是 MyBatis-Plus 的扩展，不直接支持更新密码的方法
            // 所以我们需要先获取管理员信息，然后设置新密码，再更新
            Admin admin = adminDao.selectById(id);
            if (admin != null) {
                // 这里假设 Admin 实体有 adminPassword 字段
                // 如果没有，需要修改 Admin 实体类，添加此字段
                // admin.setAdminPassword(encryptedPassword);
                adminDao.updateById(admin);
            }
        } else {
            throw new RuntimeException("数据访问组件未正确配置");
        }

        return true;
    }
    /**
     * 管理员登录验证
     *
     * @param adminName 用户名
     * @param adminPassword 密码
     * @return 管理员对象或null（登录失败）
     */
    public Admin login(String adminName, String adminPassword) {
        try {
            System.out.println("尝试登录：用户名=" + adminName + ", 密码=" + adminPassword);

            if (adminMapper != null) {
                // 使用自定义SQL查询
                Admin admin = adminMapper.findByAdminName(adminName);
                if (admin != null) {
                    System.out.println("找到用户：" + admin.getAdminName());
                    System.out.println("数据库中的密码：" + admin.getAdminPassword());
                    System.out.println("输入密码：" + adminPassword);

                    // 直接比较明文密码
                    if (admin.getAdminPassword().equals(adminPassword)) {
                        System.out.println("密码验证成功！");
                        return admin;
                    } else {
                        System.out.println("密码验证失败！");
                    }
                } else {
                    System.out.println("未找到用户：" + adminName);
                }
            } else if (adminDao != null) {
                // 类似的修改...
            }

            return null;
        } catch (Exception e) {
            // 异常处理...
        }
        return null;
    }
    /**
     * 检查用户名是否已存在
     *
     * @param adminName 用户名
     * @return 是否存在
     */
    public boolean checkAdminNameExists(String adminName) {
        try {
            if (adminMapper != null) {
                Admin admin = adminMapper.findByAdminName(adminName);
                return admin != null;
            } else if (adminDao != null) {
                QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("admin_name", adminName);
                return adminDao.selectCount(queryWrapper) > 0;
            }

            throw new RuntimeException("数据访问组件未正确配置");
        } catch (Exception e) {
            throw new RuntimeException("检查用户名失败", e);
        }
    }

    // 在AdminService.java中修改注册方法
    /**
     * 注册管理员 - 注册后默认锁定状态
     *
     * @param admin 管理员对象
     */
    @Transactional
    public void registerAdmin(Admin admin) {
        try {
            // 对密码进行加密
            String encryptedPassword = DigestUtils.md5DigestAsHex(admin.getAdminPassword().getBytes());
            admin.setAdminPassword(encryptedPassword);

            // 设置创建时间
            admin.setGmtCreate(new Timestamp(System.currentTimeMillis()));

            // 重要：设置状态为锁定（0表示锁定）
            admin.setAdminStatus(0);  // 修改这里，原来是1

            // 设置默认管理员等级
            if (admin.getAdminLevel() == 0) {
                admin.setAdminLevel(3);
            }

            // 保存管理员信息
            if (adminMapper != null) {
                adminMapper.insertAdmin(admin);
            } else if (adminDao != null) {
                adminDao.insert(admin);
            } else {
                throw new RuntimeException("数据访问组件未正确配置");
            }

            // 注册成功后发送验证码邮件
            sendUnlockCode(admin.getAdminName());

        } catch (Exception e) {
            throw new RuntimeException("注册管理员失败", e);
        }
    }

    /**
     * 发送解锁验证码
     *
     * @param adminName 管理员名
     */
    public void sendUnlockCode(String adminName) {
        try {
            Admin admin = findByAdminName(adminName);
            if (admin == null) {
                throw new RuntimeException("用户不存在");
            }

            // 生成6位随机验证码
            String code = String.format("%06d", new Random().nextInt(999999));

            // 将验证码存入Redis，5分钟有效期
            redisService.set("unlock_code_" + adminName, code, 300);

            // 发送邮件
            emailService.sendUnlockEmail(admin.getAdminMailbox(), code);

        } catch (Exception e) {
            throw new RuntimeException("发送验证码失败", e);
        }
    }

    /**
     * 验证解锁码并解锁账号
     *
     * @param adminName 管理员名
     * @param code 验证码
     * @return 是否解锁成功
     */
    public boolean unlockAccount(String adminName, String code) {
        try {
            // 从Redis获取验证码
            String savedCode = redisService.get("unlock_code_" + adminName);

            if (savedCode == null || !savedCode.equals(code)) {
                return false;
            }

            // 验证成功，更新状态为正常
            Admin admin = findByAdminName(adminName);
            if (admin != null) {
                admin.setAdminStatus(1);  // 1表示正常状态
                if (adminMapper != null) {
                    adminMapper.updateAdminStatus(admin.getAdminId(), 1);
                } else if (adminDao != null) {
                    adminDao.updateById(admin);
                }

                // 删除Redis中的验证码
                redisService.delete("unlock_code_" + adminName);

                return true;
            }

            return false;
        } catch (Exception e) {
            throw new RuntimeException("解锁账号失败", e);
        }
    }

    /**
     * 根据用户名查找管理员
     */
    private Admin findByAdminName(String adminName) {
        if (adminMapper != null) {
            return adminMapper.findByAdminName(adminName);
        } else if (adminDao != null) {
            QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("admin_name", adminName);
            return adminDao.selectOne(queryWrapper);
        }
        return null;
    }
}