# Ponder foundation/ui 当前 Live 架构分析

日期：2026-06-28
来源工作区：E:\mc_modding\Ponder_refactor_plan
分析目标：src/main/java/net/createmod/ponder/foundation/ui（91 个 Java 文件，~11140 行）

---

## 一、现有稳定分层与类清单

当前 UI 包已从原始仓库的 27 个文件（PonderDebugScreen 2692 行为核心的泥团结构）重组为 91 个文件、8 个逻辑层。

### 1.1 screen/ —— 页面编排层

| 类名 | 行数 | 职责 | 继承/依赖 |
|------|------|------|-----------|
| `PonderDebugScreen.java` | 1437 | 调试浏览器：scene 选择、preview 编排、playback 控制、overlay 调度 | 继承 `CompatGuiScreen`，持有 state/controller/renderer 引用 |
| `PonderUI.java` | 277 | Showcase 入口：header、backdrop、logo、group popup | 继承 `PonderDebugScreen` |
| `PonderIndexScreen.java` | 366 | 组件浏览器：标签面板 + 搜索 + 网格布局 | 继承 `AbstractPonderBrowserScreen` |
| `PonderTagScreen.java` | 174 | 单标签视图：摘要 + 关联组件网格 | 继承 `AbstractPonderBrowserScreen` |
| `AbstractPonderBrowserScreen.java` | 167 | 浏览器基类：面板/slot/item 渲染、背景绘制 | 继承 `CompatGuiScreen` |
| `CompatGuiScreen.java` | 171 | Forge 1.12.2 兼容基类：`drawGradientRect`、`drawRect`、`drawTexturedModalRect` | 继承 `GuiScreen` |

### 1.2 controller/ —— 输入控制层

| 类名 | 行数 | 职责 |
|------|------|------|
| `PonderSceneController.java` | 149 | Scene 导航、component 选择、playback 跳转、缓存清理编排 |
| `DebugKeyboardController.java` | 79 | 调试模式键盘输入：R 重载、空格暂停、方向键选 scene |
| `DebugMouseController.java` | 103 | 调试模式鼠标输入：滚轮缩放/滚动、拖拽旋转 |
| `ShowcaseKeyboardController.java` | 69 | Showcase 模式键盘输入 |
| `ShowcaseMouseController.java` | 98 | Showcase 模式鼠标输入：group popup 切换、next-up 跳转 |

### 1.3 state/ —— 状态层

| 类名 | 行数 | 职责 | 特性 |
|------|------|------|------|
| `PonderSceneSelectionState.java` | 85 | Component/scene 选择、component 列表、compiled scenes | 可变，package-private |
| `PonderPlaybackState.java` | 33 | playbackTick、playing 标志、renderTick 计算 | 可变，package-private |
| `PonderPreviewCameraState.java` | 81 | yaw/pitch/zoom、drag 状态、showcase fade ticks | 可变，package-private |
| `LayoutCache.java` | 91 | **record**：preview layout、playback bar、header icon、group popup、next-up card 的 bounds 不可变快照 | Java 25 record |
| `InteractionHitCache.java` | 213 | 命中检测缓存：内嵌 PlaybackBarBounds / ShowcaseHeaderIconBounds / ShowcaseGroupPopupBounds / NextUpCardBounds 四个 record | 持有 LayoutCache + InteractionState |
| `InteractionState.java` | 13 | **record**：showcaseGroupSelectorOpen、playbackBarDragging | Java 25 record |
| `DebugPanelViewState.java` | 25 | Debug panel 的 component scroll 状态 | 可变 |

### 1.4 render/ —— 渲染层

