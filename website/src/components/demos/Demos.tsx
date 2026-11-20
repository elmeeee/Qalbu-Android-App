import { useEffect, useRef, useState, type PointerEvent as RPointerEvent } from 'react'
import { fmt, useI18n } from '../../i18n'
import { sampleSource } from '../../content/prayer'
import { CheckIcon, ChevronLeft, ChevronRight, MosqueIcon, ResetIcon } from '../Icons'

/* ---------------- Reminders ---------------- */
export function RemindersDemo() {
  const { t } = useI18n()
  const d = t.demos.reminders
  const [run, setRun] = useState(0)
  const items = [
    { title: fmt(d.prayerTitle, { prayer: t.prayers.dhuhr }), body: fmt(d.prayerBody, { prayer: t.prayers.dhuhr }), time: d.now, accent: true },
    { title: d.dhuhaTitle, body: d.dhuhaBody, time: '08:15' },
    { title: d.fastTitle, body: d.fastBody, time: '20:00' },
  ]
  return (
    <div className="demo demo--reminders">
      <span className="demo__label">
        <span className="badge badge--sample">{t.common.sample}</span>
        {d.label}
      </span>
      <ul className="notif-stack" key={run}>
        {items.map((n, i) => (
          <li key={i} className={`notif ${n.accent ? 'notif--accent' : ''}`} style={{ ['--i' as string]: i }}>
            <img src="/assets/app/saat-icon.webp" alt="" width="36" height="36" className="notif__icon" />
            <div className="notif__body">
              <div className="notif__top">
                <strong>Sāat</strong>
                <span>· {n.time}</span>
              </div>
              <p className="notif__title">{n.title}</p>
              <p className="notif__text">{n.body}</p>
            </div>
          </li>
        ))}
      </ul>
      <button type="button" className="btn btn--soft btn--sm" onClick={() => setRun((r) => r + 1)}>
        <ResetIcon className="btn__icon" />
        {d.replay}
      </button>
    </div>
  )
}

/* ---------------- Qibla (visual demo only) ---------------- */
const EXAMPLE_BEARING = 295 // arbitrary example marker, NOT a real Qibla bearing for the visitor

