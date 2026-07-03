package dev.salemlift.data.db

import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.SessionTemplate
import dev.salemlift.domain.model.Split
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@Serializable
internal data class SessionTemplateDto(
    val name: String,
    val muscles: List<Muscle>,
)

@Serializable
internal data class SplitDto(
    val name: String,
    val sessions: List<SessionTemplateDto>,
)

/**
 * JSON codecs for the tracker's TEXT columns ([MesocycleEntity.splitJson],
 * [MuscleWeekStateEntity.distributionJson]). The domain [Split] is mirrored
 * through serializable DTOs so :domain stays annotation-free; muscles are
 * encoded by enum name.
 */
internal object TrackerCodecs {
    private val json = Json
    private val intListSerializer = ListSerializer(Int.serializer())

    fun encodeSplit(split: Split): String =
        json.encodeToString(
            SplitDto.serializer(),
            SplitDto(
                name = split.name,
                sessions = split.sessions.map { SessionTemplateDto(it.name, it.muscles) },
            ),
        )

    fun decodeSplit(raw: String): Split {
        val dto = json.decodeFromString(SplitDto.serializer(), raw)
        return Split(
            name = dto.name,
            sessions = dto.sessions.map { SessionTemplate(it.name, it.muscles) },
        )
    }

    fun encodeDistribution(distribution: List<Int>): String = json.encodeToString(intListSerializer, distribution)

    fun decodeDistribution(raw: String): List<Int> = json.decodeFromString(intListSerializer, raw)
}
