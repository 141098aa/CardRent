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

    BigDecimal getTotalByStatus(@Param("status") String status);
}