| 类名 | 行数 | 职责 |
|------|------|------|
| `ScenePreviewRenderer.java` | 406 | 3D 方块/tile/actor 场景预览：BlockRendererDispatcher 调度、TE 缓存、actor 几何绘制 |
| `GuiOverlayRenderer.java` | 223 | GUI 纹理 overlay：framed panel、connector、highlight、stretch texture |
| `ActorOverlayRenderer.java` | 38 | Actor 俯视标记：方块指示、朝向线、标签文字 |
| `ShowcaseRenderer.java` | 213 | Showcase header 渲染：渐变背景、logo、标题、group 图标条 |
| `ShowcaseCaptionRenderer.java` | 129 | Showcase 字幕气泡：speech box 定位、connector、fade 计算 |
| `ShowcaseChromeRenderer.java` | 72 | Showcase 装饰元素：prev/next 卡片、进度指示 |
| `ShowcaseHudRenderer.java` | 128 | Showcase HUD：playback bar、时间标签、回放控制图标 |
| `SpeechRenderer.java` | 199 | 通用 speech box 基元：渐变框、connector、highlight、`SpeechPointing` 枚举、`Point` |
| `DebugPanelRenderer.java` | 247 | 调试面板：component 列表、operation 列表、world event 详情、scroll 计算 |

### 1.5 helper/ —— 布局与几何辅助层

| 类名 | 行数 | 职责 | 特性 |
|------|------|------|------|
| `PonderOverlayLayoutHelper.java` | 120 | 投影数学：`projectScenePoint()`、`projectSceneBounds()`、preview scale/anchor 计算 | 内嵌 `ProjectedBounds` |
| `PonderOverlayHelper.java` | 319 | Overlay 布局计算：`CaptionPlacement`、`GuiOverlayPlacement`、`GuiHighlightPlacement`、fade 计算 | 产出 placement 对象 |
| `PonderPreviewRenderHelper.java` | 62 | `getActualState` 兼容反射 + tile NBT 应用 |
| `PonderSceneRuntimeTypes.java` | 44 | **sealed interface** `TransformStep` + `MoveStep`/`RotateStep` record + `RuntimeBlockState` + `RuntimeState` record | Java 25 sealed |
| `ThermalMachinePreviewHelper.java` | 67 | 热力机器温度条 preview 渲染 |

### 1.6 cache/ —— 运行时与缓存层

| 类名 | 行数 | 职责 |
|------|------|------|
| `PonderSceneRuntime.java` | 534 | Runtime state 构建：block 可见性、fade、section 动画、actor 状态插值、camera yaw |
| `PreviewBlockAccess.java` | 69 | 实现 `IBlockAccess` 的轻量预览世界 |
| `PonderGuiSnapshotRegistry.java` | 166 | GUI snapshot 注册与检索 |
| `Snapshot.java` | 45 | Snapshot 数据模型：`FULL_TEXTURE`/`LIVE_RENDERER` 两种类型 |
| `SnapshotProvider.java` | 5 | `@FunctionalInterface`：`Snapshot provide(float currentTick)` |
| `SnapshotRenderer.java` | 5 | `@FunctionalInterface`：`void render(...)` |

### 1.7 model/ —— 数据模型层

| 类名 | 行数 | 职责 |
|------|------|------|
| `PonderScenePreview.java` | 93 | 2D 顶视 preview：`PreviewBounds`、`PreviewState`、`PreviewCellState` |
| `PonderClickRegion.java` | 18 | 泛型点击热区：`<T>` 值 + 矩形边界 |
| `PonderComponentEntry.java` | 31 | 组件条目模型：`componentId`、`label`、`displayStack`、`tags`、`sceneCount` |
| `ActorOverlayItem.java` | 25 | Actor overlay 数据：`baseColor`、`targetX/Y`、`yawDegrees`、`label` |
| `ShowcaseGroupModel.java` | 36 | Showcase 分组模型：`tag` + `componentIds` |
| `ShowcaseGroupIconHitBox.java` | 19 | Group icon 命中框 |
| `PreviewLayout.java` | 13 | Preview 区域布局模型 |
| `DrawContext.java` | 13 | 绘制上下文接口：`fillRect`、`drawString`、`drawLineSegment`、`renderItemStack` |

### 1.8 compat/ —— 兼容与快照层

| 类名 | 行数 | 职责 |
|------|------|------|
| `JeiScreenCompat.java` | 229 | JEI GUI 兼容 |
| `JeiGuiPropertiesInvocation.java` | 40 | JEI 属性面板反射调用 |
| `JeiGuiScreenHandlerInvocation.java` | 27 | JEI screen handler 反射调用 |
| `PlayerInventoryResolver.java` | 60 | 玩家背包解析 |
| `EmbeddedGuiFurnaceSnapshot.java` | 87 | 反射构造 Furnace GUI + TE 渲染 |
| `EmbeddedReflectiveGuiSnapshot.java` | 292 | 按类名反射任意 TE + GUI 实时渲染 |
| `EmbeddedReflectiveTabGuiSnapshot.java` | 387 | Tab 式 GUI snapshot（如 Creative Tab） |
| `SandboxTriggeredBlockGuiSnapshot.java` | 444 | 沙箱触发的 block GUI snapshot |

