// 导入各个模块的API
import * as adminApi from './manager/admin'
import * as userApi from './manager/user'
import * as carApi from './manager/car'
import * as orderApi from './manager/order'

// 统一导出
export { adminApi, userApi, carApi, orderApi }

// 也可以使用默认导出
// export default {
//   admin: adminApi,
//   user: userApi,
//   car: carApi,
//   order: orderApi
// }
