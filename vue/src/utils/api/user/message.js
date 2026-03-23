import request from '@/utils/request'

/**
 * 获取用户消息列表
 */
export function getMessages() {
  return request.get('/front/message/list')
}

/**
 * 获取未读消息数量
 */
export function getUnreadCount() {
  return request.get('/front/message/unread/count')
}

/**
 * 标记消息为已读
 */
export function markAsRead(id) {
  return request.put(`/front/message/read/${id}`)
}

/**
 * 全部标记为已读
 */
export function markAllAsRead() {
  return request.put('/front/message/read/all')
}

/**
 * 清空所有消息
 */
export function clearAll() {
  return request.delete('/front/message/clear')
}
