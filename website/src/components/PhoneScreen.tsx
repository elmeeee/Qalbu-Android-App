import type { Messages } from '../i18n/locales/id'
import { sampleSource, type PrayerKey } from '../content/prayer'
import { MosqueIcon, PinIcon } from './Icons'

export type Moment = 'morning' | 'afternoon' | 'night'

export const MOMENTS: Moment[] = ['morning', 'afternoon', 'night']

/** Real app header artwork (res/drawable-nodpi/day|sunset|night.webp). */
const MOMENT_ART: Record<Moment, string> = {
  morning: '/assets/app/day.webp',
  afternoon: '/assets/app/sunset.webp',
  night: '/assets/app/night.webp',
}
const MOMENT_NEXT: Record<Moment, { key: PrayerKey; countdown: string; progress: number }> = {
  morning: { key: 'dhuhr', countdown: '01:15:36', progress: 0.62 },
  afternoon: { key: 'maghrib', countdown: '00:48:05', progress: 0.74 },
  night: { key: 'fajr', countdown: '06:12:40', progress: 0.3 },
}

/**
 * Recreation of the Sāat Home screen (next-prayer card + Today’s Journey),
 * based on the app's published Google Play screenshot. Takes `t` as a prop
 * (no hooks) because it is also rendered inside the WebGL scene's DOM portal.
 * All values are samples and the screen is labelled as an illustration.
 */
export function PhoneScreen({ t, moment }: { t: Messages; moment: Moment }) {
  const schedule = sampleSource.getSchedule()
  const next = MOMENT_NEXT[moment]
  const nextIdx = schedule.entries.findIndex((e) => e.key === next.key)
  const R = 34
  const C = Math.PI * R

  return (
    <div className={`screen screen--${moment}`} aria-hidden="true">
      <div className="screen__status">
        <span>9:41</span>
        <span className="screen__status-icons">
          <i />
          <i />
          <b />
        </span>
      </div>

      <div className="screen__header">
        {(Object.keys(MOMENT_ART) as Moment[]).map((m) => (
          <img
            key={m}
            className={`screen__art ${m === moment ? 'is-active' : ''}`}
            src={MOMENT_ART[m]}
            alt=""
            width="1200"
            height="730"
            loading={m === 'morning' ? 'eager' : 'lazy'}
            decoding="async"
          />
        ))}
        <div className="screen__greet">
          <strong>{t.screen.greeting}</strong>
          <span>{t.screen.date}</span>
        </div>
        <span className="screen__loc">
          <PinIcon />
          {t.screen.location}
        </span>
      </div>

      <div className="screen__card screen__next">
        <div className="screen__next-top">
          <div>
            <span className="screen__muted">{t.screen.nextPrayer}</span>
            <strong className="screen__prayer">{t.prayers[next.key]}</strong>
          </div>
          <div className="screen__arc">
            <svg viewBox="0 0 80 46">
              <path d="M6 42a34 34 0 0 1 68 0" className="screen__arc-bg" />
              <path
                d="M6 42a34 34 0 0 1 68 0"
                className="screen__arc-fg"
                style={{ strokeDasharray: C, strokeDashoffset: C * (1 - next.progress) }}
              />
            </svg>
            <span className="screen__count">{next.countdown}</span>
            <span className="screen__until">{t.screen.untilPrayer}</span>
          </div>
        </div>
        <div className="screen__timeline">
          <span className="screen__track" />
          <span className="screen__track-fill" style={{ width: `${(nextIdx / (schedule.entries.length - 1)) * 100}%` }} />
          {schedule.entries.map((e, i) => (
            <span key={e.key} className={`screen__dot ${i < nextIdx ? 'is-past' : ''} ${i === nextIdx ? 'is-next' : ''}`} />
          ))}
        </div>
        <div className="screen__times">
          {schedule.entries.map((e) => (
            <span key={e.key} className={e.key === next.key ? 'is-next' : ''}>
              <em>{t.prayers[e.key]}</em>
              <b>{e.time}</b>
            </span>
          ))}
        </div>
      </div>

      <div className="screen__card screen__journey">
        <strong>{t.screen.journeyTitle}</strong>
        <span className="screen__muted">{t.screen.journeySub}</span>
        <div className="screen__rings">
          {(['prayer', 'dhikr', 'sunnah'] as const).map((k, i) => (
            <span key={k} className={i === 0 ? 'is-done' : ''}>
              <i />
              {t.screen.journey[k]}
            </span>
          ))}
        </div>
      </div>

      <div className="screen__card screen__tip">
        <img src="/assets/kimi/kimi-idle-still.webp" alt="" width="360" height="360" />
        <span>
          <strong>{t.demos.reminders.dhuhaTitle}</strong>
          <em>{t.demos.reminders.dhuhaBody}</em>
        </span>
      </div>

      <div className="screen__nav">
        <span className="is-active">
          <MosqueIcon />
          {t.screen.tabs.home}
        </span>
        <span>
          <i className="screen__nav-ic screen__nav-ic--worship" />
          {t.screen.tabs.worship}
        </span>
        <span>
          <i className="screen__nav-ic screen__nav-ic--more" />
          {t.screen.tabs.more}
        </span>
      </div>
      <span className="screen__sample">{t.common.sample}</span>
    </div>
  )
}
