package com.example.mapper;

import com.example.entity.finance.RefundRequest;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface RefundMapper {

    void insert(RefundRequest refund);

    void update(RefundRequest refund);

    RefundRequest selectById(@Param("id") Integer id);

    RefundRequest selectByOrderId(@Param("orderId") Integer orderId);

    List<RefundRequest> selectPage(@Param("keyword") String keyword,
                                   @Param("status") String status,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate);

    int countPending();
}