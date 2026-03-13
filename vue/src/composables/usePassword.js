import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import router from '@/router'

export function usePassword(options = {}) {
  const {
    redirectPath = '/',
    logoutAfterSuccess = true,
    passwordMinLength = 8,
    requireLetterAndNumber = true
  } = options

  const loading = ref(false)
  const formRef = ref()

  // 计算密码强度
  const passwordStrength = computed(() => {
    const pwd = data.user.newPassword || ''
    if (!pwd) return 0

    let strength = 0
    if (pwd.length >= 8) strength += 20
    if (pwd.length >= 10) strength += 10
    if (/[a-z]/.test(pwd)) strength += 15
    if (/[A-Z]/.test(pwd)) strength += 15
    if (/[0-9]/.test(pwd)) strength += 20
    if (/[^a-zA-Z0-9]/.test(pwd)) strength += 20

    return Math.min(strength, 100)
  })

  const strengthText = computed(() => {
    const strength = passwordStrength.value
    if (strength < 30) return '弱'
    if (strength < 60) return '中'
    if (strength < 80) return '强'
    return '非常强'
  })

  const data = reactive({
    user: JSON.parse(localStorage.getItem('system-user') || '{}'),
    rules: {
      password: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: passwordMinLength, message: `密码长度至少${passwordMinLength}位`, trigger: 'blur' },
        ...(requireLetterAndNumber
          ? [
              {
                validator: (rule, value, callback) => {
                  const hasLetter = /[a-zA-Z]/.test(value)
                  const hasNumber = /[0-9]/.test(value)
                  if (!hasLetter || !hasNumber) {
                    callback(new Error('密码必须包含字母和数字'))
                  } else {
                    callback()
                  }
                },
                trigger: 'blur'
              }
            ]
          : [])
      ],
      confimPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== data.user.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: ['blur', 'change']
        }
      ]
    }
  })

  // 更新密码
  const updatePassword = () => {
    if (data.user.newPassword !== data.user.confimPassword) {
      ElMessage.warning('两次输入的新密码不同，请确认！')
      return
    }

    formRef.value?.validate((valid) => {
      if (valid) {
        loading.value = true
        request
          .put('/updatePassword', data.user)
          .then((res) => {
            loading.value = false
            if (res.code === '200') {
              ElMessage.success('密码修改成功，请重新登录')
              if (logoutAfterSuccess) {
                setTimeout(() => {
                  logout()
                }, 1500)
              }
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

  // 退出登录
  const logout = () => {
    localStorage.removeItem('system-user')
    router.push('/login')
  }

  // 取消
  const handleCancel = () => {
    if (redirectPath) {
      router.push(redirectPath)
    } else {
      router.back()
    }
  }

  return {
    loading,
    formRef,
    data,
    passwordStrength,
    strengthText,
    updatePassword,
    handleCancel,
    logout
  }
}
