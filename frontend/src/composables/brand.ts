import { onBeforeUnmount, onMounted, ref, type Ref } from 'vue'

export type CompanyId = 'comfura' | 'mesh' | 'bridge'

export interface CompanyLeader {
  name: string
  role: string
  company: CompanyId
  initials: string
  bio: string
}

export interface CompanyStat {
  label: string
  value: number
  suffix?: string
  accent: CompanyId
}

export interface CompanyData {
  id: CompanyId
  name: string
  shortName: string
  country: string
  countryCode: string
  tagline: string
  mission: string
  founded: string
  foundedYear: number
  values: { title: string; desc: string }[]
  focus: string[]
  stats: CompanyStat[]
  leadership: CompanyLeader[]
  heroQuote: string
}

export interface AllianceMilestone {
  year: string
  month?: string
  side: CompanyId | 'bridge'
  title: string
  description: string
}

export interface JointProject {
  code: string
  title: string
  description: string
  status: 'Live' | 'In Progress' | 'Concept'
  progress: number
  tags: string[]
}

export const comfuraData: CompanyData = {
  id: 'comfura',
  name: 'Comfura Technologies',
  shortName: 'Comfura',
  country: 'Ethiopia',
  countryCode: 'ET',
  tagline: 'African Innovation, Global Standards',
  mission:
    'To build software products that honor local context — solving real problems for Ethiopian and East African markets with global engineering rigor.',
  founded: 'Addis Ababa, 2019',
  foundedYear: 2019,
  values: [
    { title: 'Rooted Localization', desc: 'Solutions shaped by on-the-ground research and Amharic-first UX.' },
    { title: 'Engineering Pride', desc: 'Clean code, honest deadlines, and craftsmanship we can defend at any review.' },
    { title: 'Community Led', desc: 'We mentor, we open-source, we share knowledge across the African tech ecosystem.' }
  ],
  focus: ['Enterprise SaaS', 'Public Sector Platforms', 'Fintech & Payments', 'Logistics Systems'],
  stats: [
    { label: 'Engineers', value: 48, suffix: '+', accent: 'comfura' },
    { label: 'Projects Delivered', value: 127, accent: 'comfura' },
    { label: 'Enterprise Clients', value: 22, accent: 'comfura' },
    { label: 'Open Source Stars', value: 3400, suffix: '+', accent: 'comfura' }
  ],
  leadership: [
    { name: 'Selamawit Bekele', role: 'Chief Executive Officer', company: 'comfura', initials: 'SB',
      bio: 'Former product lead at pan-African fintech. Named among the Top 50 Women in Tech Africa 2025.' },
    { name: 'Dawit Mengistu', role: 'Chief Technology Officer', company: 'comfura', initials: 'DM',
      bio: '10+ years architecting distributed systems. PhD candidate, distributed systems, AAiT.' },
    { name: 'Tsion Abera', role: 'Design Director', company: 'comfura', initials: 'TA',
      bio: 'Localization-first designer. Built the Amharic design system now used by 3 Ethiopian banks.' }
  ],
  heroQuote:
    '“We build software in Addis — for Addis, for Africa, and for partners worldwide who value craft with context.”'
}

export const meshData: CompanyData = {
  id: 'mesh',
  name: 'Mesh Asia Technology',
  shortName: 'Mesh Asia',
  country: 'China',
  countryCode: 'CN',
  tagline: 'Deep Infrastructure, Hyper-Scale Delivery',
  mission:
    'To design and ship the software infrastructure that scales — from payment rails serving hundreds of millions to supply-chain orchestration across global trade corridors.',
  founded: 'Guangzhou, 2014',
  foundedYear: 2014,
  values: [
    { title: 'Systems Thinking', desc: 'We model for scale, latency, and failure. Every. Single. Time.' },
    { title: 'Radical Delivery', desc: 'Shipping beats perfection — but perfection ships eventually. Bi-weekly releases, always.' },
    { title: 'Partnership Over Distance', desc: 'Timezones are engineering problems. Alignment is a product we ship daily.' }
  ],
  focus: ['High-Frequency Platforms', 'Cross-Border Payments', 'Supply Chain SaaS', 'AI Infrastructure'],
  stats: [
    { label: 'Engineers', value: 210, suffix: '+', accent: 'mesh' },
    { label: 'Regions Served', value: 34, accent: 'mesh' },
    { label: 'Peak TPS Handled', value: 120000, suffix: '+', accent: 'mesh' },
    { label: '99.99% Uptime (yrs)', value: 7, suffix: ' y', accent: 'mesh' }
  ],
  leadership: [
    { name: 'Lin Weihao', role: 'Founder & Chairman', company: 'mesh', initials: 'LW',
      bio: 'Ex-Principal Engineer, Alibaba Cloud. Speaker at QCon Global and ArchSummit on payment architecture.' },
    { name: 'Chen Xiaoru', role: 'VP of Engineering', company: 'mesh', initials: 'CX',
      bio: 'Led the rebuild of Guangzhou metro clearing systems. Author of two distributed systems patents.' },
    { name: 'Zhao Yiming', role: 'Head of Global Partnerships', company: 'mesh', initials: 'ZY',
      bio: '15-year China-Africa trade corridor veteran. Fluent in Mandarin, English, and basic Amharic.' }
  ],
  heroQuote:
    '“We bring hyper-scale infrastructure know-how. Comfura brings the people and context where the software actually runs.”'
}

