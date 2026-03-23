package com.example.service;

import cn.hutool.json.ObjectMapper;
import com.example.entity.User;
import com.example.entity.car.Car;
import com.example.entity.finance.RefundRequest;
import com.example.entity.order.Order;
import com.example.entity.order.RentalDiscount;
import com.example.entity.order.SeasonFactor;
import com.example.exception.CustomException;
import com.example.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.json.JSONUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private CarMapper carMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private SeasonFactorMapper seasonFactorMapper;

    @Resource
    private RentalDiscountMapper rentalDiscountMapper;

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Resource
    private TransactionService transactionService;
    @Resource
    private RefundService refundService;
    @Resource
    private DepositService depositService;
    @Resource
    private MessageService messageService;
    /**
     * 分页查询订单
     */
    public PageInfo<Order> selectPage(Integer pageNum, Integer pageSize, String keyword,
                                      String statusGroup, String startDate, String endDate) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> list = orderMapper.selectAll(keyword, statusGroup, startDate, endDate);
        return PageInfo.of(list);
    }

    /**
     * 查询订单详情
     */
    public Order selectById(Integer id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new CustomException("订单不存在");
        }
        return order;
    }

    /**
     * 审核订单
     */
    @Transactional
    public void auditOrder(Integer id, String action, String remark) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new CustomException("订单不存在");
        }

        if (!"pending_audit".equals(order.getStatus())) {
            throw new CustomException("该订单不是待审核状态");
        }

        String newStatus;
        if ("approve".equals(action)) {
            newStatus = "pending_pickup"; // 审核通过，变为待取车
            // 发送审核通过通知
            messageService.sendMessage(
                    order.getUserId(),
                    "订单审核通过",
                    String.format("您的订单 %s 已审核通过，请按时取车。", order.getOrderNo()),
                    "order",
                    "/front/orders"
            );
        } else if ("reject".equals(action)) {
            newStatus = "cancelled"; // 审核拒绝，变为已取消
            // 审核拒绝，恢复库存
            increaseStock(order.getCarId(), 1);  // 恢复库存！
        } else {
            throw new CustomException("操作类型错误");
        }

        // 更新订单状态
        orderMapper.updateStatus(id, newStatus, remark, "admin");

        // 记录日志
        orderMapper.insertLog( order.getId(),order.getOrderNo(), order.getStatus(), newStatus, "admin","admin", remark);
    }

    /**
     * 管理员确认取车
     */
    @Transactional
    public void adminConfirmPickup(Integer id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new CustomException("订单不存在");
        }

        if (!"pending_pickup".equals(order.getStatus())) {
            throw new CustomException("该订单不是待取车状态");
        }

        String newStatus = "active";
        orderMapper.updateStatus(id, newStatus, null, "admin");
        orderMapper.insertLog(
                order.getId(), order.getOrderNo(),
                order.getStatus(), newStatus,
                "admin", "admin",
                "管理员确认取车"
        );

        // 发送取车成功通知
        messageService.sendMessage(
                order.getUserId(),
                "取车成功通知",
                String.format("您的订单 %s 已由管理员确认取车，祝您用车愉快！", order.getOrderNo()),
                "order",
                "/front/orders"
        );
    }
    /**
     * 用户确认取车
     */
    @Transactional
    public void userConfirmPickup(Integer id, Integer userId) {
        Order order = orderMapper.selectUserOrderById(id, userId);
        if (order == null) {
            throw new CustomException("订单不存在或无权限操作");
        }

        if (!"pending_pickup".equals(order.getStatus())) {
            throw new CustomException("该订单不是待取车状态");
        }

        String newStatus = "active";
        orderMapper.updateStatus(id, newStatus, null, "user");
        orderMapper.insertLog(
                order.getId(), order.getOrderNo(),
                order.getStatus(), newStatus,
                "user", String.valueOf(userId),
                "用户确认取车"
        );

        // 发送取车成功通知
        messageService.sendMessage(
                userId,
                "取车成功通知",
                String.format("您的订单 %s 已成功取车，祝您用车愉快！", order.getOrderNo()),
                "order",
                "/front/orders"
        );
    }

    /**
     * 确认还车
     */
    @Transactional
    public void confirmReturn(Integer id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new CustomException("订单不存在");
        }

        // 支持 active 和 pending_return 两种状态
        if (!"active".equals(order.getStatus()) && !"pending_return".equals(order.getStatus())) {
            throw new CustomException("该订单不是进行中状态");
        }

        String newStatus = "completed";
        orderMapper.updateStatus(id, newStatus, null, "admin");
        orderMapper.insertLog(order.getId(), order.getOrderNo(), order.getStatus(), newStatus,
                "admin", "admin", "管理员确认还车");

        // 解冻押金
        depositService.unfreezeDeposit(order.getId(), order.getUserId());

        // 发送还车成功通知
        messageService.sendMessage(
                order.getUserId(),
                "还车成功通知",
                String.format("您的订单 %s 已成功还车，感谢您的使用！", order.getOrderNo()),
                "order",
                "/front/orders"
        );
    }

    /**
     * 用户申请还车
     */
    @Transactional
    public void applyReturn(Integer id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new CustomException("订单不存在");
        }

        // 只有进行中状态可以申请还车
        if (!"active".equals(order.getStatus())) {
            throw new CustomException("当前状态无法申请还车");
        }

        String newStatus = "pending_return";
        String oldStatus = order.getStatus();

        orderMapper.updateStatus(id, newStatus, null, "user");
        orderMapper.insertLog(
                order.getId(), order.getOrderNo(),
                oldStatus, newStatus,
                "user", String.valueOf(order.getUserId()),
                "用户申请还车"
        );

        // 发送申请还车通知
        messageService.sendMessage(
                order.getUserId(),
                "还车申请已提交",
                String.format("您的订单 %s 还车申请已提交，等待管理员确认。", order.getOrderNo()),
                "order",
                "/front/orders"
        );
    }
    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Integer id, String reason) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new CustomException("订单不存在");
        }

        String newStatus = "cancelled";
        orderMapper.updateStatus(id, newStatus, reason, "admin");
        orderMapper.insertLog(order.getId(),order.getOrderNo(), order.getStatus(), newStatus, "admin","admin", reason);
    }

    /**
     * 获取订单统计
     */
    public Map<String, Object> getOrderStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("all", orderMapper.countByStatusGroup("all"));
        stats.put("pending", orderMapper.countByStatusGroup("pending")); // 待处理（待付款+待审核）
        stats.put("active", orderMapper.countByStatusGroup("active"));   // 进行中
        stats.put("completed", orderMapper.countByStatusGroup("completed")); // 已完成（已完成+已取消）

        // 详细状态统计
        stats.put("pending_pay", orderMapper.countByStatus("pending_pay"));
        stats.put("pending_audit", orderMapper.countByStatus("pending_audit"));
        stats.put("pending_pickup", orderMapper.countByStatus("pending_pickup"));
        stats.put("active", orderMapper.countByStatus("active"));
        stats.put("completed", orderMapper.countByStatus("completed"));
        stats.put("cancelled", orderMapper.countByStatus("cancelled"));

        return stats;
    }

              /* 用户端方法*/
    /**
     * 计算动态价格 - 支持小时级精确计算
     */
    public Map<String, Object> calculatePrice(Integer carId, String pickupTime, String returnTime) {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取车辆信息
        Car car = carMapper.selectById(carId);
        if (car == null) {
            throw new CustomException("车辆不存在");
        }

        // 2. 解析时间
        LocalDateTime pickup = LocalDateTime.parse(pickupTime);
        LocalDateTime return_ = LocalDateTime.parse(returnTime);
        if (pickup.isAfter(return_)) {
            throw new CustomException("还车时间不能早于取车时间");
        }

// 3. 计算总分钟
        long totalMinutes = ChronoUnit.MINUTES.between(pickup, return_);

// 4. 计算完整天数和剩余分钟
        long totalDays = totalMinutes / (24 * 60);
        long remainingMinutesTotal = totalMinutes % (24 * 60);

        int fullDays = (int) totalDays;
        int remainingHours = (int) (remainingMinutesTotal / 60);
        int remainingMinutes = (int) (remainingMinutesTotal % 60);

        //  判断超时是否按天计算
        boolean isOvertimeFullDay = false;

// 先处理免费情况（小于等于30分钟）
        if (remainingHours == 0 && remainingMinutes <= 30) {
            // 免费，不收费
            remainingHours = 0;
            remainingMinutes = 0;
        }
// 处理按小时计费的情况（30分钟 < 剩余时间 ≤ 4小时）
        else if ((remainingHours < 4) || (remainingHours == 4 && remainingMinutes == 0)) {
            // 按小时计费，分钟超过30按1小时算
            if (remainingMinutes > 30) {
                remainingHours++;
            }
            // 如果加了1小时后超过4小时，需要重新判断
            if (remainingHours > 4) {
                isOvertimeFullDay = true;
                fullDays++;
                remainingHours = 0;
                remainingMinutes = 0;
            }
        }
            // 处理按天计费的情况（剩余时间 > 4小时）
        else {
            // 超过4小时（哪怕多1分钟），按1天计算
            isOvertimeFullDay = true;
            fullDays++;
            remainingHours = 0;
            remainingMinutes = 0;
        }

        // 5. 按天拆分计算
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = pickup.toLocalDate();
        LocalDate endDate = return_.toLocalDate();

        // 如果不足1天，按小时计算，不需要拆天
        if (fullDays == 0 && remainingHours == 0) {
            // 小于30分钟，免费
            result.put("carId", carId);
            result.put("days", 0);
            result.put("hours", 0);
            result.put("dailyPrice", car.getPrice());
            result.put("baseRent", BigDecimal.ZERO.setScale(2));
            result.put("dynamicRent", BigDecimal.ZERO.setScale(2));
            result.put("insurancePrice", car.getInsurancePrice());
            result.put("insuranceTotal", BigDecimal.ZERO.setScale(2));
            result.put("deposit", car.getDeposit().setScale(2));
            result.put("totalPrice", car.getDeposit().setScale(2));
            result.put("priceAdjustments", "[]");
            result.put("dailyFactors", "[]");
            result.put("overtimeInfo", "不足30分钟，免费");
            return result;
        }

        // 计算完整天数的日期列表
        for (int i = 0; i < fullDays; i++) {
            dateList.add(current.plusDays(i));
        }

        // 6. 计算各项费用
        BigDecimal baseRent = BigDecimal.ZERO;  // 基础租金（原价）
        BigDecimal weekendTotal = BigDecimal.ZERO;  // 周末溢价总额
        BigDecimal holidayTotal = BigDecimal.ZERO;  // 节假日溢价总额
        BigDecimal discountAmount = BigDecimal.ZERO;  // 长租折扣总额

        List<Map<String, Object>> dailyFactors = new ArrayList<>();
        List<Map<String, Object>> adjustments = new ArrayList<>();

        // 记录每天的系数和溢价
        for (LocalDate date : dateList) {
            // 获取当天的系数
            BigDecimal dayFactor = getDayFactor(date);

            // 基础租金累加（原价）
            baseRent = baseRent.add(car.getPrice());

            // 计算当天的溢价金额
            BigDecimal dayPremium = car.getPrice().multiply(dayFactor.subtract(BigDecimal.ONE));

            // 判断溢价类型并累加
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    date.getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean isHoliday = getHolidayFactor(date).compareTo(BigDecimal.ONE) > 0;

            if (isHoliday) {
                holidayTotal = holidayTotal.add(dayPremium);
            } else if (isWeekend && dayFactor.compareTo(BigDecimal.valueOf(1.2)) == 0) {
                weekendTotal = weekendTotal.add(dayPremium);
            }

            // 记录每天的计算明细
            Map<String, Object> dayDetail = new HashMap<>();
            dayDetail.put("date", date.toString());
            dayDetail.put("factor", dayFactor);
            dayDetail.put("description", getFactorDescription(date, dayFactor));
            dailyFactors.add(dayDetail);
        }

        // 7. 获取长租折扣（按总天数）
        BigDecimal rentalDiscount = getRentalDiscount(fullDays);

        // 计算折扣前的总金额（基础租金 + 所有溢价）
        BigDecimal amountBeforeDiscount = baseRent
                .add(weekendTotal)
                .add(holidayTotal)
                .setScale(2, RoundingMode.HALF_UP);

        // 计算折扣金额（基于折扣前总金额）
        if (rentalDiscount.compareTo(BigDecimal.ONE) < 0) {
            discountAmount = amountBeforeDiscount
                    .multiply(BigDecimal.ONE.subtract(rentalDiscount))
                    .negate()
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // 8. 计算动态租金（基础租金 + 各种溢价 - 折扣）
        BigDecimal dynamicRent = baseRent
                .add(weekendTotal)
                .add(holidayTotal)
                .add(discountAmount)  // discountAmount是负数，所以是减
                .setScale(2, RoundingMode.HALF_UP);

        // 9. 添加调价项到adjustments列表（用于前端显示）

        // 添加周末溢价调价项
        if (weekendTotal.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> adj = new HashMap<>();
            adj.put("type", "weekend");
            adj.put("name", "周末溢价");
            adj.put("factor", BigDecimal.valueOf(1.2));
            adj.put("description", "周末溢价20%");
            adj.put("amount", weekendTotal.setScale(2, RoundingMode.HALF_UP));
            adjustments.add(adj);
        }

        // 添加节假日溢价调价项
        if (holidayTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal maxHolidayFactor = getMaxHolidayFactor(dateList);
            int percent = maxHolidayFactor.multiply(BigDecimal.valueOf(100)).intValue();

            Map<String, Object> adj = new HashMap<>();
            adj.put("type", "holiday");
            adj.put("name", "节假日溢价");
            adj.put("factor", maxHolidayFactor);
            adj.put("description", "节假日溢价" + percent + "%");
            adj.put("amount", holidayTotal.setScale(2, RoundingMode.HALF_UP));
            adjustments.add(adj);
        }

        // 添加长租折扣调价项
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            Map<String, Object> adj = new HashMap<>();
            adj.put("type", "discount");
            adj.put("name", "长租折扣");
            adj.put("factor", rentalDiscount);
            adj.put("description", "长租" + (int)((1 - rentalDiscount.doubleValue()) * 100) + "%优惠");
            adj.put("amount", discountAmount.setScale(2, RoundingMode.HALF_UP));
            adjustments.add(adj);
        }

        // 10. 计算保险费用（只计算完整天数）
        BigDecimal insuranceTotal = car.getInsurancePrice()
                .multiply(BigDecimal.valueOf(fullDays))
                .setScale(2, RoundingMode.HALF_UP);

        // 11. 计算超时费用
        BigDecimal overtimeAmount = BigDecimal.ZERO;
        Map<String, Object> overtimeInfo = new HashMap<>();

        if (isOvertimeFullDay) {
            // 超时按1天计算
            // 超时的那一天应该是第 fullDays 天
            LocalDate overtimeDate = pickup.plusDays(fullDays - 1).toLocalDate();  // 注意这里用 fullDays-1

            // 检查这个日期是否已经在 dateList 中
            if (!dateList.contains(overtimeDate)) {
                // 如果不在，说明超时那天是全新的一天，需要添加到 dateList
                dateList.add(overtimeDate);

                Map<String, Object> overtimeDayDetail = new HashMap<>();
                overtimeDayDetail.put("date", overtimeDate.toString());
                overtimeDayDetail.put("factor", getDayFactor(overtimeDate));
                overtimeDayDetail.put("description", getFactorDescription(overtimeDate, getDayFactor(overtimeDate)) + " (超时)");
                dailyFactors.add(overtimeDayDetail);

                // 重新计算各项费用
                baseRent = baseRent.add(car.getPrice());

                BigDecimal dayFactor = getDayFactor(overtimeDate);
                BigDecimal dayPremium = car.getPrice().multiply(dayFactor.subtract(BigDecimal.ONE));

                boolean isWeekend = overtimeDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        overtimeDate.getDayOfWeek() == DayOfWeek.SUNDAY;
                boolean isHoliday = getHolidayFactor(overtimeDate).compareTo(BigDecimal.ONE) > 0;

                if (isHoliday) {
                    holidayTotal = holidayTotal.add(dayPremium);
                } else if (isWeekend) {
                    weekendTotal = weekendTotal.add(dayPremium);
                }
            }

            // 重新计算长租折扣
            rentalDiscount = getRentalDiscount(fullDays);

            // 重新计算动态租金
            dynamicRent = baseRent
                    .add(weekendTotal)
                    .add(holidayTotal)
                    .multiply(rentalDiscount)
                    .setScale(2, RoundingMode.HALF_UP);

            // 重新计算保险
            insuranceTotal = car.getInsurancePrice()
                    .multiply(BigDecimal.valueOf(fullDays))
                    .setScale(2, RoundingMode.HALF_UP);

            // 重新计算折扣前的总金额
            amountBeforeDiscount = baseRent
                    .add(weekendTotal)
                    .add(holidayTotal)
                    .setScale(2, RoundingMode.HALF_UP);

            // 重新计算折扣金额
            discountAmount = amountBeforeDiscount
                    .multiply(BigDecimal.ONE.subtract(rentalDiscount))
                    .negate()
                    .setScale(2, RoundingMode.HALF_UP);

            // 计算超时费用金额（用于显示）
            BigDecimal dayFactorForOvertime = getDayFactor(overtimeDate);
            BigDecimal extraDayPrice = car.getPrice().multiply(dayFactorForOvertime);
            BigDecimal overtimeFullDayAmount = extraDayPrice.multiply(rentalDiscount).setScale(2, RoundingMode.HALF_UP);

            overtimeInfo.put("type", "overtime_fullday");
            overtimeInfo.put("name", "超时费用");
            overtimeInfo.put("description", "超时超过4小时，按1天计费");
            overtimeInfo.put("amount", overtimeFullDayAmount);

            // 添加超时调价项
            Map<String, Object> adj = new HashMap<>();
            adj.put("type", "overtime");
            adj.put("name", "超时费用");
            adj.put("description", "超时超过4小时，按1天计费");
            adj.put("amount", overtimeFullDayAmount);
            adjustments.add(adj);
        }else if (remainingHours > 0) {
            // 按小时计费
            // 超时的那一天（取当天的小时费率）
            LocalDate overtimeDate = pickup.plusDays(fullDays).toLocalDate();
            BigDecimal dayFactorForOvertime = getDayFactor(overtimeDate);

            // 溢价后的小时费率
            BigDecimal hourlyRate = car.getPrice()
                    .multiply(dayFactorForOvertime)
                    .divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP);

            // 计算超时费用
            overtimeAmount = hourlyRate.multiply(BigDecimal.valueOf(remainingHours))
                    .setScale(2, RoundingMode.HALF_UP);

            // 计算动态租金：先计算折扣后租金，再加上超时费用
            dynamicRent = baseRent
                    .add(weekendTotal)
                    .add(holidayTotal)
                    .add(discountAmount)  // 先应用折扣
                    .add(overtimeAmount)   // 再加上超时费用
                    .setScale(2, RoundingMode.HALF_UP);

            overtimeInfo.put("type", "overtime_hourly");
            overtimeInfo.put("name", "超时费用");
            overtimeInfo.put("hours", remainingHours);
            overtimeInfo.put("rate", hourlyRate);
            overtimeInfo.put("description", "超时" + remainingHours + "小时");
            overtimeInfo.put("amount", overtimeAmount);

            // 添加超时调价项
            Map<String, Object> adj = new HashMap<>();
            adj.put("type", "overtime");
            adj.put("name", "超时费用");
            adj.put("description", "超时" + remainingHours + "小时 (¥" + hourlyRate + "/小时)");
            adj.put("amount", overtimeAmount);
            adjustments.add(adj);
        }

        // 12. 计算总计
        BigDecimal total = dynamicRent.add(insuranceTotal).add(car.getDeposit())
                .setScale(2, RoundingMode.HALF_UP);

        // 13. 将调价明细转为JSON
        String priceAdjustmentsJson = JSONUtil.toJsonStr(adjustments);
        String dailyFactorsJson = JSONUtil.toJsonStr(dailyFactors);

        // 设置返回值
        result.put("carId", carId);
        result.put("days", fullDays);
        result.put("hours", remainingHours);
        result.put("dailyPrice", car.getPrice());
        result.put("baseRent", baseRent.setScale(2, RoundingMode.HALF_UP));  // 基础租金 = 日租金 × 天数
        result.put("weekendPremium", weekendTotal.setScale(2, RoundingMode.HALF_UP));
        result.put("holidayPremium", holidayTotal.setScale(2, RoundingMode.HALF_UP));
        result.put("discountAmount", discountAmount.setScale(2, RoundingMode.HALF_UP));
        result.put("dynamicRent", dynamicRent.setScale(2, RoundingMode.HALF_UP));
        result.put("dailyFactors", dailyFactorsJson);
        result.put("insurancePrice", car.getInsurancePrice());
        result.put("insuranceTotal", insuranceTotal);
        result.put("deposit", car.getDeposit().setScale(2));
        result.put("overtimeAmount", overtimeAmount);
        result.put("overtimeInfo", overtimeInfo);
        result.put("totalPrice", total);
        result.put("priceAdjustments", priceAdjustmentsJson);

        return result;
    }
    /**
     * 获取日期列表中的最大节假日系数
     */
    private BigDecimal getMaxHolidayFactor(List<LocalDate> dateList) {
        BigDecimal maxFactor = BigDecimal.ONE;
        for (LocalDate date : dateList) {
            BigDecimal factor = getHolidayFactor(date);
            if (factor.compareTo(maxFactor) > 0) {
                maxFactor = factor;
            }
        }
        return maxFactor;
    }
    /**
     * 获取指定日期的系数（不叠加，取最高值）
     */
    private BigDecimal getDayFactor(LocalDate date) {
        // 1. 获取节假日系数
        BigDecimal holidayFactor = getHolidayFactor(date);

        // 2. 获取周末系数
        BigDecimal weekendFactor = getWeekendFactor(date);

        // 3. 取最大值（不叠加）
        return holidayFactor.compareTo(weekendFactor) > 0 ? holidayFactor : weekendFactor;
    }

    /**
     * 获取节假日系数（根据节假日类型区分）
     */
    private BigDecimal getHolidayFactor(LocalDate date) {
        int year = date.getYear();

        // 查询时：
        // 1. 优先找当年精确匹配的数据（year = 当前年份）
        // 2. 如果没有，找 year = NULL 的固定节假日
        List<SeasonFactor> holidayFactors = seasonFactorMapper.selectByDate(date, year);

        if (holidayFactors.isEmpty()) {
            return BigDecimal.ONE;
        }

        // 取最大系数
        return holidayFactors.stream()
                .map(SeasonFactor::getFactor)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
    }
    /**
     * 获取周末系数
     */
    private BigDecimal getWeekendFactor(LocalDate date) {
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY;

        return isWeekend ? BigDecimal.valueOf(1.2) : BigDecimal.ONE;
    }
    /**
     * 获取系数描述
     */
    private String getFactorDescription(LocalDate date, BigDecimal factor) {
        if (factor.compareTo(BigDecimal.ONE) == 0) {
            return "平日价";
        }

        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY;

        // 检查是否是节假日
        List<SeasonFactor> holidayFactors = seasonFactorMapper.selectByDate(date, date.getYear());

        if (!holidayFactors.isEmpty()) {
            // 取最贵的节假日
            SeasonFactor maxFactor = holidayFactors.stream()
                    .max(Comparator.comparing(SeasonFactor::getFactor))
                    .orElse(null);

            if (maxFactor != null) {
                return maxFactor.getName() + "溢价" +
                        maxFactor.getFactor().multiply(BigDecimal.valueOf(100)).intValue() + "%";
            }
        }

        if (isWeekend) {
            return "周末溢价20%";
        }

        return "价格系数 " + factor;
    }

    /**
     * 获取租期折扣
     */
    private BigDecimal getRentalDiscount(int days) {
        RentalDiscount discount = rentalDiscountMapper.selectByDays(days);
        return discount != null ? discount.getDiscount() : BigDecimal.ONE;
    }

    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(Order order) {
        // 1. 参数校验
        if (order.getUserId() == null) {
            throw new CustomException("用户ID不能为空");
        }
        if (order.getCarId() == null) {
            throw new CustomException("车辆ID不能为空");
        }
        if (order.getPickupTime() == null || order.getReturnTime() == null) {
            throw new CustomException("请选择取还车时间");
        }

        // 2. 检查用户认证状态
        User user = userMapper.selectById(order.getUserId());
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        // 检查实名认证状态（只有状态为1才通过）
        if (user.getRealNameVerified() != 1) {
            throw new CustomException("请先完成实名认证");
        }

        // 检查驾驶证认证状态（只有状态为1才通过）
        if (user.getDriverLicenseVerified() != 1) {
            throw new CustomException("请先完成驾驶证认证");
        }

        // 3. 获取车辆信息
        Car car = carMapper.selectById(order.getCarId());
        if (car == null) {
            throw new CustomException("车辆不存在");
        }
        if (!"available".equals(car.getStatus())) {
            throw new CustomException("该车辆当前不可租");
        }
        if (car.getStock() <= 0) {
            throw new CustomException("该车辆库存不足");
        }

        // 4. 计算租车天数
        LocalDateTime pickup = order.getPickupTime();
        LocalDateTime return_ = order.getReturnTime();
        if (pickup.isAfter(return_)) {
            throw new CustomException("还车时间不能早于取车时间");
        }
        long days = ChronoUnit.DAYS.between(pickup.toLocalDate(), return_.toLocalDate());
        if (days < 1) {
            throw new CustomException("租车天数不能少于1天");
        }
        order.setDays((int) days);

        // 5. 先调用 calculatePrice 获取价格信息（重要：先计算价格）
        Map<String, Object> priceInfo = calculatePrice(order.getCarId(),
                order.getPickupTime().toString(), order.getReturnTime().toString());

        // 6. 设置用户信息
        order.setUserName(user.getName());
        order.setUserPhone(user.getPhone());

        // 7. 设置车辆相关信息和价格
        order.setDailyPrice(car.getPrice());
        order.setCarName(car.getBrandName() + " " + car.getModel());
        order.setCarImage(car.getImage());

        // 设置车辆规格信息（座位数、变速箱、能源类型）
        order.setCarSeats(car.getSeats());
        order.setCarGear(car.getGear());
        order.setCarEnergy(car.getEnergy());
        // ====================================================

        order.setPickupLocation(car.getPickupLocation());
        order.setReturnLocation(car.getReturnLocation());
        order.setTotalPrice((BigDecimal) priceInfo.get("totalPrice"));
        order.setDynamicRent((BigDecimal) priceInfo.get("dynamicRent"));
        order.setDeposit(car.getDeposit());

        // 8. 设置调价明细
        order.setPriceAdjustments((String) priceInfo.get("priceAdjustments"));

        // 9. 生成订单号（如果没有）
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo(generateOrderNo());
        }

        // 10. 设置默认值
        if (order.getStatus() == null) {
            order.setStatus("pending_pay"); // 待付款
        }

        // 11. 保存订单
        orderMapper.insertOrder(order);

        // 12. 记录订单日志
        orderMapper.insertLog(order.getId(), order.getOrderNo(), null, order.getStatus(),
                "user", String.valueOf(order.getUserId()), "用户创建订单");

        return order;
    }

    /**
     * 生成订单号
     * 格式：ORD + 年月日 + 6位流水号
     */
    private String generateOrderNo() {
        // 格式：ORD + 年月日 + 6位序列号
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%06d", (int)(Math.random() * 1000000));
        return "ORD" + dateStr + seq;
    }

    /**
     * 查询用户订单列表 - 支持多状态查询（用逗号分隔）
     */
    public PageInfo<Order> selectUserOrders(Integer pageNum, Integer pageSize,
                                            Integer userId, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> list;

        if (status != null && !status.isEmpty()) {
            if (status.contains(",")) {
                String[] statusArray = status.split(",");
                List<String> statusList = Arrays.asList(statusArray);
                list = orderMapper.selectUserOrdersByStatusList(userId, statusList);
            } else {
                list = orderMapper.selectUserOrders(userId, status);
            }
        } else {
            list = orderMapper.selectUserOrders(userId, null);
        }

        return PageInfo.of(list);
    }

    /**
     * 用户取消订单
     */
    @Transactional
    public void cancelUserOrder(Integer id, Integer userId, String reason) {
        Order order = orderMapper.selectUserOrderById(id, userId);
        if (order == null) {
            throw new CustomException("订单不存在或无权限操作");
        }

        // 只有待付款和待审核的订单可以取消
        if (!"pending_pay".equals(order.getStatus()) && !"pending_audit".equals(order.getStatus())) {
            throw new CustomException("当前状态无法取消订单");
        }

       // 只要是下单时扣了库存的状态，取消时都要恢复
        if ("pending_pay".equals(order.getStatus()) || "pending_audit".equals(order.getStatus())) {
            increaseStock(order.getCarId(), 1);  // 恢复库存！
        }

        orderMapper.updateOrderStatus(id, "cancelled", null, reason, userId);
        orderMapper.insertLog(order.getId(), order.getOrderNo(), order.getStatus(), "cancelled",
                "user", String.valueOf(userId), reason);
    }

    /**
     * 余额支付
     */
    @Transactional
    public void payOrder(Integer id, Integer userId, String paymentMethod, String paymentPassword) {
        // 1. 查询订单
        Order order = orderMapper.selectUserOrderById(id, userId);
        if (order == null) {
            throw new CustomException("订单不存在或无权限操作");
        }

        // 2. 检查订单状态
        if (!"pending_pay".equals(order.getStatus())) {
            throw new CustomException("该订单不是待付款状态");
        }

        // 3. 验证支付密码
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        // 检查是否设置了支付密码
        if (user.getPaymentPassword() == null || user.getPaymentPassword().isEmpty()) {
            throw new CustomException("请先设置支付密码");
        }

        if (!user.getPaymentPassword().equals(paymentPassword)) {
            throw new CustomException("支付密码错误");
        }

        // 4. 检查余额是否足够
        BigDecimal orderTotal = order.getTotalPrice();
        if (user.getAccount() == null || user.getAccount().compareTo(orderTotal) < 0) {
            throw new CustomException("余额不足，请先充值");
        }

        // 5. 扣减余额
        BigDecimal newBalance = user.getAccount().subtract(orderTotal);
        user.setAccount(newBalance);
        userMapper.updateById(user);


        // 6. 创建资金流水记录（支付支出，金额为负数）
        transactionService.createTransaction(
                userId,
                user.getName(),
                "payment",                        // 类型：支付
                orderTotal.negate(),              // 金额为负数（支出）
                newBalance,                       // 交易后余额
                order.getId(),                    // 关联ID（订单ID）
                order.getOrderNo(),               // 关联单号
                "租车支付：" + order.getCarName() // 备注
        );
        // 7. 更新订单状态为待审核
        String oldStatus = order.getStatus();
        String newStatus = "pending_audit"; // 默认需要审核
        orderMapper.payOrder(id, userId, paymentMethod);  // 这里会更新订单状态和支付方式

        // 8. 扣减库存
        decreaseStock(order.getCarId(), 1);

        //冻结押金
        // 调用押金服务，创建押金冻结记录
        depositService.freezeDeposit(order.getId(), userId);

        // 9. 记录日志
        orderMapper.insertLog(order.getId(), order.getOrderNo(), oldStatus, newStatus,
                "user", String.valueOf(userId), "余额支付成功");
        //10.发送通知给用户
        messageService.sendMessage(
                userId,
                "支付成功通知",
                String.format("您的订单 %s 已支付成功，金额 ¥%s，请等待管理员审核。",
                        order.getOrderNo(), order.getTotalPrice()),
                "order",
                "/front/orders"
        );

        // 10. 可选：返回更新后的用户信息给前端
        // 不需要返回值，但前端可以通过另外的接口获取最新用户信息
    }

    /**
     * 申请退款
     */
    @Transactional
    public void refundOrder(Integer id, Integer userId, String reason) {
        Order order = orderMapper.selectUserOrderById(id, userId);
        if (order == null) {
            throw new CustomException("订单不存在或无权限操作");
        }

        // 只有待取车状态可以申请退款
        if (!"pending_pickup".equals(order.getStatus())) {
            throw new CustomException("当前状态无法申请退款");
        }
        // 创建退款申请记录
        RefundRequest refund = refundService.createRefund(id, userId, reason);

        orderMapper.applyRefund(id, userId, reason);
        orderMapper.insertLog(order.getId(), order.getOrderNo(), order.getStatus(), "refunding",
                "user", String.valueOf(userId), reason);
    }

    /**
     * 获取用户订单统计
     * 返回分类统计：全部、待处理、进行中、已完成、已取消
     */
    public Map<String, Object> getUserOrderStats(Integer userId) {
        Map<String, Object> stats = new HashMap<>();

        // 1. 全部订单
        int allCount = 0;

        // 2. 待处理 = 待付款 + 待审核
        int pendingCount = 0;

        // 3. 进行中 = 待取车 + 进行中
        int ongoingCount = 0;

        // 4. 已完成 = 已完成
        int completedCount = 0;

        // 5. 已取消 = 已取消 + 已退款
        int cancelledCount = 0;

        // 从数据库获取各状态的订单数量
        List<Map<String, Object>> list = orderMapper.countUserOrdersGroupByStatus(userId);

        // 遍历统计
        for (Map<String, Object> item : list) {
            String status = (String) item.get("status");
            int count = ((Number) item.get("count")).intValue();

            // 全部订单累加
            allCount += count;

            // 根据状态分类统计
            switch (status) {
                case "pending_pay":
                case "pending_audit":
                    pendingCount += count;
                    break;
                case "pending_pickup":
                case "active":
                case "pending_return":
                    ongoingCount += count;
                    break;

                case "completed":
                    completedCount += count;
                    break;
                case "cancelled":
                case "refunded":
                    cancelledCount += count;
                    break;
                default:
                    // 其他状态暂不处理
                    break;
            }
        }

        // 设置返回的统计数据
        stats.put("all", allCount);
        stats.put("pending", pendingCount);
        stats.put("ongoing", ongoingCount);
        stats.put("completed", completedCount);
        stats.put("cancelled", cancelledCount);

        // 可选：如果需要保留详细状态，可以继续返回
        stats.put("pending_pay", getCountByStatus(list, "pending_pay"));
        stats.put("pending_audit", getCountByStatus(list, "pending_audit"));
        stats.put("pending_pickup", getCountByStatus(list, "pending_pickup"));
        stats.put("active", getCountByStatus(list, "active"));
        stats.put("completed", getCountByStatus(list, "completed"));
        stats.put("cancelled", getCountByStatus(list, "cancelled"));

        return stats;
    }

    /**
     * 辅助方法：从列表中获取指定状态的数量
     */
    private int getCountByStatus(List<Map<String, Object>> list, String targetStatus) {
        for (Map<String, Object> item : list) {
            String status = (String) item.get("status");
            if (targetStatus.equals(status)) {
                return ((Number) item.get("count")).intValue();
            }
        }
        return 0;
    }
    /**
     * 扣减车辆库存（下单时调用）,可以解决高并发超卖
     */
    @Transactional
    public void decreaseStock(Integer carId, Integer count) {
        int result = carMapper.decreaseStock(carId, count);
        if (result == 0) {
            // 更新失败，可能是库存不足
            Car car = carMapper.selectById(carId);
            if (car == null) {
                throw new CustomException("车辆不存在");
            } else {
                throw new CustomException("库存不足");
            }
        }
    }

    /**
     * 恢复车辆库存（订单取消时调用）
     */
    @Transactional
    public void increaseStock(Integer carId, Integer count) {
        carMapper.increaseStock(carId, count);
    }

    /**
     * 定时任务：取消超时未支付的订单
     *
     */
    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行一次
    @Transactional
    public void autoCancelExpiredOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);
        List<Order> expiredOrders = orderMapper.selectExpiredOrders(expireTime);

        for (Order order : expiredOrders) {
            cancelUserOrder(order.getId(), order.getUserId(), "订单超时未支付自动取消");
        }
    }
    /**
     * 根据ID和用户ID查询订单详情
     */
    public Order selectUserOrderById(Integer id, Integer userId) {
        Order order = orderMapper.selectUserOrderById(id, userId);
        if (order == null) {
            throw new CustomException("订单不存在或无权限查看");
        }
        return order;
    }
}