package app.kamy.qalbuApp.domain.quran

import java.util.Calendar

object DailyVerseResolver {

    private data class EventRule(
        val keywords: List<String>,
        val pick: DailyVersePick
    )

    private val eventRules = listOf(
        EventRule(
            listOf("idul fitri", "eid al-fitr", "eid fitr", "lebaran", "hari raya fitri"),
            DailyVersePick("2:185", DailyVerseOccasion.EidFitr)
        ),
        EventRule(
            listOf("idul adha", "eid al-adha", "eid adha", "kurban", "hari raya haji"),
            DailyVersePick("22:37", DailyVerseOccasion.EidAdha)
        ),
        EventRule(
            listOf("isra", "mi'raj", "miraj", "isra miraj"),
            DailyVersePick("17:1", DailyVerseOccasion.IsraMiraj)
        ),
        EventRule(
            listOf("nuzulul quran", "nuzul al-qur", "turunnya al-qur"),
            DailyVersePick("97:1", DailyVerseOccasion.LailatulQadr)
        ),
        EventRule(
            listOf("lailatul qadr", "laylat al-qadr", "malam qadar"),
            DailyVersePick("97:3", DailyVerseOccasion.LailatulQadr)
        ),
        EventRule(
            listOf("asyura", "ashura", "asyuro"),
            DailyVersePick("5:54", DailyVerseOccasion.Ashura)
        ),
        EventRule(
            listOf("muharram", "tahun baru hijri", "islamic new year", "1 muharram"),
            DailyVersePick("9:36", DailyVerseOccasion.Muharram)
        ),
        EventRule(
            listOf("arafah", "arafat", "wukuf", "hajj", "haji"),
            DailyVersePick("2:198", DailyVerseOccasion.Hajj)
        ),
        EventRule(
            listOf("ramadan", "ramadhan", "puasa"),
            DailyVersePick("2:183", DailyVerseOccasion.Ramadan)
        )
    )

    /** Short, meaningful ayat — one anchor per surah for a full-year rotation. */
    private val curatedRotation = listOf(
        "1:5", "2:152", "2:186", "3:139", "3:173", "4:147", "5:6", "6:162", "7:180", "8:2",
        "9:40", "10:62", "11:114", "12:86", "13:28", "14:7", "15:49", "16:97", "17:23", "18:24",
        "19:76", "20:114", "21:87", "22:77", "23:115", "24:35", "25:63", "26:80", "27:19", "28:24",
        "29:69", "30:21", "31:17", "32:16", "33:41", "34:39", "35:2", "36:58", "37:87", "38:29",
        "39:53", "40:44", "41:30", "42:38", "43:67", "44:51", "45:13", "46:13", "47:7", "48:4",
        "49:10", "50:16", "51:56", "52:48", "53:39", "54:17", "55:78", "56:79", "57:4", "58:22",
        "59:18", "60:4", "61:13", "62:10", "63:8", "64:11", "65:2", "66:8", "67:2", "68:4",
        "69:33", "70:19", "71:10", "72:18", "73:8", "74:38", "75:20", "76:9", "77:5", "78:31",
        "79:40", "80:24", "81:27", "82:5", "83:26", "84:6", "85:14", "86:17", "87:14", "88:21",
        "89:27", "90:4", "91:9", "92:4", "93:3", "94:5", "95:4", "96:1", "97:1", "98:5",
        "99:7", "100:6", "101:5", "102:1", "103:3", "104:6", "105:5", "106:3", "107:4", "108:2",
        "109:6", "110:3", "111:5", "112:1", "113:1", "114:6"
    )

    fun resolve(context: DailyVerseContext): DailyVersePick {
        matchEvent(context.eventTitle)?.let { return it }
        matchHijriLabel(context.hijriLabel, context.isRamadanSeason)?.let { return it }
        if (context.dayOfWeek == Calendar.FRIDAY) {
            return DailyVersePick("62:9", DailyVerseOccasion.Jumuah)
        }
        val index = (context.dayOfYear - 1).coerceAtLeast(0) % curatedRotation.size
        return DailyVersePick(curatedRotation[index], DailyVerseOccasion.Daily)
    }

    private fun matchEvent(eventTitle: String?): DailyVersePick? {
        val title = eventTitle?.trim().orEmpty()
        if (title.isEmpty()) return null
        return eventRules.firstOrNull { rule ->
            rule.keywords.any { keyword -> title.contains(keyword, ignoreCase = true) }
        }?.pick
    }

    private fun matchHijriLabel(label: String?, isRamadanSeason: Boolean): DailyVersePick? {
        val text = label?.lowercase().orEmpty()
        if (text.isEmpty()) {
            return if (isRamadanSeason) DailyVersePick("2:183", DailyVerseOccasion.Ramadan) else null
        }
        return when {
            text.contains("ramadan") || text.contains("ramadhan") ->
                DailyVersePick("2:183", DailyVerseOccasion.Ramadan)
            text.contains("syawal") || text.contains("shawwal") ->
                DailyVersePick("2:185", DailyVerseOccasion.EidFitr)
            text.contains("dzulhijjah") || text.contains("dhu al-hijjah") || text.contains("zulhijjah") ->
                DailyVersePick("22:37", DailyVerseOccasion.EidAdha)
            text.contains("muharram") ->
                DailyVersePick("9:36", DailyVerseOccasion.Muharram)
            else -> if (isRamadanSeason) DailyVersePick("2:183", DailyVerseOccasion.Ramadan) else null
        }
    }

    fun contextForToday(
        dayKey: String,
        hijriLabel: String? = null,
        eventTitle: String? = null,
        isRamadanSeason: Boolean = false,
        calendar: Calendar = Calendar.getInstance()
    ): DailyVerseContext = DailyVerseContext(
        dayKey = dayKey,
        dayOfYear = calendar.get(Calendar.DAY_OF_YEAR),
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
        hijriLabel = hijriLabel,
        eventTitle = eventTitle,
        isRamadanSeason = isRamadanSeason
    )
}
