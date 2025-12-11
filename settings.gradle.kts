@file:Suppress("UnstableApiUsage")

rootProject.name = getProperty("projectName")

pluginManagement {
    val kotlinVersion: String by settings
    val shadowVersion: String by settings

    plugins {
        kotlin("plugin.serialization") version kotlinVersion apply false
        kotlin("kapt") version kotlinVersion apply false
        id("com.gradleup.shadow") version shadowVersion apply false
        id("kr.hqservice.resource-generator.bukkit") version "1.0.0" apply false
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.hqservice.kr/repository/maven-public/")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.hqservice.kr/repository/maven-public/")
        maven("https://maven.hqservice.kr/repository/maven-private/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://mvn.lumine.io/repository/maven-public/")
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            library("spigot-api", "org.spigotmc:spigot-api:${getProperty("spigotVersion")}")
            library("paper-api", "io.papermc.paper:paper-api:${getProperty("spigotVersion")}")
            library("modelengine", "com.ticxo.modelengine:ModelEngine:R4.0.4")
            library("placeholderapi", "me.clip:placeholderapi:2.11.5")
        }

        create("framework") {
            library("core", "kr.hqservice:hqframework-bukkit-core:${getProperty("hqFrameworkVersion")}")
            library("command", "kr.hqservice:hqframework-bukkit-command:${getProperty("hqFrameworkVersion")}")
            library("nms", "kr.hqservice:hqframework-bukkit-nms:${getProperty("hqFrameworkVersion")}")
            library("inventory", "kr.hqservice:hqframework-bukkit-inventory:${getProperty("hqFrameworkVersion")}")
            library("database", "kr.hqservice:hqframework-bukkit-database:${getProperty("hqFrameworkVersion")}")
        }
    }
}

includeBuild("build-logic")
includeAll("modules")

fun includeAll(modulesDir: String) {
    file("${rootProject.projectDir.path}/${modulesDir.replace(":", "/")}/").listFiles()?.forEach { modulePath ->
        include("${modulesDir.replace("/", ":")}:${modulePath.name}")
    }
}

fun getProperty(key: String): String {
    return extra[key]?.toString() ?: throw IllegalArgumentException("property with $key not found")
}
