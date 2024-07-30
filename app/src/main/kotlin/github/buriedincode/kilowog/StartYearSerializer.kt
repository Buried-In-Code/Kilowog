package github.buriedincode.kilowog

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class StartYearSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("StartYear", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int? {
        val value = decoder.decodeString()
        return value.toIntOrNull()
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Int?) {
        if (value != null) {
            encoder.encodeInt(value = value)
        } else {
            encoder.encodeNull()
        }
    }
}
