import request from '@/utils/request'

/**
 * 添加收藏
 */
export function addFavorite(data) {
  return request.post('/user/favorite/add', data)
}

/**
 * 取消收藏
 */
export function removeFavorite(targetId, targetType) {
  return request.delete(`/user/favorite/remove?targetId=${targetId}&targetType=${targetType}`)
}

/**
 * 获取收藏列表
 */
export function getFavoriteList(params) {
  return request.get('/user/favorite/list', { params })
}

/**
 * 检查是否已收藏
 */
export function checkFavorite(targetId, targetType) {
  return request.get('/user/favorite/check', { params: { targetId, targetType } })
}
