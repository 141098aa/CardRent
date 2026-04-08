package com.example.mapper;

import com.example.entity.content.Banner;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BannerMapper {

    List<Banner> selectAll(@Param("keyword") String keyword);

    Banner selectById(@Param("id") Integer id);

    void insert(Banner banner);

    void update(Banner banner);

    void deleteById(@Param("id") Integer id);

    void batchDelete(@Param("ids") List<Integer> ids);

    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    List<Banner> selectEnabled();
}