---

## 二、仍然耦合的热点与下一步抽离顺序

### 2.1 热点 1：`PonderDebugScreen.java`（1437 行）—— 核心编排残留

**问题：**
- 仍直接操作 `GlStateManager`（`enablePreviewScissor`、`drawSceneShadow`、`renderScenePreview` 方法）
- 仍持有与 `PonderPreviewCameraState` 重复的字段 `previewYaw`、`previewPitch`、`previewZoom`（通过 `resetPreviewCamera()` 同步但保留副本）
- `renderScenePreview()` 方法（~120 行）直接混合了 GL 状态管理、方块遍历、actor 投影、shadow 绘制——应完全委托给 `ScenePreviewRenderer`
- `drawSceenShadow` / `enablePreviewScissor` 应迁入 `ScenePreviewRenderer`
- 大量 `last*` 字段（`lastPlaybackBarX/Y`、`lastNextUpCardX/Y` 等）已可通过 `LayoutCache` 替代，但仍保留

**抽离顺序：**
1. 删除 `previewYaw`/`previewPitch`/`previewZoom` 副本，全部走 `PonderPreviewCameraState`
2. `drawSceneShadow` → 迁入 `ScenePreviewRenderer`
3. `enablePreviewScissor`/`disablePreviewScissor` → 迁入 `ScenePreviewRenderer`
4. `renderScenePreview` 主体逻辑 → 迁入 `ScenePreviewRenderer.renderFull()`
5. 删除所有 `last*` 字段，走 `InteractionHitCache.getLayout()`
6. 目标：PonderDebugScreen < 800 行

### 2.2 热点 2：`PonderSceneRuntime.java`（534 行）—— 直接 GL 状态操作

**问题：**
- `applyRenderTransforms()` 方法直接调用 `GlStateManager.translate/rotate`
- `SECTION_FADE_TICKS`、`CAMERA_ROTATE_TICKS` 硬编码常量，无动画引擎抽象
- `buildState()` 产出的 `RuntimeBlockState` 仍是可变对象

**抽离顺序：**
1. `applyRenderTransforms` 改为产出 `TransformStep` 列表，由 `ScenePreviewRenderer` 消费
2. 动画常量迁入独立的 `AnimationSpec` / `AnimationController`
3. `RuntimeBlockState` 改为不可变 record（需先稳定所有写入点）

### 2.3 热点 3：Snapshot source / capture 分层

**当前状态：**
- `SnapshotSource` 已升级为 sealed source 入口
- `ConstantSnapshotSource`、`ProviderSnapshotSource` 已独立为顶层 record
- `PonderGuiSnapshotRegistry` 已持有 source map 与 cache key map
- `EmbeddedReflectiveGuiSnapshot` 与 `EmbeddedReflectiveTabGuiSnapshot` 内部反射逻辑仍重

**抽离顺序：**
1. sandbox block GUI 拆成 source 与 capture session
2. `SnapshotRenderer` 扩展为带 `RenderContext` 的抽象
3. 反射 snapshot 类继续收敛 cache key 和 renderer state
4. 资源重载入口接 `PonderGuiSnapshotRegistry.rebuild()`

### 2.4 热点 4：JEI compat 嵌入 ui 包

**问题：**
- `JeiScreenCompat`（229 行）、`JeiGuiPropertiesInvocation`、`JeiGuiScreenHandlerInvocation` 属于第三方兼容代码，仍在 ui 主包
- 规划文档建议 `foundation/ui/compat/` 子包收容，尚未执行

**抽离顺序：**
1. 创建 `foundation/ui/compat/` 子包
2. 迁移 JEI 三类 + `PlayerInventoryResolver`
3. 确认 `GuiOverlayRenderer` 对 JEI 的弱依赖通过接口隔离

