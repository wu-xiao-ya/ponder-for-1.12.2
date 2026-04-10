# Ponder 1.12.2 回移分支

`crl_ponder` 是一个面向 `Minecraft 1.12.2` 的
[Ponder](https://github.com/Creators-of-Create/Ponder) 回移工作区，
运行目标为 `Cleanroom / CRL`。

这个仓库不是对上游 Ponder 的直接重编译。为了适配 1.12.2 生态，
它包含了对 UI、渲染、运行时行为和兼容层的大量重写。

## 当前状态

这个项目已经可用，但仍处在持续回移与稳定化阶段。

当前重点：

- 保证 1.12.2 分支在日常使用上足够稳定
- 继续补齐底层 runtime 缺口
- 避免把测试内容作为长期公开接口的一部分

## 当前能力

当前分支已经具备：

- 组件、标签、场景、本地化注册
- 1.12.2 可用的 Ponder 浏览器与展示 UI
- `/ponder`、`/ponderui` 及相关客户端 / 调试入口
- 通过 `mods.ponder.SceneFiles` 加载外部 JSON 场景
- 一套可复用交互定义的 1.12.2 JSON DSL
- 部分界面的 live GUI snapshot 支持
- Ponder UI 下的 JEI / HEI 兼容处理
- 持续扩展中的 world、overlay、particle、GUI runtime 支持

## 当前范围

当前分支目标：

- Minecraft `1.12.2`
- Cleanroom / CRL runtime

当前 **不** 以原版 Forge 1.12.2 兼容为目标。

## 主要限制

相比现代高版本上游 Ponder，当前最大缺口仍然在：

- 完整 instruction 体系
- element / world-section 运行时模型
- 与高版本渲染表现的长期一致性

## 构建

这个工作区运行 Gradle 需要现代版本 JDK。

常用命令：

```bat
gradlew.bat compileJava
gradlew.bat reobfJar
```

如果你的机器默认仍然是 Java 8，请看：

- [开发环境说明](development_setup.md)

## 文档

- [回移说明](../../PORTING_NOTES.md)
- [JSON 教程](json_tutorial.md)
- [Wiki 源文件](wiki/Home.md)

## 仓库结构

- `src/` 源码与随仓库分发的资源
- `run/scripts/` 本地场景示例与加载器
- `docs/` 教程与 wiki 内容

## 开发者说明

本地编辑器配置和开发流程细节已经移出公共根 README。

- [开发环境说明](development_setup.md)
