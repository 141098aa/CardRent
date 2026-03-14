<template>
  <div class="password-container admin-scope">
    <div class="password-card">
      <!-- 头部 -->
      <div class="password-header">
        <h2 class="page-title">修改密码</h2>
        <p class="page-subtitle">为了账户安全，请定期更换密码</p>
      </div>

      <!-- 表单 -->
      <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="100px" class="password-form">
        <!-- 原密码 -->
        <el-form-item prop="password" label="原密码">
          <el-input
            v-model="data.user.password"
            show-password
            autocomplete="off"
            placeholder="请输入原密码"
            class="custom-input" />
        </el-form-item>

        <!-- 新密码 -->
        <el-form-item prop="newPassword" label="新密码">
          <el-input
            v-model="data.user.newPassword"
            show-password
            autocomplete="off"
            placeholder="请输入新密码"
            class="custom-input" />
          <div class="password-tip">密码长度至少8位，包含字母和数字</div>
        </el-form-item>

        <!-- 确认新密码 -->
        <el-form-item prop="confimPassword" label="确认新密码">
          <el-input
            v-model="data.user.confimPassword"
            show-password
            autocomplete="off"
            placeholder="请再次输入新密码"
            class="custom-input" />
        </el-form-item>

        <!-- 安全提示 -->
        <div class="security-tips">
          <el-icon class="security-icon"><Warning /></el-icon>
          <span>密码强度：</span>
          <div class="strength-bar">
            <div class="strength-progress" :style="{ width: passwordStrength + '%' }"></div>
          </div>
          <span class="strength-text">{{ strengthText }}</span>
        </div>

        <!-- 按钮区域 -->
        <div class="form-actions">
          <el-button type="primary" class="submit-btn" @click="updatePassword" :loading="loading"> 确认修改 </el-button>
          <el-button class="reset-btn" @click="handleCancel"> 取 消 </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { usePassword } from '@/composables/usePassword'
import { Warning } from '@element-plus/icons-vue'

const { loading, formRef, data, passwordStrength, strengthText, updatePassword, handleCancel } = usePassword({
  redirectPath: '/manager/home'
})
</script>

<style scoped>
.password-container {
  max-width: 100%;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 100px);
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.password-card {
  width: 100%;
  max-width: 500px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  border: 1px solid #edf2f7;
  overflow: hidden;
}

.password-header {
  padding: 24px 30px 16px;
  border-bottom: 1px solid #edf2f7;
  background: #ffffff;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 4px;
  position: relative;
  display: inline-block;
}

.page-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -4px;
  width: 30px;
  height: 3px;
  background: #409eff;
  border-radius: 2px;
}

.page-subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 8px 0 0;
}

.password-form {
  padding: 24px 30px 30px;
}

/* 自定义输入框 */
.custom-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 4px 12px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.3s ease;
  height: 40px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 2px rgba(64, 158, 255, 0.2),
    0 0 0 1px #409eff inset;
}

.custom-input :deep(.el-input__inner) {
  font-size: 14px;
  color: #1e293b;
}

.custom-input :deep(.el-input__inner::placeholder) {
  color: #94a3b8;
  font-size: 13px;
}

/* 密码提示 */
.password-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
  padding-left: 4px;
}

/* 安全提示 */
.security-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: -4px 0 16px;
  padding: 10px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #edf2f7;
}

.security-icon {
  color: #409eff;
  font-size: 16px;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  overflow: hidden;
}

.strength-progress {
  height: 100%;
  background: linear-gradient(90deg, #f97316, #409eff, #10b981);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.strength-text {
  font-size: 12px;
  font-weight: 500;
  min-width: 36px;
  color: #64748b;
}

/* 按钮区域 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

.submit-btn {
  min-width: 120px;
  height: 40px;
  border-radius: 6px;
  font-weight: 500;
  background-color: #409eff;
  border-color: #409eff;
}

.submit-btn:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.submit-btn:active {
  background-color: #3a8ee6;
  border-color: #3a8ee6;
}

.reset-btn {
  min-width: 100px;
  height: 40px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #64748b;
  font-weight: 500;
}

.reset-btn:hover {
  background: #f8fafc;
  border-color: #409eff;
  color: #409eff;
}

/* 表单项样式 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #334155;
  font-size: 14px;
  padding-right: 12px;
}

:deep(.el-form-item__error) {
  padding-left: 4px;
  font-size: 12px;
}
</style>
