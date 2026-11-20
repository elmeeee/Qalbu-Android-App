import { useEffect, useRef } from 'react'
import { I18nProvider, useI18n, type Locale } from './i18n'
import { MotionProvider, useRevealObserver } from './motion/MotionContext'
import { Header } from './components/Header'
import { Hero } from './components/Hero'
import { PrayerSection } from './components/PrayerSection'
import { Features } from './components/Features'
import { KimiSection } from './components/KimiSection'
import { About, Faq, FinalCta, Footer, Platforms } from './components/Sections'

function Page() {
  const { t, locale } = useI18n()
  useRevealObserver(locale)
  const first = useRef(true)
  useEffect(() => {
    const root = document.documentElement
    root.classList.remove('lang-pending')
    ;(window as Window & { __saatBooted?: boolean }).__saatBooted = true
    // Re-apply a #section deep link if the prerendered markup was replaced (non-ID locale).
    const id = decodeURIComponent(window.location.hash.slice(1))
    if (id) requestAnimationFrame(() => document.getElementById(id)?.scrollIntoView({ block: 'start', behavior: 'instant' }))
  }, [])
  // Announce language changes to screen readers (not on first render).
  useEffect(() => {
    if (first.current) {
      first.current = false
      return
    }
    const el = document.getElementById('lang-announcer')
    if (el) el.textContent = t.lang.changed
  }, [locale, t])
  return (
    <>
      <a className="skip-link" href="#main">
        {t.common.skip}
      </a>
      <Header />
      <main id="main" tabIndex={-1}>
        <Hero />
        <PrayerSection />
        <Features />
        <KimiSection />
        <Platforms />
        <About />
        <Faq />
        <FinalCta />
      </main>
      <Footer />
      <p className="sr-only" aria-live="polite" id="lang-announcer" />
    </>
  )
}

export default function App({ initialLocale }: { initialLocale: Locale }) {
  return (
    <I18nProvider initial={initialLocale}>
      <MotionProvider>
        <Page />
      </MotionProvider>
    </I18nProvider>
  )
}