export function QiblaDemo() {
  const { t } = useI18n()
  const d = t.demos.qibla
  const [heading, setHeading] = useState(250)
  const dialRef = useRef<HTMLDivElement>(null)
  const drag = useRef<{ startAngle: number; startHeading: number } | null>(null)

  const norm = (v: number) => ((Math.round(v) % 360) + 360) % 360
  const diff = Math.abs(((heading - EXAMPLE_BEARING + 540) % 360) - 180)
  const aligned = diff <= 6

  const angleFrom = (e: RPointerEvent) => {
    const r = dialRef.current!.getBoundingClientRect()
    return (Math.atan2(e.clientY - (r.top + r.height / 2), e.clientX - (r.left + r.width / 2)) * 180) / Math.PI
  }
  const onDown = (e: RPointerEvent<HTMLDivElement>) => {
    e.currentTarget.setPointerCapture(e.pointerId)
    drag.current = { startAngle: angleFrom(e), startHeading: heading }
  }
  const onMove = (e: RPointerEvent<HTMLDivElement>) => {
    if (!drag.current) return
    const delta = angleFrom(e) - drag.current.startAngle
    // Rotating the rose clockwise means the device heading decreases.
    setHeading(norm(drag.current.startHeading - delta))
  }
  const onUp = () => {
    drag.current = null
  }
  const onKey = (e: React.KeyboardEvent) => {
    const step = e.shiftKey ? 15 : 5
    if (e.key === 'ArrowRight' || e.key === 'ArrowUp') setHeading((h) => norm(h + step))
    else if (e.key === 'ArrowLeft' || e.key === 'ArrowDown') setHeading((h) => norm(h - step))
    else return
    e.preventDefault()
  }

  const ticks = Array.from({ length: 72 }, (_, i) => i * 5)

  return (
    <div className="demo demo--qibla">
      <span className="demo__label">
        <span className="badge badge--sample">{t.common.sample}</span>
        {d.label}
      </span>
      <div className={`compass ${aligned ? 'is-aligned' : ''}`}>
        <span className="compass__pointer" aria-hidden="true" />
        <div
          ref={dialRef}
          className="compass__dial"
          role="slider"
          tabIndex={0}
          aria-label={d.dial}
          aria-valuemin={0}
          aria-valuemax={359}
          aria-valuenow={heading}
          aria-valuetext={`${fmt(d.heading, { deg: heading })}${aligned ? ` — ${d.aligned}` : ''}`}
          onPointerDown={onDown}
          onPointerMove={onMove}
          onPointerUp={onUp}
          onPointerCancel={onUp}
          onKeyDown={onKey}
        >
          <svg viewBox="-100 -100 200 200" style={{ transform: `rotate(${-heading}deg)` }} aria-hidden="true">
            <circle r="96" className="compass__ring" />
            <circle r="78" className="compass__inner" />
            {ticks.map((a) => (
              <line
                key={a}
                x1="0"
                y1={a % 30 === 0 ? -92 : -94}
                x2="0"
                y2={a % 30 === 0 ? -84 : -89}
                transform={`rotate(${a})`}
                className={a % 90 === 0 ? 'tick tick--major' : 'tick'}
              />
            ))}
            {(['N', 'E', 'S', 'W'] as const).map((c, i) => (
              <text key={c} transform={`rotate(${i * 90}) translate(0 -66) rotate(${-i * 90})`} className={`compass__card ${c === 'N' ? 'is-n' : ''}`}>
                {c}
              </text>
            ))}
            <g transform={`rotate(${EXAMPLE_BEARING}) translate(0 -66)`}>
              <g className="compass__kaaba" transform={`rotate(${-EXAMPLE_BEARING})`}>
                <rect x="-9" y="-9" width="18" height="18" rx="2.5" />
                <path d="M-9 -3.5h18" />
              </g>
            </g>
            <path d="M0 -40 L7 0 L0 6 L-7 0Z" className="compass__needle" transform={`rotate(${EXAMPLE_BEARING})`} />
            <circle r="5" className="compass__hub" />
          </svg>
        </div>
      </div>
      <div className="compass__controls">
        <button type="button" className="icon-btn" onClick={() => setHeading((h) => norm(h - 15))} aria-label={d.rotateLeft}>
          <ChevronLeft />
        </button>
        <output className="compass__readout" aria-live="polite">
          {aligned ? d.aligned : fmt(d.heading, { deg: heading })}
        </output>
        <button type="button" className="icon-btn" onClick={() => setHeading((h) => norm(h + 15))} aria-label={d.rotateRight}>
          <ChevronRight />
        </button>
      </div>
      <p className="demo__hint">{d.instructions}</p>
      <p className="demo__disclaimer" role="note">
        {d.disclaimer}
      </p>
    </div>
  )
}

/* ---------------- Tasbih ---------------- */
const TARGET = 33
export function TasbihDemo() {
  const { t } = useI18n()
  const d = t.demos.tasbih
  const [count, setCount] = useState(0)
  const [pulse, setPulse] = useState(0)
  const done = count >= TARGET
  const beads = 11
  const activeBead = count % beads

  const tap = () => {
    if (done) return
    setCount((c) => c + 1)
    setPulse((p) => p + 1)
    if ('vibrate' in navigator) {
      try {
        navigator.vibrate?.(8)
      } catch {
        /* ignore */
      }
    }
  }

  return (
    <div className="demo demo--tasbih">
      <span className="demo__label">
        <span className="badge badge--sample">{t.common.sample}</span>
        {d.label}
      </span>
      <div className="tasbih">
        <div className="tasbih__beads" aria-hidden="true">
          {Array.from({ length: beads }, (_, i) => (
            <img
              key={i}
              src={i === activeBead && count > 0 ? '/assets/app/bead_active.webp' : '/assets/app/bead.webp'}
              alt=""
              width="40"
              height="40"
              className={i === activeBead && count > 0 ? 'is-active' : ''}
              style={{ ['--i' as string]: i }}
            />
          ))}
        </div>
        <button type="button" className={`tasbih__btn ${done ? 'is-done' : ''}`} onClick={tap} aria-describedby="tasbih-status">
          <span className="tasbih__ripple" key={pulse} aria-hidden="true" />
          <span className="tasbih__count">{count}</span>
          <span className="tasbih__tap">{done ? d.done : d.tap}</span>
        </button>
        <p className="tasbih__target">{fmt(d.target, { target: TARGET, left: Math.max(0, TARGET - count) })}</p>
        <span id="tasbih-status" className="sr-only" aria-live="polite">
          {fmt(d.live, { count, target: TARGET })}
        </span>
        <button type="button" className="btn btn--soft btn--sm" onClick={() => setCount(0)} disabled={count === 0}>
          <ResetIcon className="btn__icon" />
          {d.reset}
        </button>
      </div>
      <p className="demo__hint">{d.note}</p>
    </div>
  )
}

