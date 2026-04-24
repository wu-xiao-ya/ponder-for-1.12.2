# Ponder 1.12.2 JSON Tutorial

This document describes the external JSON scene format currently supported by
`crl_ponder` on Minecraft `1.12.2 / Cleanroom`.

The goal is not to mirror the modern upstream storyboard code API one-to-one.
The goal is to provide a stable JSON DSL that is practical for this branch.

## File Placement

Examples:

- [01_minimal_scene.json](../../examples/json_tutorial/01_minimal_scene.json)
- [02_redstone_and_nbt.json](../../examples/json_tutorial/02_redstone_and_nbt.json)
- [03_gui_interaction.json](../../examples/json_tutorial/03_gui_interaction.json)
- [04_supported_features_no_gui.json](../../examples/json_tutorial/04_supported_features_no_gui.json)

## Stable Operations

### Scene Control

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

### World

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

### Overlay and Signals

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

`block_gui` opens a real block GUI through a hidden sandbox block and renders the captured screen inside Ponder.
Use it for machine GUIs that need the mod's actual container/client screen code.

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

`machine_gui` is an alias for `block_gui`.
The GUI can be referenced by later `gui_outline_text` operations through the `id` field.

## Examples

- [01_minimal_scene.json](../../examples/json_tutorial/01_minimal_scene.json)
- [02_redstone_and_nbt.json](../../examples/json_tutorial/02_redstone_and_nbt.json)
- [03_gui_interaction.json](../../examples/json_tutorial/03_gui_interaction.json)
- [04_supported_features_no_gui.json](../../examples/json_tutorial/04_supported_features_no_gui.json)
