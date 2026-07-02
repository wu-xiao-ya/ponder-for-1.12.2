# Ponder 渲染与视觉技术侦察报告

日期：2026-06-24
工作区：E:\mc_modding\Ponder_refactor_plan
来源仓库：E:\mc_modding\Ponder
目的：为 Guide/GuideNova 渲染参考落地提供模块边界清单、CRL + Java 25 渲染层重构方向、可写入规划文件的短句技术条目
范围：不动代码，只输出侦察结论
---

## 1. 当前渲染/场景/UI/文本表现相关模块边界

### 1.1 包结构快照

| 包路径 | 职责 |
|---|---|
| foundation/ui/ | Screen 层：PonderDebugScreen/PonderUI/IndexScreen/TagScreen |
| foundation/PonderScene.java | 运行时数据仓库：11 种事件录制 |
| foundation/PonderSceneBuilder.java | 场景编程入口：Overlay/World/Effect/Special 实现 |
| foundation/PonderSceneBuildingUtil.java | 场景辅助工具：selection/vector/position |
| foundation/PonderSchematic.java | NBT 方块蓝图加载 |
| foundation/PonderStoryBoardEntry.java | storyboard 注册模型 |
| api/scene/ | 场景编程 API：SceneBuilder/OverlayInstructions 等 14 个接口 |
| api/element/ | 场景元素 API：Text/Input/GuiSnapshot/WorldSection/Entity/Parrot/Minecart |
| api/PonderPalette.java | 11 色调色板枚举 |
| api/ParticleEmitter.java | 粒子发射函数接口 |
| catnip/math/Pointing.java | UP/LEFT/DOWN/RIGHT 枚举 |
| client/ClientProxy.java | 客户端入口：按键 + tooltip + 开屏队列 + JEI 兼容 |

### 1.2 Screen 层（foundation/ui/）

| 文件 | 行数 | 核心职责 |
|---|---|---|
| PonderDebugScreen | ~3600 | **超大类**。3D 场景渲染、overlay 绘制、preview、actor、播放控制、caption |
| PonderUI | ~200 | 继承 PonderDebugScreen，showcase 模式：header/backdrop/logo/group popup |
| PonderIndexScreen | ~420 | 组件浏览器：标签面板 + 搜索 + 网格布局 |
| PonderTagScreen | ~210 | 单标签视图：摘要 + 关联组件网格 |
| AbstractPonderBrowserScreen | ~200 | 浏览器基类：面板/slot/item 渲染、背景 |
| CompatGuiScreen | ~200 | Forge 1.12.2 obf 兼容：drawGradientRect/drawRect/drawTexturedModalRect |
| PonderSceneRuntime | ~580 | Runtime state builder + RenderTransform 应用 |
| PonderScenePreview | ~120 | 2D 顶视 preview 状态计算 |

### 1.3 渲染管道

PonderScene 录制事件 -> PonderSceneRuntime.buildState(tick) -> PonderDebugScreen 绘制

绘制顺序：
1. enablePreviewScissor
2. 设置相机（yaw/pitch/zoom + 旋转）
3. 遍历 blocksByPosition -> BlockRendererDispatcher.renderBlockModel
4. applyRenderTransforms (GlStateManager.translate/rotate)
5. 淡入淡出透明度、breaking overlay、TileEntityRenderer
6. disablePreviewScissor
7. overlay 绘制
8. actor 绘制
9. preview 面板
10. playback bar + caption

### 1.4 Overlay 系统

| Overlay 类型 | API 入口 | 实现位置 |
|---|---|---|
| 文本 + 对话气泡 | TextElementBuilder -> showText | PonderDebugScreen ~500 行 |
| 轮廓包围盒 | chaseBoundingBoxOutline | PonderDebugScreen |
| 选中轮廓 | showOutline | PonderDebugScreen |
| 线（普通/加粗） | showLine / showBigLine | PonderDebugScreen |
| GUI 截图 | showGuiSnapshot | PonderDebugScreen + GuiSnapshotRegistry |
| 输入控制器 | showControls | PonderDebugScreen |
| scroll/filter 输入 | showScrollInput / showFilterSlotInput | PonderDebugScreen |

