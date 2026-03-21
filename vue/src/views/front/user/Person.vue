<template>
  <div class="profile-container">
    <div class="profile-card">
      <!-- 头部 -->
      <div class="profile-header">
        <h2 class="profile-title">个人信息</h2>
        <p class="profile-subtitle">管理您的账户信息</p>
      </div>

      <!-- 表单 -->
      <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="100px" class="profile-form">
        <!-- 头像上传 -->
        <el-form-item prop="avatar" label="头像" class="avatar-item">
          <el-upload
            class="avatar-uploader"
            :action="baseUrl + '/files/upload'"
            :show-file-list="false"
            :on-success="handleFileUpload"
            :before-upload="beforeImageUpload">
            <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar" />
            <div v-else class="avatar-placeholder">
              <el-icon class="avatar-icon"><Plus /></el-icon>
              <span>上传头像</span>
            </div>
          </el-upload>
        </el-form-item>

        <!-- 账号（不可修改） -->
        <el-form-item prop="username" label="账号">
          <div class="info-text">{{ data.user.username }}</div>
        </el-form-item>

        <!-- 姓名 -->
        <el-form-item prop="name" label="姓名">
          <el-input v-model="data.user.name" placeholder="请输入您的姓名" class="custom-input" clearable />
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item prop="phone" label="手机号">
          <el-input v-model="data.user.phone" placeholder="请输入手机号" class="custom-input" clearable />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item prop="email" label="邮箱">
          <el-input v-model="data.user.email" placeholder="请输入邮箱" class="custom-input" clearable />
        </el-form-item>

        <!-- 账户余额 -->
        <el-form-item prop="account" label="账户余额">
          <div class="balance-box" @click="goToRecharge">
            <span class="balance-label">可用余额</span>
            <span class="balance-amount">¥{{ data.user.account || '0.00' }}</span>
            <span class="recharge-text">
              充值
              <el-icon class="recharge-icon"><ArrowRight /></el-icon>
            </span>
          </div>
        </el-form-item>
      </el-form>

      <!-- 身份认证区块 - 独立区域 -->
      <div class="verification-section" ref="verificationSection">
        <div class="verification-header">
          <h3 class="verification-title">
            <el-icon><UserFilled /></el-icon>
            身份认证
          </h3>
          <el-tag :type="verificationStatus.type" size="small" effect="light">
            {{ verificationStatus.text }}
          </el-tag>
        </div>

        <div class="verification-content">
          <!-- 实名认证 -->
          <div class="verification-item">
            <div class="item-info">
              <span class="item-label">实名认证</span>
              <span class="item-desc">需年满18周岁，需上传身份证正反面</span>
            </div>
            <div
              class="item-status"
              :class="{
                verified: data.user.realNameVerified === true,
                pending: data.user.realNameStatus === 0,
                failed: data.user.realNameStatus === 2
              }">
              <el-icon v-if="data.user.realNameVerified"><CircleCheckFilled /></el-icon>
              <el-icon v-else-if="data.user.realNameStatus === 0"><Loading /></el-icon>
              <el-icon v-else-if="data.user.realNameStatus === 2"><WarningFilled /></el-icon>
              <span>{{
                data.user.realNameVerified
                  ? '已认证'
                  : data.user.realNameStatus === 0
                    ? '审核中'
                    : data.user.realNameStatus === 2
                      ? '审核失败'
                      : '未认证'
              }}</span>
            </div>
            <el-button
              v-if="!data.user.realName && !data.user.realNameStatus"
              type="primary"
              size="small"
              class="verify-btn"
              @click="openRealNameDialog">
              去认证
            </el-button>
            <el-button
              v-else-if="data.user.realNameStatus === 2"
              type="danger"
              size="small"
              class="verify-btn"
              @click="showRealNameDetail = true">
              查看原因
            </el-button>
            <el-button v-else-if="data.user.realNameStatus === 0" size="small" class="verify-btn" disabled>
              审核中
            </el-button>
            <el-button v-else size="small" class="verify-btn" @click="showRealNameDetail = true"> 查看 </el-button>
          </div>

          <!-- 驾驶证上传 -->
          <div
            class="verification-item"
            :class="{ 'highlight-item': highlightAuth === 'driverLicense' }"
            ref="driverLicenseItemRef"
            @mouseenter="hoverAuth = 'driverLicense'"
            @mouseleave="hoverAuth = null">
            <div class="item-info">
              <span class="item-label">驾驶证认证</span>
              <span class="item-desc">上传驾驶证正本照片，审核通过后可租车</span>
            </div>
            <div
              class="item-status"
              :class="{
                verified: data.user.driverLicenseVerified === true,
                pending: data.user.driverLicenseStatus === 0,
                failed: data.user.driverLicenseStatus === 2
              }">
              <el-icon v-if="data.user.driverLicenseVerified"><CircleCheckFilled /></el-icon>
              <el-icon v-else-if="data.user.driverLicenseStatus === 0"><Loading /></el-icon>
              <el-icon v-else-if="data.user.driverLicenseStatus === 2"><WarningFilled /></el-icon>
              <span>{{
                data.user.driverLicenseVerified
                  ? '已认证'
                  : data.user.driverLicenseStatus === 0
                    ? '审核中'
                    : data.user.driverLicenseStatus === 2
                      ? '审核失败'
                      : '未认证'
              }}</span>
            </div>
            <!-- 未认证状态 - 显示上传按钮 -->
            <el-button
              v-if="
                !data.user.driverLicenseVerified &&
                data.user.driverLicenseStatus !== 0 &&
                data.user.driverLicenseStatus !== 2
              "
              type="primary"
              size="small"
              class="verify-btn"
              @click="openDriverLicenseDialog">
              上传
            </el-button>
            <!-- 审核中状态 - 显示禁用按钮 -->
            <el-button v-else-if="data.user.driverLicenseStatus === 0" size="small" class="verify-btn" disabled>
              审核中
            </el-button>
            <!-- 审核失败状态 - 显示查看原因按钮 -->
            <el-button
              v-else-if="data.user.driverLicenseStatus === 2"
              type="danger"
              size="small"
              class="verify-btn"
              @click="showLicenseDetail = true">
              查看原因
            </el-button>
            <!-- 已认证状态 - 显示查看按钮 -->
            <el-button
              v-else-if="data.user.driverLicenseVerified"
              size="small"
              class="verify-btn"
              @click="showLicenseDetail = true"
              >查看
            </el-button>
          </div>

          <!-- 芝麻信用（可选） -->
          <div class="verification-item" v-if="showCreditOption">
            <div class="item-info">
              <span class="item-label">芝麻信用</span>
              <span class="item-desc">授权后可免押金租车</span>
            </div>
            <div class="item-status" :class="{ verified: data.user.creditAuthorized }">
              <el-icon v-if="data.user.creditAuthorized"><CircleCheckFilled /></el-icon>
              <span>{{ data.user.creditAuthorized ? '已授权' : '未授权' }}</span>
            </div>
            <el-button v-if="!data.user.creditAuthorized" size="small" class="verify-btn" @click="authorizeCredit">
              授权
            </el-button>
          </div>
        </div>

        <!-- 认证说明 -->
        <div class="verification-note" v-if="showVerificationNote">
          <el-alert type="info" :closable="false" show-icon>
            <template #default>
              {{
                data.user.realNameVerified && !data.user.driverLicenseVerified
                  ? '驾驶证认证完成后即可租车。认证信息仅用于租车审核，我们将严格保护您的隐私。'
                  : !data.user.realNameVerified && data.user.driverLicenseVerified
                    ? '实名认证完成后即可租车。认证信息仅用于租车审核，我们将严格保护您的隐私。'
                    : '完成实名认证和驾驶证认证后即可租车。认证信息仅用于租车审核，我们将严格保护您的隐私。'
              }}
            </template>
          </el-alert>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button type="primary" class="submit-btn" @click="update" :loading="loading"> 保存修改 </el-button>
        <el-button class="reset-btn" @click="resetForm">取消</el-button>
      </div>
    </div>

    <!-- 实名认证对话框 -->
    <el-dialog v-model="showRealNameDialog" title="实名认证" width="550px" destroy-on-close>
      <el-form :model="realNameForm" label-width="100px" ref="realNameFormRef" :rules="realNameRules">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="realNameForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="realNameForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="realNameForm.idNumber" placeholder="请输入身份证号" />
        </el-form-item>

        <!-- 身份证正反面并排显示 -->
        <el-form-item label="身份证照片" required class="id-card-section">
          <div class="id-card-row">
            <div class="id-card-item">
              <div class="id-card-label">身份证正面</div>
              <el-upload
                class="id-card-uploader"
                :action="baseUrl + '/files/upload'"
                :show-file-list="false"
                :on-success="handleIdFrontUpload"
                :before-upload="beforeImageUpload">
                <div class="upload-card" :class="{ 'has-image': realNameForm.idFrontImage }">
                  <img v-if="realNameForm.idFrontImage" :src="realNameForm.idFrontImage" class="id-image" />
                  <div v-else class="upload-placeholder">
                    <el-icon><Plus /></el-icon>
                    <span>上传正面</span>
                  </div>
                </div>
              </el-upload>
            </div>

            <div class="id-card-item">
              <div class="id-card-label">身份证背面</div>
              <el-upload
                class="id-card-uploader"
                :action="baseUrl + '/files/upload'"
                :show-file-list="false"
                :on-success="handleIdBackUpload"
                :before-upload="beforeImageUpload">
                <div class="upload-card" :class="{ 'has-image': realNameForm.idBackImage }">
                  <img v-if="realNameForm.idBackImage" :src="realNameForm.idBackImage" class="id-image" />
                  <div v-else class="upload-placeholder">
                    <el-icon><Plus /></el-icon>
                    <span>上传背面</span>
                  </div>
                </div>
              </el-upload>
            </div>
          </div>
          <div class="upload-tip">请上传清晰的身份证照片，确保信息可识别</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRealNameDialog = false">取 消</el-button>
          <el-button type="primary" @click="submitRealName" :loading="realNameLoading">提 交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看实名认证详情 -->
    <el-dialog v-model="showRealNameDetail" title="实名认证信息" width="600px">
      <div class="detail-content">
        <div class="detail-item">
          <span class="detail-label">真实姓名：</span>
          <span class="detail-value">{{ data.user.realName || '--' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">身份证号：</span>
          <span class="detail-value">{{ formatIdNumber(data.user.idNumber) || '--' }}</span>
        </div>
        <!-- 添加审核备注显示 -->
        <div class="detail-item" v-if="data.user.realNameRemark">
          <span class="detail-label">审核备注：</span>
          <span class="detail-value" style="color: #f56c6c">{{ data.user.realNameRemark }}</span>
        </div>
        <div class="detail-images">
          <div class="image-item">
            <span class="image-label">身份证正面</span>
            <el-image
              :src="data.user.idFrontImage"
              class="detail-image"
              :preview-src-list="[data.user.idFrontImage]"
              fit="contain">
              <template #error>
                <div class="image-error">暂无图片</div>
              </template>
            </el-image>
          </div>
          <div class="image-item">
            <span class="image-label">身份证背面</span>
            <el-image
              :src="data.user.idBackImage"
              class="detail-image"
              :preview-src-list="[data.user.idBackImage]"
              fit="contain">
              <template #error>
                <div class="image-error">暂无图片</div>
              </template>
            </el-image>
          </div>
        </div>
      </div>

      <!-- 底部按钮区域 - 新增 -->
      <template #footer>
        <div class="dialog-footer" style="display: flex; justify-content: center; gap: 12px">
          <el-button @click="showRealNameDetail = false">关 闭</el-button>
          <!-- 当审核失败时显示重新上传按钮 -->
          <el-button v-if="data.user.realNameStatus === 2" type="primary" @click="handleReuploadRealName">
            重新上传
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 驾驶证上传对话框 -->
    <el-dialog v-model="showDriverLicenseDialog" title="驾驶证认证" width="550px" destroy-on-close>
      <div class="upload-content">
        <el-form :model="licenseForm" label-width="100px" ref="licenseFormRef" :rules="licenseRules">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="licenseForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="licenseForm.name" placeholder="请输入驾驶证上的姓名" />
          </el-form-item>
          <el-form-item label="驾驶证号" prop="licenseNumber">
            <el-input v-model="licenseForm.licenseNumber" placeholder="请输入驾驶证号" />
          </el-form-item>
          <el-form-item label="准驾车型" prop="vehicleType">
            <el-input v-model="licenseForm.vehicleType" placeholder="例如：C1" />
          </el-form-item>
          <el-form-item label="有效期限" prop="validPeriod">
            <el-date-picker
              v-model="licenseForm.validPeriod"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
              :disabled-date="disabledDate" />
          </el-form-item>

          <!-- 驾驶证正面和副页并排 -->
          <el-form-item label="驾驶证照片" required class="license-section">
            <div class="license-row">
              <div class="license-item">
                <div class="license-label">驾驶证正面</div>
                <el-upload
                  class="license-uploader"
                  :action="baseUrl + '/files/upload'"
                  :show-file-list="false"
                  :on-success="handleLicenseFrontUpload"
                  :before-upload="beforeImageUpload">
                  <div class="upload-card" :class="{ 'has-image': licenseForm.licenseFrontImage }">
                    <img
                      v-if="licenseForm.licenseFrontImage"
                      :src="licenseForm.licenseFrontImage"
                      class="license-image" />
                    <div v-else class="upload-placeholder">
                      <el-icon><Plus /></el-icon>
                      <span>上传正面</span>
                    </div>
                  </div>
                </el-upload>
              </div>

              <div class="license-item">
                <div class="license-label">驾驶证副页</div>
                <el-upload
                  class="license-uploader"
                  :action="baseUrl + '/files/upload'"
                  :show-file-list="false"
                  :on-success="handleLicenseBackUpload"
                  :before-upload="beforeImageUpload">
                  <div class="upload-card" :class="{ 'has-image': licenseForm.licenseBackImage }">
                    <img
                      v-if="licenseForm.licenseBackImage"
                      :src="licenseForm.licenseBackImage"
                      class="license-image" />
                    <div v-else class="upload-placeholder">
                      <el-icon><Plus /></el-icon>
                      <span>上传副页(可选)</span>
                    </div>
                  </div>
                </el-upload>
              </div>
            </div>
            <div class="upload-tip">请上传清晰的原图照片，确保信息可识别</div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDriverLicenseDialog = false">取 消</el-button>
          <el-button type="primary" @click="submitLicense" :loading="licenseLoading">提 交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看驾驶证详情 -->
    <el-dialog v-model="showLicenseDetail" title="驾驶证信息" width="600px">
      <div class="detail-content">
        <div class="detail-item">
          <span class="detail-label">姓名：</span>
          <span class="detail-value">{{ data.user.driverName || '--' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">驾驶证号：</span>
          <span class="detail-value">{{ data.user.licenseNumber || '--' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">准驾车型：</span>
          <span class="detail-value">{{ data.user.vehicleType || '--' }}</span>
        </div>
        <div class="detail-item" v-if="data.user.driverLicenseRemark">
          <span class="detail-label">审核备注：</span>
          <span class="detail-value" style="color: #f56c6c">{{ data.user.driverLicenseRemark }}</span>
        </div>
        <div class="detail-images">
          <div class="image-item">
            <span class="image-label">驾驶证正面</span>
            <el-image
              :src="data.user.licenseFrontImage"
              class="detail-image"
              :preview-src-list="[data.user.licenseFrontImage]"
              fit="contain">
              <template #error>
                <div class="image-error">暂无图片</div>
              </template>
            </el-image>
          </div>
          <div v-if="data.user.licenseBackImage" class="image-item">
            <span class="image-label">驾驶证副页</span>
            <el-image
              :src="data.user.licenseBackImage"
              class="detail-image"
              :preview-src-list="[data.user.licenseBackImage]"
              fit="contain">
              <template #error>
                <div class="image-error">暂无图片</div>
              </template>
            </el-image>
          </div>
        </div>
      </div>

      <!-- 底部按钮区域 - 新增 -->
      <template #footer>
        <div class="dialog-footer" style="display: flex; justify-content: center; gap: 12px">
          <el-button @click="showLicenseDetail = false">关 闭</el-button>
          <!-- 当审核失败时显示重新上传按钮 -->
          <el-button v-if="data.user.driverLicenseStatus === 2" type="primary" @click="handleReuploadLicense">
            重新上传
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Plus,
  ArrowRight,
  UserFilled,
  CircleCheckFilled,
  InfoFilled,
  Loading,
  WarningFilled
} from '@element-plus/icons-vue'
import { userProfileApi, userAuthApi } from '@/utils/api'

const highlightAuth = ref(null)
const hoverAuth = ref(null)

// 创建 route 和 router 实例
const route = useRoute()
const router = useRouter()

const verificationSection = ref(null)
const emit = defineEmits(['updateUser'])
const loading = ref(false)
const realNameLoading = ref(false)
const licenseLoading = ref(false)

const baseUrl = import.meta.env.VITE_BASE_URL
const formRef = ref()
const realNameFormRef = ref()
const licenseFormRef = ref()
const realNameItemRef = ref(null)
const driverLicenseItemRef = ref(null)

// 对话框显示控制
const showRealNameDialog = ref(false)
const showRealNameDetail = ref(false)
const showDriverLicenseDialog = ref(false)
const showLicenseDetail = ref(false)
const showCreditOption = ref(true)

// 实名认证表单
const realNameForm = reactive({
  realName: '',
  idNumber: '',
  phone: '',
  idFrontImage: '',
  idBackImage: ''
})

// 实名认证表单验证规则
const realNameRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    {
      // 15位全数字 或 18位前17位数字最后一位数字或X/x，仅验证长度和字符类型
      pattern: /^\d{15}$|^\d{17}[\dXx]$/,
      message: '身份证号应为15位数字或18位数字（最后一位可为X）',
      trigger: 'blur'
    }
  ],
  idFrontImage: [{ required: true, message: '请上传身份证正面', trigger: 'change' }],
  idBackImage: [{ required: true, message: '请上传身份证背面', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ]
}

// 驾驶证表单
const licenseForm = reactive({
  name: '',
  licenseNumber: '',
  vehicleType: '',
  phone: '',
  validPeriod: [],
  licenseFrontImage: '',
  licenseBackImage: ''
})

// 驾驶证表单验证规则
const licenseRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  licenseNumber: [{ required: true, message: '请输入驾驶证号', trigger: 'blur' }],
  vehicleType: [{ required: true, message: '请输入准驾车型', trigger: 'blur' }],
  validPeriod: [{ required: true, message: '请选择有效期限', trigger: 'change' }],
  licenseFrontImage: [{ required: true, message: '请上传驾驶证正面', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ]
}
// 打开实名认证对话框（从"去认证"按钮触发）
const openRealNameDialog = () => {
  // 预填充当前手机号
  realNameForm.phone = data.user.phone || ''
  // 其他字段清空
  realNameForm.realName = ''
  realNameForm.idNumber = ''
  realNameForm.idFrontImage = ''
  realNameForm.idBackImage = ''
  showRealNameDialog.value = true
}

// 打开驾驶证认证对话框（从"上传"按钮触发）
const openDriverLicenseDialog = () => {
  // 预填充当前手机号
  licenseForm.phone = data.user.phone || ''
  // 其他字段清空
  licenseForm.name = ''
  licenseForm.licenseNumber = ''
  licenseForm.vehicleType = ''
  licenseForm.validPeriod = []
  licenseForm.licenseFrontImage = ''
  licenseForm.licenseBackImage = ''
  showDriverLicenseDialog.value = true
}
// 禁用过去的日期（只能选择今天及之后的日期）
const disabledDate = (time) => {
  // 获取今天的开始时间（00:00:00）
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  // 返回 true 表示禁用该日期
  return time.getTime() < today.getTime()
}

// 是否显示认证说明（两个都认证通过就不显示）
const showVerificationNote = computed(() => {
  return !(data.user.realNameVerified && data.user.driverLicenseVerified)
})

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules: {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    email: [{ type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }]
  }
})

if (!data.user?.id) {
  ElMessage.error('请登录!')
  router.push('/login')
}

// 认证状态计算
const verificationStatus = computed(() => {
  const realNameVerified = data.user.realNameVerified || false
  const driverLicenseVerified = data.user.driverLicenseVerified || false
  const hasRealNamePending = data.user.realName && !data.user.realNameVerified
  const hasDriverLicensePending = data.user.driverName && !data.user.driverLicenseVerified

  if (realNameVerified && driverLicenseVerified) {
    return { type: 'success', text: '已全部认证' }
  } else if (hasRealNamePending || hasDriverLicensePending) {
    return { type: 'warning', text: '审核中' }
  } else if (realNameVerified || driverLicenseVerified) {
    return { type: 'warning', text: '部分认证' }
  } else {
    return { type: 'danger', text: '未认证' }
  }
})

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 加载认证状态
const loadAuthStatus = () => {
  // 查询实名认证状态
  userAuthApi
    .getRealNameStatus(data.user.id)
    .then((res) => {
      if (res.code === '200' && res.data) {
        const status = res.data.status
        data.user.realNameStatus = status
        data.user.realNameVerified = status === 1
        if (res.data.realName) {
          data.user.realName = res.data.realName
          data.user.idNumber = res.data.idNumber
          data.user.idFrontImage = res.data.idFrontImage
          data.user.idBackImage = res.data.idBackImage
          data.user.realNameStatus = status
          data.user.realNameRemark = res.data.auditRemark
        } else {
          clearRealNameData()
        }
      } else {
        clearRealNameData()
      }
    })
    .catch(() => {})

  // 查询驾驶证认证状态
  userAuthApi
    .getDriverLicenseStatus(data.user.id)
    .then((res) => {
      if (res.code === '200' && res.data) {
        const status = res.data.status
        data.user.driverLicenseStatus = status
        data.user.driverLicenseVerified = status === 1
        if (res.data.driverName) {
          data.user.driverName = res.data.driverName
          data.user.licenseNumber = res.data.licenseNumber
          data.user.vehicleType = res.data.vehicleType
          data.user.licenseFrontImage = res.data.licenseFrontImage
          data.user.licenseBackImage = res.data.licenseBackImage
          data.user.driverLicenseStatus = status
          data.user.driverLicenseRemark = res.data.auditRemark
        } else {
          clearDriverLicenseData()
        }
      } else {
        clearDriverLicenseData()
      }
    })
    .catch(() => {})
}

// 清空实名认证数据
const clearRealNameData = () => {
  data.user.realName = null
  data.user.idNumber = null
  data.user.idFrontImage = null
  data.user.idBackImage = null
  data.user.realNameStatus = null
  data.user.realNameRemark = null
  data.user.realNameVerified = false // 重置为false
}

// 清空驾驶证认证数据
const clearDriverLicenseData = () => {
  data.user.driverName = null
  data.user.licenseNumber = null
  data.user.vehicleType = null
  data.user.licenseFrontImage = null
  data.user.licenseBackImage = null
  data.user.driverLicenseStatus = null
  data.user.driverLicenseRemark = null
  data.user.driverLicenseVerified = false // 重置为false
}

// 获取用户信息
const loadUser = () => {
  userProfileApi.getUserById(data.user.id).then((res) => {
    const userData = res.data

    // 将用户表中的状态值转换为前端需要的格式
    if (userData.realNameVerified !== undefined) {
      userData.realNameStatus = userData.realNameVerified
      userData.realNameVerified = userData.realNameVerified === 1
    }

    if (userData.driverLicenseVerified !== undefined) {
      userData.driverLicenseStatus = userData.driverLicenseVerified
      userData.driverLicenseVerified = userData.driverLicenseVerified === 1
    }

    data.user = userData
    localStorage.setItem('system-user', JSON.stringify(userData))
    emit('updateUser')
    // 加载认证状态（会覆盖从用户表加载的状态，以认证表为准）
    loadAuthStatus()
  })
}

// 在页面加载时处理滚动和高亮，并加载数据
onMounted(async () => {
  const { tab, highlight } = route.query

  if (tab === 'auth') {
    await nextTick()
    setTimeout(() => {
      if (verificationSection.value) {
        verificationSection.value.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        })

        if (highlight === 'realName') {
          highlightAuth.value = 'realName'
          setTimeout(() => {
            highlightAuth.value = null
          }, 3000)
        } else if (highlight === 'driverLicense') {
          highlightAuth.value = 'driverLicense'
          setTimeout(() => {
            highlightAuth.value = null
          }, 3000)
        }
      }
    }, 300)
  }

  // 加载用户数据（内部会调用 loadAuthStatus）
  loadUser()
})

