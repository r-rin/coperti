import com.github.gradle.node.npm.task.NpmTask

plugins {
    base
    id("com.github.node-gradle.node") version "7.1.0"
}

node {
    version.set("24.19.0")
    download.set(true)
}

val npmBuild = tasks.register<NpmTask>("npmBuild") {
    description = "Builds the frontend"
    group = "application"
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
    inputs.files(fileTree(projectDir) {
        exclude("node_modules", ".next", ".gradle", "build")
    })
    outputs.dir(layout.projectDirectory.dir(".next/standalone"))
}

tasks.assemble { dependsOn(npmBuild) }

tasks.clean {
    delete(".next")
}

tasks.register<NpmTask>("runDev") {
    description = "Runs the frontend in development mode"
    group = "application"
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "dev"))
}