package app.kamy.saatApp.domain.util

/**
 * Validates email addresses and blocks temporary/disposable email domains.
 */
object DisposableEmailValidator {

    private val BLOCKED_DOMAINS = hashSetOf(
        // Getnada ecosystem
        "getnada.com",
        "abunawa.com",
        "givmail.com",
        "inboxbear.com",
        "maildrop.cc",
        "nada.ltd",
        "nada.email",
        "tempr.email",
        "tmail.ws",
        "crazymailing.com",
        "clrmail.com",
        "duck2.club",
        "cars2.club",
        "woofmail.net",
        "getairmail.com",

        // TempMail ecosystem
        "tempmail.com",
        "temp-mail.org",
        "tempmail.net",
        "10minutemail.com",
        "10minutemail.net",
        "10minemail.com",
        "20minutemail.com",

        // GuerrillaMail ecosystem
        "guerrillamail.com",
        "guerrillamail.net",
        "guerrillamail.org",
        "guerrillamail.biz",
        "guerrillamailblock.com",
        "sharklasers.com",
        "grr.la",
        "pokemail.net",
        "spam4.me",

        // Mailinator ecosystem
        "mailinator.com",
        "mailinator2.com",
        "suremail.info",
        "mailinater.com",

        // YOPmail ecosystem
        "yopmail.com",
        "yopmail.fr",
        "yopmail.net",
        "cool.fr.nf",
        "jetable.org",
        "nospam.ze.tc",
        "nomail.xl.cx",
        "mega.zik.dj",
        "speed.1s.fr",
        "courriel.fr.nf",
        "moncourrier.fr.nf",
        "monemail.fr.nf",
        "monmail.fr.nf",

        // Other well-known disposable providers
        "dispostable.com",
        "throwawaymail.com",
        "fakeinbox.com",
        "trashmail.com",
        "trashmail.net",
        "trashmail.me",
        "mohmal.com",
        "burnermail.io",
        "dropmail.me",
        "byom.de",
        "mytemp.email",
        "tempinbox.com",
        "emailondeck.com",
        "mailnull.com",
        "spamgourmet.com",
        "harakirimail.com",
        "zetmail.com",
        "armyspy.com",
        "cuvox.de",
        "dayrep.com",
        "fleckens.hu",
        "gustr.com",
        "jourrapide.com",
        "rhyta.com",
        "superrito.com",
        "teleworm.us",
        "tinemail.com",
        "chacuo.net"
    )

    private val BLOCKED_KEYWORDS = listOf(
        "tempmail",
        "10minute",
        "guerrilla",
        "throwaway",
        "fakeinbox",
        "trashmail",
        "disposable",
        "yopmail",
        "getnada"
    )

    /**
     * Checks if the email is from a disposable/temporary provider.
     * Returns true if the email is disposable (should be blocked).
     */
    fun isDisposable(email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val atIndex = cleanEmail.indexOf('@')
        if (atIndex == -1 || atIndex == cleanEmail.length - 1) {
            return false
        }
        val domain = cleanEmail.substring(atIndex + 1)
        if (domain in BLOCKED_DOMAINS) {
            return true
        }
        return BLOCKED_KEYWORDS.any { keyword -> domain.contains(keyword) }
    }
}

