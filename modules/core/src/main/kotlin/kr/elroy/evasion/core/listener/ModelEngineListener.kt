package kr.elroy.evasion.core.listener

import com.ticxo.modelengine.api.events.ModelRegistrationEvent
import com.ticxo.modelengine.api.generator.ModelGenerator
import kr.elroy.evasion.core.service.CrystalService
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe

@Listener
class ModelEngineListener(
    private val crystalService: CrystalService,
) {
    @Subscribe
    suspend fun onModelEngineLoaded(event: ModelRegistrationEvent) {
        if (event.phase != ModelGenerator.Phase.FINISHED) {
            return
        }

        crystalService.loadAllCrystals()
    }
}