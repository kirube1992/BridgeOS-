import { createI18n } from 'vue-i18n'

export type Locale = 'en' | 'zh'

const messages = {
  en: {
    language: { english: 'English', chinese: '中文' },
    auth: {
      login: {
        eyebrowArt: 'One workspace / two perspectives',
        titleArt: 'Work that crosses the distance.',
        descriptionArt: 'One clear rhythm for teams in Ethiopia and China. Keep context close, move tasks forward, and make every handoff easier to understand.',
        eyebrow: 'Workspace access',
        title: 'Welcome back.',
        description: 'Sign in to pick up where your team left off.',
        email: 'Email address',
        emailPlaceholder: "you{'@'}company.com",
        password: 'Password',
        passwordPlaceholder: 'Enter your password',
        submit: 'Enter workspace →',
        loading: 'Signing in...',
        newToBridge: 'New to BridgeOS?',
        createAccount: 'Create an account',
        footer: 'Built for clearer collaboration across Ethiopia and China.',
        failed: 'Login failed'
      },
      register: {
        eyebrowArt: 'A better handoff starts here',
        titleArt: 'Make context travel with the task.',
        descriptionArt: 'Bridge language, time zones, and working styles in one calm place. Your team gets the clarity it needs without losing its own way of working.',
        eyebrow: 'Team onboarding',
        title: 'Set up your seat.',
        description: 'Join the workspace your cross-border team already uses.',
        name: 'Full name',
        namePlaceholder: 'Your full name',
        email: 'Work email',
        emailPlaceholder: "you{'@'}company.com",
        password: 'Password',
        passwordPlaceholder: 'Create a password',
        role: 'Your team',
        roles: { ethiopian: 'Ethiopian team', chinese: 'Chinese team', hq: 'HQ contact', admin: 'Workspace admin' },
        submit: 'Create my seat →',
        loading: 'Creating seat...',
        existing: 'Already have access?',
        signIn: 'Sign in',
        footer: 'BridgeOS / Ethiopia × China collaboration',
        success: 'Account created! Please login.',
        failed: 'Registration failed'
      },
      languageLabel: 'Language selector'
    },
    dashboard: {
      workspace: 'Workspace', overview: 'Overview', myTasks: 'My tasks', projects: 'Projects', people: 'People & teams', ethiopia: 'Ethiopia', china: 'China',
      logout: 'Log out / 退出', member: 'BridgeOS member', teamMember: 'Team member',
      date: 'TUESDAY · 18 AUGUST 2026', greeting: 'Good morning, {name}.',
      languageSummary: 'English · 中文',
      statsLabel: 'Workspace summary', activeProjects: 'Active projects', activeProjectsZh: '活跃项目', openTasks: 'Open tasks', openTasksZh: '待处理任务', teamMembers: 'Team members', teamMembersZh: '团队成员',
      totalProjects: '{count} total projects', openTaskSummary: '{count} open tasks', teamSummary: 'Live team directory', loading: 'Refreshing...', refresh: 'Refresh', retry: 'Try again', loadError: 'We could not load the latest workspace data.', loadingTasks: 'Loading recent tasks...', noTasks: 'No recent tasks found.', loadingTeam: 'Loading team directory...', noMembers: 'No team members found.', moreMembers: '+{count} more members', monthChange: 'Live from workspace', dueWeek: 'Open work items only', countries: 'Live team directory',
      queueTitle: 'Work queue', queueZh: '工作队列', queueDescription: 'Shared priorities, clear next steps.', addTask: '+ Add task',
      pulseTitle: 'Team pulse', pulseZh: '团队动态', pulseDescription: 'Workload by location', shared: 'Shared', timezone: 'UTC+3 / UTC+8',
      note: 'A little more context creates a much better handoff. Keep decisions and acceptance notes attached to every task.',
      tasks: {
        payment: { title: 'Confirm payment flow acceptance notes', owner: 'Mekdes · Addis Ababa', context: 'Due today', status: 'IN PROGRESS' },
        checklist: { title: 'Translate onboarding checklist for launch', owner: 'Wei · Shanghai', context: 'Due tomorrow', status: 'IN REVIEW' },
        release: { title: 'Share Friday release decision', owner: 'Shared team', context: 'Completed yesterday', status: 'DONE' },
        screens: { title: 'Review mobile handoff screens', owner: 'Liang · Shenzhen', context: 'Due 21 Aug', status: 'TODO' }
      }
    }
  },
  zh: {
    language: { english: 'English', chinese: '中文' },
    auth: {
      login: {
        eyebrowArt: '一个工作区 / 两种视角', titleArt: '让工作跨越距离。', descriptionArt: '为埃塞俄比亚和中国团队建立清晰的协作节奏。保留上下文，推进任务，让每一次交接都更容易理解。',
        eyebrow: '工作区登录', title: '欢迎回来。', description: '登录并继续团队未完成的工作。', email: '邮箱地址', emailPlaceholder: "you{'@'}company.com", password: '密码', passwordPlaceholder: '输入密码', submit: '进入工作区 →', loading: '正在登录...', newToBridge: '还没有 BridgeOS 账号？', createAccount: '创建账号', footer: '为埃塞俄比亚和中国之间更清晰的协作而打造。', failed: '登录失败'
      },
      register: {
        eyebrowArt: '从更好的交接开始', titleArt: '让上下文跟随任务流动。', descriptionArt: '在一个平静的空间里连接语言、时区和工作方式。让团队拥有所需的清晰度，同时保留自己的工作节奏。',
        eyebrow: '团队注册', title: '创建你的席位。', description: '加入你的跨国团队正在使用的工作区。', name: '姓名', namePlaceholder: '你的姓名', email: '工作邮箱', emailPlaceholder: "you{'@'}company.com", password: '密码', passwordPlaceholder: '创建密码', role: '你的团队', roles: { ethiopian: '埃塞俄比亚团队', chinese: '中国团队', hq: '总部联系人', admin: '工作区管理员' }, submit: '创建我的席位 →', loading: '正在创建...', existing: '已经有账号？', signIn: '登录', footer: 'BridgeOS / 埃塞俄比亚 × 中国协作', success: '账号创建成功！请登录。', failed: '注册失败'
      },
      languageLabel: '语言选择'
    },
    dashboard: {
      workspace: '工作区', overview: '总览', myTasks: '我的任务', projects: '项目', people: '成员与团队', ethiopia: '埃塞俄比亚', china: '中国', logout: '退出登录 / Log out', member: 'BridgeOS 成员', teamMember: '团队成员', date: '2026年8月18日 · 星期二', greeting: '早上好，{name}。', languageSummary: '中文 · English',
      statsLabel: '工作区摘要', activeProjects: '活跃项目', activeProjectsZh: '活跃项目', openTasks: '待处理任务', openTasksZh: '待处理任务', teamMembers: '团队成员', teamMembersZh: '团队成员', totalProjects: '共 {count} 个项目', openTaskSummary: '{count} 个待处理任务', teamSummary: '实时团队目录', loading: '正在刷新...', refresh: '刷新', retry: '重试', loadError: '无法加载最新的工作区数据。', loadingTasks: '正在加载最近任务...', noTasks: '暂无最近任务。', loadingTeam: '正在加载团队目录...', noMembers: '暂无团队成员。', moreMembers: '另有 {count} 位成员', monthChange: '来自工作区的实时数据', dueWeek: '仅统计待处理任务', countries: '实时团队目录',
      queueTitle: '工作队列', queueZh: 'Work queue', queueDescription: '共享优先级，明确下一步。', addTask: '+ 添加任务', pulseTitle: '团队动态', pulseZh: 'Team pulse', pulseDescription: '按地点查看工作量', shared: '共享团队', timezone: 'UTC+3 / UTC+8', note: '更多上下文会带来更好的交接。请将决策和验收说明附在每个任务中。',
      tasks: {
        payment: { title: '确认支付流程验收说明', owner: 'Mekdes · 亚的斯亚贝巴', context: '今天到期', status: '进行中' }, checklist: { title: '翻译上线入职清单', owner: 'Wei · 上海', context: '明天到期', status: '审核中' }, release: { title: '分享周五发布决定', owner: '共享团队', context: '昨天完成', status: '已完成' }, screens: { title: '审核移动端交接页面', owner: 'Liang · 深圳', context: '8月21日到期', status: '待处理' }
      }
    }
  }
}

const savedLocale = localStorage.getItem('bridgeos-locale') as Locale | null

export const i18n = createI18n({
  legacy: false,
  locale: savedLocale === 'zh' ? 'zh' : 'en',
  fallbackLocale: 'en',
  messages
})

export const setLocale = (locale: Locale): void => {
  i18n.global.locale.value = locale
  localStorage.setItem('bridgeos-locale', locale)
}
