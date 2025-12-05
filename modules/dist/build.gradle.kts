plugins {
    id("elroy.shared")
    id("elroy.shadow")
    id("kr.hqservice.resource-generator.bukkit")
}

bukkitResourceGenerator {
    main = "kr.elroy."
    name = ""
    apiVersion = "1.20"
    libraries = excludedRuntimeDependencies()
    depend = listOf("HQFramework")
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(framework.core)
    runtimeOnly(project(":modules:core"))
    runtimeOnly(project(":modules:api"))
}
