package app.kamy.saatApp.domain.model

enum class OptionalWorshipHabit(val prefKey: String) {
    QIYAMUL_LAIL("qiyam"),
    MONDAY_THURSDAY_FAST("mon_thu_fast"),
    AYYAMUL_BIDH_SAHUR("ayyamul_bidh_sahur");

    companion object {
        val ALL = entries.toList()
    }
}
