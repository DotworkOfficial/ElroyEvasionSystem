plugins {
    java
    id("com.gradleup.shadow")
}

tasks.shadowJar {
    archiveBaseName.set(project.rootProject.name)
    archiveClassifier.set("")
    archiveVersion.set("")

    if (project.rootProject.hasProperty("debugOutputDir")) {
        destinationDirectory.set(file("${project.rootProject.properties["debugOutputDir"]}"))
    } else {
        destinationDirectory.set(file(rootProject.projectDir.path + "/build_outputs"))
    }
}

configurations.runtimeClasspath.get().apply {
    exclude("org.jetbrains.kotlin", "*")
}