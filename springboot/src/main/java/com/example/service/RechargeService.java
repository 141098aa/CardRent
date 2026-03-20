package com.example.service;

import com.example.entity.User;
import com.example.entity.finance.RechargeRecord;
import com.example.exception.CustomException;
import com.example.mapper.RechargeMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RechargeService {

    @Resource
    private RechargeMapper rechargeMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private PaymentPasswordService paymentPasswordService;

    /**
     * 创建充值订单
     */
    @Transactional
    public RechargeRecord createRecharge(Integer userId, BigDecimal amount, String paymentMethod) {
        // 参数校验
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("充值金额必须大于0");
        }
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            throw new CustomException("单次充值金额不能超过10000元");
        }

        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        // 创建充值记录
        RechargeRecord record = new RechargeRecord();
        record.setRechargeNo(generateRechargeNo());
        record.setUserId(userId);
        record.setUserName(user.getName());
        record.setAmount(amount);
        record.setPaymentMethod(paymentMethod);
        record.setStatus("pending"); // 待支付
        record.setExpireTime(LocalDateTime.now().plusMinutes(15)); // 15分钟后过期
        record.setCanCancel(true);

        rechargeMapper.insert(record);
        return record;
    }

    /**
     * 支付充值
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payRecharge(String rechargeNo, Integer userId, String paymentPassword, String remark) {
        Map<String, Object> result = new HashMap<>();

        // 查询充值记录
        RechargeRecord record = rechargeMapper.selectByRechargeNo(rechargeNo);
        if (record == null) {
            throw new CustomException("充值记录不存在");
        }

        // 检查充值记录是否属于当前用户
        if (!record.getUserId().equals(userId)) {
            throw new CustomException("无权操作此充值记录");
        }

        // 检查状态
        if (!"pending".equals(record.getStatus())) {
            throw new CustomException("该充值记录已被处理");
        }

        // 检查是否过期
        if (record.getExpireTime() != null && record.getExpireTime().isBefore(LocalDateTime.now())) {
            record.setStatus("cancelled");
            record.setCanCancel(false);
            rechargeMapper.update(record);
            throw new CustomException("充值订单已过期，请重新发起充值");
        }

        // 这里验证密码可能会抛出异常，但不会回滚前面的查询操作
        // 因为密码验证涉及的错误记录表是独立的
        try {
            paymentPasswordService.verifyPassword(userId, paymentPassword);
        } catch (CustomException e) {
            // 重新抛出异常，让前端知道密码错误
            throw e;
        }

        // 获取用户信息（验证通过后）
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        // 更新用户余额
        BigDecimal newBalance = user.getAccount().add(record.getAmount());
        user.setAccount(newBalance);
        userMapper.updateById(user);

        // 更新充值记录状态
        record.setStatus("success");
        record.setPaymentTime(LocalDateTime.now());
        record.setCanCancel(false);
        record.setRemark(remark);
        rechargeMapper.update(record);

        // 返回用户信息和充值记录
        result.put("user", user);
        result.put("rechargeRecord", record);

        return result;
    }

    /**
     * 查询用户充值记录
     */
    public PageInfo<RechargeRecord> getUserRecharges(Integer userId, String status,
                                                     Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<RechargeRecord> list = rechargeMapper.selectByUserId(userId, status);
        return PageInfo.of(list);
    }
    /**
     * 取消充值订单
     */
    @Transactional
    public void cancelRecharge(String rechargeNo, Integer userId) {
        RechargeRecord record = rechargeMapper.selectByRechargeNo(rechargeNo);
        if (record == null) {
            throw new CustomException("充值记录不存在");
        }

        if (!record.getUserId().equals(userId)) {
            throw new CustomException("无权操作此充值记录");
        }

        if (!"pending".equals(record.getStatus())) {
            throw new CustomException("该充值记录无法取消");
        }

        if (!record.getCanCancel()) {
            throw new CustomException("该充值记录已不可取消");
        }

        record.setStatus("cancelled");
        record.setCanCancel(false);
        rechargeMapper.update(record);
    }
    /**
     * 重新支付充值订单
     */
    @Transactional
    public RechargeRecord repPayRecharge(String rechargeNo, Integer userId) {
        RechargeRecord record = rechargeMapper.selectByRechargeNo(rechargeNo);
        if (record == null) {
            throw new CustomException("充值记录不存在");
        }

        if (!record.getUserId().equals(userId)) {
            throw new CustomException("无权操作此充值记录");
        }

        // 只有已取消或已过期的订单可以重新支付
        if (!"cancelled".equals(record.getStatus())) {
            throw new CustomException("该充值记录无法重新支付");
        }

        // 创建新的充值记录
        return createRecharge(userId, record.getAmount(), record.getPaymentMethod());
    }
    /**
     * 定时任务：取消超时未支付的充值订单（每5分钟执行一次）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void autoCancelExpiredRecharges() {
        LocalDateTime now = LocalDateTime.now();
        List<RechargeRecord> expiredRecords = rechargeMapper.selectExpiredPending(now);

        for (RechargeRecord record : expiredRecords) {
            record.setStatus("cancelled");
            record.setCanCancel(false);
            rechargeMapper.update(record);
        }
    }

    /**
     * 查询充值记录详情
     */
    public RechargeRecord getRechargeDetail(String rechargeNo, Integer userId) {
        RechargeRecord record = rechargeMapper.selectByRechargeNo(rechargeNo);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new CustomException("充值记录不存在或无权限查看");
        }
        return record;
    }

    /**
     * 生成充值单号
     */
    private String generateRechargeNo() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%04d", (int)(Math.random() * 10000));
        return "RCH" + dateStr + randomStr;
    }
}