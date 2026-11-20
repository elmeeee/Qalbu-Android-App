import { StrictMode } from 'react'
import { createRoot, hydrateRoot } from 'react-dom/client'
import '@fontsource-variable/inter'
import '@fontsource-variable/playfair-display'
import './styles/global.css'
import App from './App'
import { DEFAULT_LOCALE, detectLocale } from './i18n'

const container = document.getElementById('root')!
const locale = detectLocale()
const app = (
  <StrictMode>
    <App initialLocale={locale} />
  </StrictMode>
)

document.documentElement.classList.add('js')

// The prerendered HTML is Indonesian. Hydrate when it matches; otherwise render fresh.
if (container.firstElementChild && locale === DEFAULT_LOCALE) {
  hydrateRoot(container, app)
} else {
  container.textContent = ''
  createRoot(container).render(app)
}
