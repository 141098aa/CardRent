package com.example.service;

import com.example.entity.User;
import com.example.entity.finance.PaymentPasswordError;
import com.example.entity.finance.PaymentPasswordReset;
import com.example.exception.CustomException;
import com.example.mapper.PaymentPasswordErrorMapper;
import com.example.mapper.PaymentPasswordResetMapper;
import com.example.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class PaymentPasswordService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PaymentPasswordErrorMapper errorMapper;

    @Resource
    private PaymentPasswordResetMapper resetMapper;

    private static final int MAX_SHORT_ERRORS = 5;
    private static final int SHORT_LOCK_MINUTES = 20;
    private static final int MAX_LONG_ERRORS = 10;
    private static final int LONG_LOCK_DAYS = 3;

    /**
     * 设置支付密码
     */
    @Transactional
    public void setPassword(Integer userId, String password) {
        if (password == null || !password.matches("\\d{6}")) {
            throw new CustomException("支付密码必须为6位数字");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        user.setPaymentPassword(password);
        userMapper.updateById(user);
        errorMapper.deleteByUserId(userId);
    }

    /**
     * 验证支付密码
     */
    public void verifyPassword(Integer userId, String password) {
        System.out.println("=== 验证支付密码 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("输入的密码: " + password);

        // 1. 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        // 2. 检查支付密码是否已设置
        if (user.getPaymentPassword() == null || user.getPaymentPassword().isEmpty()) {
            throw new CustomException("请先设置支付密码");
        }

        // 3. 检查是否被锁定
        PaymentPasswordError errorRecord = errorMapper.selectByUserId(userId);
        System.out.println("当前错误记录: " + (errorRecord != null ? errorRecord.getErrorCount() : "无"));

        if (errorRecord != null && errorRecord.getLockEndTime() != null) {
            checkLockStatus(errorRecord);
        }

        // 4. 验证密码
        if (!user.getPaymentPassword().equals(password)) {
            // 密码错误，记录错误（这里会立即插入/更新数据库）
            handlePasswordError(userId);
            throw new CustomException("支付密码错误");
        }

        // 5. 密码正确，清除错误记录
        if (errorRecord != null) {
            errorMapper.deleteByUserId(userId);
            System.out.println("密码正确，已清除错误记录");
        }
    }

    /**
     * 处理密码错误 - 使用独立事务，确保不会被外部事务回滚
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePasswordError(Integer userId) {
        System.out.println("=== 处理密码错误 ===");
        System.out.println("用户ID: " + userId);

        PaymentPasswordError error = errorMapper.selectByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        if (error == null) {
            // 首次错误
            error = new PaymentPasswordError();
            error.setUserId(userId);
            error.setErrorCount(1);
            error.setTotalErrorCount(1);
            error.setFirstErrorTime(now);
            errorMapper.insert(error);
            System.out.println("首次错误记录已创建，错误次数: 1");
        } else {
            // 更新错误次数
            int newErrorCount = error.getErrorCount() + 1;
            error.setErrorCount(newErrorCount);
            error.setTotalErrorCount(error.getTotalErrorCount() + 1);
            System.out.println("错误次数更新: " + error.getErrorCount() + " -> " + newErrorCount);

            // 检查是否需要锁定
            if (newErrorCount >= MAX_SHORT_ERRORS && newErrorCount < MAX_LONG_ERRORS) {
                // 短期锁定
                error.setLockType("short");
                error.setLockStartTime(now);
                error.setLockEndTime(now.plusMinutes(SHORT_LOCK_MINUTES));
                System.out.println("触发短期锁定，锁定20分钟，错误次数: " + newErrorCount);
            } else if (newErrorCount >= MAX_LONG_ERRORS) {
                // 长期锁定
                error.setLockType("long");
                error.setLockStartTime(now);
                error.setLockEndTime(now.plusDays(LONG_LOCK_DAYS));
                System.out.println("触发长期锁定，锁定3天，错误次数: " + newErrorCount);
            }

            errorMapper.update(error);
            System.out.println("错误记录更新完成");
        }

        // 验证是否成功插入/更新
        PaymentPasswordError verifyError = errorMapper.selectByUserId(userId);
        System.out.println("验证数据库记录: 错误次数=" + (verifyError != null ? verifyError.getErrorCount() : "无"));
    }

    /**
     * 检查锁定状态
     */
    private void checkLockStatus(PaymentPasswordError error) {
        if (error.getLockEndTime() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        System.out.println("检查锁定状态，锁定结束时间: " + error.getLockEndTime());

        if (now.isBefore(error.getLockEndTime())) {
            long minutes = java.time.Duration.between(now, error.getLockEndTime()).toMinutes();
            String message;

            if ("short".equals(error.getLockType())) {
                message = String.format("支付密码错误次数过多，请在%d分钟后重试", minutes);
            } else {
                long hours = java.time.Duration.between(now, error.getLockEndTime()).toHours();
                message = String.format("支付密码错误次数过多，请在%d小时后重试", hours);
            }

            throw new CustomException(message);
        }

        // 已过锁定期，清除锁定状态
        if (now.isAfter(error.getLockEndTime())) {
            error.setLockType(null);
            error.setLockStartTime(null);
            error.setLockEndTime(null);
            errorMapper.update(error);
            System.out.println("锁定期已过，清除锁定状态");
        }
    }

    /**
     * 获取锁定状态
     */
    public Map<String, Object> getLockStatus(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        PaymentPasswordError error = errorMapper.selectByUserId(userId);
        System.out.println("获取锁定状态，用户ID: " + userId);
        System.out.println("错误记录: " + (error != null ?
                "错误次数=" + error.getErrorCount() +
                        ", 锁定结束时间=" + error.getLockEndTime() : "无"));

        if (error != null && error.getLockEndTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(error.getLockEndTime())) {
                long minutes = java.time.Duration.between(now, error.getLockEndTime()).toMinutes();
                result.put("locked", true);
                result.put("lockType", error.getLockType());
                result.put("lockEndTime", error.getLockEndTime());
                result.put("remainingMinutes", minutes);
                result.put("errorCount", error.getErrorCount());
                result.put("totalErrorCount", error.getTotalErrorCount());
            } else {
                result.put("locked", false);
                result.put("errorCount", 0);
            }
        } else {
            result.put("locked", false);
            result.put("errorCount", error != null ? error.getErrorCount() : 0);
        }

        System.out.println("锁定状态返回: " + result);
        return result;
    }

    /**
     * 生成重置验证码
     */
    @Transactional
    public String generateResetCode(Integer userId) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        PaymentPasswordReset reset = new PaymentPasswordReset();
        reset.setUserId(userId);
        reset.setResetCode(code.toString());
        reset.setExpireTime(LocalDateTime.now().plusMinutes(10));
        reset.setUsed(false);
        resetMapper.insert(reset);

        return code.toString();
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Integer userId, String resetCode, String newPassword) {
        if (newPassword == null || !newPassword.matches("\\d{6}")) {
            throw new CustomException("支付密码必须为6位数字");
        }

        PaymentPasswordReset reset = resetMapper.selectByUserIdAndCode(userId, resetCode);
        if (reset == null) {
            throw new CustomException("验证码无效");
        }

        if (reset.getUsed()) {
            throw new CustomException("验证码已被使用");
        }

        if (reset.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new CustomException("验证码已过期");
        }

        User user = userMapper.selectById(userId);
        user.setPaymentPassword(newPassword);
        userMapper.updateById(user);

        reset.setUsed(true);
        resetMapper.update(reset);
        errorMapper.deleteByUserId(userId);
    }
}