import type { SVGProps } from 'react'

type P = SVGProps<SVGSVGElement>
const base = { fill: 'none', stroke: 'currentColor', strokeWidth: 1.8, strokeLinecap: 'round', strokeLinejoin: 'round' } as const

export const GooglePlayIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...p}>
    <path fill="#34A853" d="M3.6 2.3c-.3.3-.4.7-.4 1.2v17c0 .5.1.9.4 1.2l9.5-9.7-9.5-9.7z" />
    <path fill="#FBBC04" d="M16.3 15.2 13.1 12l3.2-3.2 3.8 2.2c1.1.6 1.1 1.6 0 2.2l-3.8 2z" />
    <path fill="#EA4335" d="M16.3 15.2 13.1 12l-9.5 9.7c.4.4 1 .4 1.7 0l11-6.5z" />
    <path fill="#4285F4" d="M16.3 8.8 5.3 2.3c-.7-.4-1.3-.4-1.7 0l9.5 9.7 3.2-3.2z" />
  </svg>
)

export const ArrowDown = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M12 5v14M6 13l6 6 6-6" />
  </svg>
)
export const ArrowUpRight = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M7 17 17 7M8 7h9v9" />
  </svg>
)
export const MenuIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M4 7h16M4 12h16M4 17h10" />
  </svg>
)
export const CloseIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M6 6l12 12M18 6 6 18" />
  </svg>
)
export const GlobeIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <circle cx="12" cy="12" r="9" />
    <path d="M3 12h18M12 3c2.5 2.6 3.8 5.6 3.8 9s-1.3 6.4-3.8 9c-2.5-2.6-3.8-5.6-3.8-9S9.5 5.6 12 3z" />
  </svg>
)
export const PauseIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M9 6v12M15 6v12" />
  </svg>
)
export const PlayIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M8 5.5v13l10-6.5-10-6.5z" />
  </svg>
)
export const CheckIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="m5 12.5 4.5 4.5L19 7.5" />
  </svg>
)
export const ClockIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <circle cx="12" cy="12" r="9" />
    <path d="M12 7v5l3 2" />
  </svg>
)
export const PinIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M12 21s-6.5-5.6-6.5-11A6.5 6.5 0 0 1 18.5 10c0 5.4-6.5 11-6.5 11z" />
    <circle cx="12" cy="10" r="2.3" />
  </svg>
)
export const BellIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M6 16V11a6 6 0 1 1 12 0v5l1.5 2h-15L6 16zM10 20.5a2 2 0 0 0 4 0" />
  </svg>
)
export const ShieldIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M12 3 4.5 6v5.5c0 4.6 3.2 8.3 7.5 9.5 4.3-1.2 7.5-4.9 7.5-9.5V6L12 3z" />
    <path d="m9 12 2.2 2.2L15.5 10" />
  </svg>
)
export const LeafIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M5 19c0-8 5-13 14-14-1 9-6 14-14 14zM5 19l7-7" />
  </svg>
)
export const HeartIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M12 20s-7.5-4.6-7.5-10A4.3 4.3 0 0 1 12 7.3 4.3 4.3 0 0 1 19.5 10c0 5.4-7.5 10-7.5 10z" />
  </svg>
)
export const MosqueIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M4 20h16M6 20v-6h12v6M12 4c-2.8 1.8-4.5 3.6-4.5 6.2h9C16.5 7.6 14.8 5.8 12 4zM12 2.5V4M10.5 20v-3a1.5 1.5 0 0 1 3 0v3" />
  </svg>
)
export const SparkIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...p}>
    <path fill="currentColor" d="M12 2c.6 4.6 2.4 6.4 7 7-4.6.6-6.4 2.4-7 7-.6-4.6-2.4-6.4-7-7 4.6-.6 6.4-2.4 7-7z" />
  </svg>
)
export const MailIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <rect x="3.5" y="5.5" width="17" height="13" rx="2.5" />
    <path d="m4.5 7 7.5 6 7.5-6" />
  </svg>
)
export const ChevronLeft = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="m14.5 6-6 6 6 6" />
  </svg>
)
export const ChevronRight = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="m9.5 6 6 6-6 6" />
  </svg>
)
export const ResetIcon = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M4.5 12a7.5 7.5 0 1 0 2.2-5.3M4.5 4.5v4h4" />
  </svg>
)

/* Platform marks: generic device glyphs (no third-party logos). */
export const PhoneGlyph = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <rect x="7" y="2.8" width="10" height="18.4" rx="2.6" />
    <path d="M11 18.2h2" />
  </svg>
)
export const LaptopGlyph = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <rect x="5" y="5" width="14" height="10" rx="1.6" />
    <path d="M3 18.5h18" />
  </svg>
)
export const DesktopGlyph = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <rect x="3" y="4" width="18" height="12" rx="1.8" />
    <path d="M9 20h6M12 16v4" />
  </svg>
)
export const StoreGlyph = (p: P) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" {...base} {...p}>
    <path d="M5 8h14l-1 12H6L5 8zM9 8V6.5a3 3 0 0 1 6 0V8" />
  </svg>
)
