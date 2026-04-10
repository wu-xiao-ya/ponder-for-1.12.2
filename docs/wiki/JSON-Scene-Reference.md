# 场景与字段参考

## 1. 场景对象常用字段

```json
{
  "namespace": "ponder",
  "component": "minecraft:crafting_table",
  "componentKey": "tutorial:minimal_scene",
  "componentMeta": 0,
  "componentNbt": "{...}",
  "componentDisplayNbt": "{...}",
  "schematic": "ponder:debug/runtime_test",
  "sceneId": "json_tutorial_minimal_scene",
  "title": "示例 01：最小场景",
  "tags": ["ponder:debug"],
  "componentTags": ["ponder:json_examples"],
  "orderBefore": [],
  "orderAfter": [],
  "operations": []
}
```

## 2. 字段含义

- `namespace`
  - 默认可省略，默认值 `ponder`
- `component`
  - 组件物品 id，必填
- `componentKey`
  - 手动指定组件 key，用于把同一物品拆成多个教程入口
- `componentMeta`
  - 组件物品 meta
- `componentNbt`
  - 组件匹配用 NBT
- `componentDisplayNbt`
  - 组件显示堆栈用 NBT
- `schematic`
  - 结构资源位置
- `sceneId`
  - 场景唯一 id
- `title`
  - 展示标题
- `tags`
  - 场景归属标签
- `componentTags`
  - 组件所在分组
- `groups`
  - `componentTags` 的别名
- `orderBefore`
  - 当前场景排在谁前面
- `orderAfter`
  - 当前场景排在谁后面

## 3. 标签定义

```json
{
  "tagDefinitions": [
    {
      "id": "json_examples",
      "title": "JSON 教程示例",
      "description": "用于演示当前分支的外部 JSON 写法。",
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

## 4. 稳定可用操作

### 场景控制

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

### 世界方块

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

### 提示

- `text`
- `outline_text`
- `indicate_redstone`
- `indicate_success`

## 5. 常用操作字段

### 方块类

- `state`
- `meta`
- `nbt`
- `pos`
- `from`
- `to`

### 文本类

- `duration`
- `text`
- `color`
- `pointAt`
- `pointMode`
- `placeNearTarget`
- `independentY`

### 动画类

- `ticks`
- `times`
- `offset`
- `rotation`
- `pivot`
- `rotX`
- `rotY`
- `rotZ`

## 6. 兼容建议

如果目标是以后给高版本也复用内容，建议优先用这些“跨版本更容易映射”的能力：

- 文本
- 基础高亮
- 方块显示/隐藏/替换
- 相机旋转
- keyframe

尽量把平台相关能力单独隔离：

- 真实 GUI
- GUI 快照
- live tab 页面
- JEI/HEI 兼容
