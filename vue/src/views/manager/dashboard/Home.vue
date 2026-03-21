<template>
  <div>
    <p>系统首页</p>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import request from '@/utils/request'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
})
</script>
