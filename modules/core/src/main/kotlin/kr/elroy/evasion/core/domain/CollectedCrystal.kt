package kr.elroy.evasion.core.domain

import java.util.UUID
import kr.hqservice.framework.database.component.Table
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.and

class CollectedCrystal(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CollectedCrystal>(CollectedCrystalTable) {
        fun create(playerId: UUID, crystalId: Long): CollectedCrystal {
            return new {
                this.evasionUser = EvasionUser.get(playerId)
                this.crystal = Crystal[crystalId]
            }
        }

        fun findByPlayerIdAndCrystalId(playerId: UUID, crystalId: Long): CollectedCrystal? {
            val evasionUser = EvasionUser.get(playerId)

            return find {
                (CollectedCrystalTable.evasionUserId eq evasionUser.id) and
                        (CollectedCrystalTable.crystalId eq crystalId)
            }.firstOrNull()
        }

        fun findCrystalIdsByPlayerId(playerId: UUID): Set<Long> {
            val evasionUser = EvasionUser.get(playerId)

            return find { CollectedCrystalTable.evasionUserId eq evasionUser.id }
                .map { it.crystal.id.value }
                .toSet()
        }
    }

    var evasionUser by EvasionUser referencedOn CollectedCrystalTable.evasionUserId
    var crystal by Crystal referencedOn CollectedCrystalTable.crystalId
}

@Table
object CollectedCrystalTable : LongIdTable("elroyevasionsystem_collected_crystals") {
    val evasionUserId = reference("evasion_user_id", EvasionUserTable)
    val crystalId = reference("crystal_id", CrystalTable, onDelete = ReferenceOption.CASCADE)
}