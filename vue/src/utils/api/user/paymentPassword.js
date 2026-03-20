import request from '@/utils/request'

/**
 * 设置支付密码
 */
export function setPassword(data) {
  return request.post('/front/payment-password/set', data)
}

/**
 * 验证支付密码
 */
export function verifyPassword(data) {
  return request.post('/front/payment-password/verify', data)
}

/**
 * 获取锁定状态
 */
export function getLockStatus() {
  return request.get('/front/payment-password/lock-status')
}

/**
 * 发送重置验证码
 */
export function sendResetCode() {
  return request.post('/front/payment-password/send-reset-code')
}

/**
 * 重置密码
 */
export function resetPassword(data) {
  return request.post('/front/payment-password/reset', data)
}
