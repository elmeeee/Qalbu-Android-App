import { useI18n } from '../i18n'
import { DELETE_ACCOUNT_PATH, SUPPORT_EMAIL, playUrl, privacyUrl, termsUrl } from '../content/links'
import { StoreButton } from './StoreButton'
import { ArrowUpRight, CheckIcon, ClockIcon, DesktopGlyph, HeartIcon, LaptopGlyph, LeafIcon, MailIcon, PhoneGlyph, ShieldIcon, StoreGlyph } from './Icons'
import { Ornament } from './Ornament'

/* ---------------- Platforms ---------------- */
type PlatformKey = 'android' | 'ios' | 'macos' | 'huawei' | 'windows'
const PLATFORMS: { key: PlatformKey; device: 'phone' | 'laptop' | 'desktop' | 'store'; available: boolean }[] = [
  { key: 'android', device: 'phone', available: true },
  { key: 'ios', device: 'phone', available: false },
  { key: 'macos', device: 'laptop', available: false },
  { key: 'huawei', device: 'store', available: false },
  { key: 'windows', device: 'desktop', available: false },
]
const GLYPH = { phone: PhoneGlyph, laptop: LaptopGlyph, desktop: DesktopGlyph, store: StoreGlyph }

export function Platforms() {
  const { t, locale } = useI18n()
  return (
    <section className="section platforms" id="platforms" aria-labelledby="platforms-title">
      <div className="container">
        <header className="section-head" data-reveal>
          <p className="eyebrow">
            <span className="eyebrow__dot" aria-hidden="true" />
            {t.platforms.eyebrow}
          </p>
          <h2 id="platforms-title" className="h2">
            {t.platforms.title}
          </h2>
          <p className="lead">{t.platforms.lead}</p>
        </header>

        <ul className="platforms__grid">
          {PLATFORMS.map((p, i) => {
            const Glyph = GLYPH[p.device]
            const info = t.platforms[p.key]
            return (
              <li
                key={p.key}
                className={`platform platform--${p.device} ${p.available ? 'is-available' : 'is-soon'}`}
                data-reveal
                style={{ ['--d' as string]: i }}
              >
                <div className={`device device--${p.device}`} aria-hidden="true">
                  <span className="device__screen">
                    <img src="/assets/app/saat-icon.webp" alt="" width="40" height="40" loading="lazy" />
                  </span>
                </div>
                <div className="platform__body">
                  <span className={`badge ${p.available ? 'badge--available' : 'badge--soon'}`}>
                    {p.available ? <CheckIcon /> : <ClockIcon />}
                    {p.available ? t.platforms.available : t.common.comingSoon}
                  </span>
                  <h3 className="platform__name">
                    <Glyph className="platform__glyph" />
                    {info.name}
                  </h3>
                  <p>{info.desc}</p>
                  {p.key === 'android' && (
                    <a className="link-arrow" href={playUrl(locale)} target="_blank" rel="noopener">
                      {t.platforms.android.cta}
                      <ArrowUpRight />
                      <span className="sr-only"> {t.common.newTab}</span>
                    </a>
                  )}
                </div>
              </li>
            )
          })}
        </ul>
        <p className="platforms__note" data-reveal>
          {t.platforms.noDates}
        </p>
      </div>
    </section>
  )
}

/* ---------------- About ---------------- */
const VALUE_ICONS = [LeafIcon, HeartIcon, ShieldIcon]
export function About() {
  const { t } = useI18n()
  return (
    <section className="section about" id="about" aria-labelledby="about-title">
      <div className="container about__grid">
        <figure className="about__art" data-reveal>
          <img src="/assets/app/sunset.webp" alt={t.about.imageAlt} width="1200" height="730" loading="lazy" decoding="async" />
        </figure>
        <div className="about__copy">
          <div data-reveal>
            <p className="eyebrow">
              <span className="eyebrow__dot" aria-hidden="true" />
              {t.about.eyebrow}
            </p>
            <h2 id="about-title" className="h2">
              {t.about.title}
            </h2>
            {t.about.body.map((p) => (
              <p key={p} className="lead">
                {p}
              </p>
            ))}
          </div>
          <ul className="values">
            {t.about.values.map((v, i) => {
              const Icon = VALUE_ICONS[i]
              return (
                <li key={v.title} data-reveal style={{ ['--d' as string]: i + 1 }}>
                  <span className="values__icon" aria-hidden="true">
                    <Icon />
                  </span>
                  <div>
                    <h3>{v.title}</h3>
                    <p>{v.body}</p>
                  </div>
                </li>
              )
            })}
          </ul>
        </div>
      </div>
    </section>
  )
}

/* ---------------- FAQ ---------------- */
const FAQ_ORDER = ['download', 'platforms', 'deletion', 'support', 'offline', 'kimi', 'location', 'sample'] as const

