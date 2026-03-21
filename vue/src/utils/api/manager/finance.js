import request from '@/utils/request'

// 资金流水
export function getTransactions(params) {
  return request.get('/manager/finance/transactions', { params })
}

export function getStatistics(params) {
  return request.get('/manager/finance/statistics', { params })
}

export function getStatisticsByType(params) {
  return request.get('/manager/finance/statistics/by-type', { params })
}

// 退款管理
export function getRefunds(params) {
  return request.get('/manager/finance/refunds', { params })
}

export function getPendingRefundCount() {
  return request.get('/manager/finance/refunds/pending/count')
}

export function auditRefund(data) {
  return request.put('/manager/finance/refunds/audit', data)
}

export function batchAuditRefunds(data) {
  return request.put('/manager/finance/refunds/batch-audit', data)
}

// 押金管理
export function getDeposits(params) {
  return request.get('/manager/finance/deposits', { params })
}

export function getDepositStats() {
  return request.get('/manager/finance/deposits/stats')
}

export function deductDeposit(data) {
  return request.post('/manager/finance/deposits/deduct', data)
}
