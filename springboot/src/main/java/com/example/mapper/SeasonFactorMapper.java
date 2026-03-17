package com.example.mapper;

import com.example.entity.order.SeasonFactor;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

public interface SeasonFactorMapper {

    List<SeasonFactor> selectOverlapping(@Param("start") LocalDate start,
                                         @Param("end") LocalDate end);

    List<SeasonFactor> selectAll(@Param("name") String name);

    SeasonFactor selectById(@Param("id") Integer id);

    void insert(SeasonFactor factor);

    void update(SeasonFactor factor);

    void deleteById(@Param("id") Integer id);
}