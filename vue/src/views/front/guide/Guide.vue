<template>
  <div class="guide-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">服务指南</h1>
      <p class="page-subtitle">租车流程 · 保险说明 · 常见问题 · 取还车指南</p>
    </div>

    <!-- 快速入口卡片 -->
    <div class="quick-guide">
      <div class="guide-card" @click="scrollToSection('process')">
        <div class="guide-card__icon">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="guide-card__content">
          <h3>租车流程</h3>
          <p>4步轻松租车</p>
        </div>
      </div>
      <div class="guide-card" @click="scrollToSection('insurance')">
        <div class="guide-card__icon">
          <el-icon><Lock /></el-icon>
        </div>
        <div class="guide-card__content">
          <h3>保险说明</h3>
          <p>保障您的出行</p>
        </div>
      </div>
      <div class="guide-card" @click="scrollToSection('faq')">
        <div class="guide-card__icon">
          <el-icon><QuestionFilled /></el-icon>
        </div>
        <div class="guide-card__content">
          <h3>常见问题</h3>
          <p>24个热门问题</p>
        </div>
      </div>
      <div class="guide-card" @click="scrollToSection('pickup')">
        <div class="guide-card__icon">
          <el-icon><Van /></el-icon>
        </div>
        <div class="guide-card__content">
          <h3>取还车指南</h3>
          <p>门店/送车上门</p>
        </div>
      </div>
    </div>

    <!-- 租车流程 -->
    <div id="process" class="guide-section">
      <h2 class="section-title">租车流程</h2>
      <div class="process-steps">
        <div class="process-step">
          <div class="process-step__number">01</div>
          <div class="process-step__content">
            <h3>选择车型</h3>
            <p>浏览车辆列表，根据您的需求筛选品牌、座位数、能源类型等，选择心仪车型。</p>
            <div class="process-step__tip">
              <el-icon><InfoFilled /></el-icon>
              <span>建议提前2-3天预订，节假日需提前7天</span>
            </div>
          </div>
        </div>
        <div class="process-step">
          <div class="process-step__number">02</div>
          <div class="process-step__content">
            <h3>填写信息</h3>
            <p>填写取还车时间、地点，上传身份证和驾驶证照片，确认租车人信息。</p>
            <div class="process-step__tip">
              <el-icon><InfoFilled /></el-icon>
              <span>请确保证件信息清晰、在有效期内</span>
            </div>
          </div>
        </div>
        <div class="process-step">
          <div class="process-step__number">03</div>
          <div class="process-step__content">
            <h3>支付押金</h3>
            <p>根据车型支付租车费用和押金，支持微信、支付宝、银联等多种支付方式。</p>
            <div class="process-step__tip">
              <el-icon><InfoFilled /></el-icon>
              <span>押金在还车后3-7个工作日内原路退回</span>
            </div>
          </div>
        </div>
        <div class="process-step">
          <div class="process-step__number">04</div>
          <div class="process-step__content">
            <h3>取还车辆</h3>
            <p>按约定时间到店取车或等待送车上门，还车时请确保油量/电量与取车时一致。</p>
            <div class="process-step__tip">
              <el-icon><InfoFilled /></el-icon>
              <span>取车时请仔细检查车况，拍照留存</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 保险说明 -->
    <div id="insurance" class="guide-section">
      <h2 class="section-title">保险说明</h2>
      <div class="insurance-grid">
        <div class="insurance-card">
          <div class="insurance-card__header">
            <span class="insurance-card__badge">基础保障</span>
            <span class="insurance-card__price">已包含</span>
          </div>
          <h3 class="insurance-card__title">车辆损失险</h3>
          <p class="insurance-card__desc">覆盖车辆在租赁期间的碰撞、刮擦等损失</p>
          <ul class="insurance-card__list">
            <li>免赔额：¥2000</li>
            <li>最高赔付：车辆价值</li>
            <li>事故处理：协助理赔</li>
          </ul>
        </div>
        <div class="insurance-card">
          <div class="insurance-card__header">
            <span class="insurance-card__badge">推荐升级</span>
            <span class="insurance-card__price">¥50/天</span>
          </div>
          <h3 class="insurance-card__title">尊享服务包</h3>
          <p class="insurance-card__desc">降低免赔额，增加更多保障项目</p>
          <ul class="insurance-card__list">
            <li>免赔额：¥0</li>
            <li>玻璃轮胎保障</li>
            <li>道路救援服务</li>
          </ul>
          <el-button class="insurance-card__btn" size="small">了解详情</el-button>
        </div>
        <div class="insurance-card">
          <div class="insurance-card__header">
            <span class="insurance-card__badge">按需选择</span>
            <span class="insurance-card__price">¥30/天</span>
          </div>
          <h3 class="insurance-card__title">人身意外险</h3>
          <p class="insurance-card__desc">为驾乘人员提供意外伤害保障</p>
          <ul class="insurance-card__list">
            <li>意外伤害：¥50万</li>
            <li>意外医疗：¥5万</li>
            <li>24小时救援</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 常见问题 -->
    <div id="faq" class="guide-section">
      <h2 class="section-title">常见问题</h2>
      <div class="faq-list">
        <div v-for="(faq, index) in faqs" :key="index" class="faq-item">
          <div class="faq-question" @click="faq.open = !faq.open">
            <span class="faq-q">Q{{ index + 1 }}.</span>
            <span class="faq-title">{{ faq.question }}</span>
            <el-icon class="faq-icon" :class="{ rotated: faq.open }">
              <ArrowDown />
            </el-icon>
          </div>
          <div class="faq-answer" v-show="faq.open">
            <span class="faq-a">A.</span>
            <span>{{ faq.answer }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 取还车指南 -->
    <div id="pickup" class="guide-section">
      <h2 class="section-title">取还车指南</h2>
      <div class="pickup-grid">
        <div class="pickup-card">
          <h3 class="pickup-card__title">门店自取</h3>
          <p class="pickup-card__desc">前往我们遍布全国的服务网点取车</p>
          <div class="pickup-card__features">
            <span class="feature-tag">200+城市</span>
            <span class="feature-tag">500+门店</span>
            <span class="feature-tag">24小时服务</span>
          </div>
          <div class="pickup-card__steps">
            <div class="step-item">
              <span class="step-dot"></span>
              <span>到达门店后出示身份证、驾驶证</span>
            </div>
            <div class="step-item">
              <span class="step-dot"></span>
              <span>工作人员陪同验车</span>
            </div>
            <div class="step-item">
              <span class="step-dot"></span>
              <span>签署电子合同，取车出发</span>
            </div>
          </div>
        </div>
        <div class="pickup-card">
          <h3 class="pickup-card__title">送车上门</h3>
          <p class="pickup-card__desc">工作人员将车辆送到您指定地点</p>
          <div class="pickup-card__features">
            <span class="feature-tag">免费送车</span>
            <span class="feature-tag">提前预约</span>
            <span class="feature-tag">现场验车</span>
          </div>
          <div class="pickup-card__steps">
            <div class="step-item">
              <span class="step-dot"></span>
              <span>预约送车时间和地点</span>
            </div>
            <div class="step-item">
              <span class="step-dot"></span>
              <span>工作人员准时送达</span>
            </div>
            <div class="step-item">
              <span class="step-dot"></span>
              <span>现场验车，取车出发</span>
            </div>
          </div>
        </div>
        <div class="pickup-card">
          <h3 class="pickup-card__title">还车方式</h3>
          <p class="pickup-card__desc">多种还车方式，方便快捷</p>
          <div class="pickup-card__features">
            <span class="feature-tag">门店还车</span>
            <span class="feature-tag">上门取车</span>
            <span class="feature-tag">异地还车</span>
          </div>
          <div class="pickup-card__steps">
            <div class="step-item">
              <span class="step-dot"></span>
              <span>按约定时间还车</span>
            </div>
            <div class="step-item">
              <span class="step-dot"></span>
              <span>工作人员验车</span>
            </div>
            <div class="step-item">
              <span class="step-dot"></span>
              <span>结算费用，退还押金</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Calendar, Lock, QuestionFilled, Van, InfoFilled, ArrowDown } from '@element-plus/icons-vue'

