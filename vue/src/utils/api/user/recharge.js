import request from '@/utils/request'

/**
 * 创建充值订单
 */
export function createRecharge(data) {
  return request.post('/front/recharge/create', data)
}

/**
 * 确认支付
 */
export function payRecharge(data) {
  return request.post('/front/recharge/pay', data)
}

/**
 * 查询充值记录列表
 */
export function getRechargeList(params) {
  return request.get('/front/recharge/list', { params })
}

/**
 * 查询充值记录详情
 */
export function getRechargeDetail(rechargeNo) {
  return request.get(`/front/recharge/detail/${rechargeNo}`)
}

/**
 * 取消充值订单
 */
export function cancelRecharge(data) {
  return request.post('/front/recharge/cancel', data)
}

/**
 * 重新支付充值订单
 */
export function repayRecharge(data) {
  return request.post('/front/recharge/repay', data)
}
