# Ponder 1.12.2 重构执行规划

日期：`2026-07-02`
分支：`codex/ponder-refactor-plan-20260624`
工作树：`E:\mc_modding\Ponder_refactor_plan`

## 0. 当前执行约束

当前重构继续以独立工作树为准：

- 施工工作树：`E:\mc_modding\Ponder_refactor_plan`
- 保护工作树：`E:\mc_modding\Ponder`
- 当前分支：`codex/ponder-refactor-plan-20260624`
- 远程仓库：`wu-xiao-ya/ponder-for-1.12.2`
- 当前远程验证：GitHub Actions
- 本地验证范围：`git diff --check`、`rg`、文件审查
- 本地资源约束：Gradle 编译、测试、打包统一交给远程 CI
- 子代理路由：新派发子代理统一使用 `gpt-5.4-mini-pixel`
- 参考项目：`RuiXuqi/GuideNova`、`CleanroomMC/CleanroomModTemplate`

当前已知状态：

- `937a362 Refactor ponder foundation architecture` 已推送
- `d661bbc Retry remote CI Gradle build` 已推送
- `72aca521001429140a7dee88c2dfa534426cdb63` 是当前记录的远端可编译基线，ScenePreviewBlockRenderer 已通过 CI Build `28804497886`
- `patch1.patch` 是未跟踪文件，暂存前需要确认用途
- `OverlayPlacementEngine` / `PonderOverlayHelper` placement 收口已完成
- `SnapshotSource` / `ConstantSnapshotSource` / `ProviderSnapshotSource` / `SnapshotRegistryStore` 已完成
- `PonderTheme` / `PonderThemes` / `ThemeResolver` / `ponder_themes.json` 已完成
- `ExternalRegistrationDiagnostic(s)` 已完成
- `RenderContext` 2D bridge 已完成，Actor / Scene / Particle / POI / Controls overlay 已迁入 `RenderContext`，`OverlayDrawContextAdapter` 已抽出
- `ScenePreviewRenderer.renderPreviewScenePass(...)`、`ScenePreviewStateScope`、`ScenePreviewShadowRenderer`、`ScenePreviewBlockRenderer`、`ScenePreviewTileEntityRenderer`、`TileEntityPreviewScope`、`ActorPreviewStateScope`、`ActorPreviewRenderPass`、`ActorPreviewAppearance`、`ActorPreviewBodyRenderer`、`ActorPreviewRenderData` 与 `BirbPoseKind` 已抽出，appearance 策略层与 body/primitive 渲染层已分离，preview 帧生命周期、主场景 pass、shadow 绘制、block renderer、tile entity renderer/scope、actor-pass scope、actor render dispatch、actor primitive drawing、actor body dispatch、actor render data 与 `poseName` 分类已分层，`renderActorPreviews()` 已收成 actor pass 入口
- `PonderDebugScreen` adapter 切片已完成，HUD host、renderer host、caption host 已收口，showcase HUD 文案提供器已内联
- `ShowcaseHudHoverLabelHostAdapter` 已退场，`PonderDebugScreenHostSupport` 直接实现 `ShowcaseHudRenderer.HoverLabelHost`
- tag 注册结果已统一到 `RegistrationOutcome`，`ExternalTagDefinitionRegistrar.Result` 已退场
- external scan 已产出 `ExternalScanResult`，files / scannedRoots / skippedRoots 进入结构化结果
- external validate 已产出 `ValidationReport` 与 `ExternalValidationDiagnostic(s)`，文件级加载失败和重复 interaction warning 进入诊断汇总
- external parse 已产出 `ExternalParseResult`，`definitions + scanResult + validationReport` 进入单次加载结果
- `PonderReloadOrchestrator` 已落地，`PonderReloadDetails` 开始汇总 validation、compile summary、registration diagnostics 与三段 registration outcome
- scene registration outcome 已区分 registered / skipped / failed，`indexExclusions` 命中项进入 skipped 口径

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

- `PonderDebugScreen` 当前约 `1433` 行，主职责继续向页面协调器收口
- `PonderSceneController`、`DebugMouseController`、`ShowcaseMouseController` 已承担输入和编排的一部分
- `LayoutCache`、`InteractionState`、`Snapshot.RenderCapability`、`PonderSceneRuntimeTypes.TransformStep` 已开始吃到 `record` 与 `sealed interface`
- `ScenePreviewRenderer`、`ShowcaseRenderer`、`ShowcaseHudRenderer`、`DebugPanelRenderer`、`SceneOverlayRenderer`、`GuiOverlayRenderer` 已形成 renderer 分层，`ScenePreviewBlockRenderer`、`ScenePreviewTileEntityRenderer`、`TileEntityPreviewScope`、`ActorPreviewStateScope`、`ActorPreviewRenderPass`、`ActorPreviewAppearance`、`ActorPreviewBodyRenderer`、`ActorPreviewRenderData` 与 `BirbPoseKind` 已落位，appearance 策略层与 body/primitive 渲染层已抽出
- `componentScroll` 与 `operationScroll` 已由 `DebugPanelRenderer` 持有，`DebugPanelViewState` 已退场
- speech box、connector、line segment 绘制已集中到 `SpeechRenderer`
- section fade、camera rotate、actor progress 已接入 `AnimationSpec`
- `OverlayPlacementEngine`、`PonderOverlayHelper` 的 placement 收口已完成，`OverlayDrawContextAdapter` 已抽出
- `SnapshotSource`、`ConstantSnapshotSource`、`ProviderSnapshotSource`、`SnapshotRegistryStore` 已完成
- `PonderTheme`、`PonderThemes`、`ThemeResolver`、`ponder_themes.json` 已完成
- `ExternalRegistrationDiagnostic(s)` 已完成
- external 注册已通过 `CompiledSceneBundle` 进入显式 compile 阶段
- registration 内部写入已通过 `RegistrationCommands` 与 `RegistrationCommandService` 收口
- tag definition 注册与 component-tag assignment 已统一用 `RegistrationOutcome` 汇总
- external scan 阶段已通过 `ExternalScanResult` 暴露扫描文件与根路径统计
- external validate 阶段已通过 `ValidationReport` 汇总 filesScanned / filesLoaded / filesFailed / warnings / errors
- external parse 阶段已通过 `ExternalParseResult` 汇总 definitions / scanResult / validationReport
- `RenderContext` 2D bridge 已完成，Actor / Scene / Particle / POI / Controls overlay 已迁入 `RenderContext`
- `ScenePreviewRenderer` 已把 preview frame lifecycle、scene pass、block renderer、tile entity preview renderer/scope、actor-pass scope、actor render dispatch、actor primitive drawing、actor appearance strategy、body/primitive 渲染层与 actor render data 分开，`ScenePreviewBlockRenderer` 已把 block model preview 状态收进局部 renderer，`ScenePreviewTileEntityRenderer` 已把 TileEntity 渲染状态收进局部 renderer/scope，`ActorPreviewStateScope` 已把 actor blend 状态恢复收进局部 scope，`BirbPoseKind` 已收口 `poseName` 判定
- `PonderReloadOrchestrator` 已落地，reload report 已携带 external validation、compile summary、registration diagnostics 与 registration 明细

