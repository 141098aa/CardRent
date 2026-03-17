package com.example.service;

import cn.hutool.json.ObjectMapper;
import com.example.entity.User;
import com.example.entity.car.Car;
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
     * 确认取车
     */
    @Transactional
    public void confirmPickup(Integer id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new CustomException("订单不存在");
        }

        if (!"pending_pickup".equals(order.getStatus())) {
            throw new CustomException("该订单不是待取车状态");
        }

        String newStatus = "active";
        orderMapper.updateStatus(id, newStatus, null, "admin");
        orderMapper.insertLog(order.getId(),order.getOrderNo(), order.getStatus(), newStatus, "admin", "admin","管理员确认取车");
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

        if (!"active".equals(order.getStatus())) {
            throw new CustomException("该订单不是进行中状态");
        }

        String newStatus = "completed";
        orderMapper.updateStatus(id, newStatus, null, "admin");
        orderMapper.insertLog(order.getId(),order.getOrderNo(), order.getStatus(), newStatus, "admin","admin", "管理员确认还车");
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
     * 计算动态价格
     */
    public Map<String, Object> calculatePrice(Integer carId, String pickupTime, String returnTime) {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取车辆信息
        Car car = carMapper.selectById(carId);
        if (car == null) {
            throw new CustomException("车辆不存在");
        }

        // 2. 计算租车天数
        LocalDateTime pickup = LocalDateTime.parse(pickupTime);
        LocalDateTime return_ = LocalDateTime.parse(returnTime);
        long days = ChronoUnit.DAYS.between(pickup.toLocalDate(), return_.toLocalDate());
        if (days < 1) {
            throw new CustomException("租车天数不能少于1天");
        }

        // 3. 获取各项系数
        BigDecimal seasonFactor = getSeasonFactor(pickup.toLocalDate(), return_.toLocalDate());
        BigDecimal weekendFactor = getWeekendFactor(pickup.toLocalDate(), return_.toLocalDate());
        BigDecimal rentalDiscount = getRentalDiscount((int) days);

        // 4. 计算基础租金
        BigDecimal baseRent = car.getPrice().multiply(BigDecimal.valueOf(days));
        BigDecimal currentRent = baseRent;

        // 5. 创建调价明细列表
        List<Map<String, Object>> adjustments = new ArrayList<>();
        BigDecimal totalFactor = BigDecimal.ONE;

        // 季节溢价
        if (seasonFactor.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal factor = seasonFactor;
            BigDecimal amount = baseRent.multiply(factor.subtract(BigDecimal.ONE));
            Map<String, Object> adj = new HashMap<>();
            adj.put("type", "season");
            adj.put("name", "节假日溢价");
            adj.put("factor", factor);
            adj.put("description", "节假日+" + factor.multiply(BigDecimal.valueOf(100)).subtract(BigDecimal.valueOf(100)).intValue() + "%");
            adj.put("amount", amount);
            adjustments.add(adj);
            totalFactor = totalFactor.multiply(factor);
        }

        // 周末溢价
        if (weekendFactor.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal factor = weekendFactor;
            BigDecimal amount = baseRent.multiply(factor.subtract(BigDecimal.ONE));
            Map<String, Object> adj = new HashMap<>();
            adj.put("type", "weekend");
            adj.put("name", "周末溢价");
            adj.put("factor", factor);
            adj.put("description", "周末+" + factor.multiply(BigDecimal.valueOf(100)).subtract(BigDecimal.valueOf(100)).intValue() + "%");
            adj.put("amount", amount);
            adjustments.add(adj);
            totalFactor = totalFactor.multiply(factor);
        }

        // 长租折扣
        if (rentalDiscount.compareTo(BigDecimal.ONE) < 0) {
            BigDecimal factor = rentalDiscount;
            BigDecimal amount = baseRent.multiply(BigDecimal.ONE.subtract(factor)).negate();
            Map<String, Object> adj = new HashMap<>();
            adj.put("type", "discount");
            adj.put("name", "长租折扣");
            adj.put("factor", factor);
            adj.put("description", "长租" + (int)((1 - factor.doubleValue()) * 100) + "%优惠");
            adj.put("amount", amount);
            adjustments.add(adj);
            totalFactor = totalFactor.multiply(factor);
        }

        // 6. 计算动态租金
        BigDecimal dynamicRent = baseRent.multiply(totalFactor);

        // 7. 计算保险费用
        BigDecimal insuranceTotal = car.getInsurancePrice()
                .multiply(BigDecimal.valueOf(days));

        // 8. 计算总计
        BigDecimal total = dynamicRent.add(insuranceTotal).add(car.getDeposit());

        // 9. 将调价明细转为JSON（使用 Hutool）
        String priceAdjustmentsJson = JSONUtil.toJsonStr(adjustments);

        result.put("carId", carId);
        result.put("days", days);
        result.put("dailyPrice", car.getPrice());
        result.put("baseRent", baseRent);
        result.put("dynamicRent", dynamicRent);
        result.put("insurancePrice", car.getInsurancePrice());
        result.put("insuranceTotal", insuranceTotal);
        result.put("deposit", car.getDeposit());
        result.put("totalPrice", total);
        result.put("priceAdjustments", priceAdjustmentsJson);  // 调价明细

        return result;
    }

    /**
     * 获取周末系数
     */
    private BigDecimal getWeekendFactor(LocalDate start, LocalDate end) {
        // 检查租期内是否包含周六日
        LocalDate date = start;
        boolean hasWeekend = false;
        while (!date.isAfter(end)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                hasWeekend = true;
                break;
            }
            date = date.plusDays(1);
        }
        return hasWeekend ? BigDecimal.valueOf(1.2) : BigDecimal.ONE; // 周末溢价20%
    }

    /**
     * 获取季节系数
     */
    private BigDecimal getSeasonFactor(LocalDate start, LocalDate end) {
        // 查询季节系数表，取重叠日期的最大系数
        List<SeasonFactor> factors = seasonFactorMapper.selectOverlapping(start, end);
        BigDecimal maxFactor = BigDecimal.ONE;
        for (SeasonFactor factor : factors) {
            if (factor.getFactor().compareTo(maxFactor) > 0) {
                maxFactor = factor.getFactor();
            }
        }
        return maxFactor;
    }

    /**
     * 获取供需系数（基于库存）
     * 库存越少，系数越高
     */
    private BigDecimal getDemandFactor(Integer stock) {
        if (stock == null) return BigDecimal.ONE;

        // 从系统配置获取供需系数阈值
        String config = systemConfigMapper.getValueByKey("demand_factors");
        // 格式：{"5":1.2, "3":1.5, "1":2.0}

        if (stock <= 1) return BigDecimal.valueOf(2.0);
        if (stock <= 3) return BigDecimal.valueOf(1.5);
        if (stock <= 5) return BigDecimal.valueOf(1.2);
        return BigDecimal.ONE;
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
     * 支付订单
     */
    @Transactional
    public void payOrder(Integer id, Integer userId, String paymentMethod, String paymentPassword) {
        Order order = orderMapper.selectUserOrderById(id, userId);
        if (order == null) {
            throw new CustomException("订单不存在或无权限操作");
        }

        if (!"pending_pay".equals(order.getStatus())) {
            throw new CustomException("该订单不是待付款状态");
        }

        // 验证支付密码
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
        // 保存旧状态用于日志
        String oldStatus = order.getStatus();

        // 支付成功后扣减库存
        decreaseStock(order.getCarId(), 1);

        // 支付成功后更新订单状态为待审核
        String newStatus = "pending_audit"; // 默认需要审核
        orderMapper.payOrder(id, userId, paymentMethod);

        // 更新内存对象的状态
        order.setStatus(newStatus);

        // 记录日志
        orderMapper.insertLog(order.getId(), order.getOrderNo(), oldStatus, newStatus,
                "user", String.valueOf(userId), "用户完成支付");
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