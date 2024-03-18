import request from '@/utils/request'

export function searchMessage(query) {
    return request({
      url: '/message/searchMessage',
      method: 'get',
      params: query
    })
  }