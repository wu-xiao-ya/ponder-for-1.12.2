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
- `patch1.patch` 是未跟踪文件，暂存前需要确认用途
- GitHub Actions 当前卡在 Cleanroom 依赖解析阶段，尚未进入 Java 编译诊断

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

- `PonderDebugScreen` 当前约 `2065` 行，主职责继续向页面协调器收口
- `PonderSceneController`、`DebugMouseController`、`ShowcaseMouseController` 已承担输入和编排的一部分
- `LayoutCache`、`InteractionState`、`Snapshot.RenderCapability`、`PonderSceneRuntimeTypes.TransformStep` 已开始吃到 `record` 与 `sealed interface`
- `ScenePreviewRenderer`、`ShowcaseRenderer`、`ShowcaseHudRenderer`、`DebugPanelRenderer`、`SceneOverlayRenderer`、`GuiOverlayRenderer` 已形成 renderer 分层
- `componentScroll` 与 `operationScroll` 已由 `DebugPanelRenderer` 持有，`DebugPanelViewState` 已退场
- speech box、connector、line segment 绘制已集中到 `SpeechRenderer`
- section fade、camera rotate、actor progress 已接入 `AnimationSpec`
- external 注册已通过 `CompiledSceneBundle` 进入显式 compile 阶段
- registration 内部写入已通过 `RegistrationCommands` 与 `RegistrationCommandService` 收口
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

- 直接 `GlStateManager` 调用仍集中在 preview 绘制桥接和 scene shadow 周边
- 匿名 Host 接线占据较多 screen 篇幅
- `isMouseOver*` 与 hover label host 仍保留在 screen 侧
- showcase theme 构造仍散在 `PonderDebugScreen`、`PonderUI`、renderer 内嵌类型之间
- line count 当前反映 adapter 与 renderer 过渡期成本，下一步目标是拆出 Host adapter 与 RenderContext

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
- `fillGradientRect()`
- `fillTexturedRect()`
- `fillTriangle()`
- `drawLine()`
- `renderItem()`
- `renderText()`
- `drawHoveringText()`

第一批实现：

- `GlRenderContext`
- `GLStateGuard`
- `SnapshotRenderContext`

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
    void fillTriangle(Point a, Point b, Point c, int color);
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
5. `ScenePreviewRenderer` 和 snapshot renderer 接入 scoped scissor / matrix / depth guard

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

当前 theme 分布：

- `ShowcaseChromeRenderer.Theme`：chrome glow、logo alpha、边框与标题色
- `ShowcaseRenderer.Theme`：header box、fallback panel、popup 与文本色
- `ShowcaseHudRenderer.Theme`：playback bar、next-up card、hover label
- `PonderUI` 与 `PonderDebugScreen` 各自构造一套 showcase/debug preset

目标结构：

```java
record PonderTheme(String id, Map<SymbolicColor, Integer> colors, Map<ThemeMetric, Float> metrics) {}

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

先提供 `PonderThemes.showcase()` 与 `PonderThemes.debug()` 两个 Java preset，再把资源配置接入 `ThemeResolver`。

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

目标类型：

```java
sealed interface SnapshotSource permits StaticTextureSnapshotSource, LiveGuiSnapshotSource,
    CyclingSnapshotSource, SandboxBlockGuiSnapshotSource {
    Snapshot resolve(SnapshotContext context);
    SnapshotInvalidationPolicy invalidationPolicy();
}

