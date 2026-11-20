import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode, type RefObject } from 'react'

const MOTION_KEY = 'saat.motion'

type MotionCtx = {
  /** OS-level prefers-reduced-motion. */
  reduced: boolean
  /** Visitor pressed the pause button. */
  paused: boolean
  /** Tab is hidden. */
  hidden: boolean
  /** True when non-essential looping motion may run. */
  animate: boolean
  togglePaused: () => void
}

const Ctx = createContext<MotionCtx>({
  reduced: false,
  paused: false,
  hidden: false,
  animate: false,
  togglePaused: () => {},
})

export function MotionProvider({ children }: { children: ReactNode }) {
  const [reduced, setReduced] = useState(false)
  const [paused, setPaused] = useState(false)
  const [hidden, setHidden] = useState(false)

  useEffect(() => {
    const mq = window.matchMedia('(prefers-reduced-motion: reduce)')
    const onMq = () => setReduced(mq.matches)
    onMq()
    mq.addEventListener('change', onMq)
    try {
      setPaused(window.localStorage.getItem(MOTION_KEY) === 'paused')
    } catch {
      /* ignore */
    }
    const onVis = () => setHidden(document.visibilityState === 'hidden')
    onVis()
    document.addEventListener('visibilitychange', onVis)
    return () => {
      mq.removeEventListener('change', onMq)
      document.removeEventListener('visibilitychange', onVis)
    }
  }, [])

  const animate = !reduced && !paused && !hidden

  useEffect(() => {
    const root = document.documentElement
    root.dataset.motion = reduced ? 'reduced' : paused || hidden ? 'paused' : 'on'
  }, [reduced, paused, hidden])

  const togglePaused = useCallback(() => {
    setPaused((p) => {
      const next = !p
      try {
        window.localStorage.setItem(MOTION_KEY, next ? 'paused' : 'on')
      } catch {
        /* ignore */
      }
      return next
    })
  }, [])

  const value = useMemo(
    () => ({ reduced, paused, hidden, animate, togglePaused }),
    [reduced, paused, hidden, animate, togglePaused],
  )
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>
}

export const useMotion = () => useContext(Ctx)

/** Visibility of an element in the viewport (used to pause off-screen loops / 3D). */
export function useInView<T extends Element>(ref: RefObject<T | null>, rootMargin = '0px'): boolean {
  const [inView, setInView] = useState(false)
  useEffect(() => {
    const el = ref.current
    if (!el) return
    if (!('IntersectionObserver' in window)) {
      setInView(true)
      return
    }
    const io = new IntersectionObserver(([entry]) => setInView(entry.isIntersecting), { rootMargin })
    io.observe(el)
    return () => io.disconnect()
  }, [ref, rootMargin])
  return inView
}

/**
 * Adds `.is-in` to `[data-reveal]` elements as they enter the viewport.
 * Content is visible by default; hiding only applies under `html.js` (see CSS),
 * so nothing disappears if JavaScript fails.
 */
export function useRevealObserver(dep: unknown) {
  useEffect(() => {
    const els = Array.from(document.querySelectorAll<HTMLElement>('[data-reveal]:not(.is-in)'))
    if (!('IntersectionObserver' in window)) {
      els.forEach((el) => el.classList.add('is-in'))
      return
    }
    const io = new IntersectionObserver(
      (entries) => {
        for (const e of entries) {
          if (e.isIntersecting) {
            e.target.classList.add('is-in')
            io.unobserve(e.target)
          }
        }
      },
      { rootMargin: '0px 0px -8% 0px', threshold: 0.08 },
    )
    els.forEach((el) => io.observe(el))
    return () => io.disconnect()
  }, [dep])
}