对应参考：

- `E:\mc_modding\Ponder_refactor_plan\docs\UI_LIVE_ARCHITECTURE_ANALYSIS.md:1`
- `E:\mc_modding\Ponder_refactor_plan\src\main\java\net\createmod\ponder\foundation\ui\PonderDebugScreen.java:1`
- `E:\mc_modding\Ponder_refactor_plan\src\main\java\net\createmod\ponder\foundation\ui\DebugPanelRenderer.java:1`
- `E:\mc_modding\Ponder_refactor_plan\src\main\java\net\createmod\ponder\foundation\ui\LayoutCache.java:1`
- `E:\mc_modding\Ponder_refactor_plan\src\main\java\net\createmod\ponder\foundation\ui\InteractionState.java:1`

### 3.2 当前最大的三块技术债

#### A. `PonderDebugScreen` 仍偏胖

当前残留热点：

- `ScenePreviewRenderer` 的 preview scene pass、preview state scope、`ScenePreviewShadowRenderer`、`ScenePreviewBlockRenderer`、`ScenePreviewTileEntityRenderer`、`TileEntityPreviewScope`、actor-pass scope、`ActorPreviewRenderPass`、`ActorPreviewAppearance`、`ActorPreviewBodyRenderer`、`ActorPreviewRenderData`、`BirbPoseKind`、`ActorPrimitiveDrawer`、`ActorBodyRenderer`、`ActorBodyDrawContext` 已收束，`renderActorPreviews()` 已收成 actor pass 入口，下一步指向剩余 render state 收尾
- 匿名 Host 接线占据较多 screen 篇幅
- `isMouseOver*` 仍服务 hover / click / drag，showcase hover-label 已由 `ShowcaseHudRenderer.computeHoverLabel(...)` 统一承接
- `PonderDebugScreen` 继续向页面协调器和 host adapter 收口
- reload 结果结构化继续推进，validation、compile summary、registration diagnostics 与 register 结果已进入 reload report
- line count 当前反映 adapter 与 renderer 过渡期成本，actor 绘制原语与 tile entity renderer 下沉已完成，后续自然切片转向剩余 render state 收尾

#### B. Snapshot 体系已完成首轮收口

当前收口结果：

- `SnapshotSource` / `ConstantSnapshotSource` / `ProviderSnapshotSource` / `SnapshotRegistryStore` 已完成
- `SnapshotRenderer.GuiSnapshotRenderer` 已升级为 sealed 子接口
- `PonderGuiSnapshotRegistry` 继续细化 cache scope、失效语义和 rebuild 边界
- 反射型 snapshot provider 继续向 source 模型迁移

#### C. 构建层和兼容层重复

当前残留热点：

- `build.gradle` 与 `gradle/scripts/*` 继续收口重复逻辑
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
- `drawBorderedRect()`
- `drawCrossMarker()`
- `fillGradientRect()`
- `fillTexturedRect()`
- `drawLine()`
- `renderItem()`
- `renderText()`
- `drawHoveringText()`

第一批实现：

- `GlRenderContext`
- `GLStateGuard`
- `DebugPanelDrawContextAdapter`
- `OverlayDrawContextAdapter`

状态：

- `RenderContext` 2D bridge 已完成，preview / snapshot 继续通过各自 state scope 收口
- Actor / Scene / Particle / POI / Controls overlay 已迁入 `RenderContext`
- `OverlayDrawContextAdapter` 已抽出

目标：

- renderer 直接依赖 `RenderContext`
- 预览渲染、overlay 渲染、snapshot 渲染共享同一上下文风格

接口建议：

```java
interface RenderContext {
    MatrixScope push();
    ScissorScope scissor(int x, int y, int width, int height);
    void translate(float x, float y, float z);
    void scale(float x, float y, float z);
    void rotate(float angle, float x, float y, float z);
    void fillRect(int x, int y, int width, int height, int color);
    void drawBorderedRect(int left, int top, int right, int bottom, int fillColor, int borderColor);
    void drawCrossMarker(int centerX, int centerY, int armRadius, int centerRadius, int accentColor, int fillColor);
    void drawLine(Point from, Point to, int color, float width);
}
```

