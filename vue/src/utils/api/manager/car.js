import request from '@/utils/request'

/**
 * 分页查询车辆
 * @param {Object} params 查询参数
 * @param {number} params.pageNum 当前页码
 * @param {number} params.pageSize 每页条数
 * @param {string} params.keyword 关键词搜索（品牌/型号）
 * @param {string} params.brand 品牌筛选
 * @param {string} params.category 分类筛选
 * @param {string} params.status 状态筛选
 * @param {number} params.minPrice 最低价格
 * @param {number} params.maxPrice 最高价格
 * @returns {Promise}
 */
export function selectPage(params) {
  return request.get('/car/selectPage', { params })
}

/**
 * 获取车辆详情
 * @param {number|string} id 车辆ID
 * @returns {Promise}
 */
export function selectById(id) {
  return request.get(`/car/selectById/${id}`)
}

/**
 * 添加车辆
 * @param {Object} data 车辆信息
 * @param {string} data.brand 品牌
 * @param {string} data.model 型号
 * @param {number} data.year 年份
 * @param {number} data.price 日租价格
 * @param {number} data.stock 库存
 * @param {string} data.status 状态（available/rented/maintenance）
 * @param {string} data.image 图片URL
 * @param {string} data.description 描述
 * @param {number} data.seats 座位数
 * @param {string} data.gear 变速箱
 * @param {string} data.energy 能源类型
 * @param {string} data.tag 标签
 * @param {number} data.mileage 里程
 * @param {string} data.displacement 排量
 * @param {Array} data.features 特色配置
 * @param {Array} data.configs 详细配置
 * @returns {Promise}
 */
export function add(data) {
  return request.post('/car/add', data)
}

/**
 * 修改车辆信息
 * @param {Object} data 车辆信息
 * @returns {Promise}
 */
export function update(data) {
  return request.put('/car/update', data)
}

/**
 * 删除车辆
 * @param {number|string} id 车辆ID
 * @returns {Promise}
 */
export function deleteCar(id) {
  return request.delete(`/car/delete/${id}`)
}

/**
 * 批量删除车辆
 * @param {Array} ids 车辆ID数组
 * @returns {Promise}
 */
export function batchDelete(ids) {
  return request.delete('/car/batchDelete', { data: ids })
}

/**
 * 更新车辆状态
 * @param {number|string} id 车辆ID
 * @param {string} status 状态（available/rented/maintenance）
 * @returns {Promise}
 */
export function updateStatus(id, status) {
  return request.put(`/car/updateStatus/${id}`, { status })
}

/**
 * 获取所有品牌列表
 * @returns {Promise}
 */
export function getBrandList() {
  return request.get('/car/brand/list')
}

/**
 * 获取所有分类列表
 * @returns {Promise}
 */
export function getCategoryList() {
  return request.get('/car/category/list')
}

/**
 * 获取热门推荐车辆
 * @param {number} limit 限制数量
 * @returns {Promise}
 */
export function getHotCars(limit = 4) {
  return request.get('/car/hot', { params: { limit } })
}

/**
 * 获取相似车型推荐
 * @param {number|string} id 车辆ID
 * @param {number} limit 限制数量
 * @returns {Promise}
 */
export function getSimilarCars(id, limit = 4) {
  return request.get(`/car/similar/${id}`, { params: { limit } })
}

/**
 * 上传车辆图片
 * @param {FormData} formData 包含图片文件的FormData
 * @returns {Promise}
 */
export function uploadImage(formData) {
  return request.post('/car/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传车辆图片
 * @param {FormData} formData 包含多个图片文件的FormData
 * @returns {Promise}
 */
export function uploadImages(formData) {
  return request.post('/car/upload/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出车辆数据
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function exportData(params) {
  return request.get('/car/export', {
    params,
    responseType: 'blob'
  })
}

/**
 * 导入车辆数据
 * @param {FormData} formData 包含Excel文件的FormData
 * @returns {Promise}
 */
export function importData(formData) {
  return request.post('/car/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
