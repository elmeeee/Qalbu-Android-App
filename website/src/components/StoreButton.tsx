import { useI18n } from '../i18n'
import { playUrl } from '../content/links'
import { GooglePlayIcon } from './Icons'

export function StoreButton({ variant = 'dark', className = '' }: { variant?: 'dark' | 'light'; className?: string }) {
  const { t, locale } = useI18n()
  return (
    <a
      className={`store-btn store-btn--${variant} ${className}`}
      href={playUrl(locale)}
      target="_blank"
      rel="noopener"
      aria-label={`${t.store.full} ${t.common.newTab}`}
    >
      <GooglePlayIcon className="store-btn__icon" />
      <span className="store-btn__text">
        <span className="store-btn__eyebrow">{t.store.eyebrow}</span>
        <span className="store-btn__name">{t.store.name}</span>
      </span>
    </a>
  )
}
