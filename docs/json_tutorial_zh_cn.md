# Ponder 1.12.2 JSON 教程

这份文档说明当前 `crl_ponder` 在 1.12.2 / Cleanroom 环境下支持的外部 JSON 场景写法。

目标不是复刻高版本原生 storyboard 代码 API，而是提供一套在当前分支里稳定可用、便于移植和批量维护的 JSON DSL。

## 1. 文件放在哪里

推荐目录：

- `run/scripts/ponder/...`

例如：

- `run/scripts/ponder/examples/json_tutorial/01_minimal_scene.json`
- `run/scripts/ponder/examples/json_tutorial/02_redstone_and_nbt.json`
- `run/scripts/ponder/examples/json_tutorial/03_gui_interaction.json`

脚本加载器放在：

- `run/scripts/*.zs`

示例：

```zenscript
import mods.ponder.SceneFiles;

SceneFiles.loadJson("examples/json_tutorial/01_minimal_scene.json");
SceneFiles.reloadPonder();
```

如果你要一次性加载多份：

```zenscript
import mods.ponder.SceneFiles;

SceneFiles.clearQueuedJson();
SceneFiles.loadJson("examples/json_tutorial/01_minimal_scene.json");
SceneFiles.loadJson("examples/json_tutorial/02_redstone_and_nbt.json");
SceneFiles.loadJson("examples/json_tutorial/03_gui_interaction.json");
SceneFiles.reloadPonder();
```

## 2. 根结构

支持三种常见根结构：

1. 单场景对象
2. 多场景数组
3. 一个总对象，里面同时带：
   - `tagDefinitions`
   - `interactionDefinitions`
   - `scenes`

推荐统一写成总对象，后期更好维护。

## 3. 场景对象字段

一个场景对象最常用的字段如下：

```json
{
  "namespace": "ponder",
  "component": "minecraft:crafting_table",
  "schematic": "ponder:debug/runtime_test",
  "sceneId": "example_scene",
  "title": "示例场景",
  "tags": ["ponder:debug"],
  "operations": []
}
```

### 3.1 基础字段

- `namespace`
  - 默认可省略，默认为 `ponder`
- `component`
  - 必填，组件物品 id
- `schematic`
  - 必填，结构文件位置
- `sceneId`
  - 场景唯一 id
- `title`
  - 展示标题
- `tags`
  - 场景归属标签

### 3.2 组件匹配增强

下面这些字段用于把同一个物品拆成不同教程组件：

- `componentMeta`
- `componentNbt`
- `componentDisplayNbt`
- `componentKey`

如果你想手动指定组件 key，而不是让系统按 `item/meta/nbt` 推导，直接写：

```json
{
  "component": "thermalexpansion:machine",
  "componentMeta": 0,
  "componentKey": "thermalexpansion:machine_furnace"
}
```

### 3.3 组件所在分组

可用：

- `componentTags`
- `groups`

这两个字段等价。

### 3.4 排序

可用：

- `orderBefore`
- `orderAfter`

它们用于控制场景在同组件下的顺序。

## 4. 标签定义

可以在同一个 JSON 顶层里定义标签：

```json
{
  "tagDefinitions": [
    {
      "namespace": "ponder",
      "id": "json_examples",
      "title": "JSON 教程示例",
      "description": "用于展示当前 1.12.2 外部 JSON 写法。",
      "item": "minecraft:book",
      "meta": 0,
      "addToIndex": true
    }
  ]
}
```

常用字段：

- `id`
- `title`
- `description`
- `item`
- `meta`
- `nbt`
- `icon`
- `addToIndex`
- `useItemAsIcon`
- `useItemAsMainItem`

## 5. 可复用交互定义

如果一段 GUI 演示要重复使用，可以写在：

- `interactionDefinitions`
- `guiInteractions`

两者等价。

示例：

