import request from './request'

export function calculateAbcClassification() {
  return request({
    url: '/algorithms/abc-classification',
    method: 'get'
  })
}

export function forecastDemand(period) {
  return request({
    url: '/algorithms/demand-forecast',
    method: 'get',
    params: { period }
  })
}

export function forecastMaterialDemand(materialId, period) {
  return request({
    url: `/algorithms/demand-forecast/${materialId}`,
    method: 'get',
    params: { period }
  })
}

export function calculateReorderPoints() {
  return request({
    url: '/algorithms/reorder-points',
    method: 'get'
  })
}

export function calculateMaterialReorderPoint(materialId) {
  return request({
    url: `/algorithms/reorder-points/${materialId}`,
    method: 'get'
  })
}

export function detectAnomalies(date) {
  return request({
    url: '/algorithms/anomaly-detection',
    method: 'get',
    params: { date }
  })
}

export function getInventoryTurnoverRate() {
  return request({
    url: '/algorithms/charts/inventory-turnover',
    method: 'get'
  })
}

export function getAbcClassificationChartData() {
  return request({
    url: '/algorithms/charts/abc-classification',
    method: 'get'
  })
}

export function getMonthlyApplicationTrend() {
  return request({
    url: '/algorithms/charts/monthly-application-trend',
    method: 'get'
  })
}

export function getForecastVsActualComparison(materialId) {
  return request({
    url: `/algorithms/charts/forecast-vs-actual/${materialId}`,
    method: 'get'
  })
}

export function scheduleAbcClassification() {
  return request({
    url: '/algorithms/schedule/abc-classification',
    method: 'post'
  })
}

export function scheduleReorderPointCalculation() {
  return request({
    url: '/algorithms/schedule/reorder-point-calculation',
    method: 'post'
  })
}

export function scheduleDemandForecast() {
  return request({
    url: '/algorithms/schedule/demand-forecast',
    method: 'post'
  })
}