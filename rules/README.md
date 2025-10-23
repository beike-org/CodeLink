# Rules模块

## 简介

Rules模块是CodeLink插件的重要组件之一，主要负责管理和处理AI模型的规则配置。
该模块提供了一套完整的规则管理机制，包括全局规则和项目级规则的配置、存储和应用，使得用户能够更好地控制AI模型的行为。

全局规则在所有项目中都生效，项目规则只作用于当前项目。全局规则配置后会自动在对话过程中带入到上下文中。
项目规则分为三种类型：手动、始终和指定类型。

- 手动规则（manual）：用户可以使用@Rules指令来选择哪个规则需要带到上下文中
- 始终规则（always）：会在对话中自动带入到上下文中
- 指定类型规则（specified_type）：用户可以配置文件过滤的通配符，在对话中使用@Files指令时只会检索出指定类型的文件带到上下文中

## 功能特性

- 规则配置管理
    - 支持全局规则和项目级规则
    - 提供规则的添加、编辑、删除功能
    - 支持规则的实时生效
- 规则类型
    - 手动规则（manual）：通过@Rules指令手动选择带入对话上下文
    - 始终规则（always）：自动带入对话上下文
    - 指定类型规则（specified_type）：配置文件过滤通配符，与@Files指令配合使用
- 规则存储
    - 全局规则：存储在IDE配置目录下的.idea/global_rules.txt
    - 项目规则：存储在项目目录下的.idea/project_rules.json

## 核心组件

### 1. 规则管理

- ProjectRuleManager：项目规则管理器，负责规则的读取、保存、编辑和删除
- RuleType：规则类型枚举，定义了手动、始终和指定类型三种规则类型

### 2. 数据模型

- ProjectRuleDTO：项目规则数据传输对象，包含名称、内容、类型和正则表达式列表
- RuleInfoDTO：规则信息数据传输对象，用于前端展示

### 3. UI组件

- RuleConfigurationComponent：规则配置主界面
- RuleConfigurationConfigurable：规则配置设置项
- GlobalRuleDialog：全局规则对话框
- ProjectRuleDialog：项目规则对话框

### 4. WebView处理程序

- ManualRuleListWTPHandler：获取手动规则列表
- RuleInfoWTPHandler：获取规则信息
- AddProjectRulesWTPHandler：添加项目规则处理器
- DefaultFileListWTPHandler：获取默认文件列表

## 实现细节

### 文件存储

- 全局规则：存储在`PathManager.getConfigPath()/.idea/global_rules.txt`，以纯文本形式保存
- 项目规则：存储在`project.getBasePath()/.idea/project_rules.json`，以JSON格式保存

### 规则类型

1. 手动规则（MANUAL）
    - 用户需要通过@Rules指令手动选择规则
    - 适用于特定场景的规则，不需要始终应用

2. 始终规则（ALWAYS）
    - 在对话中自动带入上下文
    - 适用于需要始终应用的规则

3. 指定类型规则（SPECIFIED_TYPE）
    - 配置文件过滤的通配符
    - 与@Files指令配合使用，只检索出指定类型的文件

### 用户界面

规则模块提供了直观的配置界面，用户可以：

- 添加、编辑和删除项目规则
- 配置全局规则
- 启用或禁用全局规则

## 使用示例

1. 添加项目规则：

```java
ProjectRuleDTO rule = new ProjectRuleDTO();
rule.setName("代码风格规则");
rule.setContent("请遵循Google Java代码风格指南");
rule.setType(RuleType.ALWAYS);
project.getService(ProjectRuleManager.class).saveProjectRules(rule);
```

2. 设置全局规则：

```java
String globalRule = "始终以中文输出答案";
project.getService(ProjectRuleManager.class).saveAppRule(globalRule);
```

3. 添加指定类型规则：

```java
ProjectRuleDTO rule = new ProjectRuleDTO();
rule.setName("Java文件规则");
rule.setContent("只分析Java文件");
rule.setType(RuleType.SPECIFIED_TYPE);
rule.setRegex(List.of("*.java"));
project.getService(ProjectRuleManager .class).saveProjectRules(rule);
```

## 开发指南

### 扩展规则类型

如需添加新的规则类型：

1. 在RuleType枚举中添加新类型

```java
MODEL_DECIDE("model_decide","模型决定")
```

2. 在相关处理程序中添加对新类型的支持
3. 更新UI组件以支持新类型

### 增强规则功能

1. 在ProjectRuleManager中添加新的规则处理方法
2. 实现对应的UI交互
3. 添加相应的WebView处理程序

## 注意事项

- 全局规则字符数限制为6000
- 规则修改后会实时保存
- 确保规则的格式正确性
- 项目规则存储在项目目录下，与项目绑定