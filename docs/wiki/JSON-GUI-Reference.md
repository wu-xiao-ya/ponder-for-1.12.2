# GUI 与交互参考

## 1. 适用范围

当前 1.12.2 分支额外扩展了 GUI 相关 JSON 能力：

- `gui_texture`
- `gui_snapshot`
- `gui_outline_text`
- `gui_interaction`
- `interactionDefinitions`

这部分不是高版本原生官方 JSON 规范，而是本分支扩展。

## 2. `interactionDefinitions`

如果一段 GUI 演示要复用，推荐写成：

```json
{
  "interactionDefinitions": [
    {
      "id": "furnace_live_demo",
      "defaults": {
        "offsetY": -10
      },
      "steps": [
        {
          "type": "gui_snapshot",
          "snapshot": "ponder:gui_snapshot/minecraft_furnace_live",
          "id": "main_gui",
          "duration": 80
        },
        {
          "type": "gui_outline_text",
          "gui": "main_gui",
          "guiX": 56,
          "guiY": 17,
          "guiWidth": 66,
          "guiHeight": 52,
          "duration": 80,
          "color": "BLUE",
          "text": "这里是熔炉 GUI 的主要加工区域。"
        }
      ]
    }
  ]
}
```

也支持使用别名：

- `guiInteractions`

## 3. `gui_interaction`

调用方式：

```json
{
  "type": "gui_interaction",
  "interaction": "ponder:furnace_live_demo",
  "id": "demo",
  "pointAt": [2, 1, 2],
  "pointMode": "center",
  "placeNearTarget": true
}
```

如果不想抽定义，也可以直接内联 `steps`。

## 4. `gui_snapshot`

最常用字段：

- `snapshot`
- `id`
- `duration`
- `offsetX`
- `offsetY`
- `guiX`
- `guiY`
- `parentGui`
- `scaleToParent`
- `framed`

示例：

```json
{
  "type": "gui_snapshot",
  "snapshot": "ponder:gui_snapshot/minecraft_furnace_live",
  "id": "main_gui",
  "duration": 80,
  "offsetY": -10
}
```

## 5. `gui_outline_text`

它本质上是对某个 GUI 区域画高亮并附文本。

常用字段：

- `gui`
- `guiX`
- `guiY`
- `guiWidth`
- `guiHeight`
- `duration`
- `color`
- `text`

示例：

```json
{
  "type": "gui_outline_text",
  "gui": "main_gui",
  "guiX": 56,
  "guiY": 17,
  "guiWidth": 66,
  "guiHeight": 52,
  "duration": 80,
  "color": "BLUE",
  "text": "这里是熔炉 GUI 的主要加工区域。"
}
```

## 6. 当前已接好的 live snapshot

当前仓库内至少已确认这些 live snapshot key：

- `ponder:gui_snapshot/minecraft_furnace_live`
- `ponder:gui_snapshot/te_<machine>_live`
- `ponder:gui_snapshot/te_<machine>_panel_config_live`
- `ponder:gui_snapshot/te_<machine>_panel_augment_live`
- `ponder:gui_snapshot/te_<machine>_panel_redstone_live`

注册入口在：

- [`PonderGuiSnapshotRegistry.java`](../../src/main/java/net/createmod/ponder/foundation/ui/PonderGuiSnapshotRegistry.java)

## 7. 使用建议

GUI 这组能力建议只在这些场景下使用：

- 真实 GUI 本身就是教程重点
- 需要演示二级侧栏
- 普通世界方块演示不够表达结构

如果只是基础机器流程，优先仍然建议：

- 方块演示
- 文字
- 区域高亮

GUI 交互适合放在后半幕，而不是一开始就占满整场。
