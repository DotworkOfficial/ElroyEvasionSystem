package kr.elroy.evasion.core.service

import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.elroy.evasion.core.Settings
import kr.elroy.evasion.core.domain.CollectedCrystal
import kr.elroy.evasion.core.domain.Crystal
import kr.elroy.evasion.core.dto.EvasionLocation
import kr.elroy.evasion.core.hook.ModelEngineHook
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import kr.hqservice.framework.global.core.component.Service
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@Service
class CrystalService(
    private val modelEngineHook: ModelEngineHook,
) {
    @Volatile
    private var isInitialized = false
    private val crystalIdByModelIdMap = mutableMapOf<UUID, Long>()

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

        if (player.isOp) {
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

        crystals.forEach { crystal ->
            val location = crystal.location.toBukkitLocation()
            val modelId = modelEngineHook.spawnModel(location, Settings.CRYSTAL_MODEL_ID)
            crystalIdByModelIdMap[modelId] = crystal.id.value
        }

        isInitialized = true
    }

    suspend fun initCrystalModelsForPlayer(player: Player) {
        val collectedCrystalIds = newSuspendedTransaction {
            CollectedCrystal.findCrystalIdsByPlayerId(player.uniqueId)
        }

        withContext(Dispatchers.BukkitMain) {
            crystalIdByModelIdMap.forEach { (modelId, crystalId) ->
                if (collectedCrystalIds.contains(crystalId)) {
                    modelEngineHook.hideModelForPlayer(player, modelId)
                } else {
                    modelEngineHook.showModelForPlayer(player, modelId)
                }
            }
        }
    }
}