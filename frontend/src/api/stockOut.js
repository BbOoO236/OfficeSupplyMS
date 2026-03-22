import request from './request'

export function createStockOut(data) {
  return request({
    url: '/stock-outs',
    method: 'post',
    data
  })
}

export function getStockOutById(id) {
  return request({
    url: `/stock-outs/${id}`,
    method: 'get'
  })
}

export function getStockOutByNo(stockOutNo) {
  return request({
    url: `/stock-outs/no/${stockOutNo}`,
    method: 'get'
  })
}

export function getStockOutList(params) {
  return request({
    url: '/stock-outs',
    method: 'get',
    params
  })
}

export function getStockOutsByMaterialId(materialId) {
  return request({
    url: `/stock-outs/material/${materialId}`,
    method: 'get'
  })
}

export function getStockOutsByType(type) {
  return request({
    url: `/stock-outs/type/${type}`,
    method: 'get'
  })
}

export function getStockOutByApplicationId(applicationId) {
  return request({
    url: `/stock-outs/application/${applicationId}`,
    method: 'get'
  })
}

export function getStockOutsByTimeRange(startTime, endTime) {
  return request({
    url: '/stock-outs/time-range',
    method: 'get',
    params: { startTime, endTime }
  })
}

export function getRecentStockOuts(limit = 10) {
  return request({
    url: '/stock-outs/recent',
    method: 'get',
    params: { limit }
  })
}

export function sumQuantityByMaterialId(materialId) {
  return request({
    url: `/stock-outs/material/${materialId}/total-quantity`,
    method: 'get'
  })
}