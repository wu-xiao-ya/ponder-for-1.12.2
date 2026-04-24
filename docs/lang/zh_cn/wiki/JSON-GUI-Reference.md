# GUI 与交互参考

当前已支持的 GUI 相关操作：

- `gui_texture`
- `gui_snapshot`
- `block_gui`
- `machine_gui`
- `gui_outline_text`
- `gui_interaction`
- `interactionDefinitions`

## `gui_texture`

把贴图区域绘制为 GUI 叠层。适合稳定的静态界面和自定义面板。

常用字段：

- `texture`：资源路径，省略 `textures/` 和 `.png` 时会自动补全
- `regionWidth`、`regionHeight`：源区域尺寸
- `displayWidth`、`displayHeight`：显示尺寸
- `id`：叠层 id，供后续高亮引用
- `pointAt`、`placeNearTarget`、`independentY`、`offsetX`、`offsetY`

## `gui_snapshot`

按 id 绘制已注册的 GUI 快照。内置示例包含原版熔炉和 Thermal Expansion 机器快照。

```json
{
  "type": "gui_snapshot",
  "id": "furnace_gui",
  "snapshot": "ponder:gui_snapshot/minecraft_furnace_live",
  "duration": 80,
  "pointAt": [1.5, 1.0, 1.5],
  "placeNearTarget": true
}
```

## `block_gui` / `machine_gui`

通过隐藏沙盒方块打开真实方块 GUI，捕获打开后的界面，并渲染到 Ponder 场景里。
`machine_gui` 是 `block_gui` 的别名。

```json
{
  "type": "block_gui",
  "id": "machine_gui",
  "blockGui": "thermalexpansion:machine",
  "meta": 0,
  "nbt": { "Energy": 12000 },
  "guiWidth": 198,
  "guiHeight": 166,
  "duration": 100,
  "pointAt": [1.5, 1.0, 1.5],
  "placeNearTarget": true
}
```

必填字段：

- `blockGui`：用于打开 GUI 的方块 id
- `guiWidth`、`guiHeight`：GUI 逻辑尺寸

可选字段：

- `meta`：方块 metadata，默认 `0`
- `nbt`：打开 GUI 前合并到沙盒 TileEntity 的 NBT
- `id`：叠层 id，供 `gui_outline_text` 引用
- `pointAt`、`placeNearTarget`、`independentY`、`offsetX`、`offsetY`

## `gui_outline_text`

在父 GUI 叠层内部高亮一个矩形区域，并绑定说明文字。

```json
{
  "type": "gui_outline_text",
  "gui": "machine_gui",
  "guiX": 54,
  "guiY": 24,
  "guiWidth": 22,
  "guiHeight": 22,
  "duration": 60,
  "text": "输入槽"
}
```

## `gui_interaction`

执行 `interactionDefinitions` 中定义的复用 GUI 步骤序列，也可以直接执行内联 `steps`。
