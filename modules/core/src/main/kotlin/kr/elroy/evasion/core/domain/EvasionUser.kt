package kr.elroy.evasion.core.domain

import java.util.UUID
import kr.hqservice.framework.database.component.Table
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

class EvasionUser(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EvasionUser>(EvasionUserTable) {
        fun get(playerId: UUID): EvasionUser {
            return find { EvasionUserTable.playerId eq playerId }
                .firstOrNull() ?: new {
                this.playerId = playerId
            }
        }
    }

    var playerId by EvasionUserTable.playerId
    var evasionCount by EvasionUserTable.evasionCount
}

@Table
object EvasionUserTable : LongIdTable("elroyevasionsystem_user") {
    val playerId = uuid("player_id").uniqueIndex()
    val evasionCount = integer("evasion_count").default(0)
}