`MatrixScope`、`ScissorScope`、`GLStateGuard` 统一采用 `AutoCloseable`，调用侧使用 try-with-resources 表达矩阵栈、裁剪栈和 GL 状态生命周期。

当前调用点按风险分层：

- 低风险 2D 底座：`CompatGuiScreen`、`DrawContext`、`DebugPanelRenderer`、`ActorOverlayRenderer`
- 中风险 2D chrome：`AbstractPonderBrowserScreen`、`PonderIndexScreen`、`OverlayRenderer`、`PonderUI`
- 中风险 overlay：`GuiOverlayRenderer`、`ShowcaseChromeRenderer`、`ShowcaseRenderer`、`SpeechRenderer`
- 高风险 3D preview：`ScenePreviewRenderer`、`PonderSceneRuntime.applyRenderTransforms`
- 高风险 snapshot：`EmbeddedReflectiveGuiSnapshot`、`EmbeddedReflectiveTabGuiSnapshot`、`EmbeddedGuiFurnaceSnapshot`、`SandboxTriggeredBlockGuiSnapshot`

迁移顺序：

1. `CompatGuiScreen` 与 `DrawContext` 先提供 `RenderContext` 适配层
2. `DebugPanelRenderer`、`ActorOverlayRenderer` 迁入纯 2D context
3. `OverlayRenderer`、`SpeechRenderer`、`GuiOverlayRenderer` 迁入 line / gradient / triangle 能力
4. `PonderUI` 与 showcase renderer 迁入 theme-aware context
5. `ScenePreviewRenderer` 和 snapshot renderer 接入 scoped scissor / matrix / depth guard，appearance 策略层已抽出，后续继续推进 `TileEntityPreviewScope` 与更细 render state 收尾

这条顺序先稳住 2D 绘制和状态恢复，再处理 3D preview 与外部 GUI 嵌入。

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

接口建议：

```java
record ProjectedBounds(float minX, float minY, float maxX, float maxY, float depth) {}
record CaptionPlacement(ProjectedBounds anchor, int boxX, int boxY, int boxWidth, int boxHeight) {}

interface SceneProjectionContext {
    Optional<Point> projectScenePoint(Vec3d point);
    Optional<ProjectedBounds> projectSceneBounds(AxisAlignedBB bounds);
}
```

`OverlayPlacementEngine` 输入 scene-space 数据，输出 screen-space placement。renderer 接收 placement 后只负责绘制。

状态：

- `OverlayPlacementEngine`、`PonderOverlayHelper` placement 收口已完成
- `OverlayDrawContextAdapter` 已抽出
- `PonderOverlayLayoutHelper` 继续保留 projection 适配职责

当前入口按职责分为六组：

- `PonderOverlayLayoutHelper`：`projectScenePoint()`、`projectSceneBounds()`
- `PonderOverlayHelper`：GUI overlay placement、highlight placement、caption target、overlap avoidance
- `GuiOverlayRenderer` 与 `ShowcaseCaptionRenderer`：placement 消费
- `PonderDebugScreen`：preview layout 汇总和 renderer 分发
- `PonderSceneBuilder` 与 `PonderScene.OverlayEvent`：作者态 overlay 事件
- `SceneOperationDefinitionFactory` 与 `ExternalOverlayEnqueuer`：JSON overlay 入口

迁移顺序：

1. 新建 `SceneProjectionContext` 与 `ProjectedBounds`
2. 让 `PonderOverlayLayoutHelper` 成为 context 薄适配层
3. 把 `PonderOverlayHelper` placement 计算迁入 `OverlayPlacementEngine`
4. 把 `CaptionPlacement`、`GuiOverlayPlacement`、`GuiHighlightPlacement` 变成独立 record
5. 让 renderer 只消费 placement record
6. 把 JSON / Java builder overlay spec 对齐到同一 placement 输入模型

### 6.3 动画路线

当前已引入 `AnimationSpec` 与 `EasingFunction`。后续把调度层继续收口到：

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

接口建议：

```java
sealed interface HitRegion permits RectHitRegion, CompositeHitRegion {
    boolean contains(int mouseX, int mouseY);
    HitAction action();
}

record RectHitRegion(String id, int x, int y, int width, int height, HitAction action) implements HitRegion {}
record CompositeHitRegion(String id, List<HitRegion> children, HitAction action) implements HitRegion {}
```

`DebugMouseController`、`ShowcaseMouseController` 通过 `HitRegionTree.resolve(mouseX, mouseY)` 获取语义动作，再进入 controller handler。

### 6.5 Theme 路线

颜色系统从硬编码 `int` 收口为：

- `PonderTheme`
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

实现顺序：

1. 先把 `ShowcaseRenderer.Theme`、`ShowcaseHudRenderer.Theme`、`ShowcaseChromeRenderer.Theme` 合并为 `PonderTheme`
2. 再用 `SymbolicColor` 替代裸 `int` 颜色参数
3. 最后把默认主题迁入资源配置，保留 Java 默认值兜底

状态：

- `PonderThemes.showcase()` / `PonderThemes.debug()` Java preset 已落地
- debug 与 showcase 的 renderer 创建入口已收口

当前 theme 分布：

- `ShowcaseChromeRenderer.Theme`：chrome glow、logo alpha、边框与标题色
- `ShowcaseRenderer.Theme`：header box、fallback panel、popup 与文本色
- `ShowcaseHudRenderer.Theme`：playback bar、next-up card、hover label
- `PonderUI` 与 `PonderDebugScreen` 已通过 `PonderThemes` 获取 showcase/debug preset

