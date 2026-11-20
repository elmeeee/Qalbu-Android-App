import { useEffect, useRef, useState } from 'react'
import { useI18n } from '../i18n'
import { DuaDemo, QiblaDemo, RemindersDemo, TasbihDemo, TrackerDemo, WidgetDemo } from './demos/Demos'
import { BellIcon, CheckIcon } from './Icons'

const FEATURE_KEYS = ['reminders', 'qibla', 'tasbih', 'dua', 'tracker', 'widget'] as const
type FeatureKey = (typeof FEATURE_KEYS)[number]

const ICONS: Record<FeatureKey, string | null> = {
  reminders: null,
  qibla: '/assets/icons/qibla_3d.webp',
  tasbih: '/assets/icons/tasbih_3d.webp',
  dua: '/assets/icons/doazikir_3d.webp',
  tracker: '/assets/icons/sunnah_3d.webp',
  widget: null,
}

/** Verified against the Google Play listing (description + screenshots + "What's new"). */
const AVAILABILITY: Record<FeatureKey, 'available' | 'unconfirmed'> = {
  reminders: 'available',
  qibla: 'available',
  tasbih: 'available',
  dua: 'available',
  tracker: 'available',
  widget: 'unconfirmed', // implemented in the app code; not mentioned in the store listing
}

const DEMOS: Record<FeatureKey, () => React.JSX.Element> = {
  reminders: RemindersDemo,
  qibla: QiblaDemo,
  tasbih: TasbihDemo,
  dua: DuaDemo,
  tracker: TrackerDemo,
  widget: WidgetDemo,
}

const TOOLS = ['faraidh', 'zakat', 'hajj', 'jamak', 'janazah', 'fidyah', 'encyclopedia', 'asmaulhusna', 'sunnah', 'radio'] as const
const TOOL_ICON: Record<(typeof TOOLS)[number], string> = {
  faraidh: 'faraidh_3d',
  zakat: 'zakat_3d',
  hajj: 'hajj_umrah_3d',
  jamak: 'jamak_3d',
  janazah: 'jenazah_3d',
  fidyah: 'fidyah_3d',
  encyclopedia: 'encyclopedia_3d',
  asmaulhusna: 'asmaulhusna_3d',
  sunnah: 'sunnah_3d',
  radio: 'radio_3d',
}

export function Features() {
  const { t } = useI18n()
  const [active, setActive] = useState<FeatureKey>('reminders')
  const [mounted, setMounted] = useState(false)
  const tabRefs = useRef<Record<string, HTMLButtonElement | null>>({})

  // Before hydration every panel is visible (works without JS); afterwards tabs take over.
  useEffect(() => setMounted(true), [])

  const onKeyDown = (e: React.KeyboardEvent) => {
    const i = FEATURE_KEYS.indexOf(active)
    let next = -1
    if (e.key === 'ArrowRight' || e.key === 'ArrowDown') next = (i + 1) % FEATURE_KEYS.length
    else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') next = (i - 1 + FEATURE_KEYS.length) % FEATURE_KEYS.length
    else if (e.key === 'Home') next = 0
    else if (e.key === 'End') next = FEATURE_KEYS.length - 1
    if (next < 0) return
    e.preventDefault()
    const k = FEATURE_KEYS[next]
    setActive(k)
    tabRefs.current[k]?.focus()
  }

  return (
    <section className="section features" id="features" aria-labelledby="features-title">
      <div className="container">
        <header className="section-head" data-reveal>
          <p className="eyebrow">
            <span className="eyebrow__dot" aria-hidden="true" />
            {t.features.eyebrow}
          </p>
          <h2 id="features-title" className="h2">
            {t.features.title}
          </h2>
          <p className="lead">{t.features.lead}</p>
        </header>

        <div className={`tabs ${mounted ? 'is-mounted' : ''}`} data-reveal style={{ ['--d' as string]: 1 }}>
          <div className="tabs__list" role="tablist" aria-label={t.features.tabsLabel} onKeyDown={onKeyDown}>
            {FEATURE_KEYS.map((k) => (
              <button
                key={k}
                ref={(el) => {
                  tabRefs.current[k] = el
                }}
                type="button"
                role="tab"
                id={`tab-${k}`}
                aria-controls={`panel-${k}`}
                aria-selected={active === k}
                tabIndex={active === k ? 0 : -1}
                className="tabs__tab"
                onClick={() => setActive(k)}
              >
                {ICONS[k] ? (
                  <img src={ICONS[k]!} alt="" width="28" height="30" className="tabs__icon" />
                ) : (
                  <span className="tabs__icon tabs__icon--glyph" aria-hidden="true">
                    {k === 'reminders' ? <BellIcon /> : <span className="tabs__widget-glyph" />}
                  </span>
                )}
                {t.features.items[k].tab}
              </button>
            ))}
          </div>

          {FEATURE_KEYS.map((k) => {
            const item = t.features.items[k]
            const Demo = DEMOS[k]
            const hidden = mounted && active !== k
            return (
              <div
                key={k}
                id={`panel-${k}`}
                role={mounted ? 'tabpanel' : undefined}
                aria-labelledby={mounted ? `tab-${k}` : undefined}
                hidden={hidden}
                className="tabs__panel"
                tabIndex={mounted ? 0 : undefined}
              >
                <div className="feature">
                  <div className="feature__copy">
                    {AVAILABILITY[k] === 'available' && (
                      <span className="badge badge--available">
                        <CheckIcon />
                        {t.common.availableAndroid}
                      </span>
                    )}
                    <h3 className="h3">{item.title}</h3>
                    <p>{item.body}</p>
                  </div>
                  <div className="feature__demo">{!hidden && <Demo />}</div>
                </div>
              </div>
            )
          })}
        </div>

        <div className="tools" data-reveal>
          <div className="tools__head">
            <h3 className="h3">{t.features.moreTitle}</h3>
            <p>{t.features.moreLead}</p>
          </div>
          <ul className="tools__grid">
            {TOOLS.map((k, i) => (
              <li key={k} className="tool" style={{ ['--i' as string]: i }}>
                <img src={`/assets/icons/${TOOL_ICON[k]}.webp`} alt="" width="72" height="77" loading="lazy" decoding="async" />
                <strong>{t.features.tools[k].name}</strong>
                <span>{t.features.tools[k].desc}</span>
              </li>
            ))}
            <li className="tool tool--calendar" style={{ ['--i' as string]: TOOLS.length }}>
              <span className="tool__cal" aria-hidden="true">
                <i />
              </span>
              <strong>{t.features.calendar.name}</strong>
              <span>{t.features.calendar.desc}</span>
            </li>
          </ul>
        </div>
      </div>
    </section>
  )
}
