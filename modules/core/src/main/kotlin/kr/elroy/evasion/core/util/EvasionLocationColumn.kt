package kr.elroy.evasion.core.util

import kr.elroy.evasion.core.dto.EvasionLocation
import kr.hqservice.framework.database.util.ExposedPropertyDelegate
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import kotlin.reflect.KProperty

fun Table.evasionLocation(name: String): Column<String> = varchar(name, 128)

fun Entity<*>.evasionLocation(column: Column<String>): ExposedPropertyDelegate<EvasionLocation> =
    object : ExposedPropertyDelegate<EvasionLocation> {
        override operator fun <ID : Comparable<ID>> getValue(
            entity: Entity<ID>,
            desc: KProperty<*>,
        ): EvasionLocation {
            val data = entity.run { column.getValue(this, desc) }

            return EvasionLocation.fromString(data)
        }

        override operator fun <ID : Comparable<ID>> setValue(
            entity: Entity<ID>,
            desc: KProperty<*>,
            value: EvasionLocation,
        ) {
            val parsed = value.toString()
            entity.apply { column.setValue(this, desc, parsed) }
        }
    }

@JvmName("locationNullable")
fun Entity<*>.evasionLocation(column: Column<String?>): ExposedPropertyDelegate<EvasionLocation?> =
    object : ExposedPropertyDelegate<EvasionLocation?> {
        override operator fun <ID : Comparable<ID>> getValue(
            entity: Entity<ID>,
            desc: KProperty<*>,
        ): EvasionLocation? {
            val data = entity.run { column.getValue(this, desc) } ?: return null

            return EvasionLocation.fromString(data)
        }

        override operator fun <ID : Comparable<ID>> setValue(
            entity: Entity<ID>,
            desc: KProperty<*>,
            value: EvasionLocation?,
        ) {
            val parsed = value?.run {
                value.toString()
            }

            entity.apply { column.setValue(this, desc, parsed) }
        }
    }