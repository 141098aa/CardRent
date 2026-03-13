import request from '@/utils/request'

/**
 * 认证审核API
 * @module authApi
 */

/**
 * 分页查询认证列表
 * @param {Object} params 查询参数
 * @param {number} params.pageNum 当前页码
 * @param {number} params.pageSize 每页条数
 * @param {number} params.status 审核状态(0:待审核 1:已认证 2:审核失败)
 * @param {string} params.authType 认证类型(all/real_name/driver_license)
 * @returns {Promise}
 */
export function selectPage(params) {
  return request.get('/userAuth/selectPage', { params })
}

/**
 * 审核实名认证
 * @param {Object} data 审核数据
 * @param {number} data.id 认证ID
 * @param {number} data.status 审核状态(1:通过 2:拒绝)
 * @param {string} data.remark 审核备注
 * @returns {Promise}
 */
export function auditRealName(data) {
  return request.put('/userAuth/auditRealName', data)
}

/**
 * 审核驾驶证认证
 * @param {Object} data 审核数据
 * @param {number} data.id 认证ID
 * @param {number} data.status 审核状态(1:通过 2:拒绝)
 * @param {string} data.remark 审核备注
 * @returns {Promise}
 */
export function auditDriverLicense(data) {
  return request.put('/userAuth/auditDriverLicense', data)
}

/**
 * 批量审核
 * @param {Object} data 批量审核数据
 * @param {Array} data.ids 认证ID列表
 * @param {string} data.authType 认证类型(real_name/driver_license)
 * @param {number} data.status 审核状态(1:通过 2:拒绝)
 * @param {string} data.remark 审核备注
 * @returns {Promise}
 */
export function batchAudit(data) {
  return request.put('/userAuth/batchAudit', data)
}

/**
 * 获取认证统计信息
 * @returns {Promise}
 */
export function getStats() {
  return request.get('/userAuth/stats')
}
