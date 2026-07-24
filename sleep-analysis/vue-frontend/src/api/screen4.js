import request from './request'

export const predictSleepScore = (payload) => request.post('/screen4/predict', payload)
export const getFeatureImportance = () => request.get('/screen4/feature-importance')
export const getModelMetrics = () => request.get('/screen4/model-metrics')
export const getClusterResult = () => request.get('/screen4/cluster')
