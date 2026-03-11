<template>
  <div class="about-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">关于我们</h1>
      <p class="page-subtitle">了解易租车 · 品牌故事 · 联系方式 · 合作加盟</p>
    </div>

    <!-- 品牌介绍 Banner -->
    <div class="brand-banner">
      <div class="brand-banner__content">
        <h2>易租车 · 轻松出行</h2>
        <p>让每一次出行都变得简单、安心、愉悦</p>
        <div class="brand-stats">
          <div class="stat-item">
            <span class="stat-number">50万+</span>
            <span class="stat-label">注册用户</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">200+</span>
            <span class="stat-label">覆盖城市</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">5000+</span>
            <span class="stat-label">服务车辆</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">24h</span>
            <span class="stat-label">客服在线</span>
          </div>
        </div>
      </div>
      <div class="brand-banner__image">
        <img
          src="https://images.unsplash.com/photo-1494976388531-d1058494cdd8?auto=format&fit=crop&w=1200&q=80"
          alt="易租车" />
      </div>
    </div>

    <!-- 品牌故事 -->
    <div class="about-section">
      <h2 class="section-title">品牌故事</h2>
      <div class="story-content">
        <p>
          易租车成立于2020年，是一家专注于汽车租赁服务的互联网平台。我们致力于为用户提供便捷、透明、安全的租车体验，让每一次出行都变得简单而美好。
        </p>
        <p>
          从成立之初的10辆车，到现在覆盖全国200多个城市、拥有5000多辆服务车辆的规模，易租车始终坚持以用户为中心，不断优化服务流程，提升用户体验。
        </p>
        <p>
          我们相信，出行不仅是从A点到B点的移动，更是一种生活方式的延伸。因此，我们精心挑选每一辆车，严格把控每一道服务环节，只为让您的每一次出行都成为愉快的记忆。
        </p>
      </div>
    </div>

    <!-- 核心价值观 -->
    <div class="about-section values-section">
      <h2 class="section-title">核心价值观</h2>
      <div class="values-grid">
        <div class="value-card">
          <div class="value-card__icon">
            <el-icon><Check /></el-icon>
          </div>
          <h3>透明诚信</h3>
          <p>价格透明，无隐形消费；服务承诺，说到做到。</p>
        </div>
        <div class="value-card">
          <div class="value-card__icon">
            <el-icon><Clock /></el-icon>
          </div>
          <h3>高效便捷</h3>
          <p>极简流程，快速响应，让租车像点外卖一样简单。</p>
        </div>
        <div class="value-card">
          <div class="value-card__icon">
            <el-icon><Lock /></el-icon>
          </div>
          <h3>安全保障</h3>
          <p>车况严格检查，保险全面覆盖，24小时救援。</p>
        </div>
        <div class="value-card">
          <div class="value-card__icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <h3>用户至上</h3>
          <p>倾听用户声音，持续优化服务，用心服务每一位用户。</p>
        </div>
      </div>
    </div>

    <!-- 发展历程 -->
    <div class="about-section timeline-section">
      <h2 class="section-title">发展历程</h2>
      <div class="timeline">
        <div class="timeline-item">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <span class="timeline-year">2020</span>
            <h3>易租车成立</h3>
            <p>10辆车起步，在杭州开启汽车租赁服务</p>
          </div>
        </div>
        <div class="timeline-item">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <span class="timeline-year">2021</span>
            <h3>覆盖20城</h3>
            <p>服务范围扩展至长三角地区20个城市</p>
          </div>
        </div>
        <div class="timeline-item">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <span class="timeline-year">2022</span>
            <h3>用户突破10万</h3>
            <p>注册用户数突破10万，车辆规模达1000辆</p>
          </div>
        </div>
        <div class="timeline-item">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <span class="timeline-year">2023</span>
            <h3>全国布局</h3>
            <p>服务网络覆盖全国150个城市</p>
          </div>
        </div>
        <div class="timeline-item">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <span class="timeline-year">2024</span>
            <h3>品牌升级</h3>
            <p>服务城市突破200个，车辆规模达5000辆</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 团队介绍 -->
    <div class="about-section team-section">
      <h2 class="section-title">核心团队</h2>
      <div class="team-grid">
        <div class="team-card">
          <div class="team-card__avatar">
            <img src="https://randomuser.me/api/portraits/men/1.jpg" alt="张伟" />
          </div>
          <h3>张伟</h3>
          <p class="team-card__position">创始人 & CEO</p>
          <p class="team-card__desc">前滴滴出行高级产品总监，10年出行行业经验</p>
        </div>
        <div class="team-card">
          <div class="team-card__avatar">
            <img src="https://randomuser.me/api/portraits/women/2.jpg" alt="李娜" />
          </div>
          <h3>李娜</h3>
          <p class="team-card__position">联合创始人 & COO</p>
          <p class="team-card__desc">曾任神州租车运营总监，精通车辆运营管理</p>
        </div>
        <div class="team-card">
          <div class="team-card__avatar">
            <img src="https://randomuser.me/api/portraits/men/3.jpg" alt="王强" />
          </div>
          <h3>王强</h3>
          <p class="team-card__position">CTO</p>
          <p class="team-card__desc">前阿里巴巴高级技术专家，负责平台技术架构</p>
        </div>
        <div class="team-card">
          <div class="team-card__avatar">
            <img src="https://randomuser.me/api/portraits/women/4.jpg" alt="赵敏" />
          </div>
          <h3>赵敏</h3>
          <p class="team-card__position">CMO</p>
          <p class="team-card__desc">资深市场营销专家，擅长品牌建设与用户增长</p>
        </div>
      </div>
    </div>

    <!-- 联系方式 -->
    <div class="about-section contact-section">
      <h2 class="section-title">联系方式</h2>
      <div class="contact-grid">
        <div class="contact-card">
          <div class="contact-card__icon">
            <el-icon><Phone /></el-icon>
          </div>
          <h3>客服电话</h3>
          <p>400-123-4567</p>
          <span>周一至周日 9:00-21:00</span>
        </div>
        <div class="contact-card">
          <div class="contact-card__icon">
            <el-icon><Message /></el-icon>
          </div>
          <h3>电子邮箱</h3>
          <p>service@yizuche.com</p>
          <span>24小时内回复</span>
        </div>
        <div class="contact-card">
          <div class="contact-card__icon">
            <el-icon><Location /></el-icon>
          </div>
          <h3>公司地址</h3>
          <p>北京市朝阳区科技园B座12层</p>
          <span>欢迎来访</span>
        </div>
        <div class="contact-card">
          <div class="contact-card__icon">
            <el-icon><ChatLineRound /></el-icon>
          </div>
          <h3>商务合作</h3>
          <p>coop@yizuche.com</p>
          <span>商家入驻、广告投放</span>
        </div>
      </div>
    </div>

    <!-- 合作伙伴 -->
    <div class="about-section partners-section">
      <h2 class="section-title">合作伙伴</h2>
      <div class="partners-grid">
        <div class="partner-item">比亚迪</div>
        <div class="partner-item">特斯拉</div>
        <div class="partner-item">宝马</div>
        <div class="partner-item">奔驰</div>
        <div class="partner-item">奥迪</div>
        <div class="partner-item">大众</div>
        <div class="partner-item">丰田</div>
        <div class="partner-item">平安保险</div>
        <div class="partner-item">中国人保</div>
        <div class="partner-item">支付宝</div>
        <div class="partner-item">微信支付</div>
        <div class="partner-item">银联</div>
      </div>
    </div>

    <!-- 加入我们 -->
    <div class="about-section join-section">
      <h2 class="section-title">加入我们</h2>
      <div class="join-content">
        <p>如果你热爱出行行业，渴望在快速发展的互联网平台施展才华，欢迎加入易租车大家庭！</p>
        <div class="join-buttons">
          <el-button type="primary" class="join-btn" @click="goToJoin">查看招聘职位</el-button>
          <el-button class="join-btn-outline" @click="goToContact">联系我们</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Check, Clock, Lock, UserFilled, Phone, Message, Location, ChatLineRound } from '@element-plus/icons-vue'

