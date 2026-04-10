# Ponder 1.12.2 JSON Tutorial

This document describes the external JSON scene format currently supported by
`crl_ponder` on Minecraft `1.12.2 / Cleanroom`.

The goal is not to mirror the modern upstream storyboard code API one-to-one.
The goal is to provide a stable JSON DSL that is practical for this branch.

## File Placement

Examples:

- `docs/examples/json_tutorial/01_minimal_scene.json`
- `docs/examples/json_tutorial/02_redstone_and_nbt.json`
- `docs/examples/json_tutorial/03_gui_interaction.json`

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
- `gui_outline_text`
- `gui_interaction`
- `sequence`

## Examples

- `docs/examples/json_tutorial/01_minimal_scene.json`
- `docs/examples/json_tutorial/02_redstone_and_nbt.json`
- `docs/examples/json_tutorial/03_gui_interaction.json`
