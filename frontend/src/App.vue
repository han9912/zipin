<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'

type Opportunity = {
  id: number
  category: string
  title: string
  content: string
  contact: string
  createdAt: string
}

type ApiResult<T> = {
  success: boolean
  data: T
  message: string
}

type TelegramAuthStatus = {
  enabled: boolean
  authenticated: boolean
  user: {
    username: string | null
    displayName: string
    pictureUrl: string | null
  } | null
}

const opportunities = ref<Opportunity[]>([])
const authStatus = ref<TelegramAuthStatus>({
  enabled: false,
  authenticated: false,
  user: null,
})
const authError = ref('')
const csrfToken = ref('')
const searchQuery = ref('')
const appliedSearchQuery = ref('')
const activeCategory = ref('全部')
const isLoading = ref(true)
const loadError = ref('')
const isSubmitting = ref(false)
const submitError = ref('')
const isCreateDialogOpen = ref(false)
const selectedOpportunity = ref<Opportunity | null>(null)

const form = reactive({
  category: '',
  title: '',
  content: '',
  contact: '',
})

const categories = computed(() => [
  '全部',
  ...new Set(opportunities.value.map((opportunity) => opportunity.category)),
])

const filteredOpportunities = computed(() => {
  return opportunities.value.filter((opportunity) => {
    const matchesCategory =
      activeCategory.value === '全部' || opportunity.category === activeCategory.value
    return matchesCategory
  })
})

async function readApiResult<T>(response: Response): Promise<T> {
  let result: ApiResult<T>
  try {
    result = (await response.json()) as ApiResult<T>
  } catch {
    throw new Error(
      response.ok
        ? '服务器返回了无法识别的响应'
        : `后端暂不可用（HTTP ${response.status}）`,
    )
  }
  if (!response.ok || !result.success) {
    throw new Error(result.message || `请求失败（${response.status}）`)
  }
  return result.data
}

async function loadOpportunities(keyword = appliedSearchQuery.value): Promise<boolean> {
  isLoading.value = true
  loadError.value = ''

  try {
    const query = new URLSearchParams()
    if (keyword) query.set('keyword', keyword)
    const queryString = query.toString()
    const response = await fetch(`/api/opportunities${queryString ? `?${queryString}` : ''}`)
    opportunities.value = await readApiResult<Opportunity[]>(response)
    return true
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '读取招聘信息失败'
    return false
  } finally {
    isLoading.value = false
  }
}

async function loadAuthStatus() {
  authError.value = ''
  try {
    const response = await fetch('/api/auth/status')
    authStatus.value = await readApiResult<TelegramAuthStatus>(response)
  } catch (error) {
    authError.value = error instanceof Error ? error.message : '读取登录状态失败'
  }
}

async function getCsrfToken(): Promise<string> {
  if (csrfToken.value) return csrfToken.value
  const response = await fetch('/api/auth/csrf')
  csrfToken.value = await readApiResult<string>(response)
  return csrfToken.value
}

function openCreateDialog() {
  if (!authStatus.value.authenticated) {
    if (authStatus.value.enabled) {
      window.location.assign('/api/auth/telegram/login/telegram')
    } else {
      authError.value = 'Telegram 登录尚未配置，请先配置 Bot 和 OIDC 参数。'
    }
    return
  }
  isCreateDialogOpen.value = true
}

async function logout() {
  authError.value = ''
  try {
    const token = await getCsrfToken()
    const response = await fetch('/api/auth/logout', {
      method: 'POST',
      headers: { 'X-XSRF-TOKEN': token },
    })
    await readApiResult<void>(response)
    authStatus.value = { enabled: authStatus.value.enabled, authenticated: false, user: null }
    csrfToken.value = ''
  } catch (error) {
    authError.value = error instanceof Error ? error.message : '退出登录失败'
  }
}

async function searchOpportunities() {
  const keyword = searchQuery.value.trim()
  searchQuery.value = keyword
  appliedSearchQuery.value = keyword
  activeCategory.value = '全部'
  await loadOpportunities(keyword)
}

