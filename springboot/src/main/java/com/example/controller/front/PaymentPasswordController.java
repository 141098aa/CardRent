package com.example.controller.front;

import com.example.common.Result;
import com.example.exception.CustomException;
import com.example.service.PaymentPasswordService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@RestController
@RequestMapping("/front/payment-password")
public class PaymentPasswordController {

    @Resource
    private PaymentPasswordService paymentPasswordService;

    /**
     * 设置支付密码
     */
    @PostMapping("/set")
    public Result setPassword(@RequestBody Map<String, String> params) {
        Integer userId = getCurrentUserId();
        String password = params.get("password");
        paymentPasswordService.setPassword(userId, password);
        return Result.success();
    }

    /**
     * 验证支付密码（充值、下单时调用）
     */
    @PostMapping("/verify")
    public Result verifyPassword(@RequestBody Map<String, String> params) {
        Integer userId = getCurrentUserId();
        String password = params.get("password");

        // 验证密码，如果错误会自动记录错误次数和锁定
        paymentPasswordService.verifyPassword(userId, password);
        return Result.success();
    }

    /**
     * 获取锁定状态
     */
    @GetMapping("/lock-status")
    public Result getLockStatus() {
        Integer userId = getCurrentUserId();
        Map<String, Object> status = paymentPasswordService.getLockStatus(userId);
        return Result.success(status);
    }

    /**
     * 发送重置验证码（忘记密码）
     */
    @PostMapping("/send-reset-code")
    public Result sendResetCode() {
        Integer userId = getCurrentUserId();
        String resetCode = paymentPasswordService.generateResetCode(userId);
        // 这里可以发送验证码到用户（显示在页面或发送短信）
        return Result.success(Map.of("resetCode", resetCode));
    }

    /**
     * 验证重置码并重置密码
     */
    @PostMapping("/reset")
    public Result resetPassword(@RequestBody Map<String, String> params) {
        Integer userId = getCurrentUserId();
        String resetCode = params.get("resetCode");
        String newPassword = params.get("newPassword");

        paymentPasswordService.resetPassword(userId, resetCode, newPassword);
        return Result.success();
    }

    /**
     * 获取当前登录用户ID
     * 从请求头中获取 X-User-Id
     */
    private Integer getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new CustomException("未登录");
        }
        try {
            return Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            throw new CustomException("无效的用户ID");
        }
    }

}