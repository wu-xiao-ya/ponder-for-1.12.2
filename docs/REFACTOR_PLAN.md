# Ponder 1.12.2 重构执行规划

日期：`2026-06-28`
分支：`codex/ponder-refactor-plan-20260624`
工作树：`E:\mc_modding\Ponder_refactor_plan`

## 1. 目标

这轮重构服务五个结果：

1. 把 `foundation/ui`、`foundation/external`、`foundation/registration` 变成可持续扩展的主骨架
2. 把 `PonderDebugScreen` 从巨型协调器继续压缩成页面编排层
3. 主动拥抱 `Java 25`，把 modern Java 用在最值钱的内部结构上
4. 明确项目真实身份，统一围绕 `Unimined + Cleanroom / CRL + Java 25`
5. 在独立工作树内推进，保持 `E:\mc_modding\Ponder` 主仓库现状不受影响

## 2. 当前平台身份

当前项目身份已经可以冻结：

- 构建链：`Unimined 1.4.16-kappa`
- 运行目标：`Cleanroom / CRL 0.5.6-alpha`
- Minecraft 基线：`1.12.2`
- 主源码语言：`Java 25`
- API 外壳：`Forge 1.12.2` 表面
- 兼容副产物：`forgeServerShimJar`

对应证据：

- `E:\mc_modding\Ponder_refactor_plan\build.gradle:7`
- `E:\mc_modding\Ponder_refactor_plan\build.gradle:42`
- `E:\mc_modding\Ponder_refactor_plan\build.gradle:70`
- `E:\mc_modding\Ponder_refactor_plan\build.gradle:74`
- `E:\mc_modding\Ponder_refactor_plan\build.gradle:305`
- `E:\mc_modding\Ponder_refactor_plan\README.md:5`
- `E:\mc_modding\Ponder_refactor_plan\PORTING_NOTES.md:5`

工程身份按下面这条主线表述：

```text
Unimined
  -> Cleanroom / CRL runtime
  -> Java 25 main sources
  -> Forge 1.12.2 API shell
  -> forgeServerShim compatibility artifact
```

`RFG` 在这份规划里只保留为历史对照词汇。当前 live 构建链属于 `Unimined`。

## 3. 当前 live 架构判断

### 3.1 UI 包现状

`foundation/ui` 当前已经形成 8 层稳定结构：

- `screen`
- `controller`
- `state`
- `render`
- `helper`
- `cache`
- `model`
- `compat`

关键结论：

- `PonderDebugScreen` 已降到约 `1795` 行，主职责继续向页面协调器收口
- `PonderSceneController`、`DebugMouseController`、`ShowcaseMouseController` 已承担输入和编排的一部分
- `LayoutCache`、`InteractionState`、`Snapshot.RenderCapability`、`PonderSceneRuntimeTypes.TransformStep` 已开始吃到 `record` 与 `sealed interface`
- `ScenePreviewRenderer`、`ShowcaseRenderer`、`ShowcaseHudRenderer`、`DebugPanelRenderer`、`SceneOverlayRenderer`、`GuiOverlayRenderer` 已形成 renderer 分层
- `componentScroll` 与 `operationScroll` 已由 `DebugPanelRenderer` 持有，`DebugPanelViewState` 已退场
- external 管线已拆成 `definition / parse / scan / register / execute / validate / compat`，执行端继续拆为 scene / world / overlay 分派器

对应参考：

- `E:\mc_modding\Ponder_refactor_plan\docs\UI_LIVE_ARCHITECTURE_ANALYSIS.md:1`
- `E:\mc_modding\Ponder_refactor_plan\src\main\java\net\createmod\ponder\foundation\ui\PonderDebugScreen.java:1`
- `E:\mc_modding\Ponder_refactor_plan\src\main\java\net\createmod\ponder\foundation\ui\DebugPanelRenderer.java:1`
- `E:\mc_modding\Ponder_refactor_plan\src\main\java\net\createmod\ponder\foundation\ui\LayoutCache.java:1`
- `E:\mc_modding\Ponder_refactor_plan\src\main\java\net\createmod\ponder\foundation\ui\InteractionState.java:1`

### 3.2 当前最大的三块技术债