async function clearSearch() {
  searchQuery.value = ''
  appliedSearchQuery.value = ''
  activeCategory.value = '全部'
  await loadOpportunities('')
}

async function submitOpportunity() {
  isSubmitting.value = true
  submitError.value = ''

  try {
    const token = await getCsrfToken()
    const response = await fetch('/api/opportunities', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': token },
      body: JSON.stringify(form),
    })
    await readApiResult<Opportunity>(response)
    activeCategory.value = '全部'
    searchQuery.value = ''
    appliedSearchQuery.value = ''
    if (!(await loadOpportunities(''))) {
      throw new Error('信息已保存，但首页刷新失败，请稍后重试')
    }
    form.category = ''
    form.title = ''
    form.content = ''
    form.contact = ''
    isCreateDialogOpen.value = false
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : '发布失败，请稍后重试'
  } finally {
    isSubmitting.value = false
  }
}

function formatCreatedAt(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

function closeDialogs() {
  isCreateDialogOpen.value = false
  selectedOpportunity.value = null
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') closeDialogs()
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  void loadOpportunities()
  void loadAuthStatus()
})

onUnmounted(() => window.removeEventListener('keydown', handleKeydown))
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <a class="brand" href="#" aria-label="Zipin 首页">
        <span class="brand-mark" aria-hidden="true">
          <svg viewBox="0 0 32 32" fill="none">
            <path
              d="M7 8.5h18L10 23.5h15"
              stroke="currentColor"
              stroke-width="3"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </span>
        <span>zipin<span class="brand-dot">.</span></span>
      </a>
      <nav class="main-nav" aria-label="主导航">
        <a class="nav-link active" href="#opportunities">招聘信息</a>
      </nav>
      <div class="auth-controls">
        <a
          v-if="!authStatus.authenticated && authStatus.enabled"
          class="telegram-login-link"
          href="/api/auth/telegram/login/telegram"
        >
          Telegram 登录
        </a>
        <span v-else-if="!authStatus.authenticated" class="auth-unavailable">登录未配置</span>
        <template v-else>
          <span class="auth-display-name">{{ authStatus.user?.displayName }}</span>
          <button class="text-button" type="button" @click="logout">退出</button>
        </template>
      </div>
      <button class="button button-dark publish-button" type="button" @click="openCreateDialog">
        <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path
            d="M10 4v12M4 10h12"
            stroke="currentColor"
            stroke-width="1.7"
            stroke-linecap="round"
          />
        </svg>
        发布信息
      </button>
    </header>
    <p v-if="authError" class="auth-error" role="alert">{{ authError }}</p>

    <main>
      <section class="hero">
        <div class="hero-orbit orbit-one" aria-hidden="true"></div>
        <div class="hero-orbit orbit-two" aria-hidden="true"></div>
        <div class="hero-content">
          <div class="eyebrow"><span class="eyebrow-dot"></span> 简单发布，直接联系</div>
          <h1>好机会，<span>正在发生。</span></h1>
          <p class="hero-description">用几句话说清楚需求，让合适的人找到你。</p>
          <form class="search-panel" role="search" @submit.prevent="searchOpportunities">
            <label class="search-field">
              <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="10.8" cy="10.8" r="6.8" stroke="currentColor" stroke-width="1.7" />
                <path
                  d="m16 16 4 4"
                  stroke="currentColor"
                  stroke-width="1.7"
                  stroke-linecap="round"
                />
              </svg>
              <input
                v-model="searchQuery"
                aria-label="搜索招聘信息"
                placeholder="搜索类目、标题或内容"
                type="search"
              />
            </label>
            <button
              v-if="searchQuery"
              class="search-clear-button"
              type="button"
              aria-label="清空搜索"
              @click="clearSearch"
            >
              清空
            </button>
            <button class="button button-accent search-button" type="submit" :disabled="isLoading">
              搜索
            </button>
          </form>
        </div>
        <div class="hero-sticker" aria-hidden="true">
          <span>MAKE</span>
          <strong>ROOM</strong>
          <span>FOR MORE</span>
          <svg viewBox="0 0 34 34" fill="none">
            <path
              d="M8 25 25 8M10 8h15v15"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </div>
      </section>

      <section id="opportunities" class="content-layout">
        <div class="feed-column">
          <div class="section-heading">
            <div>
              <div class="section-kicker">OPPORTUNITIES</div>
              <h2>最新招聘信息</h2>
            </div>
            <button class="text-button refresh-button" type="button" @click="loadOpportunities()">
              重新读取
            </button>
          </div>

          <div v-if="categories.length > 1" class="category-tabs" aria-label="信息类目">
            <button
              v-for="category in categories"
              :key="category"
              type="button"
              :class="{ selected: activeCategory === category }"
              :aria-pressed="activeCategory === category"
              @click="activeCategory = category"
            >
              {{ category }}
            </button>
          </div>

          <div class="feed-toolbar">
            <p v-if="!isLoading && !loadError">
              找到 <strong>{{ filteredOpportunities.length }}</strong> 条信息
            </p>
            <p v-else>招聘信息来自数据库</p>
            <span class="database-label"><span></span> MySQL</span>
          </div>

          <div v-if="isLoading" class="state-panel" role="status">
            正在读取招聘信息…
          </div>
          <div v-else-if="loadError" class="state-panel error-panel" role="alert">
            <p>暂时无法读取招聘信息：{{ loadError }}</p>
            <button class="button button-outline" type="button" @click="loadOpportunities()">
              重试
            </button>
          </div>
          <div v-else-if="filteredOpportunities.length" class="opportunity-list">
            <article
              v-for="opportunity in filteredOpportunities"
              :key="opportunity.id"
              class="opportunity-card"
            >
              <div class="company-avatar" aria-hidden="true">{{ opportunity.category.slice(0, 1) }}</div>
              <div class="card-main">
                <div class="card-title-row">
                  <span class="category-badge">{{ opportunity.category }}</span>
                  <span class="published-at">{{ formatCreatedAt(opportunity.createdAt) }}</span>
                </div>
                <h3 class="opportunity-title">
                  <button type="button" @click="selectedOpportunity = opportunity">
                    {{ opportunity.title }}
                  </button>
                </h3>
                <p class="opportunity-content">{{ opportunity.content }}</p>
                <div class="contact-row">
                  <span class="contact-label">联系人</span>
                  <span>{{ opportunity.contact }}</span>
                </div>
              </div>
              <button
                class="details-link"
                type="button"
                :aria-label="`查看${opportunity.title}详情`"
                @click.stop="selectedOpportunity = opportunity"
              >
                <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
                  <path
                    d="M3 8h9m-4-4 4 4-4 4"
                    stroke="currentColor"
                    stroke-width="1.4"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </button>
            </article>
          </div>
          <div v-else class="empty-state">
            <span class="empty-icon" aria-hidden="true">⌕</span>
            <h3>
              {{ appliedSearchQuery || activeCategory !== '全部' ? '没有匹配的信息' : '还没有招聘信息' }}
            </h3>
            <p>
              {{
                appliedSearchQuery || activeCategory !== '全部'
                  ? '试试修改搜索词或类目。'
                  : '发布第一条信息后，它会从数据库读取并显示在这里。'
              }}
            </p>
            <button
              v-if="!appliedSearchQuery && activeCategory === '全部'"
              class="button button-accent"
              type="button"
              @click="openCreateDialog"
            >
              发布第一条信息
            </button>
            <button
              v-else
              class="button button-outline"
              type="button"
              @click="clearSearch"
            >
              清除筛选
            </button>
          </div>
        </div>

        <aside class="sidebar">
          <section class="side-card welcome-card">
            <div class="side-card-icon sparkle-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none">
                <path
                  d="m12 3 1.8 6.2L20 11l-6.2 1.8L12 19l-1.8-6.2L4 11l6.2-1.8L12 3Z"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
            <p class="side-eyebrow">KEEP IT SIMPLE</p>
            <h3>不必套用复杂模板，<br />把需求说清楚就好。</h3>
            <p class="side-copy">类目、标题、内容和联系人，几步完成发布。</p>
            <button class="side-link" type="button" @click="openCreateDialog">
              发布一条信息
              <span aria-hidden="true">→</span>
            </button>
          </section>

          <section class="side-card info-card">
            <div class="side-section-title">
              <h3>一条信息包含</h3>
              <span>4 FIELDS</span>
            </div>
            <div class="field-row"><span>01</span><strong>类目</strong></div>
            <div class="field-row"><span>02</span><strong>标题</strong></div>
            <div class="field-row"><span>03</span><strong>需求内容</strong></div>
            <div class="field-row"><span>04</span><strong>联系人</strong></div>
          </section>
          <p class="sidebar-note">每条信息直接保存至 MySQL，首页按最新发布顺序读取。</p>
        </aside>
      </section>
    </main>

    <footer class="footer">
      <a class="footer-brand" href="#">zipin<span>.</span></a>
      <span>给下一步，多一点可能。</span>
      <span class="footer-year">© 2026 ZIPIN</span>
    </footer>

    <Transition name="fade">
      <div
        v-if="isCreateDialogOpen"
        class="modal-backdrop"
        @click.self="isCreateDialogOpen = false"
      >
        <section
          class="detail-modal create-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="create-title"
        >
          <button
            class="modal-close"
            type="button"
            aria-label="关闭发布表单"
            @click="isCreateDialogOpen = false"
          >
            ×
          </button>
          <p class="modal-eyebrow">NEW OPPORTUNITY</p>
          <h2 id="create-title">发布招聘信息</h2>
          <p class="modal-intro">填写简要信息，发布后会保存至数据库并显示在首页。</p>

          <form class="opportunity-form" @submit.prevent="submitOpportunity">
            <label>
              <span>类目 <em>*</em></span>
              <input
                v-model="form.category"
                name="category"
                maxlength="80"
                placeholder="例如：远程兼职"
                required
              />
            </label>
            <label>
              <span>标题 <em>*</em></span>
              <input
                v-model="form.title"
                name="title"
                maxlength="160"
                placeholder="用一句话概括需求"
                required
              />
            </label>
            <label>
              <span>需求内容 <em>*</em></span>
              <textarea
                v-model="form.content"
                name="content"
                maxlength="4000"
                rows="5"
                placeholder="描述工作内容、要求或合作方式"
                required
              ></textarea>
              <small>{{ form.content.length }}/4000</small>
            </label>
            <label>
              <span>联系人 <em>*</em></span>
              <input
                v-model="form.contact"
                name="contact"
                maxlength="120"
                placeholder="Telegram、邮箱或其他联系方式"
                required
              />
            </label>
            <p v-if="submitError" class="form-error" role="alert">{{ submitError }}</p>
            <button class="button button-dark modal-action" type="submit" :disabled="isSubmitting">
              {{ isSubmitting ? '正在保存…' : '发布并保存' }}
            </button>
          </form>
        </section>
      </div>
    </Transition>

    <Transition name="fade">
      <div
        v-if="selectedOpportunity"
        class="modal-backdrop"
        @click.self="selectedOpportunity = null"
      >
        <section
          class="detail-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="detail-title"
        >
          <button
            class="modal-close"
            type="button"
            aria-label="关闭详情"
            @click="selectedOpportunity = null"
          >
            ×
          </button>
          <span class="category-badge">{{ selectedOpportunity.category }}</span>
          <h2 id="detail-title" class="detail-title">{{ selectedOpportunity.title }}</h2>
          <p class="detail-date">{{ formatCreatedAt(selectedOpportunity.createdAt) }}</p>
          <div class="detail-content">{{ selectedOpportunity.content }}</div>
          <div class="detail-contact">
            <span class="contact-label">联系人</span>
            <strong>{{ selectedOpportunity.contact }}</strong>
          </div>
        </section>
      </div>
    </Transition>
  </div>
</template>
