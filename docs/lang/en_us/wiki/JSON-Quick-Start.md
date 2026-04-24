# Quick Start

- Put JSON files under `run/scripts/ponder/...`
- Load them through `mods.ponder.SceneFiles`
- Start with a minimal scene before adding GUI or complex runtime behavior

## CraftTweaker Entry

```zenscript
mods.ponder.SceneFiles.loadJson("ponder/my_scene.json");
mods.ponder.SceneFiles.reloadPonder();
```

Use `loadJsonAndReload(path)` during iteration when you want to queue and reload in one call.

## Minimal Shape

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
