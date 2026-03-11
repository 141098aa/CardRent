<template>
  <footer class="footer-simple">
    <div class="footer-simple-container">
      <div class="footer-simple-links">
        <a href="/front/about">关于我们</a>
        <span class="divider">|</span>
        <a href="/front/guide">租车指南</a>
        <span class="divider">|</span>
        <a href="/front/terms">服务条款</a>
        <span class="divider">|</span>
        <a href="/front/privacy">隐私政策</a>
        <span class="divider">|</span>
        <a href="/front/contact">联系我们</a>
      </div>
      <div class="footer-simple-copyright">© 2024 易租车 版权所有 | 京ICP备12345678号-1</div>
    </div>
  </footer>
</template>

<style scoped>
.footer-simple {
  background: #f8f9fa;
  border-top: 1px solid #edf2f7;
  padding: 24px 0;
}

.footer-simple-container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.footer-simple-links {
  margin-bottom: 12px;
}

.footer-simple-links a {
  color: #64748b;
  text-decoration: none;
  font-size: 14px;
  margin: 0 8px;
  transition: color 0.2s;
}

.footer-simple-links a:hover {
  color: #c8a165;
}

.divider {
  color: #cbd5e0;
  font-size: 12px;
}

.footer-simple-copyright {
  color: #94a3b8;
  font-size: 13px;
}
</style>
