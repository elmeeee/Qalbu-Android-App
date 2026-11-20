import { useEffect, useRef, useState } from 'react'
import { useI18n } from '../i18n'
import { useInView, useMotion } from '../motion/MotionContext'
import { BellIcon, CheckIcon, SparkIcon } from './Icons'

/**
 * Kīmi uses only authentic app assets: the idle/thinking animations
 * (res/drawable/kimi_*_preview.gif, re-encoded as animated WebP) and mascot
 * stills. Motion is limited to gentle CSS transforms; the character itself is
 * never redrawn. When motion is paused/reduced we swap to still frames.
 */
export function KimiSection() {
  const { t } = useI18n()
  const { animate } = useMotion()
  const ref = useRef<HTMLElement>(null)
  const inView = useInView(ref, '80px')
  const play = animate && inView
  const [greeting, setGreeting] = useState(false)
  const [topic, setTopic] = useState<number | null>(null)
  const [thinking, setThinking] = useState(false)

  useEffect(() => {
    if (!greeting) return
    const id = window.setTimeout(() => setGreeting(false), 3200)
    return () => window.clearTimeout(id)
  }, [greeting])

  useEffect(() => {
    if (topic === null) return
    setThinking(true)
    const id = window.setTimeout(() => setThinking(false), 1800)
    return () => window.clearTimeout(id)
  }, [topic])

  const c = t.kimi.chat

  return (
    <section ref={ref} className="section kimi" id="kimi" aria-labelledby="kimi-title">
      <div className="kimi__stars" aria-hidden="true" />
      <div className="container kimi__grid">
        <div className="kimi__hero" data-reveal>
          <div className={`kimi__stage ${greeting ? 'is-greeting' : ''}`}>
            <span className="kimi__glow" aria-hidden="true" />
            <button type="button" className="kimi__mascot" onClick={() => setGreeting(true)} aria-describedby="kimi-bubble">
              <img
                src={play ? '/assets/kimi/kimi-idle.webp' : '/assets/kimi/kimi-idle-still.webp'}
                alt={t.kimi.imageAlt}
                width="360"
                height="360"
                loading="lazy"
                decoding="async"
              />
              <span className="sr-only">{t.kimi.greet}</span>
            </button>
            <p id="kimi-bubble" className="kimi__bubble" role="status" aria-live="polite">
              {greeting ? t.kimi.greeting : ''}
            </p>
          </div>
          <button type="button" className="btn btn--glass btn--sm kimi__greet" onClick={() => setGreeting(true)}>
            <SparkIcon className="btn__icon" />
            {t.kimi.greet}
          </button>
        </div>

        <div className="kimi__copy">
          <div data-reveal>
            <p className="eyebrow eyebrow--light">
              <span className="eyebrow__dot" aria-hidden="true" />
              {t.kimi.eyebrow}
            </p>
            <h2 id="kimi-title" className="h2 h2--light">
              {t.kimi.title}
            </h2>
            <p className="lead lead--light">{t.kimi.lead}</p>
          </div>

          <div className="kimi__cards">
            <article className="kcard" data-reveal style={{ ['--d' as string]: 1 }}>
              <span className="kcard__icon" aria-hidden="true">
                <BellIcon />
              </span>
              <div>
                <span className="badge badge--available badge--dark">
                  <CheckIcon />
                  {t.common.availableAndroid}
                </span>
                <h3>{t.kimi.cards.reminders.title}</h3>
                <p>{t.kimi.cards.reminders.body}</p>
              </div>
            </article>
            <article className="kcard" data-reveal style={{ ['--d' as string]: 2 }}>
              <span className="kcard__icon" aria-hidden="true">
                <SparkIcon />
              </span>
              <div>
                <span className="badge badge--available badge--dark">
                  <CheckIcon />
                  {t.common.availableAndroid}
                </span>
                <h3>{t.kimi.cards.ask.title}</h3>
                <p>{t.kimi.cards.ask.body}</p>
              </div>
            </article>
          </div>

          <div className="chat" data-reveal style={{ ['--d' as string]: 3 }} role="group" aria-label={c.label}>
            <div className="chat__head">
              <img src="/assets/kimi/kimi-thinking-still.webp" alt="" width="40" height="40" />
              <div>
                <strong>{t.kimi.cards.ask.title}</strong>
                <span className="badge badge--sample badge--tiny">{t.common.sample}</span>
              </div>
            </div>
            <div className="chat__body">
              <p className="chat__msg chat__msg--kimi">{c.welcome}</p>
              {topic !== null && (
                <p className="chat__msg chat__msg--me" key={`q${topic}`}>
                  {c.topics[topic]}
                </p>
              )}
              {topic !== null && (
                <div className="chat__msg chat__msg--kimi chat__msg--state" key={`a${topic}`} aria-live="polite">
                  {thinking ? (
                    <>
                      <img
                        src={animate ? '/assets/kimi/kimi-thinking.webp' : '/assets/kimi/kimi-thinking-still.webp'}
                        alt=""
                        width="36"
                        height="36"
                      />
                      <span>{c.thinking}</span>
                    </>
                  ) : (
                    <span>{c.inApp}</span>
                  )}
                </div>
              )}
            </div>
            <div className="chat__topics" role="group" aria-label={c.topicsLabel}>
              {c.topics.map((tp, i) => (
                <button key={tp} type="button" className="chip" aria-pressed={topic === i} onClick={() => setTopic(i)}>
                  {tp}
                </button>
              ))}
            </div>
            <p className="chat__disclaimer" role="note">
              {c.disclaimer}
            </p>
          </div>
        </div>
      </div>

      <div className="container">
        <h3 className="kimi__moments-title" data-reveal>
          {t.kimi.moments}
        </h3>
        <ul className="kimi__moments">
          {(
            [
              ['prayer', '/assets/kimi/prayer.webp'],
              ['lantern', '/assets/kimi/bawa_lentera.webp'],
              ['announce', '/assets/kimi/toa.webp'],
            ] as const
          ).map(([k, src], i) => (
            <li key={k} data-reveal style={{ ['--d' as string]: i }}>
              <img src={src} alt={t.kimi.momentAlts[k]} width="320" height="427" loading="lazy" decoding="async" style={{ ['--i' as string]: i }} />
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}
