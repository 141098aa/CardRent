package com.example.mapper;


import com.example.entity.finance.PaymentPasswordError;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PaymentPasswordErrorMapper {

    PaymentPasswordError selectByUserId(@Param("userId") Integer userId);

    void insert(PaymentPasswordError error);

    void update(PaymentPasswordError error);

    void deleteByUserId(@Param("userId") Integer userId);

    List<PaymentPasswordError> selectAllLocked();

    void deleteExpiredLocks();
}
