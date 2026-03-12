import request from '@/utils/request'

/**
 * 分页查询管理员
 * @param {Object} params 查询参数
 * @param {number} params.pageNum 当前页码
 * @param {number} params.pageSize 每页条数
 * @param {string} params.name 名称关键词
 * @returns {Promise}
 */
export function selectPage(params) {
  return request.get('/admin/selectPage', { params })
}

/**
 * 添加管理员
 * @param {Object} data 管理员信息
 * @returns {Promise}
 */
export function add(data) {
  return request.post('/admin/add', data)
}

/**
 * 修改管理员
 * @param {Object} data 管理员信息
 * @returns {Promise}
 */
export function update(data) {
  return request.put('/admin/update', data)
}

/**
 * 删除管理员
 * @param {number|string} id 管理员ID
 * @returns {Promise}
 */
export function deleteAdmin(id) {
  return request.delete(`/admin/delete/${id}`)
}

/**
 * 根据ID查询管理员
 * @param {number|string} id 管理员ID
 * @returns {Promise}
 */
export function selectById(id) {
  return request.get(`/admin/selectById/${id}`)
}