const router = useRouter()

// 跳转到加入我们页面
const goToJoin = () => {
  router.push('/front/join')
}

// 跳转到联系方式
const goToContact = () => {
  const element = document.getElementById('contact')
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<style scoped>
.about-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 8px;
  position: relative;
  display: inline-block;
}

.page-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -4px;
  width: 40px;
  height: 3px;
  background: #c8a165;
  border-radius: 2px;
}

.page-subtitle {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

/* 品牌 Banner */
.brand-banner {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  background: linear-gradient(135deg, #f8fafc, #ffffff);
  border-radius: 24px;
  overflow: hidden;
  margin-bottom: 40px;
  border: 1px solid #edf2f7;
}

.brand-banner__content {
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-banner__content h2 {
  font-size: 32px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 12px;
}

.brand-banner__content p {
  font-size: 16px;
  color: #64748b;
  margin-bottom: 30px;
}

.brand-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #c8a165;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.brand-banner__image {
  height: 100%;
}

.brand-banner__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 章节通用样式 */
.about-section {
  margin-bottom: 48px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 24px;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 22px;
  background: #c8a165;
  border-radius: 2px;
}

/* 品牌故事 */
.story-content p {
  font-size: 16px;
  color: #4a5568;
  line-height: 1.8;
  margin-bottom: 16px;
}

.story-content p:last-child {
  margin-bottom: 0;
}

/* 核心价值观 */
.values-section {
  background: #f8fafc;
  padding: 40px;
  border-radius: 24px;
}

.values-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.value-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
}

.value-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.04);
  border-color: #c8a165;
}

