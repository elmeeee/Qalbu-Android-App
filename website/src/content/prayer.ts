/**
 * Prayer-time data layer.
 *
 * The first website release ONLY shows sample values. The `PrayerScheduleSource`
 * interface exists so a live source (e.g. calculation from user-provided
 * coordinates) can be added later without touching the UI components.
 * Do not request geolocation or schedule notifications here.
 */
export type PrayerKey = 'fajr' | 'sunrise' | 'dhuhr' | 'asr' | 'maghrib' | 'isha'
export type ObligatoryPrayer = Exclude<PrayerKey, 'sunrise'>

export interface PrayerEntry {
  key: PrayerKey
  /** HH:mm, 24h */
  time: string
}

export interface PrayerSchedule {
  /** 'sample' must always be labelled as an example in the UI. */
  kind: 'sample' | 'live'
  entries: PrayerEntry[]
}

export interface PrayerScheduleSource {
  getSchedule(): PrayerSchedule
}

export const PRAYER_ORDER: PrayerKey[] = ['fajr', 'sunrise', 'dhuhr', 'asr', 'maghrib', 'isha']
export const OBLIGATORY: ObligatoryPrayer[] = ['fajr', 'dhuhr', 'asr', 'maghrib', 'isha']

/** Example values only — not tied to any location or date. */
export const sampleSource: PrayerScheduleSource = {
  getSchedule: () => ({
    kind: 'sample',
    entries: [
      { key: 'fajr', time: '04:29' },
      { key: 'sunrise', time: '05:46' },
      { key: 'dhuhr', time: '11:50' },
      { key: 'asr', time: '15:04' },
      { key: 'maghrib', time: '17:53' },
      { key: 'isha', time: '19:01' },
    ],
  }),
}

/** Static example countdowns shown next to each selected prayer (illustration only). */
export const SAMPLE_COUNTDOWN: Record<ObligatoryPrayer, string> = {
  fajr: '00:42:10',
  dhuhr: '01:15:36',
  asr: '01:32:17',
  maghrib: '00:48:05',
  isha: '00:27:44',
}

/** Real app artwork (res/drawable-nodpi/bg_intent_*.webp) per prayer. */
export const PRAYER_ART: Record<ObligatoryPrayer, string> = {
  fajr: '/assets/app/moment-fajr.webp',
  dhuhr: '/assets/app/moment-dhur.webp',
  asr: '/assets/app/moment-asr.webp',
  maghrib: '/assets/app/moment-maghrib.webp',
  isha: '/assets/app/moment-isha.webp',
}