目标结构：

```java
record PonderTheme(String id, Map<SymbolicColor, Integer> colors, Map<ThemeMetric, Float> metrics) {}

record ThemeResourceTheme(String id, Map<SymbolicColor, Integer> colors, Map<ThemeMetric, Float> metrics,
    @Nullable ResourceLocation logoTexture) {}

enum SymbolicColor {
    PANEL_BACKGROUND,
    PANEL_BORDER,
    TIMELINE_ACTIVE,
    TIMELINE_INACTIVE,
    OVERLAY_CAPTION,
    OVERLAY_WARNING,
    SHOWCASE_HEADER,
    SHOWCASE_POPUP
}
```

`PonderTheme`、`PonderThemes`、`ThemeResolver`、`ponder_themes.json` 已完成当前阶段目标。

### 6.6 SnapshotSource 路线

snapshot 体系拆成三层：

```text
SnapshotSource
  -> SnapshotProvider compatibility adapter
  -> Snapshot renderer / texture result
```

当前来源：

- 静态贴图：`minecraft_furnace`、Thermal 面板和机器贴图
- 反射实时 GUI：`EmbeddedReflectiveGuiSnapshot`
- 反射 tab GUI：`EmbeddedReflectiveTabGuiSnapshot`
- 沙盒捕获 GUI：`SandboxTriggeredBlockGuiSnapshot`
- tick 动态 provider：`te_*_cycle`

状态：

- `SnapshotSource` 已完成
- `ConstantSnapshotSource` 已完成
- `ProviderSnapshotSource` 已完成
- `SnapshotRegistryStore` 已完成

目标类型：

```java
sealed interface SnapshotSource permits StaticTextureSnapshotSource, LiveGuiSnapshotSource,
    CyclingSnapshotSource, SandboxBlockGuiSnapshotSource {
    Snapshot resolve(SnapshotContext context);
    SnapshotInvalidationPolicy invalidationPolicy();
    SnapshotCacheKey cacheKey(ResourceLocation id, SnapshotContext context);
}

record SnapshotContext(float currentTick, EntityPlayerSP player, World world) {
    String cacheScopeKey();
}
record SnapshotCacheKey(ResourceLocation id, int tickBucket, String contextKey, String sourceKey) {}
```

迁移顺序：

1. `SnapshotProvider` 作为 compatibility adapter 接入 `SnapshotSource`
2. registry 从 `Map<ResourceLocation, SnapshotProvider>` 迁到 `Map<ResourceLocation, SnapshotSource>`
3. 静态贴图使用永久缓存，`te_*_cycle` 使用 `currentTick / 20` bucket
4. live GUI 缓存键加入 player/world scope、gui class、tile class
5. sandbox capture 拆成不可变 source 与独立 capture session
6. registry 增加 `registerSource`、`clearCache()`、`clear()`、`rebuild()` 入口，接资源重载与数据刷新

## 7. 构建与兼容层整改方案

### 7.1 构建层

历史 CI 关键阻塞：

```text
Could not GET 'https://curse.cleanroommc.com/net/minecraft/minecraft/1.12.2/minecraft-1.12.2.pom'
Received status code 502 from server: Bad Gateway
```

当前相关文件：

- `E:\mc_modding\Ponder_refactor_plan\gradle\scripts\dependencies.gradle:1`
- `E:\mc_modding\Ponder_refactor_plan\.github\workflows\build.yml:1`

整改原则：

1. 保持 Unimined + Cleanroom 主链
2. 先解决 `net.minecraft:minecraft:1.12.2` 元数据解析源
3. GitHub Actions 保持三次重试和 Gradle cache
4. 依赖解析通过后再处理 Java 编译诊断

当前网络探测结果：

- `https://curse.cleanroommc.com/net/minecraft/minecraft/1.12.2/minecraft-1.12.2.pom` 返回 `502`
- `https://curse.cleanroommc.com/net/minecraft/minecraft/1.12.2/minecraft-1.12.2.jar` 返回 `502`
- `https://maven.cleanroommc.com/net/minecraft/minecraft/1.12.2/minecraft-1.12.2.pom` 返回 `404`
- `https://repo.cleanroommc.com/releases/net/minecraft/minecraft/1.12.2/minecraft-1.12.2.pom` 返回 `404`
- `https://repo.cleanroommc.com/snapshots/net/minecraft/minecraft/1.12.2/minecraft-1.12.2.pom` 返回 `404`
- `https://libraries.minecraft.net/net/minecraft/minecraft/1.12.2/minecraft-1.12.2.jar` 返回 `404`

CI 整改候选：

1. GitHub Actions 显式缓存 `~/.gradle/caches/unimined/net/minecraft/minecraft/1.12.2`
2. 缓存键绑定 `minecraft-1.12.2`、`Cleanroom-FG3`、`0.5.6-alpha`
3. 同步缓存 `~/.gradle/caches/modules-2`，提升 Cleanroom 依赖复用率
4. 若缓存命中仍失败，再考虑把 Unimined 产物整理成 `mavenLocal()` 可读结构
5. 远程 CI 进入 Java 编译阶段后再处理源码错误

状态：已完成并通过远程 CI

