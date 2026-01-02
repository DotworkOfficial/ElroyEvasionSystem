package kr.elroy.evasion.core.service

import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.elroy.evasion.core.Settings.CRYSTALS_PER_DASH
import kr.elroy.evasion.core.domain.EvasionUser
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitAsync
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import kr.hqservice.framework.global.core.component.Service
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.math.cos
import kotlin.math.sin


@Service
class EvasionService(
    private val coroutineScope: CoroutineScope,
) {
    private val disabledPlayers = mutableListOf<UUID>()
    private val maxEvasionCountCache = mutableMapOf<UUID, Int>()
    private val availableEvasionCountCache = mutableMapOf<UUID, Int>()
    private val cooldownCache = mutableListOf<Player>()

    suspend fun addMaxEvasionCount(player: Player) {
        val maxCount = newSuspendedTransaction {
            ++EvasionUser.get(player.uniqueId).evasionCount
        }

        maxEvasionCountCache[player.uniqueId] = maxCount
        availableEvasionCountCache[player.uniqueId] = maxCount
    }

    suspend fun setMaxEvasionCount(player: Player, amount: Int) {
        newSuspendedTransaction {
            EvasionUser.get(player.uniqueId).evasionCount = amount
        }

        maxEvasionCountCache[player.uniqueId] = amount
        availableEvasionCountCache[player.uniqueId] = amount
    }

    suspend fun getMaxEvasionCount(player: Player): Int {
        return newSuspendedTransaction {
            EvasionUser.get(player.uniqueId).evasionCount
        }
    }

    fun getMaxEvasionCountFromCache(player: Player): Int {
        return maxEvasionCountCache[player.uniqueId] ?: 0
    }

    fun getAvailableEvasionCount(player: Player): Int {
        return availableEvasionCountCache[player.uniqueId] ?: 0
    }

    fun addAvailableEvasionCount(player: Player, amount: Int) {
        availableEvasionCountCache[player.uniqueId] = getAvailableEvasionCount(player) + amount
    }

    fun isFull(player: Player): Boolean {
        return getAvailableEvasionCount(player) >= getMaxEvasionCountFromCache(player)
    }

    @Suppress("DEPRECATION")
    fun executeEvasion(player: Player) {
        if (disabledPlayers.contains(player.uniqueId)) {
            player.sendActionBar("&c대쉬가 비활성화 되어있습니다.".colorize())
            return
        }

        if (getAvailableEvasionCount(player) <= 0) {
            return player.sendActionBar(
                "&f驾대쉬 &e${getAvailableEvasionCount(player)} &7/ &e${
                    getMaxEvasionCountFromCache(
                        player
                    )
                }".colorize()
            )
        }

        addAvailableEvasionCount(player, -1)
        player.sendActionBar("&f驾대쉬 &e${getAvailableEvasionCount(player)} &7/ &e${getMaxEvasionCountFromCache(player)}".colorize())

        player.velocity = Vector(0.0, 0.7, 0.0)

        coroutineScope.launch(Dispatchers.BukkitAsync) {
            bukkitDelay(2)

            drawCircleParticle(player, Color.YELLOW, 1.3, -1.0)
            drawCircleParticle(player, Color.AQUA, 1.1, -0.5)
            drawCircleParticle(player, Color.WHITE, 0.9, 0.0)

            bukkitDelay(4)

            val vector = player.location.direction
            vector.multiply(1.7)
            vector.setY(0.3)
            player.velocity = vector
        }

        if (!cooldownCache.contains(player)) {
            cooldownCache.add(player)
            cooldown(player)
        }
    }

    fun cooldown(player: Player) {
        if (!player.isOnline) {
            cooldownCache.remove(player)
            return
        }

        coroutineScope.launch(Dispatchers.Default) {
            delay(5000)
            addAvailableEvasionCount(player, 1)

            @Suppress("DEPRECATION")
            player.sendActionBar(
                "&f驾대쉬 &e${getAvailableEvasionCount(player)} &7/ &e${
                    getMaxEvasionCountFromCache(
                        player
                    )
                }".colorize()
            )

            if (isFull(player)) {
                cooldownCache.remove(player)
                return@launch
            }

            cooldown(player)
        }
    }

    fun tryAcquireEvasion(player: Player, collectedCrystalCount: Int): Boolean {
        val maxEvasionCount = getMaxEvasionCountFromCache(player)

        if (maxEvasionCount >= 5) {
            player.sendColorizedMessage("&c최대 대쉬 개수는 5개입니다.")
            return false
        }

        if (collectedCrystalCount % CRYSTALS_PER_DASH != 0 || collectedCrystalCount == 0) {
            player.sendColorizedMessage("&a대쉬 획득까지 ${CRYSTALS_PER_DASH - (collectedCrystalCount % CRYSTALS_PER_DASH)}개 남았습니다.")
            return false
        }

        player.sendColorizedMessage("&a대쉬를 획득하였습니다.")

        coroutineScope.launch(Dispatchers.BukkitAsync) {
            addMaxEvasionCount(player)
        }

        return true
    }

    fun toggleEvasion(player: Player): Boolean {
        if (disabledPlayers.remove(player.uniqueId)) {
            return true
        }

        disabledPlayers.add(player.uniqueId)
        return false
    }

    private fun drawCircleParticle(player: Player, color: Color, radius: Double, distance: Double) {
        val playerLocation = player.location.clone().add(0.0, 1.2, 0.0) // add to Y-coordinate for eye level
        val viewDirection = playerLocation.direction.normalize()

        viewDirection.setY(0)

        val up = Vector(0, 1, 0)
        val horizontalOffset = viewDirection.clone().multiply(distance)
        val particleDirection = viewDirection.crossProduct(up).normalize()
        val particles = 30
        for (i in 0 until particles) {
            val angle = 2 * Math.PI * i / particles
            val x = radius * cos(angle)
            val z = radius * sin(angle)
            val particleLocation = playerLocation.clone().add(particleDirection.clone().multiply(x))
                .add(Vector(0.0, z, 0.0))
            particleLocation.add(horizontalOffset)

            particleLocation.world!!.spawnParticle(
                Particle.REDSTONE,
                particleLocation,
                1,
                Particle.DustOptions(color, 1.0f)
            )
        }
    }

    /************** CACHE **************/
    suspend fun loadToCache(player: Player) {
        maxEvasionCountCache[player.uniqueId] = getMaxEvasionCount(player)
        availableEvasionCountCache[player.uniqueId] = getMaxEvasionCountFromCache(player)
    }

    fun removeFromCache(player: Player) {
        maxEvasionCountCache.remove(player.uniqueId)
        availableEvasionCountCache.remove(player.uniqueId)
        disabledPlayers.remove(player.uniqueId)
        cooldownCache.remove(player)
    }
}