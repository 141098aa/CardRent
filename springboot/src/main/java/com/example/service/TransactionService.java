package com.example.service;

import com.example.entity.finance.TransactionRecord;
import com.example.mapper.TransactionRecordMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    @Resource
    private TransactionRecordMapper transactionMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * 创建流水记录
     */
    public void createTransaction(Integer userId, String userName, String type,
                                  BigDecimal amount, BigDecimal balance,
                                  Integer relatedId, String relatedNo, String remark) {
        TransactionRecord record = new TransactionRecord();
        record.setTransactionNo(generateTransactionNo());
        record.setUserId(userId);
        record.setUserName(userName);
        record.setType(type);
        record.setAmount(amount);
        record.setBalance(balance);
        record.setRelatedId(relatedId);
        record.setRelatedNo(relatedNo);
        record.setRemark(remark);
        transactionMapper.insert(record);
    }

    /**
     * 分页查询资金流水
     */
    public PageInfo<TransactionRecord> selectPage(Integer pageNum, Integer pageSize,
                                                  String keyword, String type,
                                                  String startDate, String endDate) {
        PageHelper.startPage(pageNum, pageSize);
        List<TransactionRecord> list = transactionMapper.selectPage(keyword, type, startDate, endDate);
        return PageInfo.of(list);
    }

    /**
     * 获取财务统计数据
     */
    public Map<String, Object> getStatistics(String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 用户充值总额（正数）
        BigDecimal userRecharge = transactionMapper.getTotalByType("recharge", startDate, endDate).abs();

        // 租车支付总额（取绝对值）
        BigDecimal paymentIncome = transactionMapper.getTotalByType("payment", startDate, endDate).abs();

        // 押金扣除总额（取绝对值）
        BigDecimal depositDeduct = transactionMapper.getTotalByType("deposit_deduct", startDate, endDate).abs();

        // 平台实际收入
        BigDecimal platformIncome = paymentIncome.add(depositDeduct);

        // 退款支出（取绝对值）
        BigDecimal refundExpense = transactionMapper.getTotalByType("refund", startDate, endDate).abs();

        // 平台净收益
        BigDecimal netProfit = platformIncome.subtract(refundExpense);

        // 平台总余额
        BigDecimal totalBalance = userMapper.getTotalBalance();

        stats.put("userRecharge", userRecharge);
        stats.put("platformIncome", platformIncome);
        stats.put("refundExpense", refundExpense);
        stats.put("netProfit", netProfit);
        stats.put("totalFlow", userRecharge.add(platformIncome).add(refundExpense));
        stats.put("totalBalance", totalBalance);

        // 按类型统计
        List<Map<String, Object>> typeStats = transactionMapper.getStatisticsByType(startDate, endDate);
        stats.put("typeStats", typeStats);

        return stats;
    }

    /**
     * 生成流水号
     */
    private String generateTransactionNo() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%04d", (int)(Math.random() * 10000));
        return "TR" + dateStr + randomStr;
    }

    /**
     * 按类型统计收支
     */
    public List<Map<String, Object>> getStatisticsByType(String startDate, String endDate) {
        return transactionMapper.getStatisticsByType(startDate, endDate);
    }
}