```json
{
  "interactionDefinitions": [
    {
      "id": "furnace_live_demo",
      "defaults": {
        "offsetY": -10
      },
      "steps": [
        {
          "type": "gui_snapshot",
          "snapshot": "ponder:gui_snapshot/minecraft_furnace_live",
          "id": "main",
          "duration": 70
        },
        {
          "type": "gui_outline_text",
          "gui": "main",
          "guiX": 55,
          "guiY": 16,
          "guiWidth": 68,
          "guiHeight": 54,
          "duration": 70,
          "color": "BLUE",
          "text": "这里是炉子的主要加工区域。"
        }
      ]
    }
  ]
}
```

调用方式：

```json
{
  "type": "gui_interaction",
  "interaction": "ponder:furnace_live_demo",
  "pointAt": [2, 1, 2]
}
```

如果 `interaction` 不写，也可以直接把 `steps` 内联进去。

## 6. 当前稳定可用的操作

### 6.1 场景控制

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

### 6.2 世界方块

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

### 6.3 运行时增强

- `toggle_redstone_power`
- `indicate_redstone`
- `indicate_success`

### 6.4 文本与标注

- `text`
- `outline_text`

### 6.5 GUI

- `gui_texture`
- `gui_snapshot`
- `gui_outline_text`
- `gui_interaction`
- `sequence`

## 7. 操作字段速查

### 7.1 通用定位字段

- `pos`: 单点，格式 `[x, y, z]`
- `from` / `to`: 区域包围盒
- `pointAt`: 文本箭头指向
- `direction`: `UP / DOWN / NORTH / SOUTH / EAST / WEST`

### 7.2 方块类字段

- `state`
- `meta`
- `nbt`

### 7.3 动画类字段

- `duration`
- `ticks`
- `times`
- `offset`
- `rotation`
- `pivot`
- `rotX`
- `rotY`
- `rotZ`

### 7.4 GUI 类字段

- `snapshot`
- `texture`
- `id`
- `gui`
- `parentGui`
- `guiX`
- `guiY`
- `guiWidth`
- `guiHeight`
- `offsetX`
- `offsetY`
- `displayWidth`
- `displayHeight`
- `regionWidth`
- `regionHeight`
- `stretch`
- `stretchBorder`
- `scaleToParent`
- `framed`

## 8. 推荐写法

### 8.1 最小场景

适合验证：

- 组件注册
- 文本
- 方块显示

见：

- `run/scripts/ponder/examples/json_tutorial/01_minimal_scene.json`

### 8.2 状态/NBT/提示测试

适合验证：

- `set_block`
- `toggle_redstone_power`
- `indicate_redstone`
- `indicate_success`
- 带 `nbt` 的方块更新

见：

- `run/scripts/ponder/examples/json_tutorial/02_redstone_and_nbt.json`

### 8.3 GUI 交互示例

适合验证：

- `interactionDefinitions`
- `gui_snapshot`
- `gui_outline_text`
- `gui_interaction`

见：

- `run/scripts/ponder/examples/json_tutorial/03_gui_interaction.json`

## 9. 与高版本的关系

当前 1.12.2 分支的 JSON DSL 是本分支自定义扩展，不等于高版本原生 storyboard API。

因此建议这样使用：

- 想要“当前分支可维护、可批量写教程”时：直接用这套 JSON
- 想要“未来和高版本共用一套教程”时：把教程内容限制在双方都容易映射的最小子集

当前最适合做跨版本最小子集的能力有：

- 文本
- 基础高亮
- 方块显示/隐藏/替换
- 相机旋转
- keyframe

平台分叉能力则包括：

- `gui_snapshot`
- `gui_interaction`
- 真实 GUI live 页
- 1.12.2 特定兼容写法

## 10. 最后建议

如果你打算把 JSON 作为正式写法长期维护，建议仓库里固定保留：

- 一份总文档
- 三到五份最小完整示例
- 一个默认关闭的示例加载器

这样后面即使 runtime 大改，也能拿这几份文件做回归标准。
