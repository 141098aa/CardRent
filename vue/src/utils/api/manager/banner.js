import request from '@/utils/request'

/**
 * 分页查询轮播图列表
 */
export function getBannerList(params) {
  return request.get('/manager/banner/list', { params })
}

/**
 * 查询轮播图详情
 */
export function getBannerDetail(id) {
  return request.get(`/manager/banner/detail/${id}`)
}

/**
 * 新增轮播图
 */
export function addBanner(data) {
  return request.post('/manager/banner/add', data)
}

/**
 * 修改轮播图
 */
export function updateBanner(data) {
  return request.put('/manager/banner/update', data)
}

/**
 * 删除轮播图
 */
export function deleteBanner(id) {
  return request.delete(`/manager/banner/delete/${id}`)
}

/**
 * 批量删除轮播图
 */
export function batchDeleteBanner(ids) {
  return request.delete('/manager/banner/batchDelete', { data: ids })
}

/**
 * 更新状态
 */
export function updateBannerStatus(data) {
  return request.put('/manager/banner/updateStatus', data)
}
