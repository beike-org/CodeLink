# CodeLink
CodeLink是一个自托管、开源的AI 编码助手，为用户提供媲美Cursor Agent的开发体验，提供 GitHub Copilot插件的本地化替代方案。

## 核心特性
1. ChatBot模式：智能编程问答，解决各类编程问题，包括：代码重构与优化、代码分析与诊断、Bugfix等
2. 本地Agent模式：Agent自主规划，端到端解决问题，MCPTools\Rules\PastChats\Files\Notepad等各种上下文工具一站配齐。
3. 远程Agent模式（陆续开放）：将开发任务委派至云端沙箱中的编程Agent异步执行，支持多任务并行，随时随地观察执行进展，一键生成PR。

## 技术特性
1. 基于IntelliJ Platform SDK开发
2. 模块化设计，各功能模块职责明确
3. 多进程设计，保证IDE稳定性
4. 部分能力以Webview实现，灵活升级，发版简单。关联的webview项目，详见https://github.com/beike-org/codelink-webview 

## 项目代码结构
### Common模块
Common模块是核心基础组件模块，为其他功能模块提供基础设施和共享功能。

- **WebView支持**：WebView管理和双向通信机制
- **编辑器增强**：代码编辑器功能扩展和事件监听
- **工具窗口**：自定义工具窗口和通信机制
- **丰富工具类**：提供各类实用工具方法

### Agentic模块
Agentic模块是核心基础模块，负责管理和协调IDE与外部代理进程之间的通信。

- **代理进程管理**：完整生命周期管理和自动端口分配
- **Socket通信**：基于NanoHTTPD的轻量级HTTP Socket服务器
- **文件操作**：高性能文件读写和变更监听
- **诊断功能**：代码智能诊断和问题检测

### MCP模块
MCP (Model Context Protocol) 模块负责管理和处理模型上下文协议服务。

- **MCP服务配置管理**：添加、编辑、删除服务配置
- **服务通信**：基于HTTP协议的双向通信
- **UI交互**：直观的服务配置界面和状态可视化

### Rules模块
Rules模块负责管理和处理AI模型的规则配置。

- **规则配置管理**：全局规则和项目级规则管理
- **规则类型**：手动规则、始终规则和指定类型规则
- **规则存储**：全局和项目级规则的存储机制
- **用户界面**：直观的规则配置和管理界面

### Notepad模块
Notepad模块提供记事本编辑和管理功能。

- **记事本管理**：创建、编辑和删除记事本
- **记事本集成**：通过@notepad指令在对话中引用内容
- **文件存储**：以Markdown格式存储记事本内容

## 系统要求
- IntelliJ IDEA 2023.1+
- JDK 17+

## 快速开始
### 方式一：通过zip包安装（推荐）
1. 下载最新版本的插件zip包
2. 打开IDEA，进入 `Settings/Preferences` -> `Plugins`
3. 点击齿轮图标 ⚙️，选择 `Install Plugin from Disk...`
4. 选择下载的zip包文件
5. 点击 `OK` 并重启IDEA
6. 通过工具窗口配置个人API Provider和API Key

### 方式二：开发者模式运行
1. 克隆项目到本地
2. 使用Gradle导入项目
3. 运行 `runIde` 任务启动开发实例
4. 通过工具窗口配置个人API Provider和API Key

## 常见问题解答 (FAQ)
### 故障排除
#### 当前IDE不支持JCEF，无法正常使用插件？
1. Help -> Find Action
2. 搜索 boot runtime，找到「Choose Boot Java Runtime for the IDE」选项
3. 在「New」中，选择任何一个带有 JCEF最大版本 的 Runtime
4. 等待Runtime替换完成,重启IDE

### 隐私与安全
#### 如何确保API密钥安全？
1. API密钥存储在用户本地
2. 不会上报您的任何个人信息，请放心使用

## 许可证

[LICENSE](LICENSE)
