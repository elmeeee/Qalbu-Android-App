# Sāat — official website

Public product website for the Sāat app, served by Firebase Hosting
(project `saatnya-hijra`, https://saatnya-hijra.web.app/).

- `/` — product website (React + TypeScript + Vite, prerendered to static HTML).
- `/delete-account.html` — account & associated data deletion page (static, preserved
  from the previous site; source in `public/delete-account.html`).

The website is fully separate from the Android/iOS app sources.

## Requirements

- Node.js 20+ and npm
- Firebase CLI (`npm i -g firebase-tools`) for deployment

## Development

```bash
cd website
npm ci
npm run dev          # http://localhost:5173
```

## Build & preview

```bash
npm run build        # type-check, client build, SSR prerender -> dist/
npm run preview      # serves dist/ on http://localhost:4173
```

`npm run build` prerenders the Indonesian markup into `dist/index.html` (so the page
works without JavaScript) and fails if `dist/delete-account.html` is missing.

## Deploy to Firebase Hosting

`firebase.json` (repo root) serves `website/dist` and runs the build automatically as a
`predeploy` step.

```bash
# from the repository root
npm --prefix website ci
firebase login
firebase use saatnya-hijra
firebase hosting:channel:deploy preview --expires 7d   # optional: review on a preview URL
firebase deploy --only hosting                          # production
```

Using `--only hosting` avoids touching Firestore rules.

## Structure

```
src/
  i18n/locales/{id,en,ms}.ts   all public copy (id.ts defines the key shape)
  i18n/index.tsx               detection (?lang= > saved > browser > id), provider
  content/links.ts             verified external links (Google Play, policy, support)
  content/prayer.ts            sample prayer data + PrayerScheduleSource interface
  motion/MotionContext.tsx     reduced-motion, pause toggle, tab visibility, reveal
  components/                  sections, 3D hero (HeroScene, lazy-loaded), demos
public/
  assets/                      optimized copies of real app artwork (res/drawable-*)
  delete-account.html          account deletion page
scripts/prerender.mjs          injects SSR markup into dist/index.html
```

### Languages

Indonesian (default/fallback), English, Malay. The choice is stored in `localStorage`
(`saat.lang`), shared with the deletion page, and can be forced with `?lang=id|en|ms`.
All locales must keep identical keys — TypeScript enforces this.

### Prayer times

The site only shows **sample** values, always labelled “Contoh tampilan / Example
preview / Contoh paparan”. It never requests location or schedules notifications.
To add live times later, implement `PrayerScheduleSource` in `src/content/prayer.ts`.

### 3D and motion

The WebGL hero (three.js + React Three Fiber) is loaded lazily on screens ≥ 768 px
when WebGL is available. Small screens, `prefers-reduced-motion`, Data Saver, and
WebGL failures use the static CSS phone. Loops pause off-screen, when the tab is
hidden, or when the visitor presses “Pause animations”.
