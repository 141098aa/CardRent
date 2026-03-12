import request from '@/utils/request'

/**
 * 分页查询用户
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function selectPage(params) {
  return request.get('/user/selectPage', { params })
}

/**
 * 添加用户
 * @param {Object} data 用户信息
 * @returns {Promise}
 */
export function add(data) {
  return request.post('/user/add', data)
}

/**
 * 修改用户
 * @param {Object} data 用户信息
 * @returns {Promise}
 */
export function update(data) {
  return request.put('/user/update', data)
}

/**
 * 删除用户
 * @param {number|string} id 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  return request.delete(`/user/delete/${id}`)
}

/**
 * 根据ID查询用户
 * @param {number|string} id 用户ID
 * @returns {Promise}
 */
export function selectById(id) {
  return request.get(`/user/selectById/${id}`)
}
