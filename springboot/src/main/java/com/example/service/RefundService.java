package com.example.service;

import com.example.entity.User;
import com.example.entity.finance.RefundRequest;
import com.example.entity.order.Order;
import com.example.exception.CustomException;
import com.example.mapper.OrderMapper;
import com.example.mapper.RefundMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class RefundService {

    @Resource
    private RefundMapper refundMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TransactionService transactionService;

    /**
     * 创建退款申请（用户端调用）
     */
    @Transactional
    public RefundRequest createRefund(Integer orderId, Integer userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new CustomException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new CustomException("无权操作");
        }

        // 检查订单状态
        if (!"pending_pickup".equals(order.getStatus())) {
            throw new CustomException("当前状态无法申请退款");
        }

        // 检查是否已有退款申请
        RefundRequest existing = refundMapper.selectByOrderId(orderId);
        if (existing != null && "pending".equals(existing.getStatus())) {
            throw new CustomException("已有待处理的退款申请");
        }

        // 创建退款申请
        RefundRequest refund = new RefundRequest();
        refund.setRefundNo(generateRefundNo());
        refund.setOrderId(orderId);
        refund.setOrderNo(order.getOrderNo());
        refund.setUserId(userId);
        refund.setUserName(order.getUserName());
        refund.setAmount(order.getTotalPrice());
        refund.setReason(reason);
        refund.setStatus("pending");

        refundMapper.insert(refund);

        return refund;
    }

    /**
     * 审核退款申请
     */
    @Transactional
    public void auditRefund(Integer id, String action, String remark) {
        RefundRequest refund = refundMapper.selectById(id);
        if (refund == null) {
            throw new CustomException("退款申请不存在");
        }

        if (!"pending".equals(refund.getStatus())) {
            throw new CustomException("该退款申请已被处理");
        }

        LocalDateTime now = LocalDateTime.now();

        if ("approve".equals(action)) {
            // 通过审核，执行退款
            refund.setStatus("approved");
            refund.setAuditRemark(remark);
            refund.setAuditTime(now);
            refundMapper.update(refund);

            // 执行退款操作
            processRefund(refund);

        } else if ("reject".equals(action)) {
            // 拒绝退款
            refund.setStatus("rejected");
            refund.setAuditRemark(remark);
            refund.setAuditTime(now);
            refundMapper.update(refund);
        } else {
            throw new CustomException("操作类型错误");
        }
    }

    /**
     * 执行退款
     */
    @Transactional
    public void processRefund(RefundRequest refund) {
        // 1. 更新订单状态
        Order order = orderMapper.selectById(refund.getOrderId());
        order.setStatus("refunded");
        orderMapper.updateOrderStatus(order.getId(), "refunded", null, null, null);

        // 2. 退还用户余额
        User user = userMapper.selectById(refund.getUserId());
        BigDecimal newBalance = user.getAccount().add(refund.getAmount());
        user.setAccount(newBalance);
        userMapper.updateById(user);

        // 3. 创建退款流水
        transactionService.createTransaction(
                user.getId(), user.getName(), "refund",
                refund.getAmount(), newBalance,
                refund.getId(), refund.getRefundNo(),
                "订单退款：" + refund.getOrderNo()
        );
        // 4. 创建退款流水（收入，金额为正数）
        transactionService.createTransaction(
                user.getId(),
                user.getName(),
                "refund",                    // 类型：退款
                refund.getAmount(),          // 金额为正数（收入）
                newBalance,                  // 交易后余额
                refund.getId(),
                refund.getRefundNo(),
                "订单退款：" + order.getOrderNo()
        );

        //5. 更新退款状态为已完成
        refund.setStatus("completed");
        refund.setCompleteTime(LocalDateTime.now());
        refundMapper.update(refund);
    }

    /**
     * 分页查询退款申请
     */
    public PageInfo<RefundRequest> selectPage(Integer pageNum, Integer pageSize,
                                              String keyword, String status,
                                              String startDate, String endDate) {
        PageHelper.startPage(pageNum, pageSize);
        List<RefundRequest> list = refundMapper.selectPage(keyword, status, startDate, endDate);
        return PageInfo.of(list);
    }

    /**
     * 批量审核退款
     */
    @Transactional
    public void batchAudit(List<Integer> ids, String action, String remark) {
        for (Integer id : ids) {
            auditRefund(id, action, remark);
        }
    }

    /**
     * 获取待审核数量
     */
    public int getPendingCount() {
        return refundMapper.countPending();
    }

    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%04d", (int)(Math.random() * 10000));
        return "RF" + dateStr + randomStr;
    }
}