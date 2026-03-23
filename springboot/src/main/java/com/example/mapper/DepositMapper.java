package com.example.mapper;

import com.example.entity.finance.DepositRecord;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

public interface DepositMapper {

    void insert(DepositRecord deposit);

    void update(DepositRecord deposit);

    DepositRecord selectById(@Param("id") Integer id);

    DepositRecord selectByOrderId(@Param("orderId") Integer orderId);

    List<DepositRecord> selectPage(@Param("keyword") String keyword,
                                   @Param("status") String status,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate);

    // 冻结中的押金（状态为 frozen）
    BigDecimal getTotalFrozen();

    // 已解冻的金额（实际退还用户的金额）
    BigDecimal getTotalUnfrozen();

    // 已扣除的金额
    BigDecimal getTotalDeducted();
}