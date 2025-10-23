plugins {
    id("java")
    id("org.jetbrains.intellij.platform.module")
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()
val intellijBuildVersion = providers.gradleProperty("intellijBuildVersion").get()


dependencies {
    implementation(project(":common"))
    implementation(project(":agentic"))

    intellijPlatform {
        intellijIdeaUltimate(intellijBuildVersion)
        instrumentationTools()

    }

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    implementation("com.alibaba:fastjson:1.2.83")

}

