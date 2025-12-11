package kr.elroy.evasion.core.dto

import org.bukkit.Bukkit
import org.bukkit.Location

data class EvasionLocation(
    val clusterId: String,
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
) {
    companion object {
        fun fromString(string: String): EvasionLocation {
            val split = string.split(";")
            return EvasionLocation(
                split[0],
                split[1],
                split[2].toDouble(),
                split[3].toDouble(),
                split[4].toDouble(),
                split[5].toFloat(),
                split[6].toFloat(),
            )
        }
    }

    fun toBukkitLocation() = Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
    override fun toString() = "$clusterId;$world;$x;$y;$z;$yaw;$pitch"
}