export function Faq() {
  const { t, locale } = useI18n()
  const hrefFor: Partial<Record<(typeof FAQ_ORDER)[number], { href: string; external: boolean }>> = {
    download: { href: playUrl(locale), external: true },
    deletion: { href: DELETE_ACCOUNT_PATH, external: false },
    support: { href: `mailto:${SUPPORT_EMAIL}?subject=${encodeURIComponent('Sāat')}`, external: false },
  }
  return (
    <section className="section faq" id="faq" aria-labelledby="faq-title">
      <div className="container faq__grid">
        <header className="section-head section-head--left" data-reveal>
          <p className="eyebrow">
            <span className="eyebrow__dot" aria-hidden="true" />
            {t.faq.eyebrow}
          </p>
          <h2 id="faq-title" className="h2">
            {t.faq.title}
          </h2>
          <img className="faq__kimi" src="/assets/kimi/kimi-thinking-still.webp" alt="" width="160" height="160" loading="lazy" />
        </header>
        <div className="faq__list">
          {FAQ_ORDER.map((k, i) => {
            const item = t.faq.items[k]
            const link = hrefFor[k]
            return (
              <details key={k} className="faq__item" data-reveal style={{ ['--d' as string]: Math.min(i, 4) }} id={`faq-${k}`}>
                <summary>
                  <span>{item.q}</span>
                  <span className="faq__plus" aria-hidden="true" />
                </summary>
                <div className="faq__answer">
                  <p>{item.a}</p>
                  {link && item.link && (
                    <a
                      className="link-arrow"
                      href={link.href}
                      {...(link.external ? { target: '_blank', rel: 'noopener' } : {})}
                    >
                      {item.link}
                      <ArrowUpRight />
                      {link.external && <span className="sr-only"> {t.common.newTab}</span>}
                    </a>
                  )}
                </div>
              </details>
            )
          })}
        </div>
      </div>
    </section>
  )
}

/* ---------------- Final CTA ---------------- */
export function FinalCta() {
  const { t } = useI18n()
  return (
    <section className="cta" aria-labelledby="cta-title">
      <div className="container">
        <div className="cta__card" data-reveal>
          <img className="cta__bg" src="/assets/app/night.webp" alt="" width="1200" height="730" loading="lazy" decoding="async" />
          <div className="cta__content">
            <img className="cta__icon" src="/assets/app/saat-icon.webp" alt="" width="72" height="72" loading="lazy" />
            <Ornament className="cta__orn" />
            <h2 id="cta-title" className="h2 h2--light">
              {t.cta.title}
            </h2>
            <p className="lead lead--light">{t.cta.lead}</p>
            <StoreButton variant="light" />
            <p className="cta__note">{t.hero.platformNote}</p>
          </div>
        </div>
      </div>
    </section>
  )
}

/* ---------------- Footer ---------------- */
export function Footer() {
  const { t, locale } = useI18n()
  const year = new Date().getFullYear()
  return (
    <footer className="footer">
      <div className="container footer__grid">
        <div className="footer__brand">
          <a className="brand brand--footer" href="#top" aria-label={t.nav.home}>
            <img className="brand__icon" src="/assets/app/saat-icon.webp" alt="" width="40" height="40" loading="lazy" />
            <span className="brand__word">Sāat</span>
          </a>
          <p>{t.footer.tagline}</p>
          <StoreButton variant="light" className="store-btn--compact" />
        </div>
        <nav className="footer__col" aria-labelledby="f-explore">
          <h2 id="f-explore">{t.footer.explore}</h2>
          <ul>
            <li>
              <a href="#features">{t.nav.features}</a>
            </li>
            <li>
              <a href="#kimi">{t.nav.kimi}</a>
            </li>
            <li>
              <a href="#platforms">{t.nav.platforms}</a>
            </li>
            <li>
              <a href="#about">{t.nav.about}</a>
            </li>
            <li>
              <a href="#faq">{t.nav.faq}</a>
            </li>
          </ul>
        </nav>
        <nav className="footer__col" aria-labelledby="f-legal">
          <h2 id="f-legal">{t.footer.legal}</h2>
          <ul>
            <li>
              <a href={privacyUrl(locale)} target="_blank" rel="noopener">
                {t.footer.privacy}
                <span className="sr-only"> {t.common.newTab}</span>
              </a>
            </li>
            <li>
              <a href={termsUrl(locale)} target="_blank" rel="noopener">
                {t.footer.terms}
                <span className="sr-only"> {t.common.newTab}</span>
              </a>
            </li>
            <li>
              <a href={DELETE_ACCOUNT_PATH}>{t.footer.deletion}</a>
            </li>
          </ul>
        </nav>
        <nav className="footer__col" aria-labelledby="f-help">
          <h2 id="f-help">{t.footer.help}</h2>
          <ul>
            <li>
              <a href={`mailto:${SUPPORT_EMAIL}`}>
                <MailIcon className="footer__ic" />
                {t.footer.support}
              </a>
            </li>
            <li>
              <a href={playUrl(locale)} target="_blank" rel="noopener">
                Google Play
                <span className="sr-only"> {t.common.newTab}</span>
              </a>
            </li>
          </ul>
        </nav>
      </div>
      <div className="container footer__base">
        <p>
          © {year} Sāat. {t.footer.rights}
        </p>
      </div>
    </footer>
  )
}
