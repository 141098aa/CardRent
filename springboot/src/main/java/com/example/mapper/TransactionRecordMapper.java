package com.example.mapper;

import com.example.entity.finance.TransactionRecord;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionRecordMapper {

    void insert(TransactionRecord record);

    List<TransactionRecord> selectPage(@Param("keyword") String keyword,
                                       @Param("type") String type,
                                       @Param("startDate") String startDate,
                                       @Param("endDate") String endDate);

    TransactionRecord selectByTransactionNo(@Param("transactionNo") String transactionNo);

    BigDecimal getTotalIncome(@Param("startDate") String startDate,
                              @Param("endDate") String endDate);

    BigDecimal getTotalExpense(@Param("startDate") String startDate,
                               @Param("endDate") String endDate);

    List<Map<String, Object>> getStatisticsByType(String startDate, String endDate);

    // 按类型统计金额
    BigDecimal getTotalByType(@Param("type") String type,
                              @Param("startDate") String startDate,
                              @Param("endDate") String endDate);
}