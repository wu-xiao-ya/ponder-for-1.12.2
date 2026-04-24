# 快速开始

- 把 JSON 文件放在 `run/scripts/ponder/...`
- 通过 `mods.ponder.SceneFiles` 加载
- 先写最小场景，再逐步加 GUI 或复杂 runtime 行为

## CraftTweaker 入口

```zenscript
mods.ponder.SceneFiles.loadJson("ponder/my_scene.json");
mods.ponder.SceneFiles.reloadPonder();
```

迭代调试时可以使用 `loadJsonAndReload(path)` 一次完成加入队列和重载。

## 最小结构

```json
{
  "namespace": "example",
  "component": "minecraft:furnace",
  "schematic": "debug/scene_1",
  "sceneId": "furnace_intro",
  "title": "Furnace basics",
  "operations": [
    { "type": "base_plate", "xOffset": 0, "zOffset": 0, "size": 3 },
    { "type": "show_base_plate" },
    { "type": "text", "duration": 60, "text": "This is a Ponder scene." }
  ]
}
```
