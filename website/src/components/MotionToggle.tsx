import { useI18n } from '../i18n'
import { useMotion } from '../motion/MotionContext'
import { PauseIcon, PlayIcon } from './Icons'

export function MotionToggle({ className = '' }: { className?: string }) {
  const { t } = useI18n()
  const { paused, reduced, togglePaused } = useMotion()
  if (reduced) return null
  const label = paused ? t.motion.play : t.motion.pause
  return (
    <button
      type="button"
      className={`motion-toggle ${className}`}
      onClick={togglePaused}
      aria-pressed={paused}
      aria-label={t.motion.pause}
      title={label}
    >
      {paused ? <PlayIcon /> : <PauseIcon />}
      <span className="motion-toggle__text">{label}</span>
    </button>
  )
}
