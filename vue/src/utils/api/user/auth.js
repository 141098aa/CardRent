import request from '@/utils/request'

/**
 * 用户认证API
 * @module userAuthApi
 */

/**
 * 提交实名认证
 * @param {Object} data 实名认证数据
 * @param {number} data.userId 用户ID
 * @param {string} data.realName 真实姓名
 * @param {string} data.idNumber 身份证号
 * @param {string} data.idFrontImage 身份证正面图片
 * @param {string} data.idBackImage 身份证背面图片
 * @returns {Promise}
 */
export function submitRealName(data) {
  return request.post('/userAuth/submitRealName', data)
}

/**
 * 提交驾驶证认证
 * @param {Object} data 驾驶证认证数据
 * @param {number} data.userId 用户ID
 * @param {string} data.driverName 驾驶人姓名
 * @param {string} data.licenseNumber 驾驶证号
 * @param {string} data.vehicleType 准驾车型
 * @param {string} data.validStart 有效期开始
 * @param {string} data.validEnd 有效期结束
 * @param {string} data.licenseFrontImage 驾驶证正面图片
 * @param {string} data.licenseBackImage 驾驶证背面图片
 * @returns {Promise}
 */
export function submitDriverLicense(data) {
  return request.post('/userAuth/submitDriverLicense', data)
}

/**
 * 查询实名认证状态
 * @param {number|string} userId 用户ID
 * @returns {Promise}
 */
export function getRealNameStatus(userId) {
  return request.get(`/userAuth/realName/status/${userId}`)
}

/**
 * 查询驾驶证认证状态
 * @param {number|string} userId 用户ID
 * @returns {Promise}
 */
export function getDriverLicenseStatus(userId) {
  return request.get(`/userAuth/driverLicense/status/${userId}`)
}
