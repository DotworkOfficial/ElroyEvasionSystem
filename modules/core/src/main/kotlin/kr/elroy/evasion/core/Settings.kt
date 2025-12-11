package kr.elroy.evasion.core

import kr.elroy.library.yaml.YamlConfig
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

object Settings {
    private val yaml by lazy {
        YamlConfig.createWithDefault(plugin, "settings.yml").also {
            it.load()
        }
    }

    private val plugin: Plugin by lazy {
        Bukkit.getPluginManager().getPlugin("ElroyEvasionSystem")
            ?: throw IllegalStateException("ElroyEvasionSystem plugin not found")
    }

    fun reload() {
        yaml.load()
    }

    val CLUSTER_ID get() = yaml.findString("ClusterId")!!
    val CRYSTAL_MODEL_ID get() = yaml.findString("CrystalModelId")!!
}