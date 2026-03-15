import request from '@/utils/request'

// 车辆管理API

/**
 * 分页查询车辆列表
 */
export function selectPage(params) {
  return request.get('/car/selectPage', { params })
}

/**
 * 查询车辆详情
 */
export function selectById(id) {
  return request.get(`/car/selectById/${id}`)
}

/**
 * 添加车辆
 */
export function add(data) {
  return request.post('/car/add', data)
}

/**
 * 修改车辆
 */
export function update(data) {
  return request.put('/car/update', data)
}

/**
 * 删除车辆
 */
export function deleteCar(id) {
  return request.delete(`/car/delete/${id}`)
}

/**
 * 批量删除车辆
 */
export function batchDelete(ids) {
  return request.delete('/car/batchDelete', { data: ids })
}

/**
 * 更新车辆状态
 */
export function updateStatus(data) {
  return request.put('/car/updateStatus', data)
}

// 品牌管理API

/**
 * 分页查询品牌列表
 */
export function selectBrandPage(params) {
  return request.get('/car/brand/selectPage', { params })
}

/**
 * 查询所有品牌（下拉框用）
 */
export function listAllBrands() {
  return request.get('/car/brand/listAll')
}

/**
 * 添加品牌
 */
export function addBrand(data) {
  return request.post('/car/brand/add', data)
}

/**
 * 修改品牌
 */
export function updateBrand(data) {
  return request.put('/car/brand/update', data)
}

/**
 * 删除品牌
 */
export function deleteBrand(id) {
  return request.delete(`/car/brand/delete/${id}`)
}

// 分类管理API

/**
 * 分页查询分类列表
 */
export function selectCategoryPage(params) {
  return request.get('/car/category/selectPage', { params })
}

/**
 * 查询所有分类（下拉框用）
 */
export function listAllCategories() {
  return request.get('/car/category/listAll')
}

/**
 * 添加分类
 */
export function addCategory(data) {
  return request.post('/car/category/add', data)
}

/**
 * 修改分类
 */
export function updateCategory(data) {
  return request.put('/car/category/update', data)
}

/**
 * 删除分类
 */
export function deleteCategory(id) {
  return request.delete(`/car/category/delete/${id}`)
}