- `.github/workflows/build.yml` 已加入 `actions/cache@v4`
- 缓存路径覆盖 Unimined Minecraft 1.12.2 与 Gradle modules-2
- 缓存键包含 `minecraft-1.12.2`、`Cleanroom-FG3`、`0.5.6-alpha`
- 保留 JDK 25、Gradle cache、三次 Gradle retry
- `gradle/scripts/dependencies.gradle` 在 CI 中优先使用 `mavenLocal()`
- 远程 Gradle 首轮失败后会尝试从 Unimined cache 的 Cleanroom MCP jar 生成 `net.minecraft:minecraft:1.12.2` 的本地 Maven 条目，再进入下一轮 retry
- 远程验证：`Build 28804497886 success`，对应当前记录的远端可编译基线 `72aca521001429140a7dee88c2dfa534426cdb63`
- 通过提交：`72aca521001429140a7dee88c2dfa534426cdb63`

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

这轮规划把校验从边缘事项提到正式骨架内。

第一批建议：

- 先落 GitHub Actions artifact verifier，固定 beta 产物内容门
- 为 `api` 与 definition model 规划首批纯 JVM 单元测试
- 把 `compileJava`、`remapJar`、`forgeServerShimJar` 与产物复核分成更清楚的验证门

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

状态：第二批进行中

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

#### P0-3A：抽离 controller Host adapter

目标：

- `PonderDebugScreen` 只保留事件入口和页面编排
- controller host 迁入独立 adapter
- showcase/debug 双分支共享 `PonderDebugScreenHostSupport`

第一批 adapter：

- `PonderDebugScreenHostSupport`
- `PonderSceneControllerHostAdapter`
- `DebugKeyboardHostAdapter`
- `ShowcaseKeyboardHostAdapter`
- `DebugMouseHostAdapter`
- `ShowcaseMouseHostAdapter`

第二批 adapter：

- `ShowcaseHudHostAdapter`
- `ShowcaseRendererHostAdapter`
- `ShowcaseCaptionHostAdapter`
- `ScreenDrawContextAdapter`

迁移顺序：

1. `PonderDebugScreenHostSupport` 集中 screen 视图能力、状态对象、按钮刷新、场景查询
2. `PonderSceneControllerHostAdapter` 接收 reload、select、seek、step、button state 命令
3. keyboard adapter 接收 debug/showcase 键盘命令
4. mouse adapter 接收 preview、playback、component list、operation list 命中逻辑
5. showcase render adapter 接收 HUD、caption、header、draw context 接线

关键时序：

- `initGui()` 后按钮字段完整
- adapter 中的按钮刷新沿用当前生命周期
- `selectionState`、`playbackState`、`interactionHitCache`、`previewCameraState` 继续保持单一所有权

状态：已完成首轮

- `PonderDebugScreenHostSupport` 已落地
- `PonderSceneControllerHostAdapter` 已落地
- `createSceneControllerHost()` 已改为返回 adapter
- `PonderDebugScreen` 暴露最小包可见桥接：`getDebugPanelRenderer()`、`clearPreviewCaches()`、`centerOperationsOnActiveLine()`、`resetPreviewCamera()`
- `DebugKeyboardHostAdapter` 已落地，debug 键盘命令经 `PonderDebugScreenHostSupport` 转发
- `ShowcaseKeyboardHostAdapter` 已落地，showcase 键盘命令经 `PonderDebugScreenHostSupport` 转发
- `DebugMouseHostAdapter`、`ShowcaseMouseHostAdapter` 已落地，鼠标命令经 `PonderDebugScreenHostSupport` 转发

#### P0-4：把残留 GL 直接操作迁出 `PonderDebugScreen`

目标：

- `ScenePreviewShadowRenderer`
- preview scissor
- preview 主渲染接线

全部迁入 renderer 或 render context

状态：部分完成

- speech box、connector、line segment 已由 `SpeechRenderer` 承担
- `PonderDebugScreen` 当前仍保留少量 preview 桥接
- `ScenePreviewRenderer` 已抽出 `renderPreviewScenePass(...)`、`ScenePreviewStateScope`、`ScenePreviewShadowRenderer`、`ScenePreviewBlockRenderer`、`ScenePreviewTileEntityRenderer`、`TileEntityPreviewScope`、`ActorPreviewStateScope`、`ActorPreviewRenderPass`、`ActorPreviewAppearance`、`ActorPreviewBodyRenderer`、`ActorPreviewRenderData`、`BirbPoseKind` 与 `ActorPrimitiveDrawer`、`ActorBodyRenderer`、`ActorBodyDrawContext`，appearance 策略层、body/primitive 渲染层、shadow renderer 与 tile entity renderer 已收口，`renderActorPreviews()` 已收成 actor pass 入口，后续指向剩余 render state 收尾

### P1：建立稳定渲染骨架

#### P1-1：引入 `RenderContext + GLStateGuard`

交付物：

- `foundation/ui/render/RenderContext.java`
- `foundation/ui/render/GlRenderContext.java`
- `foundation/ui/render/GLStateGuard.java`
- `foundation/ui/render/ScissorStack.java`

验收点：

- `CompatGuiScreen` 的 gradient / textured rect 通过 context 暴露
- `DrawContext` 能桥接到 `RenderContext`
- `GLStateGuard` 覆盖 blend、alpha、texture、shade model、line width、color、lighting
- `ScissorStack` 统一坐标换算和 GL scissor 生命周期

状态：已完成基础层

- `foundation/ui/render/RenderContext.java` 已落地
- `foundation/ui/render/GlRenderContext.java` 已落地
- `foundation/ui/render/GLStateGuard.java` 已落地
- `foundation/ui/render/ScissorStack.java` 已落地
- `CompatGuiScreen` 已实现 `DrawContext`
- `RenderContext` 已补 `drawBorderedRect` / `drawCrossMarker` 等通用 primitive
- `GLStateGuard` 已补 scissor / texture / cull / blend / alpha / shade / line width / color / lighting 入场快照恢复
- `DebugPanelRenderer` 已接入 local `RenderContext` bridge 复用 entry chrome
- `DrawContext` 已继承 `RenderContext` 并提供 1.12.2 GUI bridge
- `RenderContext` 2D bridge 已完成，Actor / Scene / Particle / POI / Controls overlay 已迁入 `RenderContext`，preview / snapshot 继续通过各自 state scope 收口

