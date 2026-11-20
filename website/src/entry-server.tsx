import { renderToString } from 'react-dom/server'
import App from './App'
import { DEFAULT_LOCALE } from './i18n'

export function render(): string {
  return renderToString(<App initialLocale={DEFAULT_LOCALE} />)
}
