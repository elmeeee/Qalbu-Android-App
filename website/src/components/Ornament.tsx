/** Gold divider derived from the Sāat logo lockup (line · eight-point star · line). */
export function Ornament({ className = '' }: { className?: string }) {
  return (
    <svg className={`ornament ${className}`} viewBox="0 0 160 16" aria-hidden="true" focusable="false">
      <path d="M2 8h58M100 8h58" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" />
      <path d="M2 8l3-2v4zM158 8l-3-2v4z" fill="currentColor" />
      <g transform="translate(80 8)">
        <rect x="-6" y="-6" width="12" height="12" rx="1.5" fill="currentColor" />
        <rect x="-6" y="-6" width="12" height="12" rx="1.5" fill="currentColor" transform="rotate(45)" />
        <circle r="2.6" fill="var(--emerald-700)" />
      </g>
    </svg>
  )
}

/** Eight-point star used as a small accent. */
export function Star8({ className = '' }: { className?: string }) {
  return (
    <svg className={className} viewBox="-10 -10 20 20" aria-hidden="true" focusable="false">
      <rect x="-6" y="-6" width="12" height="12" rx="1.2" fill="currentColor" />
      <rect x="-6" y="-6" width="12" height="12" rx="1.2" fill="currentColor" transform="rotate(45)" />
    </svg>
  )
}
