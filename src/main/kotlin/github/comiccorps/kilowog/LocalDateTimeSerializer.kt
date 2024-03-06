package github.comiccorps.kilowog

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDateTime::class)
object LocalDateTimeSerializer {
    private val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val dateString = decoder.decodeString()
        return LocalDateTime.parse(dateString, format)
    }

    override fun serialize(
        encoder: Encoder,
        value: LocalDateTime,
    ) {
        encoder.encodeString(value = value.format(format))
    }
}
