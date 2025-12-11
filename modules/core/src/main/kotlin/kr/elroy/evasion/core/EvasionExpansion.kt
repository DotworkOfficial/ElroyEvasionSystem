package kr.elroy.evasion.core

import kr.elroy.evasion.core.service.EvasionService
import kr.hqservice.framework.global.core.component.Singleton
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

@Singleton
class EvasionExpansion(
    private val evasionService: EvasionService,
) : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "evasion"
    }

    override fun getAuthor(): String {
        return "ElroyKR"
    }

    override fun getVersion(): String {
        return "1.0.0-SNAPSHOT"
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null || !player.isOnline) return null

        return when (params) {
            "max_evasion_count" -> evasionService.getMaxEvasionCountFromCache(player.player!!).toString()

            "cur_evasion_count" -> evasionService.getAvailableEvasionCount(player.player!!).toString()

            else -> return null
        }
    }

}