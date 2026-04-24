# Ponder 1.12.2 JSON 教程

这份文档说明当前 `crl_ponder` 在 `Minecraft 1.12.2 / Cleanroom` 环境下支持的外部 JSON 场景写法。

目标不是一比一复刻高版本 storyboard 代码 API，而是提供一套适合当前分支、稳定可用的 JSON DSL。

## 文件位置

示例：

- [01_minimal_scene.json](../../examples/json_tutorial/01_minimal_scene.json)
- [02_redstone_and_nbt.json](../../examples/json_tutorial/02_redstone_and_nbt.json)
- [03_gui_interaction.json](../../examples/json_tutorial/03_gui_interaction.json)
- [04_supported_features_no_gui.json](../../examples/json_tutorial/04_supported_features_no_gui.json)

## 当前稳定可用操作

### 场景控制

- `base_plate`
- `show_base_plate`
- `scene_scale`
- `scene_offset_y`
- `rotate_camera_y`
- `remove_shadow`
- `next_up`
- `idle`
- `keyframe`
- `lazy_keyframe`
- `mark_finished`

### 世界方块

- `set_blocks`
- `set_block`
- `replace_blocks`
- `show_section`
- `hide_section`
- `restore_blocks`
- `destroy_block`
- `break_progress`
- `move_section`
- `rotate_section`
- `toggle_redstone_power`

### 文本与提示

- `text`
- `outline_text`
- `indicate_redstone`
- `indicate_success`

### GUI

- `gui_texture`
- `gui_snapshot`
- `block_gui`
- `machine_gui`
- `gui_outline_text`
- `gui_interaction`
- `sequence`

`block_gui` 会通过隐藏沙盒方块打开真实方块 GUI，并把捕获到的界面渲染到思索场景里。
它适合展示需要机器自身 container/client screen 代码的 GUI。

```json
{
  "type": "block_gui",
  "id": "machine_gui",
  "blockGui": "thermalexpansion:machine",
  "meta": 0,
  "guiWidth": 198,
  "guiHeight": 166,
  "duration": 100,
  "pointAt": [1.5, 1.0, 1.5],
  "placeNearTarget": true
}
```

`machine_gui` 是 `block_gui` 的别名。
后续 `gui_outline_text` 可以通过 `id` 字段引用这个 GUI。

## 示例

- [01_minimal_scene.json](../../examples/json_tutorial/01_minimal_scene.json)
- [02_redstone_and_nbt.json](../../examples/json_tutorial/02_redstone_and_nbt.json)
- [03_gui_interaction.json](../../examples/json_tutorial/03_gui_interaction.json)
- [04_supported_features_no_gui.json](../../examples/json_tutorial/04_supported_features_no_gui.json)
