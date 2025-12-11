package kr.elroy.evasion.core.domain

import kr.elroy.evasion.core.dto.EvasionLocation
import kr.elroy.evasion.core.util.evasionLocation
import kr.hqservice.framework.database.component.Table
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

class Crystal(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Crystal>(CrystalTable) {
        fun create(evasionLocation: EvasionLocation): Crystal {
            return new {
                this.location = evasionLocation
            }
        }
    }

    var location by evasionLocation(CrystalTable.location)
}

@Table
object CrystalTable : LongIdTable("elroyevasionsystem_crystals") {
    val location = evasionLocation("location")
}