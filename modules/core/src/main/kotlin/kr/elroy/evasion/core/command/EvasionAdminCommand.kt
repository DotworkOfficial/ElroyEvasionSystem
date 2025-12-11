package kr.elroy.evasion.core.command

import kr.elroy.evasion.core.Settings
import kr.elroy.evasion.core.service.CrystalService
import kr.elroy.evasion.core.service.EvasionService
import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import kr.hqservice.framework.command.Command
import kr.hqservice.framework.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Command(label = "evasionadmin", isOp = true)
class EvasionAdminCommand(
    private val crystalService: CrystalService,
    private val evasionService: EvasionService,
) {
    @CommandExecutor(label = "create")
    suspend fun createCrystal(player: Player) = crystalService.createCrystal(player)

    @CommandExecutor(label = "delete")
    suspend fun deleteCrystal(sender: CommandSender, crystalId: Long) {
        crystalService.deleteCrystal(crystalId)
    }

    @CommandExecutor(label = "set")
    suspend fun set(sender: CommandSender, target: Player, amount: Int) {
        evasionService.setMaxEvasionCount(target, amount)
    }

    @CommandExecutor(label = "info")
    suspend fun info(sender: CommandSender, target: Player) {
        sender.sendColorizedMessage("${evasionService.getMaxEvasionCount(target)} 개")
    }

    @CommandExecutor(label = "toggle")
    fun toggle(sender: CommandSender, target: Player) {
        if (evasionService.toggleEvasion(target)) {
            target.sendColorizedMessage("&a회피기가 활성화되었습니다.")
        } else {
            target.sendColorizedMessage("&a회피기가 비활성화되었습니다.")
        }
    }

    @CommandExecutor(label = "reload")
    fun reload(sender: CommandSender) {
        Settings.reload()
    }
}