record SnapshotContext(float currentTick, EntityPlayerSP player, World world) {}
record SnapshotCacheKey(ResourceLocation id, int tickBucket, int dimension, String sourceKey) {}
```

迁移顺序：

1. `SnapshotProvider` 作为 compatibility adapter 接入 `SnapshotSource`
2. registry 从 `Map<ResourceLocation, SnapshotProvider>` 迁到 `Map<ResourceLocation, SnapshotSource>`
3. 静态贴图使用永久缓存，`te_*_cycle` 使用 `currentTick / 20` bucket
4. live GUI 缓存键加入 player inventory、dimension、gui class、tile class
5. sandbox capture 拆成不可变 source 与独立 capture session
6. registry 增加 `clear()` / rebuild 入口，接资源重载与数据刷新

## 7. 构建与兼容层整改方案

### 7.1 构建层

当前 CI 关键阻塞：

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

状态：已完成首轮

- `.github/workflows/build.yml` 已加入 `actions/cache@v4`
- 缓存路径覆盖 Unimined Minecraft 1.12.2 与 Gradle modules-2
- 缓存键包含 `minecraft-1.12.2`、`Cleanroom-FG3`、`0.5.6-alpha`
- 保留 JDK 25、Gradle cache、三次 Gradle retry

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
- keyboard、mouse、showcase render adapter 留在下一轮

#### P0-4：把残留 GL 直接操作迁出 `PonderDebugScreen`

目标：

- `drawSceneShadow`
- preview scissor
- preview 主渲染接线

全部迁入 renderer 或 render context

状态：部分完成

- speech box、connector、line segment 已由 `SpeechRenderer` 承担
- `PonderDebugScreen` 当前仍保留 `GlStateManager.disableLighting()` 等 preview 桥接
- 下一步先抽 `RenderContext`，再迁 `drawSceneShadow` 和 preview scissor

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
- `GLStateGuard` 覆盖 blend、alpha、texture、shade model、line width、color
- `ScissorStack` 统一坐标换算和 GL scissor 生命周期

状态：已完成基础层

- `foundation/ui/render/RenderContext.java` 已落地
- `foundation/ui/render/GlRenderContext.java` 已落地
- `foundation/ui/render/GLStateGuard.java` 已落地
- `foundation/ui/render/ScissorStack.java` 已落地
- 现阶段先提供桥接能力，renderer 迁移留在下一轮

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
- registry 具备 clear / rebuild 入口
- 旧 `SnapshotProvider#get(float)` 调用面保留 compatibility adapter

状态：已完成基础层

- `SnapshotSource` sealed interface 已落地
- `SnapshotContext` 已落地
- `SnapshotCacheKey` 已落地
- `SnapshotInvalidationPolicy` 已落地
- `SnapshotProvider#asSource()` 已提供 compatibility adapter
- `PonderGuiSnapshotRegistry` 内部存储已切换到 `SnapshotSource`
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
- external scene 执行已拆成 scene / world / overlay 三类执行器

#### P2-1：补 `compile` 阶段，显式产出 `CompiledSceneBundle`

状态：已完成首轮

- `CompiledSceneBundle` 已落在 `foundation/external/register`
- `ExternalSceneRegistrationService` 已经先 compile 再注册 component/storyboard/order
- 后续继续补结构化 diagnostic 和 source map

#### P2-2：引入结构化诊断对象

#### P2-3：引入 `RegistrationCommand`

状态：已完成首轮

- `RegistrationCommands` 已落地
- `RegistrationCommandService` 已落地
- `PonderSceneRegistry`、`PonderTagRegistry`、`PonderLocalization` 内部写入已走 command

#### P2-4：把 `PonderIndex.reload()` 收口成编排器

下一步：

- reload 只组织 scan、parse、validate、compile、register
- 每个阶段返回结构化结果
- 失败信息统一进入 diagnostic sink

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

当前 Gate C 前置任务：

- 修复 Cleanroom 依赖解析源或缓存策略
- 保留远程三次重试
- 依赖解析成功后读取 GitHub Actions Java 编译日志

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
2. 引入 `RenderContext + GLStateGuard`
3. 迁移 `CompatGuiScreen`、`DrawContext`、`DebugPanelRenderer`、`OverlayRenderer`
4. 引入 `SceneProjectionContext + OverlayPlacementEngine`
5. 升级 snapshot 体系的来源、失效、缓存语义
6. 合并 showcase / debug theme 到 `PonderTheme`
7. external 继续补结构化 diagnostic 与 source map
8. registration 继续补 command/query split
9. 构建层和 shim 层收尾

这条顺序能先消灭当前最高频的耦合点，再推进更深层的现代化改造。

## 12. 下一批子代理切片

新派发子代理统一使用 `gpt-5.4-mini-pixel`。

建议并行切片：

1. CI 依赖整改：实现 Unimined minecraft 1.12.2 缓存策略，触发远程 CI
2. UI Host adapter：落 `PonderDebugScreenHostSupport` 和 scene/keyboard/mouse adapter
3. RenderContext：落 `GLStateGuard`、`ScissorStack`、`DrawContext` bridge
4. Projection：落 `SceneProjectionContext`、`ProjectedBounds`、三类 placement record
5. Snapshot：落 `SnapshotSource`、`SnapshotContext`、`SnapshotCacheKey`
6. Theme：落 `PonderTheme`、`SymbolicColor`、`PonderThemes` preset

主代理负责范围控制、冲突处理、文档同步、远程 CI 验证。
