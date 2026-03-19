package com.example.mapper;

import com.example.entity.content.News;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NewsMapper {

    /**
     * 根据ID查询资讯
     */
    News selectById(@Param("id") Integer id);

    /**
     * 批量查询资讯
     */
    List<News> selectByIds(@Param("ids") List<Integer> ids);
}
