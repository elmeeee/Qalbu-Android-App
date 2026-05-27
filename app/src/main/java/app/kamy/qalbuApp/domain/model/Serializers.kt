package app.kamy.qalbuApp.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.long

/**
 * Backing serializer that accepts either string, int, or double from JSON and produces String.
 * Reflect API mixes id encodings across endpoints — see iOS PostAPIModels.swift `decodeFeedID`.
 */
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

/**
 * Nullable variant for optional id fields.
 */
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
