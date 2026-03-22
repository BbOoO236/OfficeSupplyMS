import request from './request'

export function getMaterialList(params) {
  return request({
    url: '/materials',
    method: 'get',
    params
  })
}

export function getMaterialById(id) {
  return request({
    url: `/materials/${id}`,
    method: 'get'
  })
}

export function getMaterialByCode(code) {
  return request({
    url: `/materials/code/${code}`,
    method: 'get'
  })
}

export function createMaterial(data) {
  return request({
    url: '/materials',
    method: 'post',
    data
  })
}

export function updateMaterial(id, data) {
  return request({
    url: `/materials/${id}`,
    method: 'put',
    data
  })
}

export function deleteMaterial(id) {
  return request({
    url: `/materials/${id}`,
    method: 'delete'
  })
}

export function getLowStockMaterials() {
  return request({
    url: '/materials/low-stock',
    method: 'get'
  })
}

export function getNeedReorderMaterials() {
  return request({
    url: '/materials/need-reorder',
    method: 'get'
  })
}

export function stockIn(materialId, data) {
  return request({
    url: `/materials/${materialId}/stock-in`,
    method: 'post',
    params: data
  })
}

export function stockOut(materialId, data) {
  return request({
    url: `/materials/${materialId}/stock-out`,
    method: 'post',
    params: data
  })
}

export function getDailyConsumption(materialId) {
  return request({
    url: `/materials/${materialId}/daily-consumption`,
    method: 'get'
  })
}

export function calculateSafetyStock(materialId) {
  return request({
    url: `/materials/${materialId}/safety-stock`,
    method: 'get'
  })
}

export function calculateReorderPoint(materialId) {
  return request({
    url: `/materials/${materialId}/reorder-point`,
    method: 'get'
  })
}