# Common 模块

Common模块是CodeLink项目的核心基础组件模块，为其他功能模块提供了基础设施和共享功能。作为项目的基础模块，它提供了一系列通用组件和服务，确保其他模块能够高效、一致地工作。

## 主要功能

### 1. WebView 支持

- 提供WebView管理和通信功能（WebViewManager）
- 支持面板工厂创建和配置（WebviewPanelFactory）
- 实现IDE与WebView之间的双向通信机制
- WebView项目配置和环境管理

### 2. 编辑器增强

- 代码编辑器功能扩展
- 行标记器（linemarker）支持
- 自定义编辑器动作
- 编辑器事件监听
- 编辑器焦点文件监听

### 3. 工具窗口

- 自定义工具窗口组件
- 工具窗口通信机制
- 内容管理和展示
- 工具窗口动作支持

### 4. 服务集成

- 通知服务（notify）
- 大模型服务（llm）

### 5. 工具类

- PSI工具类（代码结构解析）
- 文件处理工具（FileUtil）
- JSON处理工具（JsonUtil）
- 应用程序工具（ApplicationUtil）
- 主题工具（ThemeUtils）
- 进程工具（ProcessUtil）
- 本地化工具（LocalUtil）
- 运行环境工具（RuntimeEnvUtil）
- 组件工具（ComponentUtil）
- 差异比较工具（DiffUtil）
- 窗口工具（WindowUtil）
- 存储工具（StoreUtil）
- 图标工具（IconUtil）
- 路径工具（PathUtils）
- 插件工具（PluginUtil）
- 编辑器工具（EditorUtil）
- 其他通用工具类

### 6. 其他功能

- 差异比较（diff）支持
- 搜索功能
- 设置管理
- 异常处理
- UI组件和工具
- 指示器功能
- 堆栈跟踪分析

## 项目结构

```
common/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ke/
│   │   │       ├── diff/                       # 差异比较功能
│   │   │       ├── editor/                     # 编辑器功能
│   │   │       ├── exception/                  # 异常处理
│   │   │       ├── search/                     # 全局提问功能
│   │   │       ├── service/                    # 各类服务实现
│   │   │       ├── setting/                    # 设置管理
│   │   │       ├── stacktrace/                 # 堆栈跟踪分析
│   │   │       ├── toolwindow/                 # 工具窗口功能
│   │   │       ├── ui/                         # UI组件和工具
│   │   │       ├── utils/                      # 工具类
│   │   │       └── webview/                    # WebView相关功能
│   │   │       └── BaseAction                  # Action基类
│   │   │       └── Bundle                      # 动态加载文本资源
│   │   │       └── RetryTemplate               # 轮询重试模版
│   │   │       └── StartupActivityHandler      # 初始化行为扩展
│   │   └── resources/
│   │       ├── icons/           # 图标资源
│   │       └── messages/        # 国际化资源
│   ├── 23x/                     # IDE 2023.x兼容代码
│   └── 24x/                     # IDE 2024.x兼容代码
│   └── 24x/                     # IDE 2024.x兼容代码
│   
├── webview/    #CodeLink Toolwindow静态页面            
│   
```

## 技术栈

- Java
- IntelliJ Platform SDK
- Jackson JSON处理
- Retrofit2 HTTP客户端
- NanoHTTPD

## 扩展点

模块提供了以下扩展点：

- `com.ke.codelink.startupActivityHandler`: 项目启动时的初始化行为扩展点
- `com.ke.stacktrace.filter.ErrorAIExplainFilterFactory`: 扩展错误解释的过滤器以便实现不同的语言栈之间的兼容
- `com.ke.stacktrace.meta.StackTraceInfoProvider`: 根据语言栈注册不同的堆栈信息获取器
- `com.ke.editor.linemarker.action.group.LineMarkerGroupAdapter`: 根据不同的语言注册不同的代码行前Action
- `com.ke.webview.communication.handler.KeCopilotPanelHandler`: 根据不同的webview页面注册不同的消息处理器
- `com.ke.webview.WebviewPanelFactory`: 注册webview页面到WebviewManager
- `com.ke.editor.EditorPopupActionFactory`: 注册Action到选择代码时的EditorPopup
- `com.ke.toolwindow.actions.ToolWindowActionFactory`: 注册Action到CodeLink ToolWindow上方导航栏

## 依赖管理

使用Gradle进行依赖管理，支持：

- IntelliJ IDEA Ultimate版本兼容
- Python、Go、PHP等语言插件支持
- Git4Idea等内置插件集成

## 作为基础模块的重要性

作为CodeLink项目的基础模块，Common模块提供了其他功能模块所需的核心基础设施：

1. **统一接口和抽象**：提供标准化的接口和抽象，确保各功能模块之间的一致性和互操作性
2. **webview通信机制**：提供webview的接入和扩展机制
3. **共享功能**：实现常用功能，避免代码重复，提高开发效率
4. **跨模块通信**：提供模块间通信的机制，使不同功能模块能够协同工作
5. **IDE集成**：处理与IntelliJ平台的底层集成，简化其他模块的开发
6. **版本兼容**：通过23x和24x目录结构，确保在不同IDE版本上的兼容性