// 头像上传回调
const handleFileUpload = (res) => {
  data.user.avatar = res.data
}

// 身份证上传回调
const handleIdFrontUpload = (res) => {
  realNameForm.idFrontImage = res.data
}

const handleIdBackUpload = (res) => {
  realNameForm.idBackImage = res.data
}

// 驾驶证上传回调
const handleLicenseFrontUpload = (res) => {
  licenseForm.licenseFrontImage = res.data
}

const handleLicenseBackUpload = (res) => {
  licenseForm.licenseBackImage = res.data
}

// 格式化身份证号显示（脱敏）
const formatIdNumber = (idNumber) => {
  if (!idNumber) return ''
  return idNumber.replace(/^(.{6})(?:\d+)(.{4})$/, '$1********$2')
}

// 重新上传实名认证
const handleReuploadRealName = () => {
  showRealNameDetail.value = false

  realNameForm.realName = data.user.realName || ''
  realNameForm.idNumber = data.user.idNumber || ''
  realNameForm.phone = data.user.phone || '' // 预填充当前手机号
  realNameForm.idFrontImage = data.user.idFrontImage || ''
  realNameForm.idBackImage = data.user.idBackImage || ''

  showRealNameDialog.value = true
}

// 重新上传驾驶证认证
const handleReuploadLicense = () => {
  showLicenseDetail.value = false

  licenseForm.name = data.user.driverName || ''
  licenseForm.licenseNumber = data.user.licenseNumber || ''
  licenseForm.vehicleType = data.user.vehicleType || ''
  licenseForm.phone = data.user.phone || '' // 预填充当前手机号

  if (data.user.validStart && data.user.validEnd) {
    licenseForm.validPeriod = [new Date(data.user.validStart), new Date(data.user.validEnd)]
  } else {
    licenseForm.validPeriod = []
  }

  licenseForm.licenseFrontImage = data.user.licenseFrontImage || ''
  licenseForm.licenseBackImage = data.user.licenseBackImage || ''

  showDriverLicenseDialog.value = true
}
// 手机号变更检查和处理
const handlePhoneChange = (formPhone, submitCallback) => {
  // 如果没有填写手机号或与当前一致，直接执行提交
  if (!formPhone || data.user.phone === formPhone) {
    submitCallback(false)
    return
  }

  // 手机号不一致，弹出确认框
  ElMessageBox.confirm(
    `您填写的手机号 ${formPhone} 与当前账户手机号 ${data.user.phone} 不一致，是否更新手机号？`,
    '手机号变更提示',
    {
      confirmButtonText: '确认更新',
      cancelButtonText: '暂不更新',
      type: 'warning'
    }
  )
    .then(() => {
      // 用户确认更新手机号
      submitCallback(true)
    })
    .catch(() => {
      // 用户取消更新手机号
      submitCallback(false)
    })
}
// 提交实名认证
const submitRealName = () => {
  if (!realNameForm.idFrontImage) {
    ElMessage.warning('请上传身份证正面照片')
    return
  }
  if (!realNameForm.idBackImage) {
    ElMessage.warning('请上传身份证背面照片')
    return
  }

  realNameFormRef.value?.validate((valid) => {
    if (!valid) return

    // 处理手机号变更
    handlePhoneChange(realNameForm.phone, (updatePhone) => {
      realNameLoading.value = true

      // 构建提交数据
      const submitData = {
        userId: data.user.id,
        realName: realNameForm.realName,
        idNumber: realNameForm.idNumber,
        idFrontImage: realNameForm.idFrontImage,
        idBackImage: realNameForm.idBackImage
      }

      // 如果确认更新手机号，则一并提交
      if (updatePhone) {
        submitData.phone = realNameForm.phone
      }

      userAuthApi
        .submitRealName(submitData)
        .then((res) => {
          if (res.code === '200') {
            ElMessage.success('实名认证提交成功，等待审核')
            showRealNameDialog.value = false
            Object.keys(realNameForm).forEach((key) => {
              realNameForm[key] = ''
            })
            loadAuthStatus()
            // 如果手机号有更新，重新加载用户信息
            if (updatePhone) {
              loadUser()
            }
          } else {
            ElMessage.error(res.msg || '认证失败')
          }
        })
        .finally(() => {
          realNameLoading.value = false
        })
    })
  })
}

