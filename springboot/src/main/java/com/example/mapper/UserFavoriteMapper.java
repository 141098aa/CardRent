package com.example.mapper;

import com.example.entity.content.UserFavorite;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserFavoriteMapper {

    /**
     * 添加收藏
     */
    void insert(UserFavorite favorite);

    /**
     * 取消收藏
     */
    void delete(@Param("userId") Integer userId,
                @Param("targetId") Integer targetId,
                @Param("targetType") String targetType);

    /**
     * 检查是否已收藏
     */
    int checkExists(@Param("userId") Integer userId,
                    @Param("targetId") Integer targetId,
                    @Param("targetType") String targetType);

    /**
     * 查询用户的收藏列表（分页）
     */
    List<UserFavorite> selectUserFavorites(@Param("userId") Integer userId,
                                           @Param("targetType") String targetType);

    /**
     * 查询用户的收藏总数
     */
    int countUserFavorites(@Param("userId") Integer userId,
                           @Param("targetType") String targetType);

    /**
     * 批量查询用户是否收藏（用于列表页）
     */
    List<Integer> selectUserFavoriteIds(@Param("userId") Integer userId,
                                        @Param("targetIds") List<Integer> targetIds,
                                        @Param("targetType") String targetType);

    /**
     * 根据ID查询收藏详情
     */
    UserFavorite selectById(@Param("id") Integer id);

    /**
     * 清空用户某类型的所有收藏
     */
    void deleteByUserAndType(@Param("userId") Integer userId,
                             @Param("targetType") String targetType);
}
