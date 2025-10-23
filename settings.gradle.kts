import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform


plugins {
    id("org.jetbrains.intellij.platform.settings") version "2.5.0"
}
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {

        mavenCentral()

        intellijPlatform {
            defaultRepositories()
        }
    }
}


rootProject.name = "CodeLink"
include("common")
include("agentic")
include("notepad")
include("rules")
include("mcp")