.value-card__icon {
  width: 56px;
  height: 56px;
  background: rgba(200, 161, 101, 0.1);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 28px;
  color: #c8a165;
}

.value-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 8px;
}

.value-card p {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin: 0;
}

/* 发展历程 */
.timeline-section {
  background: white;
  padding: 40px;
  border-radius: 24px;
  border: 1px solid #edf2f7;
}

.timeline {
  position: relative;
  padding-left: 30px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e2e8f0;
}

.timeline-item {
  position: relative;
  padding-bottom: 30px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -34px;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #c8a165;
  border: 2px solid white;
  box-shadow: 0 0 0 2px #c8a165;
}

.timeline-content {
  padding-left: 20px;
}

.timeline-year {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(200, 161, 101, 0.1);
  border-radius: 30px;
  font-size: 13px;
  font-weight: 600;
  color: #c8a165;
  margin-bottom: 8px;
}

.timeline-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 6px;
}

.timeline-content p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

/* 团队介绍 */
.team-section {
  background: white;
  padding: 40px;
  border-radius: 24px;
  border: 1px solid #edf2f7;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.team-card {
  text-align: center;
  padding: 24px;
  background: #f8fafc;
  border-radius: 16px;
  transition: all 0.3s ease;
}

.team-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.04);
}

.team-card__avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 16px;
  border: 3px solid white;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.team-card__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.team-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 4px;
}

.team-card__position {
  font-size: 14px;
  color: #c8a165;
  font-weight: 500;
  margin-bottom: 8px;
}

.team-card__desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  margin: 0;
}

/* 联系方式 */
.contact-section {
  background: linear-gradient(135deg, #f8fafc, #ffffff);
  padding: 40px;
  border-radius: 24px;
}

.contact-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.contact-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
}

.contact-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.02);
  border-color: #c8a165;
}

.contact-card__icon {
  width: 48px;
  height: 48px;
  background: rgba(200, 161, 101, 0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 24px;
  color: #c8a165;
}

.contact-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 8px;
}

.contact-card p {
  font-size: 15px;
  color: #2c3e50;
  font-weight: 500;
  margin-bottom: 4px;
}

.contact-card span {
  font-size: 12px;
  color: #94a3b8;
}

/* 合作伙伴 */
.partners-section {
  background: white;
  padding: 40px;
  border-radius: 24px;
  border: 1px solid #edf2f7;
}

.partners-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.partner-item {
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  color: #4a5568;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
}

.partner-item:hover {
  background: white;
  border-color: #c8a165;
  color: #c8a165;
  transform: translateY(-2px);
}

/* 加入我们 */
.join-section {
  background: linear-gradient(135deg, #1e2a3a, #2d3a4a);
  border-radius: 24px;
  padding: 60px;
  text-align: center;
  color: white;
}

.join-content h2 {
  color: white;
  margin-bottom: 16px;
}

.join-content p {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 30px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.join-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.join-btn {
  background: #c8a165;
  border: none;
  color: white;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 40px;
  transition: all 0.3s ease;
}

.join-btn:hover {
  background: #b28b4f;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(200, 161, 101, 0.3);
}

.join-btn-outline {
  background: transparent;
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 40px;
  transition: all 0.3s ease;
}

.join-btn-outline:hover {
  background: white;
  color: #1e2a3a;
  border-color: white;
  transform: translateY(-2px);
}

/* 响应式 */
@media (max-width: 1100px) {
  .values-grid,
  .team-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .partners-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 900px) {
  .brand-banner {
    grid-template-columns: 1fr;
  }

  .brand-banner__image {
    height: 300px;
    order: -1;
  }

  .contact-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .brand-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .values-grid,
  .team-grid,
  .contact-grid,
  .partners-grid {
    grid-template-columns: 1fr;
  }

  .join-buttons {
    flex-direction: column;
  }

  .values-section,
  .timeline-section,
  .team-section,
  .contact-section,
  .partners-section,
  .join-section {
    padding: 30px 20px;
  }
}
</style>
