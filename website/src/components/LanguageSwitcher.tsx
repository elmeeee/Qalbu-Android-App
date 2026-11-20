import { useRef } from 'react'
import { LOCALES, LOCALE_NAMES, LOCALE_SHORT, useI18n, type Locale } from '../i18n'
import { GlobeIcon } from './Icons'

/**
 * Segmented language control. Implemented as a radio group so it works with
 * keyboard (arrow keys / Tab), touch, and screen readers.
 */
export function LanguageSwitcher({ full = false, idPrefix }: { full?: boolean; idPrefix: string }) {
  const { locale, setLocale, t } = useI18n()
  const groupRef = useRef<HTMLDivElement>(null)
  const index = LOCALES.indexOf(locale)

  const choose = (l: Locale) => {
    if (l !== locale) setLocale(l)
  }

  const onKeyDown = (e: React.KeyboardEvent) => {
    const dir = e.key === 'ArrowRight' || e.key === 'ArrowDown' ? 1 : e.key === 'ArrowLeft' || e.key === 'ArrowUp' ? -1 : 0
    if (!dir) return
    e.preventDefault()
    const next = LOCALES[(index + dir + LOCALES.length) % LOCALES.length]
    choose(next)
    requestAnimationFrame(() => groupRef.current?.querySelector<HTMLButtonElement>(`[data-locale="${next}"]`)?.focus())
  }

  return (
    <div className={`lang ${full ? 'lang--full' : ''}`}>
      <span className="lang__label" id={`${idPrefix}-lang-label`}>
        <GlobeIcon className="lang__globe" />
        <span className={full ? '' : 'sr-only'}>{t.lang.label}</span>
      </span>
      <div
        ref={groupRef}
        className="lang__group"
        role="radiogroup"
        aria-labelledby={`${idPrefix}-lang-label`}
        onKeyDown={onKeyDown}
        style={{ ['--i' as string]: index }}
      >
        <span className="lang__thumb" aria-hidden="true" />
        {LOCALES.map((l) => (
          <button
            key={l}
            type="button"
            role="radio"
            aria-checked={l === locale}
            tabIndex={l === locale ? 0 : -1}
            data-locale={l}
            lang={l}
            className="lang__opt"
            onClick={() => choose(l)}
            aria-label={LOCALE_NAMES[l]}
            title={LOCALE_NAMES[l]}
          >
            {full ? LOCALE_NAMES[l] : LOCALE_SHORT[l]}
          </button>
        ))}
      </div>
    </div>
  )
}