#### P1-2：建立 `SceneProjectionContext + OverlayPlacementEngine`

交付物：

- `foundation/ui/projection/SceneProjectionContext.java`
- `foundation/ui/projection/ProjectedBounds.java`
- `foundation/ui/overlay/OverlayPlacementEngine.java`
- caption、GUI overlay、highlight 的 placement record

验收点：

- `PonderOverlayLayoutHelper` 变成 projection adapter
- `PonderOverlayHelper` 的 placement 计算迁入 engine
- `GuiOverlayRenderer` 和 `ShowcaseCaptionRenderer` 消费 placement record

状态：第二批基础模型已预铺

- `foundation/ui/projection/SceneProjectionContext.java` 已落地
- `foundation/ui/projection/ScenePointProjector.java` 已落地
- `foundation/ui/projection/SceneBounds.java` 已落地
- `foundation/ui/projection/ProjectedBounds.java` 已落地
- `CaptionPlacement`、`GuiOverlayPlacement`、`GuiHighlightPlacement` 已在 projection 包预铺
- `PonderOverlayHelper` 已接入 projection record，caption / overlay / highlight 共用同一上下文
- `PonderOverlayLayoutHelper` 继续保留 legacy `ProjectedBounds` 给 `SceneOverlayRenderer`
- `OverlayPlacementEngine`、`PonderOverlayHelper` placement 收口已完成
- `OverlayDrawContextAdapter` 已抽出
- screen 侧 layout 汇总职责继续缩小

#### P1-3：把 snapshot 体系升级为 sealed provider/source 模型

交付物：

- `SnapshotSource` sealed interface
- `ConstantSnapshotSource`
- `BlockGuiSnapshotSource`
- `SandboxSnapshotSource`
- `SnapshotCacheKey`
- `SnapshotInvalidationPolicy`

验收点：

- 静态贴图、live GUI、cycling provider、sandbox block GUI 都是明确 source 类型
- `SandboxTriggeredBlockGuiSnapshot` 的 block id、meta、tile NBT 进入不可变 key
- registry 具备 `registerSource`、`clearCache()`、`clear()`、`rebuild()` 入口
- 旧 `SnapshotProvider#get(float)` 调用面保留 compatibility adapter

状态：已完成基础层

- `SnapshotSource` sealed interface 已落地
- `ConstantSnapshotSource` 已落地
- `ProviderSnapshotSource` 已落地
- `SnapshotRegistryStore` 已落地
- `SnapshotContext` 已落地
- `SnapshotCacheKey` 已落地
- `SnapshotInvalidationPolicy` 已落地
- `SnapshotProvider#asSource()` 已提供 compatibility adapter
- `PonderGuiSnapshotRegistry` 内部存储已切换到 `SnapshotSource`
- `PonderGuiSnapshotRegistry` 已补 source cache 与 clear / rebuild 边界
- `registerBlockGuiSnapshot(...)` 兼容入口已保留

#### P1-4：建立 animation spec 与 easing 模型

状态：已完成首轮

- `AnimationSpec` 已落地
- `EasingFunction` 已落地
- `PonderSceneRuntime` 已接入 section fade、camera rotate、actor progress

### P2：把 external 和 registration 现代化

#### P2-0：继续拆薄 external register / execute

状态：进行中

- `ExternalPonderRegistrationService` 已成为场景与标签注册门面
- `ExternalSceneRegistrationService`、`ExternalTagRegistrationService`、`ExternalSharedTextRegistrationService` 已独立
- tag 定义注册与 component-tag 关联已拆入独立 registrar
- `ExternalTagDefinitionRegistrar` 已直接返回 `RegistrationOutcome`
- `ExternalTagRegistrationResult` / `ExternalSharedTextRegistrationResult` 已作为 tag 与 shared text 侧编排返回值，绑定 registration diagnostics 与 registration outcome
- external scene 执行已拆成 scene / world / overlay 三类执行器

#### P2-1：补 `compile` 阶段，显式产出 `CompiledSceneBundle`

状态：已完成首轮

- `CompiledSceneBundle` 已落在 `foundation/external/register`
- `ExternalSceneCompileSummary` 已承载 scene definition / compiled bundle / component binding / compile failure 计数，component binding 口径为成功编译的 scene 内声明数量
- `ExternalSceneRegistrationResult` 已作为 scene 侧编排返回值，绑定 compile summary、registration diagnostics 与 registration outcome
- `ExternalSceneRegistrationService` 已经先 compile 再注册 component/storyboard/order
- `ExternalSceneRegistrationService` 已按实际 registry entry 增量统计 registered / skipped
- `RegistrationContext` 现在负责 source info 归一化
- scene 注册失败已进入结构化 diagnostic sink 与 `RegistrationDiagnosticReport`

#### P2-2：引入结构化诊断对象

状态：已完成首轮

- `ExternalRegistrationDiagnostic` 已落地，承载 severity / subject / source / detail / cause
- `ExternalSceneRegistrationService`、`ExternalTagDefinitionRegistrar`、`ExternalComponentTagRegistrar`、`ExternalSharedTextRegistrationService` 已改成通过 sink 发出失败信息
- `RegistrationDiagnosticReport` 已落地，scene compile failure、component registration failure、tag definition failure、component-tag assignment failure 与 shared text failure 已按 info / warning / error 聚合到 reload details
- `ExternalSceneCompileFailureDiagnostic` / `ExternalSceneCompileFailureReport` 已落地，`/ponder reload` 会按固定上限展示逐条 compile failure
- `RegistrationContext` 负责 source info 归一化，注册边界直接携带 `SourceInfo`

