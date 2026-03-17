package com.example.mapper;

import com.example.entity.order.RentalDiscount;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface RentalDiscountMapper {

    RentalDiscount selectByDays(@Param("days") Integer days);

    List<RentalDiscount> selectAll();

    RentalDiscount selectById(@Param("id") Integer id);

    void insert(RentalDiscount discount);

    void update(RentalDiscount discount);

    void deleteById(@Param("id") Integer id);
}