export const allianceTimeline: AllianceMilestone[] = [
  { year: '2024', month: 'Mar', side: 'mesh',
    title: 'First Trade Mission',
    description: 'Mesh Asia delegation visits Addis Ababa for the Ethiopian-China Tech Investment Summit.' },
  { year: '2024', month: 'Jul', side: 'comfura',
    title: 'Engineering Exchange',
    description: '8 Comfura engineers spend 6 weeks at Mesh Asia HQ in Guangzhou on payment-rail architecture.' },
  { year: '2024', month: 'Nov', side: 'bridge',
    title: 'BridgeOS Concept Signed',
    description: 'Both CEOs sign a joint-venture MOU to co-develop a collaboration platform for cross-border teams.' },
  { year: '2025', month: 'Feb', side: 'bridge',
    title: 'Pilot: Cross-Border Logistics',
    description: 'BridgeOS v0.3 pilots with the Addis Ababa ↔ Guangzhou air-freight corridor, 14 teams onboarded.' },
  { year: '2025', month: 'Sep', side: 'bridge',
    title: 'AI Stack Integrations',
    description: 'The jointly built AI Service layer ships: requirement translation, meeting extraction, assistant.' },
  { year: '2026', month: 'Q2', side: 'bridge',
    title: 'Commercial Rollout',
    description: 'BridgeOS is offered commercially to other Ethio-China trade partnerships and beyond.' }
]

export const jointProjects: JointProject[] = [
  { code: 'BOS-01', title: 'BridgeOS Core Platform',
    description: 'Cross-border collaboration platform powering requirement translation, AI assistance, and unified project visibility.',
    status: 'Live', progress: 92,
    tags: ['Platform', 'Vue 3', 'Spring Boot', 'FastAPI'] },
  { code: 'ETCN-04', title: 'Addis–Guangzhou Freight Tracker',
    description: 'Real-time air-freight tracker reconciling customs data from both ends of the corridor.',
    status: 'In Progress', progress: 68,
    tags: ['Logistics', 'IoT', 'Customs'] },
  { code: 'PAY-11', title: 'Dual-Corridor Payment Rail',
    description: 'Scalable payment clearing with Ethiopian Birr and Chinese Yuan corridors, KYC, and sanctions screening.',
    status: 'In Progress', progress: 51,
    tags: ['Fintech', 'Payments', 'Compliance'] },
  { code: 'AI-03', title: 'Bilingual AI Layer',
    description: 'Requirement translation (Amharic ↔ Mandarin ↔ English), meeting extractor, and project RAG assistant.',
    status: 'Live', progress: 88,
    tags: ['AI', 'LLM', 'RAG'] }
]

export function useScrollReveal(target: Ref<HTMLElement | HTMLElement[] | null>) {
  const visible = ref(false)
  let observer: IntersectionObserver | null = null

  const observe = (el: Element) => {
    if (!(el instanceof HTMLElement)) return
    if (!observer) {
      observer = new IntersectionObserver((entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            visible.value = true
            ;(entry.target as HTMLElement).classList.add('is-visible')
            observer?.unobserve(entry.target)
          }
        }
      }, { threshold: 0.15, rootMargin: '0px 0px -60px 0px' })
    }
    observer.observe(el)
  }

  onMounted(() => {
    const val = target.value
    if (val instanceof HTMLElement) {
      observe(val)
    } else if (Array.isArray(val)) {
      val.forEach(observe)
    }
  })

  onBeforeUnmount(() => observer?.disconnect())

  return { visible }
}

export function useCountUp(value: Ref<number>, trigger: Ref<boolean>, durationMs = 1800) {
  const display = ref(0)
  let raf = 0
  let startTs = 0

  const tick = (ts: number) => {
    if (!startTs) startTs = ts
    const t = Math.min(1, (ts - startTs) / durationMs)
    const eased = 1 - Math.pow(1 - t, 3)
    display.value = Math.round(value.value * eased)
    if (t < 1) raf = requestAnimationFrame(tick)
  }

  onMounted(() => {
    if (trigger.value) raf = requestAnimationFrame(tick)
  })

  const begin = () => {
    if (startTs) return
    raf = requestAnimationFrame(tick)
  }

  onBeforeUnmount(() => cancelAnimationFrame(raf))

  return { display, begin }
}
