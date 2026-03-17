package com.example.mapper;

import com.example.entity.order.SystemConfig;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SystemConfigMapper {

    String getValueByKey(@Param("key") String key);

    SystemConfig selectByKey(@Param("key") String key);

    List<SystemConfig> selectAll(@Param("configType") String configType);

    void insert(SystemConfig config);

    void update(SystemConfig config);

    void deleteById(@Param("id") Integer id);
}