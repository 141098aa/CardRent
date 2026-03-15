package com.example.controller.front;

import com.example.common.Result;
import com.example.entity.car.Car;
import com.example.service.CarService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/front/car")
public class FrontCarController {

    @Resource
    private CarService carService;

    /**
     * 用户端分页查询车辆列表
     * 支持品牌、座位数、能源类型、价格区间筛选
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "8") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String brand,
                       @RequestParam(required = false) Integer seats,
                       @RequestParam(required = false) String energy,
                       @RequestParam(required = false) Integer minPrice,
                       @RequestParam(required = false) Integer maxPrice,
                       @RequestParam(required = false) String sortBy) { // sortBy: priceAsc, priceDesc, default
        PageInfo<Car> pageInfo = carService.selectFrontPage(pageNum, pageSize, keyword, brand, seats,
                energy, minPrice, maxPrice, sortBy);
        return Result.success(pageInfo);
    }

    /**
     * 查询车辆详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        Car car = carService.selectFrontDetail(id);
        return Result.success(car);
    }
    /**
     * 热门推荐 - 基于评价数量
     * 返回评分高、评价多的车辆
     */
    @GetMapping("/recommend/hot")
    public Result hotRecommend(@RequestParam(defaultValue = "4") Integer limit) {
        List<Car> cars = carService.selectHotRecommend(limit);
        return Result.success(cars);
    }

    /**
     * 车型推荐 - 基于评分
     * 返回评分高的车辆
     */
    @GetMapping("/recommend/rating")
    public Result ratingRecommend(@RequestParam(defaultValue = "4") Integer limit) {
        List<Car> cars = carService.selectRatingRecommend(limit);
        return Result.success(cars);
    }

    /**
     * 获取所有品牌列表
     */
    @GetMapping("/brands")
    public Result getBrands() {
        List<Map<String, Object>> brands = carService.selectBrandList();
        return Result.success(brands);
    }

    /**
     * 获取所有能源类型
     */
    @GetMapping("/energies")
    public Result getEnergies() {
        List<String> energies = carService.selectEnergyList();
        return Result.success(energies);
    }

    /**
     * 获取所有座位数选项
     */
    @GetMapping("/seats")
    public Result getSeats() {
        List<Integer> seats = carService.selectSeatsList();
        return Result.success(seats);
    }
}