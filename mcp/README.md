# MCP模块

## 简介

MCP (Model Context Protocol) 模块是CodeLink插件的功能组件之一，主要负责管理和处理模型上下文协议服务。该模块提供了一套完整的服务配置、管理和通信机制，使得插件能够与各种MCP服务进行交互。

## 功能特性

- MCP服务配置管理
    - 支持添加、编辑、删除MCP服务配置
    - 提供服务状态监控和切换功能
    - 支持从远程市场获取MCP服务
- 服务通信
    - 基于PTS (Plugin-to-Sidecar)通信协议
    - 支持双向通信（WTP和PTW）
    - 提供可靠的服务状态同步机制
- UI交互
    - 提供直观的服务配置界面
    - 支持服务状态可视化
    - 集成市场服务浏览功能

## 核心组件

### 1. 配置管理

- McpConfigFileManager: 负责MCP配置文件的读写和管理
- McpConfigurationComponent: 提供MCP服务配置的UI组件
- McpConfigurationProvider: 配置服务提供者

### 2. 通信处理

- 通信分为两类，插件与Sidecar通信（PTS)，插件与Webview通信PTW（Plugin To Webview),WTP(Webview To Plugin)
- McpPTSHandler: 处理与MCP服务的PTS(Plugin To Sidecar)通信
- McpPTSApi: 定义MCP服务通信接口
- 各类WTPHandler: 处理各种与Webview的交互

### 3. UI组件

- McpMarketConfigurationDialog: MCP市场服务配置对话框
- McpSubPanelComponent: MCP服务子面板组件
- EditMcpServerDialog: MCP服务编辑对话框
- AddMcpServerDialog: 添加服务对话框

### 4. 事件监听

- McpConfigStartUpActivity: 项目启动读取配置文件
- McpProjectCloseListener: 项目关闭事件监听
- McpConfigurationUpdateListener: 配置更新事件监听

## 配置文件

MCP模块使用两个主要的配置文件：

- mcp.json: 存储MCP服务的基本配置信息
- mcp_available.json: 记录可用服务的状态信息

## 使用示例

1. 添加MCP服务：

```java
McpConfigDTO config = new McpConfigDTO();
config.setName("serviceName");
config.setType(McpTypeEnum.SSE);
mcpConfigFileManager.addOrUpdateConfig(config, true,true);
```

2. 切换服务状态(启用|禁用)：

```java
mcpConfigFileManager.switchConfig(
		serviceName,
		AvailableStatusEnum.ENABLED
		);
```

## 开发指南

1. 添加新的服务类型：
    - 在McpTypeEnum中添加新的类型
    - 创建对应的配置DTO类
    - 实现相应的处理逻辑

2. 扩展通信功能：
    - 与Sidecar的通信，在McpPTSApi中添加新的接口定义
    - 与Webview的通信，注册到McpHandler中，实现对应的Handler类

## 注意事项

- 服务配置修改后需要及时同步到配置文件
- 确保Sidecar和插件端服务状态的正确性和一致性
- 注意处理服务通信的异常情况