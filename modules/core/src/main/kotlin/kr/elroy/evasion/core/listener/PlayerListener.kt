package kr.elroy.evasion.core.listener

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.ticxo.modelengine.api.events.BaseEntityInteractEvent
import kr.elroy.evasion.core.service.CrystalService
import kr.elroy.evasion.core.service.EvasionService
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot

@Listener
class PlayerListener(
    private val crystalService: CrystalService,
    private val evasionService: EvasionService,
) {
    @Subscribe
    suspend fun onPlayerRightClickBaseEntity(event: BaseEntityInteractEvent) {
        if (event.action != BaseEntityInteractEvent.Action.INTERACT) {
            return
        }

        if (event.slot != EquipmentSlot.HAND) {
            return
        }

        val collectedCrystalCount = crystalService.tryCollectCrystal(event.player, event.baseEntity.uuid)

        if (collectedCrystalCount != -1) {
            evasionService.tryAcquireEvasion(event.player, collectedCrystalCount)
        }
    }

    @Subscribe
    fun onShiftF(event: PlayerJumpEvent) {
        if (event.player.isSneaking) {
            evasionService.executeEvasion(event.player)
        }
    }

    @Subscribe
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        evasionService.loadToCache(event.player)
        crystalService.initCrystalModelsForPlayer(event.player)
    }

    @Subscribe
    suspend fun onPlayerWorldChange(event: PlayerChangedWorldEvent) {
        evasionService.removeFromCache(event.player)
        crystalService.initCrystalModelsForPlayer(event.player)
    }
}