// 提交驾驶证认证
const submitLicense = () => {
  if (!licenseForm.licenseFrontImage) {
    ElMessage.warning('请上传驾驶证正面照片')
    return
  }

  licenseFormRef.value?.validate((valid) => {
    if (!valid) return

    // 处理手机号变更
    handlePhoneChange(licenseForm.phone, (updatePhone) => {
      licenseLoading.value = true

      // 构建提交数据
      const submitData = {
        userId: data.user.id,
        driverName: licenseForm.name,
        licenseNumber: licenseForm.licenseNumber,
        vehicleType: licenseForm.vehicleType,
        validStart: licenseForm.validPeriod[0],
        validEnd: licenseForm.validPeriod[1],
        licenseFrontImage: licenseForm.licenseFrontImage,
        licenseBackImage: licenseForm.licenseBackImage
      }

      // 如果确认更新手机号，则一并提交
      if (updatePhone) {
        submitData.phone = licenseForm.phone
      }

      userAuthApi
        .submitDriverLicense(submitData)
        .then((res) => {
          if (res.code === '200') {
            ElMessage.success('驾驶证认证提交成功，等待审核')
            showDriverLicenseDialog.value = false
            Object.keys(licenseForm).forEach((key) => {
              if (key === 'validPeriod') {
                licenseForm[key] = []
              } else {
                licenseForm[key] = ''
              }
            })
            loadAuthStatus()
            // 如果手机号有更新，重新加载用户信息
            if (updatePhone) {
              loadUser()
            }
          } else {
            ElMessage.error(res.msg || '提交失败')
          }
        })
        .finally(() => {
          licenseLoading.value = false
        })
    })
  })
}

