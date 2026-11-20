import { useState } from 'react'
import { fmt, useI18n } from '../i18n'
import { OBLIGATORY, PRAYER_ART, SAMPLE_COUNTDOWN, sampleSource, type ObligatoryPrayer } from '../content/prayer'
import { CheckIcon } from './Icons'
import { Ornament } from './Ornament'

export function PrayerSection() {
  const { t } = useI18n()
  const schedule = sampleSource.getSchedule()
  const [selected, setSelected] = useState<ObligatoryPrayer>('asr')
  const prayerName = t.prayers[selected]
  const order = OBLIGATORY.indexOf(selected)

  return (
    <section className="section prayer" id="prayer-times" aria-labelledby="prayer-title">
      <div className="container prayer__grid">
        <div className="prayer__copy" data-reveal>
          <p className="eyebrow">
            <span className="eyebrow__dot" aria-hidden="true" />
            {t.prayer.eyebrow}
          </p>
          <h2 id="prayer-title" className="h2">
            {t.prayer.title}
          </h2>
          <p className="lead">{t.prayer.lead}</p>
          <ul className="checklist">
            {t.prayer.points.map((p) => (
              <li key={p}>
                <CheckIcon />
                {p}
              </li>
            ))}
          </ul>
          <p className="note-box" role="note">
            <span className="badge badge--sample">{t.common.sample}</span>
            {t.prayer.sampleNote}
          </p>
        </div>

        <div className="prayer__panel" data-reveal style={{ ['--d' as string]: 1 }}>
          <div className="prayer__art" aria-hidden="true">
            {OBLIGATORY.map((k) => (
              <img
                key={k}
                src={PRAYER_ART[k]}
                alt=""
                width="640"
                height="1387"
                loading="lazy"
                decoding="async"
                className={k === selected ? 'is-active' : ''}
              />
            ))}
            <span className="prayer__art-shade" />
          </div>

          <div className="prayer__card">
            <div className="prayer__card-head">
              <span className="badge badge--sample badge--onlight">{t.common.sample}</span>
              <Ornament className="prayer__orn" />
            </div>

            <div className="prayer__countdown" aria-live="polite">
              <span className="prayer__count-label">{t.prayer.countdownLabel}</span>
              <span className="prayer__count" key={selected}>
                {SAMPLE_COUNTDOWN[selected]}
              </span>
              <span className="prayer__count-until">{fmt(t.prayer.countdownUntil, { prayer: prayerName })}</span>
              <span className="prayer__count-note">{t.prayer.notLive}</span>
            </div>

            <p className="prayer__hint" id="prayer-hint">
              {t.prayer.selectHint}
            </p>
            <ul className="prayer__list" aria-label={t.prayer.scheduleLabel} aria-describedby="prayer-hint">
              {schedule.entries.map((e, i) => {
                const isObligatory = e.key !== 'sunrise'
                const key = e.key as ObligatoryPrayer
                const isSel = isObligatory && key === selected
                const isPast = isObligatory && OBLIGATORY.indexOf(key) < order
                return (
                  <li key={e.key} style={{ ['--i' as string]: i }}>
                    {isObligatory ? (
                      <button
                        type="button"
                        className={`prayer__row ${isSel ? 'is-selected' : ''} ${isPast ? 'is-past' : ''}`}
                        aria-pressed={isSel}
                        onClick={() => setSelected(key)}
                      >
                        <span className="prayer__dot" aria-hidden="true" />
                        <span className="prayer__name">{t.prayers[e.key]}</span>
                        <span className="prayer__time">{e.time}</span>
                      </button>
                    ) : (
                      <div className="prayer__row prayer__row--static">
                        <span className="prayer__dot" aria-hidden="true" />
                        <span className="prayer__name">{t.prayers[e.key]}</span>
                        <span className="prayer__time">{e.time}</span>
                      </div>
                    )}
                  </li>
                )
              })}
            </ul>
            <p className="prayer__foot">{t.prayer.useApp}</p>
          </div>
          <p className="sr-only">{fmt(t.prayer.artAlt, { prayer: prayerName })}</p>
        </div>
      </div>
    </section>
  )
}
