plugins {
    id("elroy.shared")
}

dependencies {
    compileOnly(libs.paper.api)

    compileOnly(framework.core)
    compileOnly(framework.inventory)
    compileOnly(framework.command)
    compileOnly(framework.database)

    api(project(":modules:api"))
}