#### P2-3：引入 `RegistrationCommand`

状态：已完成首轮

- `RegistrationCommands` 已落地
- `RegistrationCommandService` 已落地
- `PonderSceneRegistry`、`PonderTagRegistry`、`PonderLocalization` 内部写入已走 command

#### P2-4：把 `PonderIndex.reload()` 收口成编排器，并推进 compile failure 诊断聚合

状态：已完成当前详情层

- `PonderReloadOrchestrator` 已落地
- scan / parse / validate / register 结果口径已先统一
- `ExternalParseResult` 已承载 definitions / scanResult / validationReport
- `PonderReloadDetails` 已承载 validationReport、scene compile summary、scene compile failure report、registration diagnostics 与 scene / tag / shared text registration outcome
- `ExternalSceneRegistrationResult` 已作为 scene 注册编排返回值，避免 reload 层重新推断 compile、diagnostics 与 registration 结果
- `ExternalTagRegistrationResult` / `ExternalSharedTextRegistrationResult` 已作为 tag 与 shared text 注册编排返回值，避免 reload 层重新推断 diagnostics 与 registration 结果
- compile summary 已独立展示并接入 reload 汇总，compile failure 明细由 `PonderCommand` 截断输出
- scene、tag、component-tag assignment 与 shared text 失败信息已进入 diagnostic sink 与 `RegistrationDiagnosticReport`

### P3：构建层和兼容层收尾

#### P3-1：收敛 Gradle 重复逻辑

