package app.kamy.qalbuApp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KhgtCalendarResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: List<KhgtMonth>? = null,
    @SerialName("special_days") val specialDays: List<KhgtSpecialDay>? = null
)

@Serializable
data class KhgtMonth(
    val name: String? = null,
    val year: Int? = null,
    @SerialName("masehi_range") val masehiRange: String? = null,
    val days: List<KhgtDay>? = null
)

@Serializable
data class KhgtDay(
    val masehi: String? = null,
    @SerialName("masehi_short") val masehiShort: String? = null,
    val hijri: String? = null,
    val pasaran: String? = null,
    val tooltip: String? = null,
    @SerialName("is_event") val isEvent: Boolean? = null
)

@Serializable
data class KhgtSpecialDay(
    @SerialName("tanggal_hijri") val tanggalHijri: String? = null,
    @SerialName("tanggal_masehi") val tanggalMasehi: String? = null,
    val keterangan: String? = null
)

data class KhgtTodayInfo(
    val hijriLabel: String,
    val gregorianLabel: String,
    val pasaran: String?,
    val eventTitle: String?,
    val isImportantDay: Boolean
)
