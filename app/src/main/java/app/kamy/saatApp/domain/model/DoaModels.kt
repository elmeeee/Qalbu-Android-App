package app.kamy.saatApp.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object FlexibleTranslationSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleTranslationSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        if (value != null) {
            encoder.encodeString(value)
        } else {
            encoder.encodeNull()
        }
    }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): String? {
        val input = decoder as? JsonDecoder ?: return decoder.decodeString()
        return when (val element = input.decodeJsonElement()) {
            is JsonNull -> null
            is JsonPrimitive -> element.contentOrNull
            is JsonObject -> {
                element["id"]?.jsonPrimitive?.contentOrNull
                    ?: element["ms"]?.jsonPrimitive?.contentOrNull
                    ?: element["en"]?.jsonPrimitive?.contentOrNull
            }
            else -> null
        }
    }
}

@Serializable
data class DoaCategory(
    val id: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val photo: String? = null
)

@Serializable
data class DoaItem(
    val id: String? = null,
    val title: String? = null,
    val arabic: String? = null,
    val latin: String? = null,
    @Serializable(with = FlexibleTranslationSerializer::class)
    val translation: String? = null,
    val notes: String? = null,
    val fawaid: String? = null,
    val source: String? = null,
    val category: String? = null,
    val categories: DoaCategory? = null
)

@Serializable
data class DoaListResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: List<DoaItem>? = null
)

@Serializable
data class DhikrContentItem(
    val id: String? = null,
    val arabic: String? = null,
    val latin: String? = null,
    @Serializable(with = FlexibleTranslationSerializer::class)
    val translation: String? = null,
    val fawaid: String? = null,
    val notes: String? = null,
    val source: String? = null,
    @SerialName("repeat_count") val repeatCount: Int? = null
)

@Serializable
data class DhikrBundle(
    val title: String? = null,
    val category: String? = null,
    @SerialName("compiled_by") val compiledBy: String? = null,
    val content: List<DhikrContentItem>? = null
)

@Serializable
data class DhikrListResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: List<DhikrBundle>? = null
)

@Serializable
data class DoaCategoriesResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: List<DoaCategory>? = null
)

enum class DoaCatalogKind { DOA, DHIKR }

data class DoaCatalogEntry(
    val slug: String,
    val title: String,
    val kind: DoaCatalogKind
)