### 2.5 热点 5：`PonderOverlayHelper.java`（52 行）—— placement facade

**当前状态：**
- `PonderOverlayHelper` 当前作为兼容 facade 转发到 `OverlayPlacementEngine`
- `OverlayPlacementEngine` 集中 caption / gui overlay / highlight placement 计算
- `GuiOverlayRenderer`、`ShowcaseCaptionRenderer` 继续消费 projection record

**抽离顺序：**
1. 逐步把直接调用点从 facade 切到 `OverlayPlacementEngine`
2. `PonderOverlayLayoutHelper.ProjectedBounds` 升级为顶层 record
3. 统一 overlay renderer 的 placement 输入模型

---

## 三、参考 GuideNova 式现代引导体验的升级点

### 3.1 渲染管线：GL 直接操作 → RenderContext 抽象

**当前状态：** 3D preview 仍有 `GlStateManager` + `Tessellator` + `GL11` 桥接，2D renderer 已开始接入 `RenderContext`、`GlRenderContext`、`GLStateGuard`、`DrawContext` bridge。

**升级方向：** 建立完整 `RenderContext` 抽象层，参考 GuideNova 的 `MatrixStack` + `ScissorStack` 双栈模型：
- `RenderContext.push()` / `pop()` 矩阵栈
- `RenderContext.pushScissor()` / `popScissor()` 裁剪栈
- `GLStateGuard`（`AutoCloseable`）守卫 GL 状态恢复

**具体收益：**
- `ScenePreviewRenderer`（406 行）中 ~50 行 GL 状态管理代码可压缩为守卫模式
- 后续离屏渲染、snapshot 缓存绘制可共用同一抽象
- `PonderDebugScreen` 中残留的 GL 操作可彻底消除

### 3.2 动画系统：硬编码常量 → Easing + AnimationController

**当前状态：**
- `PonderSceneRuntime.SECTION_FADE_TICKS = 15`、`CAMERA_ROTATE_TICKS = 18` 全局常量
- Section fade 走 `MathHelper.clamp` 线性插值
- Actor bob/yaw swing 用 `MathHelper.sin` 硬编码

**升级方向：**
- `EasingFunction` 接口（linear、easeInOut、easeOutBounce 等）
- `AnimationSpec` record（时长、缓动、延迟、循环）
- `AnimationController` 统一调度 section fade / camera rotate / actor move

**具体收益：**
- Section 淡入淡出可支持自定义缓动（如弹性效果）
- 摄像机旋转可从 18 tick 线性改为 easeInOutCubic
- Actor 动画（bob、yaw swing、dance）参数可配置

### 3.3 布局系统：散落计算 → 统一 ProjectionContext

**当前状态：**
- `PonderOverlayLayoutHelper` 负责投影数学（`projectScenePoint`、`projectSceneBounds`）
- `PonderOverlayHelper` 负责 placement 布局计算（caption 避让、GUI panel 定位）
- `PonderDebugScreen` 仍直接计算 `lastPreviewLayout`
- 三处存在重复的 preview scale / anchor / scissor 计算

**升级方向：** 统一 `SceneProjectionContext`：
- 持有 `PreviewLayout`、`PreviewBounds`、camera state、scale
- `projectScenePoint(Vec3d) → Point`
- `projectSceneBounds(AxisAlignedBB) → ProjectedBounds`
- 所有 renderer 和 overlay helper 通过注入获得同一个 context

**具体收益：**
- Overlay 定位与 scene 3D 坐标的映射关系单一来源
- Caption 气泡避让计算复用同一投影上下文
- 减少 screen 内重复的 bounds 计算逻辑

### 3.4 输入反馈：散落 hit test → 统一 HitRegion 路由

**当前状态：**
- `InteractionHitCache` 已初步收口 `LayoutCache` + `InteractionState`
- 但 `PonderDebugScreen` 仍保留多个 `isMouseOver*` 方法和 `last*` 字段
- `DebugMouseController` 和 `ShowcaseMouseController` 各自独立处理滚轮/拖拽

**升级方向：**
- `HitRegionTree`：层级化的命中区域（preview > overlay > button > panel）
- 统一 `InputEvent` 路由：`mouseClicked → HitRegionTree.resolve() → handler`
- 拖拽状态标准化为 `DragContext` record

