import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask

val intellijBuildVersion = providers.gradleProperty("intellijBuildVersion").get()
val ideType: String by project
val ideGoVersion: String by project
val idePycharmVersion: String by project

val phpStormVersion: String by project
val webStormVersion: String by project
var isTest = false


plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform")
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()


gradle.startParameter.taskNames.forEach { taskName ->
    if (taskName.contains("test")) {
        isTest = true
    }
}


//强制去掉slf4j,idea会自己引入。否则会有依赖冲突
configurations.all {
    resolutionStrategy {
        exclude("org.slf4j")
    }
}


dependencies {
    intellijPlatform {
        if (ideType == "pycharm" || ideType == "goland" || ideType == "phpstorm" || ideType == "webstorm") {
            if (ideType == "pycharm") {
                pycharmProfessional(idePycharmVersion)
            } else if (ideType == "goland") {
                goland(ideGoVersion)
            } else if (ideType == "phpstorm") {
                phpstorm(phpStormVersion)
            } else {
                webstorm(webStormVersion)
            }
        } else {
            when (intellijBuildVersion) {
                "2024.1" -> {
                    intellijIdeaUltimate("2024.1")
                }

                "2024.2" -> {
                    intellijIdeaUltimate("2024.2")
                }

                "2025.1" -> {
                    intellijIdeaUltimate("2025.1")
                }

                else -> {
                    intellijIdeaUltimate("2023.1")
                }
            }

//            bundledPlugins(
//                "Git4Idea",
//                "com.intellij.java",
//                "org.jetbrains.plugins.terminal",
//                "org.jetbrains.idea.maven",
//                "com.intellij.spring",
//                "org.jetbrains.plugins.yaml"
//            )

        }

        pluginModule(implementation(project(":common")))
        pluginModule(implementation(project(":rules")))
        pluginModule(implementation(project(":mcp")))
        pluginModule(implementation(project(":rules")))
        pluginModule(implementation(project(":notepad")))

        pluginVerifier()
        zipSigner()
        instrumentationTools()
        testFramework(TestFrameworkType.Platform)
    }

    implementation("com.alibaba:fastjson:1.2.83")
    implementation("org.apache.commons:commons-lang3:3.12.0")


    implementation(project(":common"))
    implementation(project(":rules"))
    implementation(project(":mcp"))
    implementation(project(":rules"))
    implementation(project(":notepad"))


    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

subprojects {
    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }
    }
}

tasks.withType<PrepareSandboxTask> {
    from("$projectDir/copilot/copilot-agent") { into("${rootProject.name}/copilot-agent") }
    from("$projectDir/agentic/agentic-bin") { into("${rootProject.name}/agentic-bin") }
    from("$projectDir/common/webview") { into("${rootProject.name}/webview") }
}


tasks {
    patchPluginXml {
        pluginId.set("com.ke.codelink")
        pluginName.set("CodeLink")
        pluginVersion.set(version.toString())
        pluginDescription.set(
            """
        <p><b>高耸的AI塔，悬挂着天空的银色月亮，让人感受到未来的无限可能。</b></p>
        <p>CodeLink将GPT和Copilot巧妙融入在一个插件的设计之中，让您的编码体验变得流畅和愉快：</p>
        <ul>
          <li><b></b> 在您编码时根据代码库和您的开发习惯提供快速、智能、高效的代码编写和自动补全功能。</li>
          <li><b></b> 可以通过问答随时解决您编码遇到的问题，也可以用于代码注释等帮您管理代码。</li>
        </ul>

        <p>CodeLink与你的组合必将摩擦出更多火花，也将催生出高级又标准的代码，快点开始体验吧</p>

        <h3>前置条件</h3>
        <p>为了使用插件，您需要点击最下方状态栏的图标进行登录</p>
        <h2>功能</h2>
        <ul>
          <li><b>工具窗口代码补全</b> - Copilot工具窗口提供多个补全选项</li>
          <li><b>对话GPT</b> - 问AI任何问题</li>
          <li><b>保存对话历史记录</b> - 可查看最近的对话历史记录并恢复以前的会话</li>
          <li><b>预定义prompt</b> - 包括解释代码，优化代码，生成单测等</li>
          <li><b>自定义prompt</b> - 为所选代码创建自定义提示词</li>
          <li><b>同时对话</b> - 在多个选项卡中与AI进行聊天</li>
        </ul>
        """.trimIndent()
        )
        changeNotes.set(
            """
            <ul>
                 <li>新增agentic模式</li>
                 <li>一些已知bug修复</li>
            </ul>
            """.trimIndent()
        )
        sinceBuild.set("220.*")
        untilBuild.set("254.*")
        if (isTest) {
            println("use test plugin.xml")
            inputFile.set(file("src/test/resources/META-INF/plugin.xml"))
        }

    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

}

tasks.runIde {
    // Always enable assertions.
    jvmArgs("-ea")

    // Copy over some JVM args from IntelliJ.
    jvmArgs("-XX:ReservedCodeCacheSize=240m")
    jvmArgs("-XX:+UseConcMarkSweepGC")
    jvmArgs("-XX:SoftRefLRUPolicyMSPerMB=50")
    jvmArgs("-XX:CICompilerCount=2")
    jvmArgs("-Djdk.module.illegalAccess.silent=true")
    jvmArgs("-XX:+UseCompressedOops")

}


val pluginLibs: Configuration by configurations.creating {
    extendsFrom(configurations.implementation.get())
}

sourceSets.all {
    compileClasspath = pluginLibs + compileClasspath
    runtimeClasspath = pluginLibs + runtimeClasspath
}

