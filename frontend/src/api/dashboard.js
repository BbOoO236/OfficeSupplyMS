import request from './request'

export function getAdminDashboard() {
  return request({
    url: '/dashboard/admin',
    method: 'get'
  })
}

export function getEmployeeDashboard(params) {
  return request({
    url: '/dashboard/employee',
    method: 'get',
    params
  })
}
