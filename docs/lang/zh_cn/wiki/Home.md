# Ponder 1.12.2 JSON Wiki

Ponder for 1.12.2 是 Ponder 教程系统面向 Cleanroom / CRL 的回移版本。
它提供场景注册、游戏内浏览器、JSON 场景、GUI 叠层，以及 Forge 专用服务端适配 shim。

建议阅读顺序：

1. [快速开始](zh_cn-JSON-Quick-Start)
2. [场景与字段参考](zh_cn-JSON-Scene-Reference)
3. [GUI 与交互参考](zh_cn-JSON-GUI-Reference)
4. [完整示例](zh_cn-JSON-Examples)

## 当前能力

- 部件、标签、场景、本地化注册
- 游戏内 Ponder 浏览器和展示界面
- `/ponder`、`/ponderui`、`/ponderuidebug` 入口
- 通过 `mods.ponder.SceneFiles` 加载外部 JSON 场景
- 静态 GUI 贴图、已注册 GUI 快照、真实方块/机器 GUI 捕获
- Forge 1.12.2 专用服务端兼容 shim

## 构建产物

- 完整客户端/运行时 jar：`gradlew.bat reobfJar`
- Forge 服务端 shim jar：`gradlew.bat forgeServerShimJar`

客户端使用完整 jar，stock Forge 专用服务端使用 shim jar。