// FAQ数据
const faqs = ref([
  {
    question: '租车需要什么证件？',
    answer: '租车需要提供本人有效身份证原件、有效驾驶证原件（满一年驾龄），部分车型可能需要信用卡预授权。',
    open: false
  },
  {
    question: '租车押金是多少？',
    answer: '押金根据车型不同，通常在2000-10000元之间。还车后3-7个工作日内，押金会原路退回。',
    open: false
  },
  {
    question: '可以异地还车吗？',
    answer: '支持异地还车服务，但可能产生一定的异地还车费用，具体费用可在下单时查看。',
    open: false
  },
  {
    question: '租车包含保险吗？',
    answer: '每笔订单都包含基础车辆损失险，您也可以根据需求选择升级保障服务包。',
    open: false
  },
  {
    question: '如何延长租车时间？',
    answer: '您可以在订单结束前通过App或联系客服申请续租，续租价格以当时系统价格为准。',
    open: false
  },
  {
    question: '遇到事故怎么办？',
    answer: '如发生事故，请立即联系客服（400-123-4567），工作人员会指导您处理，并协助保险理赔。',
    open: false
  }
])

// 滚动到指定区域
const scrollToSection = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<style scoped>
.guide-container {
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

/* 快速入口卡片 */
.quick-guide {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 40px;
}

.guide-card {
  background: white;
  border: 1px solid #edf2f7;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.guide-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.02);
  border-color: #c8a165;
}

.guide-card__icon {
  width: 48px;
  height: 48px;
  background: rgba(200, 161, 101, 0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #c8a165;
}

.guide-card__content h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 4px;
}

.guide-card__content p {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

/* 章节通用样式 */
.guide-section {
  margin-bottom: 48px;
}

.section-title {
  font-size: 22px;
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
  height: 20px;
  background: #c8a165;
  border-radius: 2px;
}

/* 租车流程 */
.process-steps {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.process-step {
  display: flex;
  gap: 24px;
  background: #f8fafc;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s ease;
}

.process-step:hover {
  background: white;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.02);
}

.process-step__number {
  font-size: 48px;
  font-weight: 800;
  color: rgba(200, 161, 101, 0.2);
  line-height: 1;
  min-width: 80px;
}

.process-step__content {
  flex: 1;
}

.process-step__content h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 8px;
}

.process-step__content p {
  font-size: 15px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 12px;
}

.process-step__tip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(200, 161, 101, 0.1);
  padding: 8px 16px;
  border-radius: 30px;
  font-size: 13px;
  color: #c8a165;
}

