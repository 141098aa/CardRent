// 导入各个模块的API
import * as adminApi from './manager/admin'
import * as userApi from './manager/user'
import * as carApi from './manager/car'
import * as orderApi from './manager/order'
import * as authApi from './manager/auth'
import * as userProfileApi from './user/profile'
import * as userAuthApi from './user/auth'
import * as userCarApi from './user/car'
import * as userFavoriteApi from './user/favorite'
import * as rechargeApi from './user/recharge'
import * as paymentPasswordApi from './user/paymentPassword'
import * as userOrderApi from './user/order'

// 统一导出
export {
  adminApi,
  userApi,
  carApi,
  orderApi,
  authApi,
  userProfileApi,
  userAuthApi,
  userCarApi,
  userFavoriteApi,
  rechargeApi,
  paymentPasswordApi,
  userOrderApi
}

// 默认导出
// export default {
//   admin: adminApi,
//   user: userApi,
//   car: carApi,
//   order: orderApi
// }
