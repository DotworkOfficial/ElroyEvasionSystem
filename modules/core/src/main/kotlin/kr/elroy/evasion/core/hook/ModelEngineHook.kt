package kr.elroy.evasion.core.hook

import com.ticxo.modelengine.api.ModelEngineAPI
import com.ticxo.modelengine.api.entity.Dummy
import java.util.UUID
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player

@Component
class ModelEngineHook {
    fun spawnModel(location: Location, modelId: String): UUID {
        val dummy = Dummy<ArmorStand>()

        dummy.location = location
        dummy.isDetectingPlayers = false

        val modeledEntity = ModelEngineAPI.createModeledEntity(dummy)
        val activeModel = ModelEngineAPI.createActiveModel(modelId)

        modeledEntity.addModel(activeModel, true)

        return dummy.uuid
    }

    fun hideModelForPlayer(player: Player, uuid: UUID) {
        findDummyByUUID(uuid).setForceHidden(player, true)
    }

    fun showModelForPlayer(player: Player, uuid: UUID) {
        findDummyByUUID(uuid).setForceViewing(player, true)
    }

    private fun findDummyByUUID(uuid: UUID): Dummy<*> {
        val modeledEntity = ModelEngineAPI.getModeledEntity(uuid)
            ?: throw IllegalArgumentException("No modeled entity found for UUID: $uuid")
        return modeledEntity.base as? Dummy<*>
            ?: throw IllegalArgumentException("Modeled entity is not a Dummy for UUID: $uuid")
    }
}