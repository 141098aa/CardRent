package com.example.controller.front;

import com.example.common.Result;
import com.example.entity.User;
import com.example.entity.finance.RechargeRecord;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.service.PaymentPasswordService;
import com.example.service.RechargeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/front/recharge")
public class RechargeController {

    @Resource
    private RechargeService rechargeService;
    @Resource
    private PaymentPasswordService paymentPasswordService;
    @Resource
    private UserMapper userMapper;

    /**
     * 创建充值订单
     */
    @PostMapping("/create")
    public Result create(@RequestBody Map<String, Object> params) {
        Integer userId = getCurrentUserId();
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String paymentMethod = (String) params.get("paymentMethod");

        RechargeRecord record = rechargeService.createRecharge(userId, amount, paymentMethod);
        return Result.success(record);
    }

    /**
     * 查询充值记录列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String status) {
        Integer userId = getCurrentUserId();
        PageInfo<RechargeRecord> pageInfo = rechargeService.getUserRecharges(userId, status, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 查询充值记录详情
     */
    @GetMapping("/detail/{rechargeNo}")
    public Result detail(@PathVariable String rechargeNo) {
        Integer userId = getCurrentUserId();
        RechargeRecord record = rechargeService.getRechargeDetail(rechargeNo, userId);
        return Result.success(record);
    }

    /**
     * 获取当前用户ID
     */
    private Integer getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new CustomException("未登录");
        }
        return Integer.parseInt(userIdStr);
    }
    /**
     * 确认支付（支付成功后调用）
     */
    @PostMapping("/pay")
    public Result pay(@RequestBody Map<String, Object> params) {
        String rechargeNo = (String) params.get("rechargeNo");
        String paymentPassword = (String) params.get("paymentPassword");
        String remark = (String) params.get("remark");  // 获取备注
        Integer userId = getCurrentUserId();

        // 1. 验证支付密码
        paymentPasswordService.verifyPassword(userId, paymentPassword);

        // 2. 处理充值，直接获取返回的Map
        Map<String, Object> result = rechargeService.payRecharge(rechargeNo, userId, paymentPassword,remark);

        return Result.success(result);
    }
    /**
     * 取消充值订单
     */
    @PostMapping("/cancel")
    public Result cancel(@RequestBody Map<String, Object> params) {
        String rechargeNo = (String) params.get("rechargeNo");
        Integer userId = getCurrentUserId();
        rechargeService.cancelRecharge(rechargeNo, userId);
        return Result.success();
    }

    /**
     * 重新支付充值订单
     */
    @PostMapping("/repay")
    public Result repay(@RequestBody Map<String, Object> params) {
        String rechargeNo = (String) params.get("rechargeNo");
        Integer userId = getCurrentUserId();
        RechargeRecord record = rechargeService.repPayRecharge(rechargeNo, userId);
        return Result.success(record);
    }
}