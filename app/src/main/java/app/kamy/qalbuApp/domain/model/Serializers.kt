package app.kamy.qalbuApp.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.long

internal object StringFromAnyScalarSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringFromAnyScalar", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        if (decoder !is JsonDecoder) return decoder.decodeString()
        val element = decoder.decodeJsonElement()
        if (element !is JsonPrimitive) {
            throw IllegalStateException("Expected primitive id, got $element")
        }
        return element.contentOrNull
            ?: element.long.toString()
    }
}

internal object NullableStringFromAnyScalarSerializer : KSerializer<String?> {
    private val delegate = String.serializer().nullable

    override val descriptor: SerialDescriptor = delegate.descriptor

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) encoder.encodeNull() else encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String? {
        if (decoder !is JsonDecoder) return decoder.decodeString()
        val element = decoder.decodeJsonElement()
        if (element is kotlinx.serialization.json.JsonNull) return null
        if (element !is JsonPrimitive) return null
        return element.contentOrNull ?: element.long.toString()
    }
}

private val <T> KSerializer<T>.nullable: KSerializer<T?>
    get() = @Suppress("UNCHECKED_CAST") (this as KSerializer<T?>)

internal object FlexibleReflectTagsSerializer : KSerializer<List<ReflectFeedTag>?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleReflectTags", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<ReflectFeedTag>?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            ListSerializer(ReflectFeedTag.serializer()).serialize(encoder, value)
        }
    }

    override fun deserialize(decoder: Decoder): List<ReflectFeedTag>? {
        if (decoder !is JsonDecoder) {
            return if (decoder.decodeNotNullMark()) {
                decoder.decodeSerializableValue(ListSerializer(ReflectFeedTag.serializer()))
            } else {
                decoder.decodeNull()
                null
            }
        }
        return when (val element = decoder.decodeJsonElement()) {
            JsonNull -> null
            is JsonArray -> element.mapNotNull { item -> item.toReflectFeedTag() }
            else -> null
        }
    }
}

internal object FlexibleReflectReferencesSerializer : KSerializer<List<ReflectFeedReference>?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleReflectReferences", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<ReflectFeedReference>?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            ListSerializer(ReflectFeedReference.serializer()).serialize(encoder, value)
        }
    }

    override fun deserialize(decoder: Decoder): List<ReflectFeedReference>? {
        if (decoder !is JsonDecoder) {
            return if (decoder.decodeNotNullMark()) {
                decoder.decodeSerializableValue(ListSerializer(ReflectFeedReference.serializer()))
            } else {
                decoder.decodeNull()
                null
            }
        }
        return when (val element = decoder.decodeJsonElement()) {
            JsonNull -> null
            is JsonArray -> element.mapNotNull { item ->
                runCatching {
                    decoder.json.decodeFromJsonElement(ReflectFeedReference.serializer(), item)
                }.getOrNull()
            }
            is JsonObject -> listOfNotNull(
                runCatching {
                    decoder.json.decodeFromJsonElement(ReflectFeedReference.serializer(), element)
                }.getOrNull()
            )
            else -> null
        }
    }
}

private fun JsonElement.toReflectFeedTag(): ReflectFeedTag? = when (this) {
    is JsonPrimitive -> ReflectFeedTag(name = contentOrNull)
    is JsonObject -> ReflectFeedTag(
        language = this["language"]?.jsonPrimitiveOrNull(),
        id = this["id"]?.jsonIntOrNull(),
        name = this["name"]?.jsonPrimitiveOrNull()
    )
    else -> null
}

private fun JsonElement.jsonPrimitiveOrNull(): String? =
    (this as? JsonPrimitive)?.contentOrNull

private fun JsonElement.jsonIntOrNull(): Int? =
    (this as? JsonPrimitive)?.contentOrNull?.toIntOrNull() ?: (this as? JsonPrimitive)?.long?.toInt()