### 1.5 GUI Snapshot 实现

| 实现 | 方式 |
|---|---|
| PonderGuiSnapshotRegistry | snapshotId -> Snapshot(texture+uv+renderer) |
| Snapshot.fullTexture | 纯纹理截图 |
| Snapshot.liveRenderer | 自定义绘制回调 |
| EmbeddedGuiFurnaceSnapshot | 反射构造 Furnace GUI + TE 实时渲染 |
| EmbeddedReflectiveGuiSnapshot | 按类名反射任意 TE + GUI 实时渲染 |
| SandboxTriggeredBlockGuiSnapshot | 沙箱触发的 block GUI |

### 1.6 已知问题

- **PonderDebugScreen 超大类**：3600 行融合 screen 编排 + 3D 渲染 + overlay + preview + actor + 播放控制
- **GlStateManager 直接操作**：所有 3D 变换使用 OpenGL 矩阵栈
- **Tessellator 直接使用**：自定义几何体走 BufferBuilder + GL11
- **反射强依赖**：GUI snapshot 反射构造 GUI/TE，无编译期类型安全
- **缺少渲染状态抽象**：没有独立 RenderState，全部就地计算
- **字体不可配置**：硬绑定 Minecraft fontRenderer
- **颜色系统极简**：PonderPalette 只有 11 个固定色
- **lwjglx 桥接依赖**：启用 enable_lwjglx，OpenGL 调用经过桥接层
- **ScaledResolution 冗余计算**：PreviewLayout 和 ScaledResolution 多处重复计算

---

## 2. 适合 CRL + Java 25 的渲染层重构方向

### 2.1 渲染状态数据模型化

推荐三段管道：RuntimeState -> RenderState（不可变 record）-> Renderer（消费 RenderState）

### 2.2 Scene Renderer 从 Screen 独立
从 PonderDebugScreen 提取 SceneRenderer、OverlayRenderer、ActorRenderer、PreviewRenderer、PlaybackController。Screen 只保留编排职责。

### 2.3 Overlay 模型升级为 sealed 类族
sealed interface OverlayEventModel + record 变体 + switch + pattern matching 替代 if-if-if。

### 2.4 GUI Snapshot 统一抽象
SnapshotSource -> SnapshotProvider -> SnapshotRenderer -> SnapshotRenderContext。

### 2.5 字体与文本排版层
TextRenderModel record + FontRendererAdapter 接口（默认绑 fontRenderer）。

### 2.6 颜色系统扩展
PonderPalette -> Theme 可注册主题色表 + PaletteEntry(semanticKey, color)。

### 2.7 摄像机系统独立
CameraState record + CameraInstruction sealed interface + easingFunction。

### 2.8 动画引擎统一
EasingFunction 接口 + AnimationController 调度 section fade/camera rotate/actor move。

### 2.9 Java 25 编码风格方案
- record 承载所有 RenderState 和 OverlayModel
- sealed interface 封装 overlay 和 snapshot 类型族
- switch + pattern matching 替代 type 分发
- instanceof pattern matching 替代 isAssignableFrom

---

## 3. 可直接写入规划文件的技术条目（短句、可执行）

### 3.1 模型提取
- 新增 foundation/ui/model/ 包：PlaybackState、PreviewState、OverlayState、SceneRenderState、CameraState 等不可变 record
- OverlayModel sealed interface，列出所有 overlay 变体 record
- SnapshotSource sealed interface，统一 snapshot 来源抽象
- CameraState record：yaw/pitch/zoom/offset/orbit 约束
- TextRenderModel record：文本 + 颜色 + 对齐 + 最大宽 + 锚点
- EasingFunction 接口与 AnimationSpec record

