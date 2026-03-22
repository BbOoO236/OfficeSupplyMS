import request from './request'

export function createInventoryCheck(data) {
  return request({
    url: '/inventory-checks',
    method: 'post',
    data
  })
}

export function getInventoryCheckById(id) {
  return request({
    url: `/inventory-checks/${id}`,
    method: 'get'
  })
}

export function getInventoryCheckByNo(checkNo) {
  return request({
    url: `/inventory-checks/no/${checkNo}`,
    method: 'get'
  })
}

export function getInventoryCheckList(params) {
  return request({
    url: '/inventory-checks',
    method: 'get',
    params
  })
}

export function getInventoryChecksByMaterialId(materialId) {
  return request({
    url: `/inventory-checks/material/${materialId}`,
    method: 'get'
  })
}

export function getInventoryChecksByTimeRange(startTime, endTime) {
  return request({
    url: '/inventory-checks/time-range',
    method: 'get',
    params: { startTime, endTime }
  })
}

export function getUnadjustedInventoryChecks() {
  return request({
    url: '/inventory-checks/unadjusted',
    method: 'get'
  })
}

export function getInventoryChecksWithDifference() {
  return request({
    url: '/inventory-checks/with-difference',
    method: 'get'
  })
}

export function markAsAdjusted(checkId) {
  return request({
    url: `/inventory-checks/${checkId}/mark-adjusted`,
    method: 'put'
  })
}

export function adjustStockByCheck(checkId) {
  return request({
    url: `/inventory-checks/${checkId}/adjust-stock`,
    method: 'put'
  })
}

export function getRecentInventoryChecks(limit = 10) {
  return request({
    url: '/inventory-checks/recent',
    method: 'get',
    params: { limit }
  })
}

export function sumDifferenceByMaterialId(materialId) {
  return request({
    url: `/inventory-checks/material/${materialId}/total-difference`,
    method: 'get'
  })
}