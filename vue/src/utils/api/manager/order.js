import request from '@/utils/request'

/**
 * 分页查询订单
 */
export function list(params) {
  return request.get('/manager/order/list', { params })
}

/**
 * 查询订单详情
 */
export function detail(id) {
  return request.get(`/manager/order/detail/${id}`)
}

/**
 * 审核订单
 */
export function audit(data) {
  return request.put('/manager/order/audit', data)
}

/**
 * 确认取车
 */
export function confirmPickup(data) {
  return request.put('/manager/order/confirmPickup', data)
}

/**
 * 确认还车
 */
export function confirmReturn(data) {
  return request.put('/manager/order/confirmReturn', data)
}

/**
 * 取消订单
 */
export function cancel(data) {
  return request.put('/manager/order/cancel', data)
}

/**
 * 获取订单统计
 */
export function getStats() {
  return request.get('/manager/order/stats')
}
