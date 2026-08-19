package app.kamy.saatApp.domain.quran

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
            listOf("tasyrik", "tashreeq"),
            DailyVersePick("2:203", DailyVerseOccasion.Tasyrik)
        ),
        EventRule(
            listOf("maulid", "mawlid", "kelahiran nabi"),
            DailyVersePick("21:107", DailyVerseOccasion.Maulid)
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
            listOf("asyura", "ashura", "asyuro", "tasua", "tasu'a"),
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
            listOf("ayyamul bidh", "puasa putih"),
            DailyVersePick("2:184", DailyVerseOccasion.AyyamulBidh)
        ),
        EventRule(
            listOf("puasa daud", "daud"),
            DailyVersePick("38:17", DailyVerseOccasion.FastDaud)
        ),
        EventRule(
            listOf("ramadan", "ramadhan", "puasa"),
            DailyVersePick("2:183", DailyVerseOccasion.Ramadan)
        )
    )

    // 1. Muharram (Hijrah, Taubat, Empat Bulan Haram)
    private val muharramRotation = listOf(
        "9:36", "2:218", "9:100", "5:54", "10:90",
        "2:156", "3:135", "9:40", "39:53", "59:18"
    )

    // 2. Safar (Tawakkal, Keselamatan, & Keteguhan)
    private val safarRotation = listOf(
        "65:3", "9:51", "3:160", "11:56", "14:7",
        "16:128", "26:78", "2:256", "67:15", "39:38"
    )

    // 3. Rabi'ul Awwal (Maulid Nabi & Sholawat)
    private val rabiulAwalRotation = listOf(
        "33:56", "33:21", "9:128", "68:4", "3:144",
        "48:29", "3:31", "21:107", "48:1", "94:1"
    )

    // 4. Rabi'ul Akhir (Tadabbur, Ilmu, & Keagungan Qur'an)
    private val rabiulAkhirRotation = listOf(
        "3:190", "3:191", "20:114", "35:28", "59:21",
        "17:82", "39:9", "58:11", "96:1", "67:2"
    )

    // 5. Jumada al-Ula (Keluarga, Ukhuwah, & Bakti Orang Tua)
    private val jumadilAwalRotation = listOf(
        "30:21", "17:23", "49:10", "31:14", "4:36",
        "66:6", "25:74", "17:24", "46:15", "49:13"
    )

    // 6. Jumada al-Akhirah (Syukur Nikmat & Istiqamah)
    private val jumadilAkhirRotation = listOf(
        "14:7", "16:97", "2:152", "41:30", "65:2",
        "65:3", "55:60", "28:77", "93:11", "2:261"
    )

    // 7. Rajab (Persiapan Ibadah & Isra' Mi'raj)
    private val rajabRotation = listOf(
        "17:1", "9:36", "17:78", "17:79", "39:53",
        "3:133", "11:114", "50:16", "23:118", "73:8"
    )

    // 8. Sya'ban (Penyerahan Amal & Nisfu Sya'ban)
    private val syabanRotation = listOf(
        "44:3", "33:56", "2:144", "35:10", "14:41",
        "3:135", "59:18", "25:63", "53:39", "89:27"
    )

    // 9. Ramadan (Puasa, Nuzulul Qur'an, & Lailatul Qadr)
    private val ramadanRotation = listOf(
        "2:183", "2:185", "2:186", "2:201", "3:133",
        "25:74", "3:191", "59:21", "97:1", "97:3",
        "2:184", "48:4"
    )

    // 10. Syawal (Fitrah, Idul Fitri, & Kemenangan)
    private val syawalRotation = listOf(
        "2:185", "87:14", "87:15", "5:3", "3:134",
        "49:10", "16:97", "2:197", "55:60", "93:5"
    )

    // 11. Dhu al-Qi'dah (Bulan Haram, Ketenangan, & Persiapan Haji)
    private val dzulqadahRotation = listOf(
        "9:36", "2:197", "49:12", "13:28", "23:1",
        "23:2", "50:18", "33:70", "41:33", "67:12"
    )

    // 12. Dhu al-Hijjah (10 Hari Pertama, Arafah, Qurban, & Tasyrik)
    private val dzulhijjahRotation = listOf(
        "89:1", "89:2", "22:28", "22:37", "2:198",
        "5:3", "108:2", "2:203", "37:102", "22:34"
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
        matchHijriLabel(context.hijriLabel, context.dayOfYear, context.isRamadanSeason)?.let { return it }

        // Day of week thematic picks
        when (context.dayOfWeek) {
            Calendar.FRIDAY -> return DailyVersePick("62:9", DailyVerseOccasion.Jumuah)
            Calendar.MONDAY -> {
                val picks = listOf("35:10", "3:133", "2:183")
                val p = picks[(context.dayOfYear - 1) % picks.size]
                return DailyVersePick(p, DailyVerseOccasion.Monday)
            }
            Calendar.THURSDAY -> {
                val picks = listOf("3:135", "11:114", "4:110")
                val p = picks[(context.dayOfYear - 1) % picks.size]
                return DailyVersePick(p, DailyVerseOccasion.Thursday)
            }
            Calendar.TUESDAY -> {
                val picks = listOf("20:114", "58:11", "31:12")
                val p = picks[(context.dayOfYear - 1) % picks.size]
                return DailyVersePick(p, DailyVerseOccasion.Tuesday)
            }
            Calendar.WEDNESDAY -> {
                val picks = listOf("65:3", "14:7", "2:152")
                val p = picks[(context.dayOfYear - 1) % picks.size]
                return DailyVersePick(p, DailyVerseOccasion.Wednesday)
            }
            Calendar.SATURDAY -> {
                val picks = listOf("30:21", "4:1", "66:6")
                val p = picks[(context.dayOfYear - 1) % picks.size]
                return DailyVersePick(p, DailyVerseOccasion.Saturday)
            }
            Calendar.SUNDAY -> {
                val picks = listOf("3:190", "67:2", "88:17")
                val p = picks[(context.dayOfYear - 1) % picks.size]
                return DailyVersePick(p, DailyVerseOccasion.Sunday)
            }
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

    private fun extractHijriDay(label: String?): Int? {
        if (label.isNullOrBlank()) return null
        val regex = Regex("\\b([1-9]|[12][0-9]|30)\\b")
        val match = regex.find(label) ?: return null
        return match.value.toIntOrNull()
    }

    private fun matchHijriLabel(label: String?, dayOfYear: Int, isRamadanSeason: Boolean): DailyVersePick? {
        val text = label?.lowercase().orEmpty()
        val dayNumber = extractHijriDay(label)

        if (text.isEmpty()) {
            return if (isRamadanSeason) {
                val pick = ramadanRotation[(dayOfYear - 1).coerceAtLeast(0) % ramadanRotation.size]
                DailyVersePick(pick, DailyVerseOccasion.Ramadan)
            } else null
        }

        return when {
            // 1. Muharram
            text.contains("muharram") || text.contains("muharam") -> {
                when (dayNumber) {
                    1 -> DailyVersePick("9:36", DailyVerseOccasion.Muharram)
                    9, 10 -> DailyVersePick("5:54", DailyVerseOccasion.Ashura)
                    else -> {
                        val pick = muharramRotation[(dayOfYear - 1).coerceAtLeast(0) % muharramRotation.size]
                        DailyVersePick(pick, DailyVerseOccasion.Muharram)
                    }
                }
            }
            // 2. Safar
            text.contains("safar") || text.contains("saphar") -> {
                val pick = safarRotation[(dayOfYear - 1).coerceAtLeast(0) % safarRotation.size]
                DailyVersePick(pick, DailyVerseOccasion.Daily)
            }
            // 3. Rabiul Awal
            text.contains("rabi") && (text.contains("awal") || text.contains("awwal")) -> {
                if (dayNumber == 12) {
                    DailyVersePick("21:107", DailyVerseOccasion.Maulid)
                } else {
                    val pick = rabiulAwalRotation[(dayOfYear - 1).coerceAtLeast(0) % rabiulAwalRotation.size]
                    DailyVersePick(pick, DailyVerseOccasion.Maulid)
                }
            }
            // 4. Rabiul Akhir
            text.contains("rabi") && (text.contains("akhir") || text.contains("thani") || text.contains("tsani")) -> {
                val pick = rabiulAkhirRotation[(dayOfYear - 1).coerceAtLeast(0) % rabiulAkhirRotation.size]
                DailyVersePick(pick, DailyVerseOccasion.Daily)
            }
            // 5. Jumadil Awal
            text.contains("jumad") && (text.contains("awal") || text.contains("ula") || text.contains("awwal")) -> {
                val pick = jumadilAwalRotation[(dayOfYear - 1).coerceAtLeast(0) % jumadilAwalRotation.size]
                DailyVersePick(pick, DailyVerseOccasion.Daily)
            }
            // 6. Jumadil Akhir
            text.contains("jumad") && (text.contains("akhir") || text.contains("thani") || text.contains("tsani") || text.contains("akhirah")) -> {
                val pick = jumadilAkhirRotation[(dayOfYear - 1).coerceAtLeast(0) % jumadilAkhirRotation.size]
                DailyVersePick(pick, DailyVerseOccasion.Daily)
            }
            // 7. Rajab
            text.contains("rajab") -> {
                if (dayNumber == 27) {
                    DailyVersePick("17:1", DailyVerseOccasion.IsraMiraj)
                } else {
                    val pick = rajabRotation[(dayOfYear - 1).coerceAtLeast(0) % rajabRotation.size]
                    DailyVersePick(pick, DailyVerseOccasion.Daily)
                }
            }
            // 8. Sya'ban
            text.contains("sya") || text.contains("sha'b") || text.contains("shab") -> {
                if (dayNumber == 15) {
                    DailyVersePick("44:3", DailyVerseOccasion.Daily)
                } else {
                    val pick = syabanRotation[(dayOfYear - 1).coerceAtLeast(0) % syabanRotation.size]
                    DailyVersePick(pick, DailyVerseOccasion.Daily)
                }
            }
            // 9. Ramadan
            text.contains("ramadan") || text.contains("ramadhan") || isRamadanSeason -> {
                if (dayNumber == 17) {
                    DailyVersePick("97:1", DailyVerseOccasion.LailatulQadr)
                } else if (dayNumber in listOf(21, 23, 25, 27, 29)) {
                    DailyVersePick("97:3", DailyVerseOccasion.LailatulQadr)
                } else {
                    val pick = ramadanRotation[(dayOfYear - 1).coerceAtLeast(0) % ramadanRotation.size]
                    DailyVersePick(pick, DailyVerseOccasion.Ramadan)
                }
            }
            // 10. Syawal
            text.contains("syawal") || text.contains("shawwal") -> {
                if (dayNumber == 1) {
                    DailyVersePick("2:185", DailyVerseOccasion.EidFitr)
                } else {
                    val pick = syawalRotation[(dayOfYear - 1).coerceAtLeast(0) % syawalRotation.size]
                    DailyVersePick(pick, DailyVerseOccasion.Daily)
                }
            }
            // 11. Dzulqa'dah
            text.contains("dzulqa") || text.contains("zulqa") || text.contains("qi'd") || text.contains("qida") -> {
                val pick = dzulqadahRotation[(dayOfYear - 1).coerceAtLeast(0) % dzulqadahRotation.size]
                DailyVersePick(pick, DailyVerseOccasion.Daily)
            }
            // 12. Dzulhijjah
            text.contains("dzulhijjah") || text.contains("zulhijjah") || text.contains("hijjah") || text.contains("hajj") -> {
                when (dayNumber) {
                    9 -> DailyVersePick("2:198", DailyVerseOccasion.Hajj)
                    10 -> DailyVersePick("22:37", DailyVerseOccasion.EidAdha)
                    11, 12, 13 -> DailyVersePick("2:203", DailyVerseOccasion.Tasyrik)
                    else -> {
                        val pick = dzulhijjahRotation[(dayOfYear - 1).coerceAtLeast(0) % dzulhijjahRotation.size]
                        DailyVersePick(pick, DailyVerseOccasion.Hajj)
                    }
                }
            }
            else -> if (isRamadanSeason) {
                val pick = ramadanRotation[(dayOfYear - 1).coerceAtLeast(0) % ramadanRotation.size]
                DailyVersePick(pick, DailyVerseOccasion.Ramadan)
            } else null
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
