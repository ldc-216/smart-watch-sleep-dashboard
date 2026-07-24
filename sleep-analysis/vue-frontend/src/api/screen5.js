import request from './request'

export const searchRecords = (payload) => request.post('/screen5/search', payload)
export const importRecords = (records) => request.post('/screen5/import', records)
export const deleteRecord = (userId, dateRecorded) => request.delete(`/screen5/delete?userId=${userId}&dateRecorded=${dateRecorded}`)
export const batchDeleteRecords = (keys) => request.post('/screen5/batch-delete', keys)
export const updateRecord = (payload) => request.put('/screen5/update', payload)
export const getRangeLimits = () => request.get('/screen5/range-limits')