- `gradle/scripts/project-conventions.gradle` 已经承接共享 JVM/toolchain/test 默认项，remap、shadow、发布逻辑继续留在主 `build.gradle`

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
.\gradlew.bat compileJava remapJar forgeServerShimJar -Pdeploy_test_mods_dir= --stacktrace
```

CI 在上传前执行 beta 产物内容门，并上传两个 beta 验证产物：

- `ponder-client-runtime`：Cleanroom / CRL 客户端运行时 jar
- `forge-server-shim`：stock Forge 1.12.2 专用服务端兼容 shim
- artifact verifier 从 `gradle.properties` 读取 `mod_id` 与 `mod_version`，复核随 beta 版本号演进
- CI 内置 artifact verifier 复核 runtime jar 的 `net/createmod/ponder/` 前缀与 `mixins.ponder.json`
- CI 内置 artifact verifier 复核 shim jar 的 `net/createmod/ponder/Reference.class` 与 `mcmod.info`

本地只做静态检查，例如 `git diff --check`、`rg`、结构性文件审计。

当前 Gate C 前置任务：

- 维持 Cleanroom 依赖解析源和缓存策略
- 保留远程三次重试
- 每批本地整合后触发 GitHub Actions 编译门

### Gate D：运行门

最少校验四项：

- preview 可旋转
- timeline 可推进
- overlay 定位正确
- showcase 可完整进入和切换

### Gate E：清理门

- 旧 helper / 旧桥接字段已删除
- 重复入口已收口
- `GLStateGuard` 覆盖范围与 live render 结构一致
- 文档与 live 结构同步，包含 `ScenePreviewShadowRenderer` shadow 绘制层、`ScenePreviewTileEntityRenderer` tile entity 渲染层、`ActorPreviewAppearance` 策略层、`ActorPreviewBodyRenderer` body/primitive 渲染层、`ActorPreviewRenderPass`、`ActorPrimitiveDrawer` 绘制原语、`BirbPoseKind` / `poseName` 分类、`ActorBodyDrawContext` 绘制上下文、`ActorBodyRenderer` 分派、`drawBirbBody`、`drawItemBody`、`drawCartWheels`、`ActorPreviewRenderData` 的归位；后续指向剩余 render state 收尾

## 11. 当前建议的开工顺序

真正开工时按下面顺序最稳：

1. 推进 CI artifact verifier 产物门，固定当前 `build.gradle + gradle/scripts/*` 构建路径
2. 收口 CraftTweaker / shim 边界
3. 视需要补齐 `PonderDebugScreen` 残余 host adapter
4. 继续把 tag / shared text 的逐条失败明细扩展成可截断输出
5. 完成 `ScenePreviewRenderer` 的 actor render dispatch 汇总层，shadow 绘制、tile entity renderer、appearance 策略层与 body/primitive 渲染层已抽出，后续推进剩余 render state 收尾

这条顺序能先消灭当前最高频的耦合点，再推进更深层的现代化改造。

## 12. 下一批子代理切片

新派发子代理统一使用 `gpt-5.4-mini-pixel`。

建议并行切片：

1. Reload 编排：继续把 tag / shared text 的逐条失败明细接入 reload 汇总
2. Showcase hover-label：维持 `ShowcaseHudRenderer.computeHoverLabel(...)` 单入口，继续压缩 screen 侧接线
3. CI / artifact verifier 门：围绕当前 `build.gradle + gradle/scripts/*` 验证路径继续补远程门禁
4. CraftTweaker / shim：收口边界和发布语义
5. ScenePreviewRenderer：继续收口剩余 render state 与 preview frame 边界，保持 `ScenePreviewShadowRenderer`、`ScenePreviewBlockRenderer`、`ScenePreviewTileEntityRenderer`、`ActorPreviewAppearance`、`ActorPreviewBodyRenderer`、`ActorPreviewRenderData` 与 `BirbPoseKind` 的归属边界清晰

主代理负责范围控制、冲突处理、文档同步、远程 CI 验证。

## 13. 执行级技术细则

### 13.1 Java 25 落地规则

- `record` 只承载不可变数据、布局结果、投影结果、注册结果和 diagnostic payload
- `sealed interface` 只用于封闭分派面，例如 `SnapshotSource`、operation、registration command
- pattern matching `instanceof` 用在 adapter 层和 renderer key 生成层，避免把反射判断扩散到 screen
- `AutoCloseable` guard 用于 GL state、scissor、matrix、depth、blend 的成对恢复
- `Map.of()` / `List.of()` 用于小型静态表，运行态热路径继续使用可控集合
- API surface 保持稳定签名，现代语法优先留在 foundation 内部

### 13.2 RenderContext 迁移规格

每个 renderer 迁移时按固定顺序推进：

1. 把裸 `GlStateManager`、`Tessellator`、`BufferBuilder` 调用收进 `GlRenderContext`
2. 把颜色、alpha、z-level、scissor 写成显式参数或 guard
3. renderer 构造器保留当前调用签名，新增 context-aware overload 作为下一阶段入口
4. screen 侧只创建 context、layout 和 state，实际绘制落在 renderer
5. 每迁一个 renderer，同步删除 screen 侧对应 helper 方法

验收点：

- renderer 方法入参可从名称判断坐标空间
- GL 状态恢复由 guard 承担
- `DrawContext` 只保留通用 primitive，不承载业务语义

### 13.3 Projection 与 Overlay 规格

坐标转换固定走三层：

```text
scene-space
  -> SceneProjectionContext
  -> placement record
  -> renderer draw call
```

落地规则：

- `SceneProjectionContext` 持有 scene、bounds、layout、tick、projector
- `PonderOverlayLayoutHelper` 只承担 scene-space 到 screen-space 的投影 adapter
- `PonderOverlayHelper` 输出 `CaptionPlacement`、`GuiOverlayPlacement`、`GuiHighlightPlacement`
- overlay renderer 只消费 placement record 和 draw context
- legacy `ProjectedBounds` 保留到所有调用点完成迁移后统一删除
- `OverlayPlacementEngine`、`PonderOverlayHelper` placement 收口已完成
- `OverlayDrawContextAdapter` 已抽出

### 13.4 Snapshot Source 规格

Snapshot registry 按 source、cache、renderer 三层拆分：

```text
SnapshotSource
  -> SnapshotCacheKey
  -> Snapshot
  -> SnapshotRenderer
```

落地规则：

- 静态贴图 source 使用 `IMMUTABLE` policy
- cycling provider 使用 `TICK_20_BUCKET` policy
- live GUI source key 必须包含 renderer class、gui class、tile class、tab field、tile NBT 这类稳定来源信息
- context key 必须覆盖 dimension、player、world scope
- registry 负责 source 注册、cache 命中、cache 清理和 rebuild
- capture session 状态留在 renderer 实例，source key 只表达输入边界

当前阶段结果：

- `ConstantSnapshotSource` 已完成
- `ProviderSnapshotSource` 已完成
- `SnapshotRegistryStore` 已完成
- sandbox block GUI 继续沿 source 与 capture session 边界演进
- 资源重载入口继续接 `PonderGuiSnapshotRegistry.rebuild()`

### 13.5 Theme 资源化规格

Theme 迁移按三步推进：

1. Java preset 承接 showcase/debug 现有数值
2. renderer 构造入口统一从 `PonderThemes` 获取 theme
3. `ThemeResolver` 接资源配置 `ponder_themes.json`

当前阶段结果：

- `PonderTheme` 已完成
- `PonderThemes` 已完成
- `ThemeResolver` 已完成
- `ponder_themes.json` 已完成

资源格式目标：

```json
{
  "id": "showcase",
  "colors": {
    "chrome_glow": "#1B2430"
  },
  "metrics": {
    "header_max_width": 340
  }
}
```

验收点：

- Java preset 与资源配置字段一一对应
- 缺失字段回退到 Java preset
- renderer 内部只使用已解析 theme object

### 13.6 构建与 CI 规格

本地工作只做轻量验证：

```powershell
git -c core.whitespace=cr-at-eol diff --check
rg -n "<<<<<<<|=======|>>>>>>>" .
```

远程编译门固定走 GitHub Actions：

```powershell
gh workflow run Build --ref codex/ponder-refactor-plan-20260624
gh run watch <run_id> --exit-status
```

远程 beta 产物门固定上传：

- `ponder-client-runtime`
- `forge-server-shim`
- 上传前从 `gradle.properties` 推导 runtime/shim jar 文件名
- 上传前固定检查 runtime jar 内容：`net/createmod/ponder/` 前缀、`mixins.ponder.json`
- 上传前固定检查 shim jar 内容：`net/createmod/ponder/Reference.class`、`mcmod.info`

构建层目标：

- `gradle/scripts/project-conventions.gradle` 承接 JVM、toolchain、test 默认项
- `gradle/scripts/dependencies.gradle` 承接依赖和仓库解析
- 主 `build.gradle` 聚焦 Unimined、资源处理、remap、发布和部署任务
- 当前 GitHub Actions 门覆盖 `compileJava remapJar forgeServerShimJar` 与 beta artifact 内容复核
- `test` 已在 conventions 层预留 JUnit Platform 与 Java 25 launcher；当前仓库尚无 `src/test`，进入 workflow 前继续作为规划项
- 后续构建切片复用同一 conventions 层，remap、shadow、发布任务按具体工程保留