#### A. `PonderDebugScreen` 仍偏胖

当前残留热点：

- 仍有少量直接 GL 状态操作，集中在语音框三角形和绘制桥接
- 匿名 Host 接线仍占据较多 screen 篇幅
- 仍保留部分 `isMouseOver*` 与 bounds 估算桥接
- 语音框几何仍留在 screen 尾部，可继续迁入 `SpeechRenderer`

#### B. Snapshot 体系过薄

当前残留热点：

- `SnapshotProvider` 已具备 `constant(...)` 便捷入口
- `SnapshotRenderer.GuiSnapshotRenderer` 已升级为 sealed 子接口
- `PonderGuiSnapshotRegistry` 已开始传递 block GUI tile NBT，失效和来源建模仍需继续强化
- 反射型 snapshot provider 仍然偏重

#### C. 构建层和兼容层重复

当前残留热点：

- `build.gradle` 与 `crl_ponder/build.gradle` 存在高重复
- `forgeServerShim` 与主源码有重合逻辑
- `mixins.ponder.json` 仍停在 `JAVA_16`
- CraftTweaker 依赖拉取方式偏临时

## 4. Java 25 技术方案

### 4.1 可以积极使用的能力

这轮重构优先把 `Java 25` 用在主源码内部结构。

优先使用：

- `record`
- `sealed interface` / `sealed abstract class`
- 现代 `switch`
- pattern matching `instanceof`
- `List.of()` / `Map.of()` / `Set.of()`
- `String.formatted()`
- text block
- `AutoCloseable` 风格守卫对象

优先落点：

- `foundation/ui`
- `foundation/external`
- `foundation/registration`
- `client`

### 4.2 使用边界

按三层边界执行：

```text
main runtime
  -> foundation/ui
  -> foundation/external
  -> foundation/registration
  -> client

api surface
  -> api
  -> catnip/math

forge server shim
  -> src/forgeServerShim
```

各层规则：

- `main runtime`
  - 积极使用 Java 25 现代特性
- `api surface`
  - 保持对第三方调用友好的稳定形状
- `forge server shim`
  - 维持单独兼容边界
  - 与主运行层隔离

### 4.3 谨慎项

这一轮先不把精力投到高风险新特性：

- virtual threads
- structured concurrency
- Foreign Function & Memory API
- preview 级语言特性

原因很直接：当前价值集中在结构升级，风险集中在 1.12.2 主线程模型和运行时兼容链。

### 4.4 运行时兼容校验点

第一轮真实施工前补一个固定验证项：

- `E:\mc_modding\Ponder_refactor_plan\src\main\resources\mixins.ponder.json:7` 当前还是 `JAVA_16`
- 需要确认 CRL Mixin 链对 Java 25 主类文件和更高 compatibility level 的实际容忍度

这个验证先于大面积引入新的 sealed 模型和更强 switch 分派。

## 5. 目标架构

### 5.1 UI 目标架构

UI 主链按下面的固定结构收口：

```text
screen orchestration
  -> controller
  -> state
  -> layout / projection
  -> renderer
  -> cache / hit regions
  -> compat
```

最终职责划分：

- `screen`
  - 页面编排
  - 输入分发
  - 页面级按钮与导航
- `controller`
  - 键盘与鼠标行为
  - scene/component/playback 操作编排
- `state`
  - selection
  - playback
  - camera
  - showcase
  - debug panel scroll
- `layout / projection`
  - preview layout
  - scene projection
  - overlay placement
- `renderer`
  - scene preview
  - overlay
  - showcase chrome
  - captions
  - debug panel
- `cache / hit regions`
  - layout snapshot
  - interaction state
  - hit region routing

### 5.2 External 目标架构

外部场景管线按单一主线推进：

```text
scan
  -> parse
  -> validate
  -> compile
  -> register
  -> execute
```

目标：

- JSON / CrT / Java helper 共用同一条编译管线
- `reload()` 只做编排
- definition、diagnostic、compiled bundle 三段清晰分离

### 5.3 Registration 目标架构

注册链按命令模型收口：

```text
plugin input
  -> registration command
  -> registry service
  -> finalized registry state
```

核心类型：

