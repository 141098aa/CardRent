// UserAuthService.java
package com.example.service;

import com.example.entity.User;
import com.example.entity.UserRealNameAuth;
import com.example.entity.UserDriverLicenseAuth;
import com.example.entity.dto.AuthAuditDTO;
import com.example.exception.CustomException;
import com.example.mapper.UserAuthMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAuthService {

    @Resource
    private UserAuthMapper userAuthMapper;

    @Resource
    private UserMapper userMapper;
    @Resource
    private MessageService messageService;

    // 认证状态常量
    public static final Integer STATUS_PENDING = 0;  // 待审核
    public static final Integer STATUS_VERIFIED = 1; // 已认证
    public static final Integer STATUS_FAILED = 2;   // 审核失败

    // 认证类型常量
    public static final String AUTH_TYPE_REAL_NAME = "real_name";
    public static final String AUTH_TYPE_DRIVER_LICENSE = "driver_license";

    /**
     * 分页查询所有认证
     */
    public PageInfo<AuthAuditDTO> selectPage(Integer pageNum, Integer pageSize,
                                             Integer status, String authType) {
        PageHelper.startPage(pageNum, pageSize);
        List<AuthAuditDTO> list = userAuthMapper.selectAllAuthPage(status, authType);

        // 可选：打印查询结果数量，用于调试
        System.out.println("查询到 " + list.size() + " 条认证记录，authType=" + authType + ", status=" + status);

        return PageInfo.of(list);
    }

    /**
     * 分页查询实名认证
     */
    public PageInfo<UserRealNameAuth> selectRealNamePage(Integer pageNum, Integer pageSize,
                                                         Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserRealNameAuth> list = userAuthMapper.selectRealNamePage(status);
        return PageInfo.of(list);
    }

    /**
     * 分页查询驾驶证认证
     */
    public PageInfo<UserDriverLicenseAuth> selectDriverLicensePage(Integer pageNum, Integer pageSize,
                                                                   Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserDriverLicenseAuth> list = userAuthMapper.selectDriverLicensePage(status);
        return PageInfo.of(list);
    }

    /**
     * 查询实名认证详情
     */
    public UserRealNameAuth selectRealNameById(Integer id) {
        return userAuthMapper.selectRealNameById(id);
    }

    /**
     * 查询驾驶证认证详情
     */
    public UserDriverLicenseAuth selectDriverLicenseById(Integer id) {
        return userAuthMapper.selectDriverLicenseById(id);
    }

    /**
     * 审核实名认证
     */
    @Transactional
    public void auditRealName(Integer id, Integer status, String remark) {
        // 参数校验
        if (status == null || (status != STATUS_VERIFIED && status != STATUS_FAILED)) {
            throw new CustomException("审核状态不正确");
        }

        // 查询认证信息
        UserRealNameAuth auth = userAuthMapper.selectRealNameById(id);
        if (auth == null) {
            throw new CustomException("认证记录不存在");
        }

/*        if (auth.getStatus() != STATUS_PENDING) {
            throw new CustomException("该记录已被审核，请勿重复操作");
        }*/

        // 更新认证表状态
        LocalDateTime now = LocalDateTime.now();
        userAuthMapper.updateRealNameStatus(id, status, remark, now);

        // 更新用户表中的认证状态
        userAuthMapper.updateUserAuthStatus(auth.getUserId(), AUTH_TYPE_REAL_NAME, status);
        // 发送站内信通知
        if (status == STATUS_VERIFIED) {
            messageService.sendMessage(
                    auth.getUserId(),
                    "实名认证通过",
                    "恭喜您，实名认证已通过！",
                    "system",
                    "/front/person"
            );
        } else if (status == STATUS_FAILED) {
            String failReason = remark != null ? remark : "信息有误，请重新上传清晰照片";
            messageService.sendMessage(
                    auth.getUserId(),
                    "实名认证失败",
                    String.format("您的实名认证未通过，原因：%s。请修改后重新提交。", failReason),
                    "system",
                    "/front/person?tab=auth&highlight=realName"
            );
        }
    }

    /**
     * 审核驾驶证认证
     */
    @Transactional
    public void auditDriverLicense(Integer id, Integer status, String remark) {
        // 参数校验
        if (status == null || (status != STATUS_VERIFIED && status != STATUS_FAILED)) {
            throw new CustomException("审核状态不正确");
        }

        // 查询认证信息
        UserDriverLicenseAuth auth = userAuthMapper.selectDriverLicenseById(id);
        if (auth == null) {
            throw new CustomException("认证记录不存在");
        }

/*        if (auth.getStatus() != STATUS_PENDING) {
            throw new CustomException("该记录已被审核，请勿重复操作");
        }*/

        // 校验驾驶证有效期
        if (status == STATUS_VERIFIED) {
            if (auth.getValidEnd() != null && auth.getValidEnd().isBefore(java.time.LocalDate.now())) {
                throw new CustomException("驾驶证已过期，无法通过审核");
            }
        }

        // 更新认证表状态
        LocalDateTime now = LocalDateTime.now();
        userAuthMapper.updateDriverLicenseStatus(id, status, remark, now);

        // 更新用户表中的认证状态
        userAuthMapper.updateUserAuthStatus(auth.getUserId(), AUTH_TYPE_DRIVER_LICENSE, status);
        // 发送站内信通知
        if (status == STATUS_VERIFIED) {
            messageService.sendMessage(
                    auth.getUserId(),
                    "驾驶证认证通过",
                    "恭喜您，驾驶证认证已通过！",
                    "system",
                    "/front/person"
            );
        } else if (status == STATUS_FAILED) {
            String failReason = remark != null ? remark : "照片不清晰或信息不符，请重新上传";
            messageService.sendMessage(
                    auth.getUserId(),
                    "驾驶证认证失败",
                    String.format("您的驾驶证认证未通过，原因：%s。请修改后重新提交。", failReason),
                    "system",
                    "/front/person?tab=auth&highlight=driverLicense"
            );
        }
    }

    /**
     * 批量审核
     */
    @Transactional
    public void batchAudit(List<Integer> ids, String authType, Integer status, String remark) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("请选择要审核的记录");
        }

        for (Integer id : ids) {
            if (AUTH_TYPE_REAL_NAME.equals(authType)) {
                auditRealName(id, status, remark);
            } else if (AUTH_TYPE_DRIVER_LICENSE.equals(authType)) {
                auditDriverLicense(id, status, remark);
            }
        }
    }

    // ========== 用户端方法 ==========

    /**
     * 用户提交实名认证（新增或更新）
     */
    @Transactional
    public void submitRealName(UserRealNameAuth auth, String phone) {
        // 检查是否已存在
        UserRealNameAuth existing = userAuthMapper.selectRealNameByUserId(auth.getUserId());

        if (existing != null) {
            // 更新现有记录
            auth.setId(existing.getId());
            userAuthMapper.updateRealNameAuth(auth, phone);
        } else {
            // 新增记录
            userAuthMapper.insertRealNameAuth(auth, phone);
        }
        // 2. 如果有手机号更新，更新用户表
        if (phone != null) {
            userAuthMapper.updateUserPhone(auth.getUserId(), phone);
        }
        // 可选：更新 user 表中的状态字段
        userAuthMapper.updateUserAuthStatus(auth.getUserId(), "real_name", auth.getStatus());
    }

    /**
     * 用户提交驾驶证认证（新增或更新）
     */
    @Transactional
    public void submitDriverLicense(UserDriverLicenseAuth auth, String phone) {
        // 检查是否已存在
        UserDriverLicenseAuth existing = userAuthMapper.selectDriverLicenseByUserId(auth.getUserId());

        if (existing != null) {
            // 更新现有记录
            auth.setId(existing.getId());
            userAuthMapper.updateDriverLicenseAuth(auth, phone);
        } else {
            // 新增记录
            userAuthMapper.insertDriverLicenseAuth(auth, phone);
        }
        // 2. 如果有手机号更新，更新用户表
        if (phone != null) {
            userAuthMapper.updateUserPhone(auth.getUserId(), phone);
        }
        // 可选：更新 user 表中的状态字段
        userAuthMapper.updateUserAuthStatus(auth.getUserId(), "driver_license", auth.getStatus());
    }

    /**
     * 根据用户ID查询实名认证
     */
    public UserRealNameAuth getRealNameByUserId(Integer userId) {
        return userAuthMapper.selectRealNameByUserId(userId);
    }

    /**
     * 根据用户ID查询驾驶证认证
     */
    public UserDriverLicenseAuth getDriverLicenseByUserId(Integer userId) {
        return userAuthMapper.selectDriverLicenseByUserId(userId);
    }
}