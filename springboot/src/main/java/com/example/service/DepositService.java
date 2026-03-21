package com.example.service;

import com.example.entity.User;
import com.example.entity.finance.DepositRecord;
import com.example.entity.order.Order;
import com.example.exception.CustomException;
import com.example.mapper.DepositMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepositService {

    @Resource
    private DepositMapper depositMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TransactionService transactionService;

    /**
     * 冻结押金（租车时自动调用）
     */
    @Transactional
    public void freezeDeposit(Integer orderId, Integer userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new CustomException("订单不存在");
        }

        User user = userMapper.selectById(userId);

        DepositRecord deposit = new DepositRecord();
        deposit.setOrderId(orderId);
        deposit.setOrderNo(order.getOrderNo());
        deposit.setUserId(userId);
        deposit.setUserName(user.getName());
        deposit.setAmount(order.getDeposit());
        deposit.setStatus("frozen");

        depositMapper.insert(deposit);

        // 创建押金冻结流水
        transactionService.createTransaction(
                userId, user.getName(), "deposit_freeze",
                order.getDeposit(), user.getAccount(),
                deposit.getId(), order.getOrderNo(),
                "租车押金冻结"
        );
    }

    /**
     * 解冻押金（还车时自动调用）
     */
    @Transactional
    public void unfreezeDeposit(Integer orderId, Integer userId) {
        DepositRecord deposit = depositMapper.selectByOrderId(orderId);
        if (deposit == null) {
            throw new CustomException("押金记录不存在");
        }

        if (!"frozen".equals(deposit.getStatus())) {
            throw new CustomException("该押金已被处理");
        }

        deposit.setStatus("unfrozen");
        deposit.setUnfrozenTime(LocalDateTime.now());
        depositMapper.update(deposit);

        User user = userMapper.selectById(userId);

        // 创建押金解冻流水
        transactionService.createTransaction(
                userId, user.getName(), "deposit_unfreeze",
                deposit.getAmount().negate(), user.getAccount(),
                deposit.getId(), deposit.getOrderNo(),
                "租车押金解冻"
        );
    }

    /**
     * 扣除押金（违章/损坏赔偿时人工操作）
     */
    @Transactional
    public void deductDeposit(Integer depositId, BigDecimal deductAmount, String reason) {
        DepositRecord deposit = depositMapper.selectById(depositId);
        if (deposit == null) {
            throw new CustomException("押金记录不存在");
        }

        if (!"frozen".equals(deposit.getStatus())) {
            throw new CustomException("该押金已被处理，无法扣除");
        }

        if (deductAmount.compareTo(deposit.getAmount()) > 0) {
            throw new CustomException("扣除金额不能超过押金金额");
        }

        // 更新押金记录
        deposit.setStatus("deducted");
        deposit.setDeductAmount(deductAmount);
        deposit.setDeductReason(reason);
        deposit.setDeductTime(LocalDateTime.now());
        depositMapper.update(deposit);

        // 如果只扣除部分，剩余金额需要解冻
        BigDecimal remainingAmount = deposit.getAmount().subtract(deductAmount);
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 剩余金额解冻
            User user = userMapper.selectById(deposit.getUserId());
            BigDecimal newBalance = user.getAccount().add(remainingAmount);
            user.setAccount(newBalance);
            userMapper.updateById(user);

            // 创建解冻流水
            transactionService.createTransaction(
                    user.getId(), user.getName(), "deposit_unfreeze",
                    remainingAmount, newBalance,
                    deposit.getId(), deposit.getOrderNo(),
                    "押金扣除后剩余部分解冻"
            );
        }

        User user = userMapper.selectById(deposit.getUserId());

        // 创建押金扣除流水（负数支出）
        transactionService.createTransaction(
                user.getId(), user.getName(), "deposit_deduct",
                deductAmount.negate(), user.getAccount(),
                deposit.getId(), deposit.getOrderNo(),
                "押金扣除：" + reason
        );
    }

    /**
     * 分页查询押金记录
     */
    public PageInfo<DepositRecord> selectPage(Integer pageNum, Integer pageSize,
                                              String keyword, String status,
                                              String startDate, String endDate) {
        PageHelper.startPage(pageNum, pageSize);
        List<DepositRecord> list = depositMapper.selectPage(keyword, status, startDate, endDate);
        return PageInfo.of(list);
    }

    /**
     * 获取押金统计
     */
    public Map<String, Object> getDepositStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFrozen", depositMapper.getTotalByStatus("frozen"));
        stats.put("totalUnfrozen", depositMapper.getTotalByStatus("unfrozen"));
        stats.put("totalDeducted", depositMapper.getTotalByStatus("deducted"));
        return stats;
    }
}