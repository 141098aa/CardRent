import request from '@/utils/request'

/**
 * 用户个人信息API
 * @module userProfileApi
 */

/**
 * 获取用户信息
 * @param {number|string} userId 用户ID
 * @returns {Promise}
 */
export function getUserById(userId) {
  return request.get(`/user/selectById/${userId}`)
}

/**
 * 更新用户信息
 * @param {Object} data 用户信息
 * @returns {Promise}
 */
export function updateUser(data) {
  return request.put('/user/update', data)
}

/**
 * 上传头像
 * @param {FormData} formData 包含图片文件的FormData
 * @returns {Promise}
 */
export function uploadAvatar(formData) {
  return request.post('/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
