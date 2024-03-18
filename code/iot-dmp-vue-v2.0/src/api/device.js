import request from '@/utils/request'

export function createDevice(data) {
  return request({
    url: '/device/createDevice',
    method: 'post',
    data
  })
}

export function deleteDevice(device_id) {
  return request({
    url: '/device/deleteDevice',
    method: 'post',
    params: { device_id }
  })
}

export function getDataList(device_id) {
  return request({
    url: '/device/getDataList',
    method: 'get',
    params: { device_id }
  })
}

export function modifyDevice(data) {
  return request({
    url: '/device/modifyDevice',
    method: 'post',
    data
  })
}

export function searchDevice(query) {
  return request({
    url: '/device/searchDevice',
    method: 'get',
    params: query
  })
}

export function getRoute(device_name) {
  return request({
    url: '/device/getRoute',
    method: 'get',
    params: { device_name }
  })
}
