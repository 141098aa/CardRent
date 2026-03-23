import request from '@/utils/request'

/**
 * 用户端订单API
 */

/**
 * 创建订单
 * @param {Object} data 订单数据
 * @param {number} data.carId 车辆ID
 * @param {string} data.pickupTime 取车时间
 * @param {string} data.returnTime 还车时间
 * @param {string} data.remark 备注（可选）
 */
export function createOrder(data) {
  return request.post('/front/order/create', data)
}

/**
 * 查询我的订单列表
 * @param {Object} params 查询参数
 * @param {number} params.pageNum 页码
 * @param {number} params.pageSize 每页条数
 * @param {string} params.status 订单状态（可以是单个状态，也可以是逗号分隔的多个状态）
 */
export function getMyOrders(params) {
  return request.get('/front/order/myOrders', { params })
}

/**
 * 查询订单详情
 * @param {number|string} id 订单ID
 */
export function getOrderDetail(id) {
  return request.get(`/front/order/detail/${id}`)
}

/**
 * 取消订单
 * @param {Object} data 取消数据
 * @param {number} data.id 订单ID
 * @param {string} data.reason 取消原因
 */
export function cancelOrder(data) {
  return request.put('/front/order/cancel', data)
}

/**
 * 支付订单
 * @param {Object} data 支付数据
 * @param {number} data.id 订单ID
 * @param {string} data.paymentMethod 支付方式(alipay)
 * @param {string} data.paymentPassword 支付密码
 */
export function payOrder(data) {
  return request.post('/front/order/pay', data)
}

/**
 * 申请退款
 * @param {Object} data 退款数据
 * @param {number} data.id 订单ID
 * @param {string} data.reason 退款原因
 */
export function refundOrder(data) {
  return request.post('/front/order/refund', data)
}

/**
 * 获取订单统计（各状态数量）
 */
export function getOrderStats() {
  return request.get('/front/order/stats')
}

/**
 * 价格预览（租车前计算动态价格）
 * @param {Object} params 价格计算参数
 * @param {number} params.carId 车辆ID
 * @param {string} params.pickupTime 取车时间
 * @param {string} params.returnTime 还车时间
 */
export function previewPrice(params) {
  return request.get('/front/order/previewPrice', { params })
}
/**
 * 用户确认取车
 * @param {Object} data 确认数据
 * @param {number} data.id 订单ID
 */
export function confirmPickup(data) {
  return request.put('/front/order/userConfirmPickup', data)
}
/**
 * 用户申请还车
 */
export function applyReturn(data) {
  return request.put('/front/order/applyReturn', data)
}
