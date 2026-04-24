# Examples

- [01_minimal_scene.json](https://github.com/wu-xiao-ya/ponder-for-1.12.2/blob/main/docs/examples/json_tutorial/01_minimal_scene.json)
- [02_redstone_and_nbt.json](https://github.com/wu-xiao-ya/ponder-for-1.12.2/blob/main/docs/examples/json_tutorial/02_redstone_and_nbt.json)
- [03_gui_interaction.json](https://github.com/wu-xiao-ya/ponder-for-1.12.2/blob/main/docs/examples/json_tutorial/03_gui_interaction.json)
- [04_supported_features_no_gui.json](https://github.com/wu-xiao-ya/ponder-for-1.12.2/blob/main/docs/examples/json_tutorial/04_supported_features_no_gui.json)

## GUI Pattern

Use `block_gui` when the tutorial needs the real machine screen:

```json
{
  "type": "block_gui",
  "id": "machine_gui",
  "blockGui": "thermalexpansion:machine",
  "meta": 0,
  "nbt": { "Energy": 12000 },
  "guiWidth": 198,
  "guiHeight": 166,
  "duration": 100
}
```

Use `gui_outline_text` with the same `id` to call out slots, tabs, progress bars, or energy bars.
