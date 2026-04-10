# 完整示例

## 示例列表

### 示例 01：最小场景

文件：

- [`run/scripts/ponder/examples/json_tutorial/01_minimal_scene.json`](../../run/scripts/ponder/examples/json_tutorial/01_minimal_scene.json)

用途：

- 验证组件是否进入索引
- 验证最基础的 `set_block`
- 验证 `text` / `outline_text`

### 示例 02：红石与 NBT

文件：

- [`run/scripts/ponder/examples/json_tutorial/02_redstone_and_nbt.json`](../../run/scripts/ponder/examples/json_tutorial/02_redstone_and_nbt.json)

用途：

- 验证 `toggle_redstone_power`
- 验证 `indicate_redstone`
- 验证 `indicate_success`
- 验证带 `nbt` 的方块更新

### 示例 03：GUI 交互

文件：

- [`run/scripts/ponder/examples/json_tutorial/03_gui_interaction.json`](../../run/scripts/ponder/examples/json_tutorial/03_gui_interaction.json)

用途：

- 验证 `interactionDefinitions`
- 验证 `gui_snapshot`
- 验证 `gui_outline_text`
- 验证 `gui_interaction`

## 加载方式

仓库已附带一个默认关闭的加载器：

- [`run/scripts/zz_ponder_json_tutorial_examples.zs.disabled`](../../run/scripts/zz_ponder_json_tutorial_examples.zs.disabled)

启用步骤：

1. 改名为 `.zs`
2. 启动游戏或重载脚本
3. 打开 Ponder 浏览器查看示例

## 推荐作为仓库标准样例

如果后续继续扩展 JSON DSL，建议这三份文件始终保留并作为回归基准：

- `01_minimal_scene.json`
- `02_redstone_and_nbt.json`
- `03_gui_interaction.json`

只要这三份还能正常工作，说明：

- 基础场景没坏
- 状态更新链路没坏
- GUI 交互链路没坏
