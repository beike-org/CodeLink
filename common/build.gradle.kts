plugins {
    id("java")
    id("org.jetbrains.intellij.platform.module")
    kotlin("jvm")
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()
val intellijBuildVersion = providers.gradleProperty("intellijBuildVersion").get()
var platformVersion = if (intellijBuildVersion < "2024") {
    "23x"
} else {
    "24x"
}

dependencies {

intellijPlatform {
        intellijIdeaUltimate(intellijBuildVersion)
        instrumentationTools()

        plugins(
            listOf(
                "Pythonid:241.14494.314",
                "org.jetbrains.plugins.go:241.14494.240",
                "com.jetbrains.php:241.18034.12",
            )
        )

        bundledPlugins(
            "org.jetbrains.plugins.terminal",
            "Git4Idea",
            "com.intellij.java",
            "org.jetbrains.idea.maven",
            "com.intellij.spring",
            "org.jetbrains.plugins.yaml"
        )
    }

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
    implementation("org.reflections:reflections:0.10.2")
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    implementation("com.alibaba:fastjson:1.2.83")
    implementation("cn.hutool:hutool-all:5.8.21")

    implementation("org.apache.commons:commons-lang3:3.12.0")



}



sourceSets {
    main {
        java.srcDirs("src/$platformVersion/main/java")
    }
}