/* ---------------- Du'a & Dhikr ---------------- */
const DUA_ICONS = ['/assets/icons/doazikir_3d.webp', '/assets/icons/sunnah_3d.webp', '/assets/icons/doazikir_3d.webp', '/assets/icons/asmaulhusna_3d.webp']
export function DuaDemo() {
  const { t } = useI18n()
  const d = t.demos.dua
  return (
    <div className="demo demo--dua">
      <span className="demo__label">
        <span className="badge badge--sample">{t.common.sample}</span>
        {d.label}
      </span>
      <ul className="dua-list">
        {d.items.map((it, i) => (
          <li key={it.title} style={{ ['--i' as string]: i }}>
            <img src={DUA_ICONS[i]} alt="" width="56" height="60" loading="lazy" />
            <span>
              <strong>{it.title}</strong>
              <em>{it.meta}</em>
            </span>
            <ChevronRight className="dua-list__chev" />
          </li>
        ))}
      </ul>
    </div>
  )
}

/* ---------------- Worship tracker ---------------- */
export function TrackerDemo() {
  const { t } = useI18n()
  const d = t.demos.tracker
  const keys = ['prayer', 'dhikr', 'sunnah'] as const
  const [done, setDone] = useState<Record<(typeof keys)[number], boolean>>({ prayer: true, dhikr: false, sunnah: false })
  const total = keys.length
  const n = keys.filter((k) => done[k]).length
  return (
    <div className="demo demo--tracker">
      <span className="demo__label">
        <span className="badge badge--sample">{t.common.sample}</span>
        {d.label}
      </span>
      <div className="tracker">
        <strong className="tracker__title">{t.screen.journeyTitle}</strong>
        <span className="tracker__sub">{t.screen.journeySub}</span>
        <div className="tracker__rings">
          {keys.map((k) => (
            <button
              key={k}
              type="button"
              className={`tracker__ring ${done[k] ? 'is-done' : ''}`}
              aria-pressed={done[k]}
              onClick={() => setDone((s) => ({ ...s, [k]: !s[k] }))}
            >
              <span className="tracker__circle" aria-hidden="true">
                <svg viewBox="0 0 44 44">
                  <circle cx="22" cy="22" r="19" className="tracker__track" />
                  <circle cx="22" cy="22" r="19" className="tracker__fill" />
                </svg>
                <CheckIcon className="tracker__check" />
              </span>
              <span className="tracker__name">{t.screen.journey[k]}</span>
              <span className="sr-only">: {done[k] ? d.done : d.notDone}</span>
            </button>
          ))}
        </div>
        <div className="tracker__bar" aria-hidden="true">
          <span style={{ width: `${(n / total) * 100}%` }} />
        </div>
        <p className="tracker__progress" aria-live="polite">
          {fmt(d.progress, { done: n, total })}
        </p>
      </div>
    </div>
  )
}

/* ---------------- Widget (based on res/layout/prayer_widget_medium.xml) ---------------- */
export function WidgetDemo() {
  const { t } = useI18n()
  const d = t.demos.widget
  const entries = sampleSource.getSchedule().entries.filter((e) => e.key !== 'sunrise')
  const [i, setI] = useState(1)
  useEffect(() => {
    // Highlight cycles only as a visual preview; not a clock.
    const root = document.documentElement
    if (root.dataset.motion !== 'on') return
    const id = window.setInterval(() => {
      if (document.documentElement.dataset.motion === 'on') setI((v) => (v + 1) % entries.length)
    }, 3200)
    return () => window.clearInterval(id)
  }, [entries.length])
  const next = entries[i]
  return (
    <div className="demo demo--widget">
      <span className="demo__label">
        <span className="badge badge--sample">{t.common.sample}</span>
        {d.label}
      </span>
      <div className="widget-wall">
        <div className="widget widget--medium">
          <span className="widget__badge">
            <MosqueIcon />
          </span>
          <span className="widget__text" key={next.key}>
            <span className="widget__title">{t.prayers[next.key]}</span>
            <span className="widget__time">{next.time}</span>
            <span className="widget__sub">{d.nextLabel}</span>
          </span>
        </div>
        <div className="widget widget--times">
          {entries.map((e, k) => (
            <span key={e.key} className={k === i ? 'is-next' : ''}>
              <em>{t.prayers[e.key]}</em>
              <b>{e.time}</b>
            </span>
          ))}
        </div>
      </div>
      <p className="demo__hint">{t.features.widgetNote}</p>
    </div>
  )
}
