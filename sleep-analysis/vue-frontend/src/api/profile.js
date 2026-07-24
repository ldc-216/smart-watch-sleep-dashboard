import request from './request'

export function getProfileDetail(targetUserId) {
  return request({
    url: '/profile/detail',
    method: 'get',
    params: targetUserId ? { targetUserId } : {}
  })
}

export function updateProfileDetail(data) {
  return request({
    url: '/profile/update',
    method: 'post',
    data
  })
}

export function changePassword(oldPassword, newPassword) {
  return request({
    url: '/auth/update-password',
    method: 'post',
    data: { oldPassword, newPassword }
  })
}
