import { Component, lazy, Suspense, useEffect, useRef, useState, type ReactNode } from 'react'
import { useI18n } from '../i18n'
import { useInView, useMotion } from '../motion/MotionContext'
import { PhoneScreen, MOMENTS, type Moment } from './PhoneScreen'
import { StoreButton } from './StoreButton'
import { ArrowDown } from './Icons'
import { Star8 } from './Ornament'

const HeroScene = lazy(() => import('./HeroScene'))

class SceneBoundary extends Component<{ onError: () => void; children: ReactNode }, { failed: boolean }> {
  state = { failed: false }
  static getDerivedStateFromError() {
    return { failed: true }
  }
  componentDidCatch() {
    this.props.onError()
  }
  render() {
    return this.state.failed ? null : this.props.children
  }
}

function webglAvailable(): boolean {
  try {
    const c = document.createElement('canvas')
    return !!(c.getContext('webgl2') || c.getContext('webgl'))
  } catch {
    return false
  }
}

type SceneState = 'pending' | 'loading' | 'ready' | 'static' | 'unsupported'

export function Hero() {
  const { t } = useI18n()
  const { animate, reduced } = useMotion()
  const stageRef = useRef<HTMLDivElement>(null)
  const inView = useInView(stageRef, '100px')
  const [moment, setMoment] = useState<Moment>('morning')
  const [scene, setScene] = useState<SceneState>('pending')

  // Decide whether to load the WebGL scene (after first paint, never blocking content).
  useEffect(() => {
    if (reduced) {
      setScene('static')
      return
    }
    const conn = (navigator as Navigator & { connection?: { saveData?: boolean } }).connection
    // Small screens keep the lighter CSS 3D phone (saves ~250 KB and battery).
    if (conn?.saveData || !window.matchMedia('(min-width: 768px)').matches) {
      setScene('static')
      return
    }
    if (!webglAvailable()) {
      setScene('unsupported')
      return
    }
    const id = window.setTimeout(() => setScene((s) => (s === 'pending' ? 'loading' : s)), 150)
    return () => window.clearTimeout(id)
  }, [reduced])

  // Gentle day-to-night storytelling on the phone screen.
  useEffect(() => {
    if (!animate || !inView) return
    const id = window.setInterval(() => {
      setMoment((m) => MOMENTS[(MOMENTS.indexOf(m) + 1) % MOMENTS.length])
    }, 6500)
    return () => window.clearInterval(id)
  }, [animate, inView])

  const showScene = scene === 'loading' || scene === 'ready'

  return (
    <section className={`hero hero--${moment}`} id="top" aria-labelledby="hero-title">
      <div className="hero__sky" aria-hidden="true">
        <span className="hero__orb" />
        <Star8 className="hero__spark hero__spark--1" />
        <Star8 className="hero__spark hero__spark--2" />
        <Star8 className="hero__spark hero__spark--3" />
      </div>

      <div className="container hero__grid">
        <div className="hero__copy">
          <p className="eyebrow hero__in" style={{ ['--d' as string]: 0 }}>
            <span className="eyebrow__dot" aria-hidden="true" />
            {t.hero.eyebrow}
          </p>
          <h1 id="hero-title" className="hero__title">
            <span className="hero__line hero__in" style={{ ['--d' as string]: 1 }}>
              {t.hero.titleA}
            </span>{' '}
            <span className="hero__line hero__line--accent hero__in" style={{ ['--d' as string]: 2 }}>
              {t.hero.titleB}
            </span>
          </h1>
          <p className="hero__lead hero__in" style={{ ['--d' as string]: 3 }}>
            {t.hero.lead}
          </p>
          <div className="hero__ctas hero__in" style={{ ['--d' as string]: 4 }}>
            <StoreButton />
            <a className="btn btn--ghost" href="#features">
              {t.hero.explore}
              <ArrowDown className="btn__icon btn__icon--end" />
            </a>
          </div>
          <p className="hero__note hero__in" style={{ ['--d' as string]: 5 }}>
            {t.hero.platformNote}
          </p>
        </div>

        <figure className="hero__visual hero__in" style={{ ['--d' as string]: 2 }} aria-label={t.hero.phoneLabel}>
          <div className="hero__arch" aria-hidden="true" />
          <div ref={stageRef} className={`hero__stage ${scene === 'ready' ? 'is-3d' : ''}`}>
            <div className="phone-css" aria-hidden={scene === 'ready'}>
              <div className="phone-css__body">
                <div className="phone-css__glass">
                  <PhoneScreen t={t} moment={moment} />
                </div>
                <span className="phone-css__cam" />
              </div>
            </div>
            {showScene && (
              <SceneBoundary onError={() => setScene('unsupported')}>
                <Suspense fallback={null}>
                  <HeroScene
                    t={t}
                    moment={moment}
                    active={animate && inView}
                    onReady={() => setScene('ready')}
                    onFail={() => setScene('unsupported')}
                  />
                </Suspense>
              </SceneBoundary>
            )}
            <span className="hero__shadow" aria-hidden="true" />
          </div>

          <figcaption className="hero__caption">
            <span className="badge badge--sample">{t.common.sample}</span>
            <span>{t.hero.illustrationNote}</span>
            {scene === 'unsupported' && <span className="hero__fallback-note">{t.hero.fallback3d}</span>}
          </figcaption>

          <div className="moment-switch" role="group" aria-label={t.hero.momentLabel}>
            {MOMENTS.map((m) => (
              <button
                key={m}
                type="button"
                className="moment-switch__btn"
                aria-pressed={m === moment}
                onClick={() => setMoment(m)}
              >
                <span className={`moment-switch__ic moment-switch__ic--${m}`} aria-hidden="true" />
                {t.hero.moments[m]}
              </button>
            ))}
          </div>
        </figure>
      </div>
    </section>
  )
}