// 芝麻信用授权
const authorizeCredit = () => {
  window.open(
    'https://render.alipay.com/p/s/i/?scheme=alipays%3A%2F%2Fplatformapi%2Fstartapp%3FappId%3D20000123%26page%3Dpages%2Fauthorize%2Fauthorize',
    '_blank'
  )
}

// 更新数据
const update = () => {
  formRef.value?.validate((valid) => {
    if (valid) {
      loading.value = true

      // 创建提交数据的副本，将布尔值转换为对应的状态码
      const submitData = {
        ...data.user,
        // 如果已经有认证状态，保留原值；否则根据布尔值设置
        realNameVerified:
          data.user.realNameStatus !== undefined ? data.user.realNameStatus : data.user.realNameVerified ? 1 : 0,
        driverLicenseVerified:
          data.user.driverLicenseStatus !== undefined
            ? data.user.driverLicenseStatus
            : data.user.driverLicenseVerified
              ? 1
              : 0
      }

      // 移除前端临时使用的字段，避免发送到后端
      delete submitData.realNameStatus
      delete submitData.driverLicenseStatus
      delete submitData.realNameRemark
      delete submitData.driverLicenseRemark

      userProfileApi
        .updateUser(submitData)
        .then((res) => {
          loading.value = false
          if (res.code === '200') {
            ElMessage.success('个人信息更新成功')
            loadUser()
          } else {
            ElMessage.error(res.msg)
          }
        })
        .catch(() => {
          loading.value = false
        })
    }
  })
}

