import request from './request'

export const getScreen3Overview = () => request.get('/screen3/overview')
export const getSnoreApneaBubble = (page = 1, size = 500) =>
  request.get('/screen3/snore-apnea-bubble', { params: { page, size } })
