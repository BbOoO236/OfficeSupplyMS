import request from './request'

export function getApplicationList(params) {
  return request({
    url: '/applications/all',
    method: 'get',
    params
  })
}

export function getMyApplications(params) {
  return request({
    url: '/applications/my',
    method: 'get',
    params
  })
}

export function getApplicationById(id) {
  return request({
    url: `/applications/${id}`,
    method: 'get'
  })
}

export function getApplicationByNo(applicationNo) {
  return request({
    url: `/applications/no/${applicationNo}`,
    method: 'get'
  })
}

export function createApplication(data) {
  return request({
    url: '/applications',
    method: 'post',
    data
  })
}

export function approveApplication(data) {
  return request({
    url: '/applications/approve',
    method: 'put',
    data
  })
}

export function outboundApplication(id) {
  return request({
    url: `/applications/${id}/outbound`,
    method: 'put'
  })
}

export function getPendingApplications() {
  return request({
    url: '/applications/pending',
    method: 'get'
  })
}

export function getToBeOutboundApplications() {
  return request({
    url: '/applications/to-be-outbound',
    method: 'get'
  })
}

export function getAbnormalApplications() {
  return request({
    url: '/applications/abnormal',
    method: 'get'
  })
}

export function getTop10MaterialsByApplication() {
  return request({
    url: '/applications/stats/top10-materials',
    method: 'get'
  })
}

export function getMonthlyApplicationStats() {
  return request({
    url: '/applications/stats/monthly',
    method: 'get'
  })
}