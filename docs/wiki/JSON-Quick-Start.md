# 快速开始

## 1. 文件位置

推荐把场景 JSON 放在：

- `run/scripts/ponder/...`

例如：

- `run/scripts/ponder/examples/json_tutorial/01_minimal_scene.json`

加载脚本放在：

- `run/scripts/*.zs`

例如：

```zenscript
import mods.ponder.SceneFiles;

SceneFiles.clearQueuedJson();
SceneFiles.loadJson("examples/json_tutorial/01_minimal_scene.json");
SceneFiles.reloadPonder();
```

## 2. 最小可用 JSON

```json
{
  "namespace": "ponder",
  "component": "minecraft:crafting_table",
  "schematic": "ponder:debug/runtime_test",
  "sceneId": "example_scene",
  "title": "示例场景",
  "tags": ["ponder:debug"],
  "operations": [
    { "type": "base_plate", "xOffset": 0, "zOffset": 0, "size": 7 },
    { "type": "show_base_plate" },
    { "type": "set_block", "pos": [2, 1, 2], "state": "minecraft:crafting_table" },
    {
      "type": "text",
      "duration": 60,
      "color": "WHITE",
      "text": "这是一个最小场景。",
      "pointAt": [2, 1, 2],
      "pointMode": "center",
      "placeNearTarget": true
    },
    { "type": "mark_finished" }
  ]
}
```

## 3. 顶层结构

当前支持三种根结构：

1. 单场景对象
2. 场景数组
3. 总对象

推荐统一使用“总对象”：

```json
{
  "namespace": "ponder",
  "tagDefinitions": [],
  "interactionDefinitions": [],
  "scenes": []
}
```

## 4. 推荐起步顺序

先只用这些操作：

- `base_plate`
- `show_base_plate`
- `set_block`
- `set_blocks`
- `text`
- `outline_text`
- `idle`
- `mark_finished`

确认能正常加载之后，再加：

- `scene_scale`
- `scene_offset_y`
- `rotate_camera_y`
- `show_section`
- `hide_section`
- GUI 类操作

## 5. 调试建议

当前分支最稳的验证方式：

1. 先写一个只放原版方块的最小场景
2. 确认组件能出现在浏览器里
3. 再往场景里逐步加操作
4. 一次只加一类能力

如果一份 JSON 失效，优先检查：

- `component`
- `schematic`
- `sceneId`
- `operations`
- JSON 编码是否为 `UTF-8`
