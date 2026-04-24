# GUI Reference

Supported GUI-related operations:

- `gui_texture`
- `gui_snapshot`
- `block_gui`
- `machine_gui`
- `gui_outline_text`
- `gui_interaction`
- `interactionDefinitions`

## `gui_texture`

Draws a texture region as a GUI overlay. Use it for stable static screens and custom panels.

Common fields:

- `texture`: resource location, with `textures/` and `.png` added automatically when omitted
- `regionWidth`, `regionHeight`: source region size
- `displayWidth`, `displayHeight`: rendered size
- `id`: overlay id for later highlight operations
- `pointAt`, `placeNearTarget`, `independentY`, `offsetX`, `offsetY`

## `gui_snapshot`

Draws a registered snapshot by id. Built-in examples include Minecraft furnace and Thermal Expansion machine snapshots.

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

Opens a real block GUI through a hidden sandbox block, captures the resulting screen, and renders it inside Ponder.
`machine_gui` is an alias for `block_gui`.

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

Required fields:

- `blockGui`: block id used to open the GUI
- `guiWidth`, `guiHeight`: logical GUI size

Optional fields:

- `meta`: block metadata, defaults to `0`
- `id`: overlay id for `gui_outline_text`
- `pointAt`, `placeNearTarget`, `independentY`, `offsetX`, `offsetY`

## `gui_outline_text`

Highlights a rectangle inside a parent GUI overlay and attaches text.

```json
{
  "type": "gui_outline_text",
  "gui": "machine_gui",
  "guiX": 54,
  "guiY": 24,
  "guiWidth": 22,
  "guiHeight": 22,
  "duration": 60,
  "text": "Input slot"
}
```

## `gui_interaction`

Runs reusable GUI step sequences from `interactionDefinitions`, or executes inline `steps`.