### 3.2 Renderer 提取
- 从 PonderDebugScreen 提取 SceneRenderer：3D 方块场景渲染
- 从 PonderDebugScreen 提取 OverlayRenderer：overlay 文本/轮廓/线/input icon
- 从 PonderDebugScreen 提取 ActorRenderer：鹦鹉/矿车/物品实体
- 从 PonderDebugScreen 提取 PreviewRenderer：2D 顶视 preview 面板
- 从 PonderDebugScreen 提取 PlaybackController：播放/暂停/scrub/速度

### 3.3 Snapshot 体系统一
- Snapshot 改造为 sealed interface + record 变体
- SnapshotRenderContext：screen 坐标 + scissor + zLevel 上下文
- EmbeddedGuiFurnaceSnapshot -> 实现 SnapshotSource 接口
- EmbeddedReflectiveGuiSnapshot -> 实现 SnapshotSource 接口
- TextureSnapshotSource 替代 Snapshot.fullTexture
- LiveRendererSource 替代 Snapshot.liveRenderer

### 3.4 Overlay 模型升级
- sealed interface + record 替代 PonderScene.OverlayEvent 松散字段
- TextElementBuilder 产出 -> TextOverlayModel record
- InputElementBuilder 产出 -> InputIconOverlayModel record
- GuiSnapshotBuilder 产出 -> GuiSnapshotOverlayModel record
- overlay 渲染改走 switch (overlay) { case TextOverlayModel t -> ... }

### 3.5 摄像机系统
- CameraInstruction sealed interface：rotateCameraY/setSceneOffsetY/scaleSceneView 统一
- 支持 scene 初始视角配置（yaw/pitch/zoom）
- 摄像机插值使用 EasingFunction，不再硬编码 18tick 线性

### 3.6 动画引擎
- AnimationController 统一调度 section fade / camera rotate / actor move
- section 淡入淡出支持自定义时长和缓动曲线
- actor 移动/旋转使用统一动画调度
- section 变换支持 rotate 轴缓动（当前无缓动）

### 3.7 颜色与主题
- PaletteEntry record：语义名 + 颜色值
- PonderPalette 改为实现 PaletteEntry，不再直接是 enum
- Theme 接口：默认主题 + 可注册扩展
- UI 颜色引用全部走 theme.getColor(semanticKey)，不再硬编码 int

### 3.8 诊断与调试
- DiagnosticOverlay model：渲染层诊断信息
- DiagnosticRenderer：FPS/tick/scene stat 覆盖层
- 渲染事件录制：记录每帧渲染 state/hash/timing

### 3.9 资源与文本
- PonderPalette 颜色值改为可从 lang 文件覆盖
- 字体渲染抽为 FontRendererAdapter 接口（默认绑 Minecraft FontRenderer）
- GUI snapshot texture 路径改为 assets/<mod>/ponder/textures/gui_snapshot/ 统一约定

### 3.10 清理清单
- PonderDebugScreen 删除 ~3000 行渲染代码（提取后只剩 screen 编排）
- 从 ClientProxy 提取 PonderTooltipHandler 和 PonderScreenQueue
- 删除 PonderSceneRuntime.applyRenderTransforms 对 GlStateManager 的直接依赖
- 清理 ScaledResolution 冗余计算

---

## 4. GuideNova 可借鉴渲染点速览

1. **内容作者工作流优先**：场景 UI 定位应以作者写场景 + 运行时播放为主线，screen 与 renderer 分离
2. **数据契约独立**：overlay model 和 render state 应成为独立层，与录制数据解耦
3. **工具链与运行时分层**：GUI snapshot 生成/录制/缓存从 screen 解耦到 service 层
4. **效果组织清晰**：每个渲染效果应对应自己的 model + renderer，而不是散落在 if-if-if 中
