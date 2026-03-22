import request from './request'

export function createStockIn(data) {
  return request({
    url: '/stock-ins',
    method: 'post',
    data
  })
}

export function getStockInById(id) {
  return request({
    url: `/stock-ins/${id}`,
    method: 'get'
  })
}

export function getStockInByNo(stockInNo) {
  return request({
    url: `/stock-ins/no/${stockInNo}`,
    method: 'get'
  })
}

export function getStockInList(params) {
  return request({
    url: '/stock-ins',
    method: 'get',
    params
  })
}

export function getStockInsByMaterialId(materialId) {
  return request({
    url: `/stock-ins/material/${materialId}`,
    method: 'get'
  })
}

export function getStockInsByTimeRange(startTime, endTime) {
  return request({
    url: '/stock-ins/time-range',
    method: 'get',
    params: { startTime, endTime }
  })
}

export function getRecentStockIns(limit = 10) {
  return request({
    url: '/stock-ins/recent',
    method: 'get',
    params: { limit }
  })
}

export function sumQuantityByMaterialId(materialId) {
  return request({
    url: `/stock-ins/material/${materialId}/total-quantity`,
    method: 'get'
  })
}

export function sumAmountByMaterialId(materialId) {
  return request({
    url: `/stock-ins/material/${materialId}/total-amount`,
    method: 'get'
  })
}