// 跳转到充值页面
const goToRecharge = () => {
  router.push('/front/recharge')
}

// 重置表单
const resetForm = () => {
  loadUser()
}
</script>

<style scoped>
.profile-container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 30px 20px;
  min-height: calc(100vh - 70px);
  background: #f8fafc;
}

.profile-card {
  max-width: 600px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(226, 232, 240, 0.8);
  overflow: hidden;
  transition: all 0.3s ease;
}

.profile-card:hover {
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05);
}

.profile-header {
  padding: 30px 30px 20px;
  border-bottom: 1px solid #edf2f7;
  background: linear-gradient(135deg, #ffffff, #fafafa);
}

.profile-title {
  font-size: 26px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 8px;
  position: relative;
  display: inline-block;
}

.profile-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -4px;
  width: 40px;
  height: 3px;
  background: #c8a165;
  border-radius: 2px;
}

.profile-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.profile-form {
  padding: 30px;
}

/* 头像上传区域 */
.avatar-item :deep(.el-form-item__label) {
  padding-top: 35px;
}

.avatar-uploader {
  display: flex;
  justify-content: flex-start;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed #e2e8f0;
  border-radius: 16px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  width: 140px;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #c8a165;
  background: rgba(200, 161, 101, 0.02);
}

.avatar {
  width: 140px;
  height: 140px;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #94a3b8;
  transition: all 0.3s ease;
}