- `RegistrationCommand`
- `RegisterSceneCommand`
- `RegisterTagCommand`
- `RegisterSharedTextCommand`
- `LinkComponentTagCommand`
- `RegisterMatcherCommand`

## 6. 渲染与交互技术方案

### 6.1 RenderContext 路线

渲染抽象按统一 `RenderContext` 推进。

第一批能力：

- `push()` / `pop()`
- `pushScissor()` / `popScissor()`
- `translate()`
- `scale()`
- `rotate()`
- `fillRect()`
- `fillTexturedRect()`
- `fillTriangle()`
- `drawLine()`
- `renderItem()`
- `renderText()`

第一批实现：

- `GlRenderContext`
- `GLStateGuard`
- `SnapshotRenderContext`

目标：

- renderer 不再直接散落 `GlStateManager`
- 预览渲染、overlay 渲染、snapshot 渲染共享同一上下文风格

### 6.2 Projection 与 Overlay 路线

这一块直接吸收 GuideNova 一类项目的组织方式。

目标对象：

- `SceneProjectionContext`
- `ProjectedBounds`
- `OverlayPlacementEngine`
- `CaptionPlacement`
- `GuiOverlayPlacement`
- `GuiHighlightPlacement`

执行要求：

- `projectScenePoint()` 与 `projectSceneBounds()` 只有单一来源
- `SpeechRenderer.Point`、`SpeechRenderer.SpeechPointing`、`ProjectedBounds` 统一语言
- caption、GUI overlay、highlight 共用同一投影语义

### 6.3 动画路线

当前线性插值和硬编码 tick 常量，后续收口到：

- `AnimationSpec`
- `AnimationController`
- `EasingFunction`

优先覆盖：

- section fade
- camera rotate
- showcase 切换过渡
- actor 轻量动态效果

### 6.4 交互命中路线

输入与热区进一步统一到：

- `LayoutCache`
- `InteractionState`
- `HitRegionTree`
- `DragContext`

效果：

- 删除 screen 内散落的 `last*` 字段
- 删除重复 `isMouseOver*` 逻辑
- 新热区只需注册进统一命中树

### 6.5 Theme 路线

颜色系统从硬编码 `int` 收口为：

- `Theme`
- `SymbolicColor`
- `ThemeResolver`
- `ponder_themes.json`

首批语义色：

- `panel.background`
- `panel.border`
- `timeline.active`
- `timeline.inactive`
- `overlay.caption`
- `overlay.warning`
- `showcase.header`
- `showcase.popup`

## 7. 构建与兼容层整改方案

### 7.1 构建层

优先整改：

1. 合并主工程与 `crl_ponder` 的重复构建脚本
2. 把公共逻辑提到 `gradle/scripts/`
3. 统一 CraftTweaker 依赖获取方式
4. 明确 `mixin.refmap` 占位符来源
5. 升级 `foojay-resolver-convention`

### 7.2 `forgeServerShim`

定位继续明确为兼容边界层。

整改方向：

1. 主源码与 shim 公共部分尽量复用单一源码来源
2. shim 只保留确实需要的 server compatibility 逻辑
3. 校验是否继续保留 Java 8 release 约束
4. 文档明确 shim 的真实分发用途

### 7.3 测试与校验

这轮规划把测试从边缘事项提到正式骨架内。

第一批建议：

- 为 `api` 与 definition model 建最小单元测试
- 在 CI 中恢复 `test` 路径
- 把 `compileJava` 和 `forgeServerShimJar` 分成更清楚的验证门

## 8. 参考项目吸收点

### 8.1 GuideNova

最值得吸收的不是单一 API，而是三条方法论：

1. 数据模型、渲染模型、页面编排清晰分离
2. 内容与运行时之间保持更稳定的数据契约
3. 视觉效果和布局逻辑建立统一抽象，而不是散在 screen 内

### 8.2 Cleanroom 模板

最值得吸收的是工程身份和运行边界的一致性：

1. `Unimined + Cleanroom` 的单一路线表述
2. `Java 25` 作为主源码能力基线
3. 构建层、运行层、兼容层边界明确

## 9. 分阶段施工计划

### P0：继续压瘦 `foundation/ui`

