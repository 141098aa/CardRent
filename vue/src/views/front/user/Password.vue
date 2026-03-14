<template>
  <div class="password-container">
    <div class="password-card">
      <!-- 头部 -->
      <div class="password-header">
        <h2 class="password-title">修改密码</h2>
        <p class="password-subtitle">为了账户安全，请定期更换密码</p>
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
  redirectPath: null // 使用 router.back()
})
</script>

<style scoped>
.password-container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: calc(100vh - 70px);
  background: #f8fafc;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.password-card {
  width: 100%;
  max-width: 500px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(226, 232, 240, 0.8);
  overflow: hidden;
  transition: all 0.3s ease;
}

.password-card:hover {
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.04);
}

.password-header {
  padding: 30px 30px 20px;
  border-bottom: 1px solid #edf2f7;
  background: linear-gradient(135deg, #ffffff, #fafafa);
}

.password-title {
  font-size: 26px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 8px;
  position: relative;
  display: inline-block;
}

.password-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -4px;
  width: 40px;
  height: 3px;
  background: #c8a165;
  border-radius: 2px;
}

.password-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.password-form {
  padding: 30px;
}

/* 自定义输入框 */
.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.3s ease;
  height: 46px;
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

.custom-input :deep(.el-input__inner::placeholder) {
  color: #94a3b8;
  font-size: 14px;
}

/* 密码提示 */
.password-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
  padding-left: 4px;
}

/* 安全提示 */
.security-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: -8px 0 20px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #edf2f7;
}

.security-icon {
  color: #c8a165;
  font-size: 16px;
}

.strength-bar {
  flex: 1;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.strength-progress {
  height: 100%;
  background: linear-gradient(90deg, #f97316, #c8a165, #10b981);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.strength-text {
  font-size: 12px;
  font-weight: 600;
  min-width: 40px;
  color: #64748b;
}

/* 按钮区域 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
}

.submit-btn {
  min-width: 140px;
  height: 48px;
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
  height: 48px;
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

/* 表单项样式 */
:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #334155;
  font-size: 15px;
  padding-right: 16px;
}

:deep(.el-form-item__error) {
  padding-left: 4px;
  font-size: 12px;
}
</style>