.avatar-placeholder:hover {
  color: #c8a165;
}

.avatar-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.avatar-placeholder span {
  font-size: 13px;
}

/* 只读信息样式 */
.info-text {
  color: #1e293b;
  font-size: 15px;
  padding: 8px 0;
  background: #f8fafc;
  border-radius: 8px;
  padding: 10px 16px;
  border: 1px solid #edf2f7;
  width: 100%;
}

/* 余额样式 */
.balance-box {
  background: linear-gradient(135deg, #fef9e7, #fff7e0);
  border: 1px solid #fde6c4;
  border-radius: 12px;
  padding: 8px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  cursor: pointer;
  transition: all 0.3s ease;
}

.balance-box:hover {
  background: linear-gradient(135deg, #fef9e7, #ffe6b3);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(200, 161, 101, 0.2);
}

.balance-label {
  font-size: 14px;
  color: #92400e;
  font-weight: 500;
}

.balance-amount {
  font-size: 22px;
  font-weight: 600;
  color: #b45309;
}

.recharge-text {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
  color: #c8a165;
  padding: 6px 12px;
  transition: all 0.3s ease;
}

.recharge-icon {
  font-size: 14px;
  transition: transform 0.3s ease;
}

.balance-box:hover .recharge-icon {
  transform: translateX(3px);
}

/* 自定义输入框样式 */
.custom-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.3s ease;
  height: 44px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c8a165 inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 2px rgba(200, 161, 101, 0.2),
    0 0 0 1px #c8a165 inset;
}

.custom-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #1e293b;
}

