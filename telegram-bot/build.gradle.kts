plugins {
    base
}

// Interpreter used to create the virtualenv; override with -PpythonExe=python3.12 or similar.
val pythonExe = providers.gradleProperty("pythonExe").getOrElse("python")
val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val venvDir = layout.projectDirectory.dir(".venv")
val venvPython = venvDir.file(if (isWindows) "Scripts/python.exe" else "bin/python").asFile.path

val venv = tasks.register<Exec>("venv") {
    description = "Creates the Python virtualenv in .venv"
    group = "build setup"
    commandLine(pythonExe, "-m", "venv", venvDir.asFile.path)
    onlyIf { !file(venvPython).exists() }
}

val pipInstall = tasks.register<Exec>("pipInstall") {
    description = "Installs runtime and test dependencies into .venv"
    group = "build setup"
    dependsOn(venv)
    inputs.files("requirements.txt", "requirements-dev.txt")
    outputs.file(layout.buildDirectory.file("pip-install.stamp"))
    commandLine(venvPython, "-m", "pip", "install", "--disable-pip-version-check", "-q", "-r", "requirements-dev.txt")
    doLast {
        layout.buildDirectory.file("pip-install.stamp").get().asFile.apply {
            parentFile.mkdirs()
            writeText("installed")
        }
    }
}

val pytest = tasks.register<Exec>("pytest") {
    description = "Runs the bot's unit tests"
    group = "verification"
    dependsOn(pipInstall)
    commandLine(venvPython, "-m", "pytest", "-q")
}

tasks.check { dependsOn(pytest) }

tasks.register<Exec>("run") {
    description = "Runs the Telegram bot (long polling); reads settings from the environment or .env"
    group = "application"
    dependsOn(pipInstall)
    commandLine(venvPython, "-m", "coperti_bot")
    environment("PYTHONPATH", layout.projectDirectory.dir("src").asFile.path)
}

tasks.clean {
    delete(venvDir, ".pytest_cache")
}
