package kr.elroy.evasion.core.service

import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.elroy.evasion.core.Settings
import kr.elroy.evasion.core.domain.CollectedCrystal
import kr.elroy.evasion.core.domain.Crystal
import kr.elroy.evasion.core.domain.CrystalTable
import kr.elroy.evasion.core.dto.CrystalDTO
import kr.elroy.evasion.core.dto.EvasionLocation
import kr.elroy.evasion.core.hook.ModelEngineHook
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitAsync
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import kr.hqservice.framework.global.core.component.Service
import org.bukkit.Location
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

private const val NEARBY_DETECTION_DISTANCE = 20.0
private const val NEARBY_CHECK_INTERVAL_TICKS = 20L

@Service
class CrystalService(
    private val modelEngineHook: ModelEngineHook,
    private val coroutineScope: CoroutineScope,
) {
    @Volatile
    private var isInitialized = false
    private val crystalIdByModelIdMap = mutableMapOf<UUID, Long>()
    private val crystalLocationByModelIdMap = mutableMapOf<UUID, Location>()
    private val notifiedCrystalsByPlayer = mutableMapOf<UUID, MutableSet<Long>>()
    private val nearbyCheckJobByPlayer = mutableMapOf<UUID, Job>()

    suspend fun getAllCrystal(): List<CrystalDTO> {
        return newSuspendedTransaction {
            Crystal.all()
                .notForUpdate()
                .orderBy(CrystalTable.id to SortOrder.ASC)
                .map { CrystalDTO(it.id.value, it.location) }
                .toList()
        }
    }

    suspend fun getAcquiredCrystals(playerId: UUID): Set<CrystalDTO> {
        return newSuspendedTransaction {
            val collectedCrystalIds = CollectedCrystal.findCrystalIdsByPlayerId(playerId)

            Crystal.find { CrystalTable.id inList collectedCrystalIds }
                .notForUpdate()
                .map { CrystalDTO(it.id.value, it.location) }
                .toSet()
        }
    }

    suspend fun createCrystal(player: Player) {
        val location = player.location

        val evasionLocation = EvasionLocation(
            Settings.CLUSTER_ID,
            location.world!!.name,
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch,
        )

        newSuspendedTransaction {
            val crystal = Crystal.create(evasionLocation)

            withContext(Dispatchers.BukkitMain) {
                val modelId = modelEngineHook.spawnModel(location, Settings.CRYSTAL_MODEL_ID)
                modelEngineHook.showModelForPlayer(player, modelId)
                crystalIdByModelIdMap[modelId] = crystal.id.value
            }

            player.sendColorizedMessage("&a크리스탈을 성공적으로 설치했습니다.")
        }
    }

    suspend fun tryCollectCrystal(player: Player, modelId: UUID): Int {
        val crystalId = crystalIdByModelIdMap[modelId] ?: return -1

        if (player.isOp && Settings.DEBUG) {
            player.sendColorizedMessage("&7[DEBUG] $crystalId 크리스탈 수집 시도")
            return -1
        }

        val playerCollectedCrystals = newSuspendedTransaction {
            CollectedCrystal.findCrystalIdsByPlayerId(player.uniqueId)
        }

        if (crystalId in playerCollectedCrystals) {
            return -1
        }

        newSuspendedTransaction {
            CollectedCrystal.create(player.uniqueId, crystalId)
        }

        withContext(Dispatchers.BukkitMain) {
            modelEngineHook.hideModelForPlayer(player, modelId)
        }

        return playerCollectedCrystals.size + 1
    }

    suspend fun deleteCrystal(crystalId: Long) {
        newSuspendedTransaction {
            Crystal.findById(crystalId)?.delete()
        }
    }

    /**
     * 모든 크리스탈을 로드하고 모델을 스폰합니다.
     * **서버 시작 시 한 번만 호출되어야합니다.**
     */
    suspend fun loadAllCrystals() {
        if (isInitialized) return

        val crystals = newSuspendedTransaction {
            Crystal.all().filter { it.location.clusterId == Settings.CLUSTER_ID }
        }

        crystalIdByModelIdMap.clear()
        crystalLocationByModelIdMap.clear()

        crystals.forEach { crystal ->
            val location = crystal.location.toBukkitLocation()
            val modelId = modelEngineHook.spawnModel(location, Settings.CRYSTAL_MODEL_ID)
            crystalIdByModelIdMap[modelId] = crystal.id.value
            crystalLocationByModelIdMap[modelId] = location
        }

        isInitialized = true
    }

    suspend fun initCrystalModelsForPlayer(player: Player) {
        val collectedCrystalIds = newSuspendedTransaction {
            CollectedCrystal.findCrystalIdsByPlayerId(player.uniqueId)
        }

        withContext(Dispatchers.BukkitMain) {
            crystalIdByModelIdMap.forEach { (modelId, crystalId) ->
                if (collectedCrystalIds.contains(crystalId) && (!player.isOp || !Settings.DEBUG)) {
                    modelEngineHook.hideModelForPlayer(player, modelId)
                } else {
                    modelEngineHook.showModelForPlayer(player, modelId)
                }
            }
        }
    }

    fun startNearbyNotification(player: Player) {
        stopNearbyNotification(player)
        notifiedCrystalsByPlayer[player.uniqueId] = mutableSetOf()

        val job = coroutineScope.launch(Dispatchers.BukkitAsync) {
            while (player.isOnline) {
                checkAndNotifyNearbyCrystals(player)
                bukkitDelay(NEARBY_CHECK_INTERVAL_TICKS)
            }
        }

        nearbyCheckJobByPlayer[player.uniqueId] = job
    }

    fun stopNearbyNotification(player: Player) {
        nearbyCheckJobByPlayer.remove(player.uniqueId)?.cancel()
        notifiedCrystalsByPlayer.remove(player.uniqueId)
    }

    private suspend fun checkAndNotifyNearbyCrystals(player: Player) {
        val playerLocation = player.location
        val notifiedCrystals = notifiedCrystalsByPlayer[player.uniqueId] ?: return

        val collectedCrystalIds = newSuspendedTransaction {
            CollectedCrystal.findCrystalIdsByPlayerId(player.uniqueId)
        }

        crystalLocationByModelIdMap.forEach { (modelId, crystalLocation) ->
            if (crystalLocation.world != playerLocation.world) return@forEach

            val crystalId = crystalIdByModelIdMap[modelId] ?: return@forEach

            if (crystalId in collectedCrystalIds) return@forEach
            if (crystalId in notifiedCrystals) return@forEach

            val distance = playerLocation.distance(crystalLocation)
            if (distance <= NEARBY_DETECTION_DISTANCE) {
                notifiedCrystals.add(crystalId)
                notifyNearbyCrystal(player, crystalId, distance)
            }
        }
    }

    private fun notifyNearbyCrystal(player: Player, crystalId: Long, distance: Double) {
        player.sendColorizedMessage("&f珂 &7주변에 &b대쉬 크리스탈&7이 있습니다!")
    }
}