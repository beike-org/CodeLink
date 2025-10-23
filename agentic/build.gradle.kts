plugins {
    id("java")
    id("org.jetbrains.intellij.platform.module")
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()
val intellijBuildVersion = providers.gradleProperty("intellijBuildVersion").get()


dependencies {
    implementation(project(":common"))

    intellijPlatform {
        intellijIdeaUltimate(intellijBuildVersion)
        instrumentationTools()
    }


    implementation("org.reflections:reflections:0.10.2")
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    implementation("com.alibaba:fastjson:1.2.83")
    implementation("cn.hutool:hutool-all:5.8.21")


    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    implementation("org.nanohttpd:nanohttpd:2.3.1")

}