**具体收益：**
- 鼠标滚轮在 preview 区域的缩放逻辑不再散落在两个 controller 中
- 新增交互区域（如 tooltip、popup）只需注册到 HitRegionTree
- `last*` 字段可完全消除

### 3.5 颜色与主题：硬编码 int → Theme + SymbolicColor

**当前状态：**
- `PonderPalette`（api 层）只有 11 个固定色
- `PonderTheme`、`PonderThemes`、`SymbolicColor`、`ThemeMetric` 已承接 debug/showcase preset
- `ThemeResolver` 已读取 `assets/ponder/ponder_themes.json` 并以 Java preset 兜底
- `SpeechRenderer`、`GuiOverlayRenderer`、`ShowcaseCaptionRenderer` 仍有局部硬编码颜色

**升级方向：**
- `PonderTheme` + `SymbolicColor` 枚举（`panel.background`、`timeline.active`、`overlay.caption` 等）
- 资源文件 `ponder_themes.json` 驱动，并接资源重载
- 深浅模式切换

**具体收益：**
- 所有 UI 颜色从硬编码 int 改为语义 key
- `PonderPalette` 保留为默认主题入口
- 为后续自定义主题和可访问性铺路

### 3.6 相机系统：散落字段 → CameraInstruction sealed 族

**当前状态：**
- `PonderPreviewCameraState` 只管自由控制态（yaw/pitch/zoom/drag）
- 场景内摄像机事件（`PonderScene.CameraEvent`）由 `PonderSceneRuntime.getCameraYaw()` 处理
- 两者独立运作，无统一接口

**升级方向：**
- `CameraInstruction` sealed interface：`RotateTo`、`PanTo`、`ZoomTo`、`Reset`
- `CameraPreset`：`isometricNorthEast`、`isometricNorthWest`、`topPreview`
- 支持关键帧插值时指定 `EasingFunction`

### 3.7 虚拟世界：完整 World 继承 → 轻量 PonderLevel

**当前状态：**
- `PreviewBlockAccess` 实现 `IBlockAccess`，作为运行时轻量容器
- TE 渲染仍需绑定 `mc.world`
- snapshot TE 构造调用 `Block.createTileEntity(world, state)` 需要完整 World 引用

**升级方向：**
- `PonderLevel`：只读轻量世界容器，不继承 `World`
- World 级重接口通过 adapter 暴露
- 场景渲染和 snapshot 生成共用同一只读世界表面

---

## 四、阶段性交付物（P0 / P1 / P2）

### P0：结构补完 —— 消除重复字段和 GL 直接操作

| 编号 | 交付物 | 涉及文件 | 验收标准 |
|------|--------|----------|----------|
| P0-1 | 删除 `PonderDebugScreen` 中 `previewYaw/pitch/zoom` 副本字段 | `PonderDebugScreen.java` | 仅通过 `PonderPreviewCameraState` 读写相机 |
| P0-2 | `drawSceneShadow` / scissor 方法迁入 `ScenePreviewRenderer` | `PonderDebugScreen.java`、`ScenePreviewRenderer.java` | `PonderDebugScreen` 不再直接调用 `GL11.glScissor` |
| P0-3 | 删除 `PonderDebugScreen` 中所有 `last*` 字段，走 `LayoutCache` | `PonderDebugScreen.java`、`InteractionHitCache.java` | `lastPlaybackBarX/Y` 等 12 个字段删除 |
| P0-4 | `Snapshot` 从 POJO 升级为 record | `Snapshot.java` | public final 字段改为 record 组件 |
| P0-5 | `PonderSceneRuntime.applyRenderTransforms` 不再直接操作 GL | `PonderSceneRuntime.java`、`ScenePreviewRenderer.java` | Transform 计算与 GL 执行分离 |

**P0 完成后 `PonderDebugScreen` 预期行数：< 1400 行**

### P1：接口升级 —— sealed 化 + 动画引擎 + 颜色主题

