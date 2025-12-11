plugins {
    id("elroy.shared")
}

dependencies {
    compileOnly(libs.paper.api)

    compileOnly(framework.core)
    compileOnly(framework.inventory)
    compileOnly(framework.command)
    compileOnly(framework.database)

    compileOnly(libs.modelengine)
    compileOnly(libs.placeholderapi)
    compileOnly(files("${rootProject.projectDir}/libs/ElroyLib-dist.jar"))

    api(project(":modules:api"))
}