/* 表单按钮 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
  margin-bottom: 30px;
}

.submit-btn {
  min-width: 140px;
  height: 46px;
  border-radius: 40px;
  background: linear-gradient(135deg, #c8a165, #b28b4f);
  border: none;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #b28b4f, #9e7a45);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(200, 161, 101, 0.3);
}

.reset-btn {
  min-width: 100px;
  height: 46px;
  border-radius: 40px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #64748b;
  font-size: 15px;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background: #f8fafc;
  border-color: #c8a165;
  color: #c8a165;
}

/* 表单项间距 */
:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #334155;
  font-size: 15px;
}

/* 认证区域样式 */
.verification-section {
  margin: 20px 30px 30px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
  border: 1px solid #edf2f7;
}

.verification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.verification-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0;
}

.verification-title .el-icon {
  color: #c8a165;
  font-size: 18px;
}

.verification-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}
/* 添加高亮动画效果 */
@keyframes highlightPulse {
  0% {
    background: white;
    border-color: #edf2f7;
    box-shadow: none;
  }
  25% {
    background: #fef9e7;
    border-color: #c8a165;
    box-shadow: 0 4px 12px rgba(200, 161, 101, 0.2);
  }
  50% {
    background: #ffe6b3;
    border-color: #c8a165;
    box-shadow: 0 4px 12px rgba(200, 161, 101, 0.3);
  }
  75% {
    background: #fef9e7;
    border-color: #c8a165;
    box-shadow: 0 4px 12px rgba(200, 161, 101, 0.2);
  }
  100% {
    background: white;
    border-color: #edf2f7;
    box-shadow: none;
  }
}

