import request from '@/utils/request'

/**
 * 用户端车辆API
 * @module frontCarApi
 */

/**
 * 分页查询车辆列表
 * @param {Object} params 查询参数
 * @param {number} params.pageNum 当前页码
 * @param {number} params.pageSize 每页条数
 * @param {string} params.keyword 搜索关键词
 * @param {string} params.brand 品牌名称
 * @param {number} params.seats 座位数
 * @param {string} params.energy 能源类型
 * @param {number} params.minPrice 最低价格
 * @param {number} params.maxPrice 最高价格
 * @param {string} params.sortBy 排序方式(priceAsc/priceDesc/default)
 */
export function getCarList(params) {
  return request.get('/front/car/list', { params })
}

/**
 * 查询车辆详情
 * @param {number|string} id 车辆ID
 */
export function getCarDetail(id) {
  return request.get(`/front/car/detail/${id}`)
}

/**
 * 热门推荐 - 基于评价数量（新用户或未登录时使用）
 * @param {number} limit 返回数量
 */
export function getHotRecommend(limit = 4) {
  return request.get('/front/car/recommend/hot', { params: { limit } })
}

/**
 * 车型推荐 - 基于评分
 * @param {number} limit 返回数量
 */
export function getRatingRecommend(limit = 4) {
  return request.get('/front/car/recommend/rating', { params: { limit } })
}
/**
 * 个性化推荐 - 基于用户历史行为（订单+收藏）
 * @param {number} limit 返回数量
 */
export function getPersonalizedRecommend(limit = 4) {
  return request.get('/front/car/recommend/personalized', { params: { limit } })
}

/**
 * 获取所有品牌列表
 */
export function getBrands() {
  return request.get('/front/car/brands')
}

/**
 * 获取所有能源类型
 */
export function getEnergies() {
  return request.get('/front/car/energies')
}

/**
 * 获取所有座位数选项
 */
export function getSeats() {
  return request.get('/front/car/seats')
}
