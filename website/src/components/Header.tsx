import { useCallback, useEffect, useRef, useState } from 'react'
import { useI18n } from '../i18n'
import { playUrl } from '../content/links'
import { LanguageSwitcher } from './LanguageSwitcher'
import { MotionToggle } from './MotionToggle'
import { CloseIcon, GooglePlayIcon, MenuIcon } from './Icons'

export const SECTION_IDS = ['features', 'kimi', 'platforms', 'about', 'faq'] as const
type SectionId = (typeof SECTION_IDS)[number]

function useActiveSection(): SectionId | null {
  const [active, setActive] = useState<SectionId | null>(null)
  useEffect(() => {
    if (!('IntersectionObserver' in window)) return
    const visible = new Map<string, number>()
    const io = new IntersectionObserver(
      (entries) => {
        for (const e of entries) visible.set(e.target.id, e.isIntersecting ? e.intersectionRatio : 0)
        let best: SectionId | null = null
        let bestRatio = 0
        for (const id of SECTION_IDS) {
          const r = visible.get(id) ?? 0
          if (r > bestRatio) {
            best = id
            bestRatio = r
          }
        }
        setActive(best)
      },
      { rootMargin: '-35% 0px -55% 0px', threshold: [0, 0.01, 0.2, 0.5, 1] },
    )
    SECTION_IDS.forEach((id) => {
      const el = document.getElementById(id)
      if (el) io.observe(el)
    })
    return () => io.disconnect()
  }, [])
  return active
}

export function Header() {
  const { t, locale } = useI18n()
  const [open, setOpen] = useState(false)
  const [scrolled, setScrolled] = useState(false)
  const active = useActiveSection()
  const menuRef = useRef<HTMLDivElement>(null)
  const toggleRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 12)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  const close = useCallback((restoreFocus = true) => {
    setOpen(false)
    if (restoreFocus) requestAnimationFrame(() => toggleRef.current?.focus())
  }, [])

  // Mobile menu: lock scroll, focus trap, Escape to close.
  useEffect(() => {
    if (!open) return
    const root = document.documentElement
    root.classList.add('menu-open')
    const panel = menuRef.current
    const focusables = () =>
      Array.from(panel?.querySelectorAll<HTMLElement>('a[href], button:not([disabled]), [tabindex="0"]') ?? []).filter(
        (el) => el.offsetParent !== null,
      )
    requestAnimationFrame(() => focusables()[0]?.focus())
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        e.preventDefault()
        close()
      } else if (e.key === 'Tab') {
        const f = focusables()
        if (!f.length) return
        const first = f[0]
        const last = f[f.length - 1]
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault()
          last.focus()
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault()
          first.focus()
        }
      }
    }
    const onResize = () => {
      if (window.innerWidth >= 1200) close(false)
    }
    document.addEventListener('keydown', onKey)
    window.addEventListener('resize', onResize)
    return () => {
      root.classList.remove('menu-open')
      document.removeEventListener('keydown', onKey)
      window.removeEventListener('resize', onResize)
    }
  }, [open, close])

  const links = SECTION_IDS.map((id) => ({ id, label: t.nav[id] }))

  return (
    <header className={`site-header ${scrolled ? 'is-scrolled' : ''} ${open ? 'is-open' : ''}`}>
      <div className="site-header__bar container">
        <a className="brand" href="#top" aria-label={t.nav.home}>
          <img className="brand__icon" src="/assets/app/saat-icon.webp" alt="" width="36" height="36" />
          <span className="brand__word">Sāat</span>
        </a>

        <nav className="nav-desktop" aria-label={t.nav.label}>
          <ul>
            {links.map((l) => (
              <li key={l.id}>
                <a href={`#${l.id}`} className="nav-link" aria-current={active === l.id ? 'true' : undefined}>
                  {l.label}
                </a>
              </li>
            ))}
          </ul>
        </nav>

        <div className="site-header__actions">
          <div className="hide-mobile">
            <LanguageSwitcher idPrefix="desk" />
          </div>
          <MotionToggle className="hide-tablet" />
          <a className="btn btn--primary btn--sm hide-mobile" href={playUrl(locale)} target="_blank" rel="noopener">
            <GooglePlayIcon className="btn__icon" />
            {t.nav.download}
            <span className="sr-only"> {t.common.newTab}</span>
          </a>
          <button
            ref={toggleRef}
            type="button"
            className="menu-toggle"
            aria-expanded={open}
            aria-controls="mobile-menu"
            onClick={() => (open ? close() : setOpen(true))}
          >
            {open ? <CloseIcon /> : <MenuIcon />}
            <span className="sr-only">{open ? t.nav.menuClose : t.nav.menuOpen}</span>
          </button>
        </div>
      </div>

      <div
        id="mobile-menu"
        ref={menuRef}
        className="mobile-menu"
        role="dialog"
        aria-modal="true"
        aria-label={t.nav.menuTitle}
        data-open={open}
      >
        <nav aria-label={t.nav.label}>
          <ul className="mobile-menu__links">
            {links.map((l, i) => (
              <li key={l.id} style={{ ['--i' as string]: i }}>
                <a href={`#${l.id}`} onClick={() => close(false)} aria-current={active === l.id ? 'true' : undefined}>
                  {l.label}
                </a>
              </li>
            ))}
          </ul>
        </nav>
        <div className="mobile-menu__footer">
          <LanguageSwitcher full idPrefix="mob" />
          <MotionToggle />
          <a className="btn btn--primary btn--block" href={playUrl(locale)} target="_blank" rel="noopener">
            <GooglePlayIcon className="btn__icon" />
            {t.store.full}
            <span className="sr-only"> {t.common.newTab}</span>
          </a>
        </div>
      </div>
    </header>
  )
}