| 编号 | 交付物 | 涉及文件 | 验收标准 |
|------|--------|----------|----------|
| P1-1 | `SnapshotSource` + `SnapshotRenderer` 扩展为 sealed/source 族 | `SnapshotSource.java`、`ConstantSnapshotSource.java`、`ProviderSnapshotSource.java`、4 个 `Embedded*Snapshot.java` | registry 通过 source/cache key 管理 snapshot |
| P1-2 | 引入 `AnimationSpec` record + `EasingFunction` 接口 | 新建 `foundation/ui/animation/` | section fade / camera rotate 走缓动 |
| P1-3 | 统一 `SceneProjectionContext` 与 `OverlayPlacementEngine` | `projection/`、`overlay/OverlayPlacementEngine.java`、`PonderOverlayHelper.java` | overlay/renderer 通过投影上下文和 placement record 协作 |
| P1-4 | 统一 `SpeechPointing`，删除 `PonderOverlayHelper` 中重复枚举 | `SpeechRenderer.java`、`PonderOverlayHelper.java` | 只有一份 `SpeechPointing` 定义 |
| P1-5 | `PonderTheme` + `SymbolicColor` + `ThemeResolver` 语义色系统 | `PonderTheme.java`、`PonderThemes.java`、`ponder_themes.json` | debug/showcase theme 由 preset 与资源 overlay 共同驱动 |

**P1 完成后 `PonderDebugScreen` 预期行数：< 1000 行**

### P2：架构进化 —— 虚拟世界 + 双栈渲染 + 缓存分层

| 编号 | 交付物 | 涉及文件 | 验收标准 |
|------|--------|----------|----------|
| P2-1 | 引入 `RenderContext` 全能力（push/pop、scissor 栈、GLStateGuard） | `foundation/ui/render/`、`DrawContext.java`、`DebugPanelRenderer.java` | renderer 逐步迁入 RenderContext primitive |
| P2-2 | `RuntimeBlockState` 改为不可变 record | `PonderSceneRuntimeTypes.java`、`PonderSceneRuntime.java` | 所有写入点收口到 builder |
| P2-3 | 建立 `PonderLevel` 轻量世界容器 | 新建 `foundation/ui/world/` | Preview 和 snapshot 共用只读世界 |
| P2-4 | JEI compat 迁移到 `foundation/ui/compat/` 子包 | `JeiScreenCompat.java` 等 4 个文件 | 主渲染链不 import JEI 类型 |
| P2-5 | `HitRegionTree` 统一输入路由 | 新建，替换散落 `isMouseOver*` | 新增交互区域只需注册 |

**P2 完成后 `PonderDebugScreen` 预期行数：< 800 行，完整实现规划文档的六子域结构**

---

## 五、当前重构进度评估

**已完成：**
- State 分离：5 个独立状态类（Selection / Playback / Camera / Layout / Interaction）✅
- Controller 分离：5 个输入控制器，通过 `Host` 接口与 Screen 通信 ✅
- Renderer 分离：9 个 renderer 类，职责明确 ✅
- Java 25 特性启用：`LayoutCache`（record）、`InteractionState`（record）、`TransformStep`（sealed interface）、`RuntimeState`（record）✅
- `PonderDebugScreen` 从 2692 行缩减至 1437 行（-47%）✅
- `OverlayPlacementEngine` 已落地，`PonderOverlayHelper` 已收口为 facade ✅
- `SnapshotSource` 已 sealed 化，`ConstantSnapshotSource` / `ProviderSnapshotSource` 已独立 ✅
- `PonderTheme` / `PonderThemes` / `ThemeResolver` 与 `ponder_themes.json` 已落地 ✅
- `ExternalRegistrationDiagnostic` 已接入 external register 链 ✅

**进行中 / 待完成：**
- Screen 内 GL 直接操作残留（scissor、shadow 还在 screen 内）⚠️
- 重复相机字段（`previewYaw/pitch/zoom` 副本）⚠️
- 12 个 `last*` 字段尚未通过 `LayoutCache` 消除 ⚠️
- Snapshot capture session 与 source 的分离仍需继续深化 ⚠️
- `PonderSceneRuntime.applyRenderTransforms` 仍直接操作 GL ⚠️

**建议优先进攻下一批**：preview GL bridge、snapshot capture session、Theme 热重载、diagnostic 汇总 sink。
