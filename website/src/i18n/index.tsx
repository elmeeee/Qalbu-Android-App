import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import id, { type Messages } from './locales/id'
import en from './locales/en'
import ms from './locales/ms'

export const LOCALES = ['id', 'en', 'ms'] as const
export type Locale = (typeof LOCALES)[number]
export const DEFAULT_LOCALE: Locale = 'id'
/** Shared with public/delete-account.html so the choice carries across pages. */
export const STORAGE_KEY = 'saat.lang'

export const MESSAGES: Record<Locale, Messages> = { id, en, ms }

/** Native names — shown the same in every locale. */
export const LOCALE_NAMES: Record<Locale, string> = {
  id: 'Bahasa Indonesia',
  en: 'English',
  ms: 'Bahasa Melayu',
}
export const LOCALE_SHORT: Record<Locale, string> = { id: 'ID', en: 'EN', ms: 'MS' }

export function isLocale(v: unknown): v is Locale {
  return typeof v === 'string' && (LOCALES as readonly string[]).includes(v)
}

/** Map a BCP-47 tag to a supported locale; unsupported -> null. */
export function matchLocale(tag: string | undefined | null): Locale | null {
  if (!tag) return null
  const t = tag.toLowerCase()
  if (t.startsWith('id') || t.startsWith('in')) return 'id'
  if (t.startsWith('ms') || t.startsWith('zsm')) return 'ms'
  if (t.startsWith('en')) return 'en'
  return null
}

/**
 * Resolution order: ?lang= query > saved choice > browser languages > Indonesian.
 * Must stay in sync with the inline script in index.html.
 */
export function detectLocale(): Locale {
  if (typeof window === 'undefined') return DEFAULT_LOCALE
  try {
    const q = new URLSearchParams(window.location.search).get('lang')
    if (isLocale(q)) return q
    const saved = window.localStorage.getItem(STORAGE_KEY)
    if (isLocale(saved)) return saved
  } catch {
    /* storage may be blocked */
  }
  const langs = navigator.languages?.length ? navigator.languages : [navigator.language]
  for (const l of langs) {
    const m = matchLocale(l)
    if (m) return m
  }
  return DEFAULT_LOCALE
}

/** Tiny `{name}` interpolation. */
export function fmt(template: string, vars: Record<string, string | number>): string {
  return template.replace(/\{(\w+)\}/g, (_, k: string) => (k in vars ? String(vars[k]) : `{${k}}`))
}

type Ctx = { locale: Locale; t: Messages; setLocale: (l: Locale) => void }
const I18nContext = createContext<Ctx | null>(null)

export function I18nProvider({ initial, children }: { initial: Locale; children: ReactNode }) {
  const [locale, setLocaleState] = useState<Locale>(initial)
  const t = MESSAGES[locale]

  const setLocale = useCallback((l: Locale) => {
    setLocaleState(l)
    try {
      window.localStorage.setItem(STORAGE_KEY, l)
    } catch {
      /* ignore */
    }
    // Drop a stale ?lang= so the saved choice wins on reload; keep the #section.
    const url = new URL(window.location.href)
    if (url.searchParams.has('lang')) {
      url.searchParams.delete('lang')
      window.history.replaceState(null, '', url.pathname + url.search + url.hash)
    }
  }, [])

  useEffect(() => {
    const root = document.documentElement
    root.lang = locale
    document.title = t.meta.title
    document.querySelector('meta[name="description"]')?.setAttribute('content', t.meta.description)
    document.querySelector('meta[property="og:title"]')?.setAttribute('content', t.meta.title)
    document.querySelector('meta[property="og:description"]')?.setAttribute('content', t.meta.description)
    document.querySelector('meta[property="og:locale"]')?.setAttribute('content', t.meta.ogLocale)
  }, [locale, t])

  const value = useMemo(() => ({ locale, t, setLocale }), [locale, t, setLocale])
  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>
}

export function useI18n(): Ctx {
  const ctx = useContext(I18nContext)
  if (!ctx) throw new Error('useI18n must be used inside I18nProvider')
  return ctx
}
