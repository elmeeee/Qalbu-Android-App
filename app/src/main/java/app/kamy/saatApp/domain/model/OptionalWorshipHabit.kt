package app.kamy.saatApp.domain.model

enum class OptionalWorshipHabit(val prefKey: String) {
    QIYAMUL_LAIL("qiyam"),
    MONDAY_THURSDAY_FAST("mon_thu_fast"),
    AYYAMUL_BIDH_SAHUR("ayyamul_bidh_sahur"),
    DHIKR_MORNING("dhikr_morning"),
    DHIKR_EVENING("dhikr_evening"),
    READ_QURAN("read_quran"),
    DAILY_CHARITY("daily_charity"),
    DHUHA("dhuha"),
    RAWATIB("rawatib");

    companion object {
        val ALL = entries.toList()
    }
}