/* 保险说明 */
.insurance-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.insurance-card {
  background: white;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s ease;
}

.insurance-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.02);
  border-color: #c8a165;
}

.insurance-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.insurance-card__badge {
  padding: 4px 12px;
  background: #f1f5f9;
  border-radius: 30px;
  font-size: 12px;
  font-weight: 500;
  color: #475569;
}

.insurance-card__price {
  font-size: 18px;
  font-weight: 700;
  color: #c8a165;
}

.insurance-card__title {
  font-size: 18px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 8px;
}

.insurance-card__desc {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;
  line-height: 1.5;
}

.insurance-card__list {
  list-style: none;
  padding: 0;
  margin: 0 0 20px;
}

.insurance-card__list li {
  padding: 6px 0;
  padding-left: 20px;
  position: relative;
  font-size: 13px;
  color: #475569;
}

.insurance-card__list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #c8a165;
  font-weight: bold;
}

.insurance-card__btn {
  width: 100%;
  border-radius: 30px;
  border: 1px solid #c8a165;
  color: #c8a165;
  background: transparent;
}

.insurance-card__btn:hover {
  background: #c8a165;
  color: white;
}

/* FAQ */
.faq-list {
  border: 1px solid #edf2f7;
  border-radius: 16px;
  overflow: hidden;
}

.faq-item {
  border-bottom: 1px solid #edf2f7;
}

.faq-item:last-child {
  border-bottom: none;
}

.faq-question {
  display: flex;
  align-items: center;
  padding: 20px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.faq-question:hover {
  background: #f8fafc;
}

.faq-q {
  font-weight: 700;
  color: #c8a165;
  margin-right: 12px;
  min-width: 40px;
}

.faq-title {
  flex: 1;
  font-size: 15px;
  color: #1e2a3a;
  font-weight: 500;
}

.faq-icon {
  color: #94a3b8;
  transition: transform 0.3s ease;
}

.faq-icon.rotated {
  transform: rotate(180deg);
}

.faq-answer {
  display: flex;
  padding: 0 20px 20px 20px;
  margin-left: 52px;
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

.faq-a {
  font-weight: 700;
  color: #c8a165;
  margin-right: 12px;
  min-width: 40px;
}

/* 取还车指南 */
.pickup-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.pickup-card {
  background: white;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  padding: 24px;
}

.pickup-card__title {
  font-size: 18px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 8px;
}

.pickup-card__desc {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;
}

.pickup-card__features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.feature-tag {
  padding: 4px 12px;
  background: #f1f5f9;
  border-radius: 30px;
  font-size: 12px;
  color: #475569;
}

.pickup-card__steps {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #64748b;
}

.step-dot {
  width: 6px;
  height: 6px;
  background: #c8a165;
  border-radius: 50%;
}

/* 响应式 */
@media (max-width: 900px) {
  .quick-guide {
    grid-template-columns: repeat(2, 1fr);
  }

  .insurance-grid,
  .pickup-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .quick-guide,
  .insurance-grid,
  .pickup-grid {
    grid-template-columns: 1fr;
  }

  .process-step {
    flex-direction: column;
    gap: 12px;
  }

  .process-step__number {
    font-size: 36px;
  }

  .faq-question {
    flex-wrap: wrap;
  }

  .faq-answer {
    margin-left: 0;
  }
}
</style>