#### P0-1：彻底收口 debug timeline scroll

状态：已完成首轮

- `operationScroll` 由 `DebugPanelRenderer` 单一持有
- screen 只保留必要访问桥接

重点位置：

- `E:\mc_modding\Ponder_refactor_plan\src\main\java\net\createmod\ponder\foundation\ui\PonderDebugScreen.java:1`
- `E:\mc_modding\Ponder_refactor_plan\src\main\java\net\createmod\ponder\foundation\ui\DebugPanelRenderer.java:1`

#### P0-2：把 `componentScroll` 也迁出 screen 侧状态缝隙

状态：已完成首轮

- debug panel 两类滚动状态统一所有权
- `DebugPanelViewState` 已删除

#### P0-3：删除重复相机字段与 `last*` 布局字段

目标：

- 相机状态只保留在 `PonderPreviewCameraState`
- 页面热区只保留在 `LayoutCache + InteractionHitCache`
- 匿名 Host 接线后续继续瘦身

#### P0-4：把残留 GL 直接操作迁出 `PonderDebugScreen`

目标：

- `drawSceneShadow`
- preview scissor
- preview 主渲染接线

全部迁入 renderer 或 render context

### P1：建立稳定渲染骨架

#### P1-1：引入 `RenderContext + GLStateGuard`

#### P1-2：建立 `SceneProjectionContext + OverlayPlacementEngine`

#### P1-3：把 snapshot 体系升级为 sealed provider/source 模型

#### P1-4：建立 animation spec 与 easing 模型

### P2：把 external 和 registration 现代化

#### P2-0：继续拆薄 external register / execute

状态：进行中

- `ExternalPonderRegistrationService` 已成为场景与标签注册门面
- `ExternalSceneRegistrationService`、`ExternalTagRegistrationService`、`ExternalSharedTextRegistrationService` 已独立
- tag 定义注册与 component-tag 关联已拆入独立 registrar
- external scene 执行已拆成 scene / world / overlay 三类执行器

#### P2-1：补 `compile` 阶段，显式产出 `CompiledSceneBundle`

#### P2-2：引入结构化诊断对象

#### P2-3：引入 `RegistrationCommand`

#### P2-4：把 `PonderIndex.reload()` 收口成编排器

### P3：构建层和兼容层收尾

#### P3-1：收敛 Gradle 重复逻辑

#### P3-2：理顺 CraftTweaker 依赖与测试开关

#### P3-3：校准 shim 定位与发行路径

## 10. 每阶段验收门

### Gate A：身份门

- 改动是否符合 `Unimined + CRL + Java 25` 主线
- 新类型是否放在正确层

### Gate B：结构门

- `PonderDebugScreen` 是否继续变薄
- renderer、state、helper 是否继续单一职责化
- compat 代码是否继续收口

### Gate C：编译门

正式编译验证统一走 GitHub Actions，避免占用本机资源。

远程 CI 工作流：

```text
.github/workflows/build.yml
```

CI 覆盖命令：

```powershell
.\gradlew.bat compileJava forgeServerShimJar --stacktrace
```

本地只做静态检查，例如 `git diff --check`、`rg`、结构性文件审计。

### Gate D：运行门

最少校验四项：

- preview 可旋转
- timeline 可推进
- overlay 定位正确
- showcase 可完整进入和切换

### Gate E：清理门

- 旧 helper / 旧桥接字段已删除
- 重复入口已收口
- 文档与 live 结构同步

## 11. 当前建议的开工顺序

真正开工时按下面顺序最稳：

1. 把 `PonderDebugScreen` 的匿名 Host 接线继续收口到 controller adapter
2. 把语音框三角形和残留 GL 绘制迁入 `SpeechRenderer` 或 render context
3. 引入 `RenderContext + GLStateGuard`
4. 引入 `SceneProjectionContext + OverlayPlacementEngine`
5. 升级 snapshot 体系的来源、失效、缓存语义
6. external 引入 `CompiledSceneBundle`
7. registration 引入 `RegistrationCommand`
8. 构建层和 shim 层收尾

这条顺序能先消灭当前最高频的耦合点，再推进更深层的现代化改造。
