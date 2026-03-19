package com.example.service;

import com.example.entity.content.UserFavorite;
import com.example.entity.car.Car;
// import com.example.entity.content.News;  // 注释掉
import com.example.exception.CustomException;
import com.example.mapper.CarMapper;
// import com.example.mapper.NewsMapper;  // 注释掉
import com.example.mapper.UserFavoriteMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserFavoriteService {

    @Resource
    private UserFavoriteMapper favoriteMapper;

    @Resource
    private CarMapper carMapper;

    // @Resource
    // private NewsMapper newsMapper;  // 注释掉

    /**
     * 添加收藏
     */
    @Transactional
    public void addFavorite(Integer userId, Integer targetId, String targetType) {
        // 1. 参数校验
        if (userId == null || targetId == null || targetType == null) {
            throw new CustomException("参数不能为空");
        }

        // 2. 检查收藏目标是否存在
        checkTargetExists(targetId, targetType);

        // 3. 检查是否已经收藏
        int exists = favoriteMapper.checkExists(userId, targetId, targetType);
        if (exists > 0) {
            throw new CustomException("已经收藏过了");
        }

        // 4. 添加收藏
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setTargetId(targetId);
        favorite.setTargetType(targetType);
        favoriteMapper.insert(favorite);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Integer userId, Integer targetId, String targetType) {
        if (userId == null || targetId == null || targetType == null) {
            throw new CustomException("参数不能为空");
        }
        favoriteMapper.delete(userId, targetId, targetType);
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorite(Integer userId, Integer targetId, String targetType) {
        if (userId == null || targetId == null || targetType == null) {
            return false;
        }
        return favoriteMapper.checkExists(userId, targetId, targetType) > 0;
    }

    /**
     * 批量查询收藏状态（用于列表页）
     */
    public Map<Integer, Boolean> batchCheckFavorites(Integer userId, List<Integer> targetIds, String targetType) {
        Map<Integer, Boolean> result = new HashMap<>();
        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            return result;
        }

        List<Integer> favoriteIds = favoriteMapper.selectUserFavoriteIds(userId, targetIds, targetType);
        for (Integer targetId : targetIds) {
            result.put(targetId, favoriteIds.contains(targetId));
        }
        return result;
    }

    /**
     * 获取用户的收藏列表（带详细信息）- 暂时只支持车辆
     */
    public PageInfo<?> getUserFavorites(Integer userId, String targetType,
                                        Integer pageNum, Integer pageSize) {
        // 1. 先分页查询收藏记录
        PageHelper.startPage(pageNum, pageSize);
        List<UserFavorite> favorites = favoriteMapper.selectUserFavorites(userId, targetType);

        // 2. 如果没有收藏记录，直接返回空的分页对象
        if (favorites == null || favorites.isEmpty()) {
            PageInfo<Object> emptyPageInfo = new PageInfo<>();
            emptyPageInfo.setList(new ArrayList<>());
            emptyPageInfo.setTotal(0);
            emptyPageInfo.setPageNum(pageNum);
            emptyPageInfo.setPageSize(pageSize);
            emptyPageInfo.setPages(0);
            return emptyPageInfo;
        }

        // 3. 根据收藏类型查询详细信息
        List<?> detailList;
        if ("car".equals(targetType)) {
            detailList = getCarFavoritesDetail(favorites);
        } else if ("news".equals(targetType)) {
            // 资讯功能暂未实现，返回空列表
            detailList = new ArrayList<>();
        } else {
            throw new CustomException("不支持的收藏类型");
        }

        // 4. 使用 PageInfo.of 创建分页对象（会自动计算总记录数）
        return PageInfo.of(detailList);
    }

    /**
     * 获取车辆收藏详细信息
     */
    private List<Car> getCarFavoritesDetail(List<UserFavorite> favorites) {
        List<Integer> carIds = favorites.stream()
                .map(UserFavorite::getTargetId)
                .collect(Collectors.toList());

        if (carIds.isEmpty()) {
            return List.of();
        }

        return carMapper.selectByIds(carIds);
    }

    /**
     * 获取资讯收藏详细信息 - 暂未实现
     */
    /*
    private List<News> getNewsFavoritesDetail(List<UserFavorite> favorites) {
        List<Integer> newsIds = favorites.stream()
                .map(UserFavorite::getTargetId)
                .collect(Collectors.toList());

        if (newsIds.isEmpty()) {
            return List.of();
        }

        return newsMapper.selectByIds(newsIds);
    }
    */

    /**
     * 检查收藏目标是否存在
     */
    private void checkTargetExists(Integer targetId, String targetType) {
        if ("car".equals(targetType)) {
            Car car = carMapper.selectById(targetId);
            if (car == null) {
                throw new CustomException("收藏的车辆不存在");
            }
        } else if ("news".equals(targetType)) {
            // 资讯功能暂未实现，直接通过
            // throw new CustomException("资讯收藏功能暂未开放");
            // 暂时允许收藏资讯，但不检查存在性
        } else {
            throw new CustomException("不支持的收藏类型");
        }
    }

    /**
     * 清空用户某类型的所有收藏
     */
    @Transactional
    public void clearFavorites(Integer userId, String targetType) {
        if (userId == null || targetType == null) {
            throw new CustomException("参数不能为空");
        }
        favoriteMapper.deleteByUserAndType(userId, targetType);
    }
}