.highlight-item {
  animation: highlightPulse 2s ease-in-out;
  transform: scale(1.02);
  z-index: 10;
  position: relative;
}
.verification-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: white;
  border-radius: 12px;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
}

.verification-item:hover {
  border-color: #c8a165;
  box-shadow: 0 4px 12px rgba(200, 161, 101, 0.1);
}
/* 让高亮时的效果更明显 */
.highlight-item:hover {
  animation: none;
  background: #ffe6b3;
  transform: translateY(-2px) scale(1.02);
}
.item-info {
  flex: 1;
}

.item-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #1e2a3a;
  margin-bottom: 4px;
}

.item-desc {
  font-size: 12px;
  color: #94a3b8;
}

.item-status {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0 16px;
  font-size: 13px;
  color: #94a3b8;
}

.item-status.verified {
  color: #10b981;
}

.item-status .el-icon {
  font-size: 16px;
}

.verify-btn {
  min-width: 70px;
  border-radius: 30px;
}

.verify-btn.el-button--primary {
  background: #c8a165;
  border: none;
}

.verify-btn.el-button--primary:hover {
  background: #b28b4f;
}

.verification-note {
  margin-top: 16px;
}

/* 身份证上传样式 */
.id-card-section {
  margin-bottom: 0;
}

.id-card-row {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.id-card-item {
  flex: 1;
}

.id-card-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}

.id-card-uploader :deep(.el-upload) {
  width: 100%;
}

/* 驾驶证上传样式 */
.license-section {
  margin-bottom: 0;
}

.license-row {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.license-item {
  flex: 1;
}

.license-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}

.license-uploader :deep(.el-upload) {
  width: 100%;
}

/* 上传卡片样式 */
.upload-card {
  width: 100%;
  height: 140px;
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.upload-card.has-image {
  border: 2px solid #c8a165;
}

.upload-card:hover {
  border-color: #c8a165;
  background: rgba(200, 161, 101, 0.02);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.upload-placeholder .el-icon {
  font-size: 28px;
  margin-bottom: 6px;
}

.upload-placeholder span {
  font-size: 13px;
}

.id-image,
.license-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #f1f5f9;
}

.upload-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f1f5f9;
  border-radius: 8px;
  font-size: 13px;
  color: #475569;
}

.upload-tip .el-icon {
  color: #c8a165;
  font-size: 16px;
}

/* 详情样式 */
.detail-content {
  padding: 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.detail-label {
  width: 100px;
  font-weight: 500;
  color: #64748b;
}

.detail-value {
  flex: 1;
  color: #1e293b;
}

.detail-images {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 20px;
}

.image-item {
  text-align: center;
}

.image-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
}

.detail-image {
  width: 100%;
  height: 180px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #94a3b8;
  font-size: 13px;
}

/* 响应式 */
@media (max-width: 640px) {
  .profile-container {
    padding: 20px 16px;
  }

  .profile-card {
    border-radius: 16px;
  }

  .profile-header {
    padding: 24px 20px;
  }

  .profile-title {
    font-size: 22px;
  }

  .profile-form {
    padding: 24px 20px;
  }

  .balance-amount {
    font-size: 18px;
  }

  .form-actions {
    flex-direction: column;
    gap: 12px;
  }

  .submit-btn,
  .reset-btn {
    width: 100%;
  }

  .verification-section {
    margin: 20px;
    padding: 16px;
  }

  .verification-item {
    flex-wrap: wrap;
    gap: 12px;
  }

  .item-status {
    margin: 0 8px;
  }

  .item-status.pending {
    color: #e6a23c;
  }

  .item-status.pending .el-icon {
    color: #e6a23c;
  }

  .verify-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    background-color: #f5f7fa;
    border-color: #e4e7ed;
    color: #c0c4cc;
  }
  .verify-btn {
    width: 100%;
    margin-top: 8px;
  }

  .id-card-row,
  .license-row {
    flex-direction: column;
    gap: 16px;
  }

  .detail-images {
    grid-template-columns: 1fr;
  }
}
</style>
