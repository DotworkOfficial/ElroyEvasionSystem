package kr.elroy.evasion.core

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule

@Component
class ElroyEvasionSystemBootstrap(
    private val evasionExpansion: EvasionExpansion,
) : HQModule {
    override fun onEnable() {
        evasionExpansion.register()
        Settings.reload()
    }
}