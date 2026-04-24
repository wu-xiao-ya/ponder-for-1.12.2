# 完整示例

- [01_minimal_scene.json](https://github.com/wu-xiao-ya/ponder-for-1.12.2/blob/main/docs/examples/json_tutorial/01_minimal_scene.json)
- [02_redstone_and_nbt.json](https://github.com/wu-xiao-ya/ponder-for-1.12.2/blob/main/docs/examples/json_tutorial/02_redstone_and_nbt.json)
- [03_gui_interaction.json](https://github.com/wu-xiao-ya/ponder-for-1.12.2/blob/main/docs/examples/json_tutorial/03_gui_interaction.json)
- [04_supported_features_no_gui.json](https://github.com/wu-xiao-ya/ponder-for-1.12.2/blob/main/docs/examples/json_tutorial/04_supported_features_no_gui.json)

## GUI 写法

教程需要真实机器界面时使用 `block_gui`：

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

后续用同一个 `id` 配合 `gui_outline_text` 标注槽位、标签页、进度条或能量条。
