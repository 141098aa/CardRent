package com.example.mapper;

import com.example.entity.order.Order;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderMapper {

    List<Order> selectAll(@Param("keyword") String keyword,
                          @Param("statusGroup") String statusGroup,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate);

    Order selectById(@Param("id") Integer id);

    void updateStatus(@Param("id") Integer id,
                      @Param("status") String status,
                      @Param("remark") String remark,
                      @Param("operator") String operator);

    void insertLog(@Param("orderId") Integer orderId,
                   @Param("orderNo") String orderNo,
                   @Param("fromStatus") String fromStatus,
                   @Param("toStatus") String toStatus,
                   @Param("operator") String operator,
                   @Param("operatorType") String operatorType,
                   @Param("remark") String remark);

    int countByStatus(@Param("status") String status);

    int countByStatusGroup(@Param("group") String group);

    // ==================== 用户端方法 ====================

    List<Order> selectUserOrders(@Param("userId") Integer userId,
                                 @Param("status") String status);

    Order selectUserOrderById(@Param("id") Integer id,
                              @Param("userId") Integer userId);

    int countUserOrdersByStatus(@Param("userId") Integer userId,
                                @Param("status") String status);


    @MapKey("status")
    List<Map<String, Object>> countUserOrdersGroupByStatus(@Param("userId") Integer userId);

    void insertOrder(Order order);

    void updateOrderStatus(@Param("id") Integer id,
                           @Param("status") String status,
                           @Param("paymentTime") LocalDateTime paymentTime,
                           @Param("cancelReason") String cancelReason,
                           @Param("userId") Integer userId);

    void payOrder(@Param("id") Integer id,
                  @Param("userId") Integer userId,
                  @Param("paymentMethod") String paymentMethod);

    void applyRefund(@Param("id") Integer id,
                     @Param("userId") Integer userId,
                     @Param("reason") String reason);

    /**
     * 查询超时未支付的订单
     * @param expireTime 过期时间（下单时间早于该时间且状态为待付款）
     */
    List<Order> selectExpiredOrders(@Param("expireTime") LocalDateTime expireTime);

    List<Order> selectUserOrdersByStatusList(@Param("userId") Integer userId,
                                             @Param("statusList") List<String> statusList);
    // 获取用户历史租车的车辆ID列表
    List<Integer> selectUserCarIds(@Param("userId") Integer userId);

    // 获取用户最喜欢的品牌
    Integer selectUserFavoriteBrand(@Param("userId") Integer userId);

    // 获取用户最喜欢的能源类型
    String selectUserFavoriteEnergy(@Param("userId") Integer userId);

    // 获取用户最喜欢的座位数
    Integer selectUserFavoriteSeats(@Param("userId") Integer userId);
}