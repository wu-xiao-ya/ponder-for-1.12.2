# JSON Tutorial Examples

本目录用于存放当前 `crl_ponder` 1.12.2 外部 JSON 教程示例。

文件说明：

- `01_minimal_scene.json`
  - 最小完整场景
  - 演示底板、方块、文本、结束标记

- `02_redstone_and_nbt.json`
  - 运行时状态变化示例
  - 演示红石切换、提示粒子、带 NBT 的方块更新

- `03_gui_interaction.json`
  - GUI 交互示例
  - 演示 `interactionDefinitions`、`gui_snapshot`、`gui_outline_text`

对应的中文说明文档见：

- `docs/json_tutorial_zh_cn.md`

如果你想在开发环境里单独加载这些示例，可以启用：

- `run/scripts/zz_ponder_json_tutorial_examples.zs.disabled`

把它改名为 `.zs` 后，再执行重载即可。
