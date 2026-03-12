import request from '@/utils/request'

/**
 * 分页查询订单
 * @param {Object} params 查询参数
 * @param {number} params.pageNum 当前页码
 * @param {number} params.pageSize 每页条数
 * @param {string} params.orderNo 订单号
 * @param {string} params.userName 用户名
 * @param {string} params.userPhone 用户手机号
 * @param {string} params.status 订单状态
 * @param {string} params.startDate 开始日期
 * @param {string} params.endDate 结束日期
 * @returns {Promise}
 */
export function selectPage(params) {
  return request.get('/order/selectPage', { params })
}

/**
 * 获取订单详情
 * @param {number|string} id 订单ID
 * @returns {Promise}
 */
export function selectById(id) {
  return request.get(`/order/selectById/${id}`)
}

/**
 * 创建订单
 * @param {Object} data 订单信息
 * @param {number} data.userId 用户ID
 * @param {number} data.carId 车辆ID
 * @param {string} data.pickupTime 取车时间
 * @param {string} data.returnTime 还车时间
 * @param {number} data.days 租车天数
 * @param {string} data.pickupLocation 取车地点
 * @param {string} data.returnLocation 还车地点
 * @param {number} data.totalPrice 总金额
 * @param {string} data.remark 备注
 * @returns {Promise}
 */
export function create(data) {
  return request.post('/order/create', data)
}

/**
 * 修改订单信息
 * @param {Object} data 订单信息
 * @returns {Promise}
 */
export function update(data) {
  return request.put('/order/update', data)
}

/**
 * 取消订单
 * @param {number|string} id 订单ID
 * @param {string} reason 取消原因
 * @returns {Promise}
 */
export function cancel(id, reason) {
  return request.put(`/order/cancel/${id}`, { reason })
}

/**
 * 确认取车
 * @param {number|string} id 订单ID
 * @returns {Promise}
 */
export function confirmPickup(id) {
  return request.put(`/order/pickup/${id}`)
}

/**
 * 申请还车
 * @param {number|string} id 订单ID
 * @returns {Promise}
 */
export function applyReturn(id) {
  return request.put(`/order/return/apply/${id}`)
}

/**
 * 确认还车
 * @param {number|string} id 订单ID
 * @param {Object} data 还车信息
 * @param {number} data.actualMileage 实际里程
 * @param {string} data.vehicleCondition 车辆状况
 * @param {Array} data.damageImages 损伤图片
 * @returns {Promise}
 */
export function confirmReturn(id, data) {
  return request.put(`/order/return/confirm/${id}`, data)
}

/**
 * 删除订单
 * @param {number|string} id 订单ID
 * @returns {Promise}
 */
export function deleteOrder(id) {
  return request.delete(`/order/delete/${id}`)
}

/**
 * 批量删除订单
 * @param {Array} ids 订单ID数组
 * @returns {Promise}
 */
export function batchDelete(ids) {
  return request.delete('/order/batchDelete', { data: ids })
}

/**
 * 获取订单统计信息
 * @returns {Promise}
 */
export function getOrderStats() {
  return request.get('/order/stats')
}

/**
 * 获取各状态订单数量
 * @returns {Promise}
 */
export function getOrderCountByStatus() {
  return request.get('/order/countByStatus')
}

/**
 * 获取用户的所有订单
 * @param {number|string} userId 用户ID
 * @param {Object} params 分页参数
 * @returns {Promise}
 */
export function getUserOrders(userId, params) {
  return request.get(`/order/user/${userId}`, { params })
}

/**
 * 获取今日订单数量
 * @returns {Promise}
 */
export function getTodayOrderCount() {
  return request.get('/order/today/count')
}

/**
 * 获取订单金额统计
 * @param {string} type 统计类型（day/week/month/year）
 * @returns {Promise}
 */
export function getOrderAmountStats(type) {
  return request.get('/order/amount/stats', { params: { type } })
}

/**
 * 导出订单数据
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function exportData(params) {
  return request.get('/order/export', {
    params,
    responseType: 'blob'
  })
}

/**
 * 支付订单
 * @param {number|string} id 订单ID
 * @param {Object} data 支付信息
 * @param {string} data.paymentMethod 支付方式
 * @param {string} data.paymentPassword 支付密码
 * @returns {Promise}
 */
export function pay(id, data) {
  return request.post(`/order/pay/${id}`, data)
}

/**
 * 申请退款
 * @param {number|string} id 订单ID
 * @param {string} reason 退款原因
 * @returns {Promise}
 */
export function applyRefund(id, reason) {
  return request.post(`/order/refund/apply/${id}`, { reason })
}

/**
 * 处理退款
 * @param {number|string} id 订单ID
 * @param {string} status 退款状态（approved/rejected）
 * @param {string} remark 处理备注
 * @returns {Promise}
 */
export function processRefund(id, status, remark) {
  return request.put(`/order/refund/process/${id}`, { status, remark })
}

/**
 * 获取订单日志
 * @param {number|string} id 订单ID
 * @returns {Promise}
 */
export function getOrderLogs(id) {
  return request.get(`/order/logs/${id}`)
}
