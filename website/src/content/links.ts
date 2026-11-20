import type { Locale } from '../i18n'

/** Verified public links only. Do not add store links for unreleased platforms. */
export const SITE_URL = 'https://saatnya-hijra.web.app/'
export const GOOGLE_PLAY_URL = 'https://play.google.com/store/apps/details?id=app.kamy.saatApp'
/** Account & data deletion page (preserved route). */
export const DELETE_ACCOUNT_PATH = '/delete-account.html'
/** Support contact as published on the existing deletion page. */
export const SUPPORT_EMAIL = 'hello@elmee.my'

/** Same policy URLs the app opens from Settings (AccountScreen.kt). */
export function privacyUrl(locale: Locale) {
  return `https://elmee.my/saat/privacy?lang=${locale}`
}
export function termsUrl(locale: Locale) {
  return `https://elmee.my/saat/terms?lang=${locale}`
}
export function playUrl(locale: Locale) {
  return `${GOOGLE_PLAY_URL}&hl=${locale === 'ms' ? 'ms' : locale}`
}
