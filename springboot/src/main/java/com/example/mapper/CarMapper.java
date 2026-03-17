package com.example.mapper;

import com.example.entity.car.Car;
import com.example.entity.car.CarBrand;
import com.example.entity.car.CarCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CarMapper {
    // ==================== 管理端方法 ====================

    //  车辆管理

    List<Car> selectAll(@Param("keyword") String keyword,
                        @Param("brandId") Integer brandId,
                        @Param("categoryId") Integer categoryId,
                        @Param("seats") Integer seats,
                        @Param("energy") String energy,
                        @Param("status") String status,
                        @Param("minPrice") Integer minPrice,
                        @Param("maxPrice") Integer maxPrice);

    Car selectById(@Param("id") Integer id);

    void insert(Car car);

    void updateById(Car car);

    void deleteById(@Param("id") Integer id);

    void insertCategoryRelations(@Param("carId") Integer carId, @Param("categoryIds") List<Integer> categoryIds);

    void deleteCategoryRelations(@Param("carId") Integer carId);

    // 品牌管理

    List<CarBrand> selectBrandAll(@Param("name") String name);

    CarBrand selectBrandById(@Param("id") Integer id);

    CarBrand selectBrandByName(@Param("name") String name);

    void insertBrand(CarBrand brand);

    void updateBrand(CarBrand brand);

    void deleteBrandById(@Param("id") Integer id);

    int countByBrandId(@Param("brandId") Integer brandId);

    // 分类管理

    List<CarCategory> selectCategoryAll(@Param("name") String name);

    CarCategory selectCategoryById(@Param("id") Integer id);

    CarCategory selectCategoryByName(@Param("name") String name);

    void insertCategory(CarCategory category);

    void updateCategory(CarCategory category);

    void deleteCategoryById(@Param("id") Integer id);

    int countByCategoryId(@Param("categoryId") Integer categoryId);

    // ==================== 用户端方法 ====================
    /**
     * 用户端查询车辆列表
     */
    List<Car> selectFrontList(@Param("keyword") String keyword,
                              @Param("brandId") Integer brandId,
                              @Param("seats") Integer seats,
                              @Param("energy") String energy,
                              @Param("minPrice") Integer minPrice,
                              @Param("maxPrice") Integer maxPrice,
                              @Param("sortBy") String sortBy);

    /**
     * 用户端查询车辆详情
     */
    Car selectFrontDetail(@Param("id") Integer id);

    /**
     * 热门推荐 - 基于评价数量
     */
    List<Car> selectHotRecommend(@Param("limit") Integer limit);

    /**
     * 车型推荐 - 基于评分
     */
    List<Car> selectRatingRecommend(@Param("limit") Integer limit);

    /**
     * 获取所有品牌列表
     */
    List<Map<String, Object>> selectBrandList();

    /**
     * 获取所有能源类型
     */
    List<String> selectEnergyList();

    /**
     * 获取所有座位数选项
     */
    List<Integer> selectSeatsList();
    int decreaseStock(@Param("id") Integer id, @Param("count") Integer count);
    void increaseStock(@Param("id") Integer id, @Param("count") Integer count);

}