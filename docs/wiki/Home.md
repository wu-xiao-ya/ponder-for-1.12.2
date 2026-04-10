# Ponder 1.12.2 JSON Wiki

这套文档对应当前 `crl_ponder` 分支在 `Minecraft 1.12.2 / Cleanroom` 环境下的外部 JSON 教程写法。

适用范围：

- 当前仓库里的 `ExternalPonderScenes`
- `run/scripts/ponder/...` 下的外部 JSON 场景
- `CraftTweaker` 脚本通过 `mods.ponder.SceneFiles` 加载的场景文件

这套 JSON DSL 是当前分支自定义扩展，不等于高版本原生 storyboard 代码 API。

建议阅读顺序：

1. [快速开始](./JSON-Quick-Start.md)
2. [场景与字段参考](./JSON-Scene-Reference.md)
3. [GUI 与交互参考](./JSON-GUI-Reference.md)
4. [完整示例](./JSON-Examples.md)

仓库内对应文件：

- 主教程文档：
  - [`docs/json_tutorial_zh_cn.md`](../json_tutorial_zh_cn.md)
- 示例目录：
  - [`run/scripts/ponder/examples/json_tutorial/`](../../run/scripts/ponder/examples/json_tutorial/)

默认关闭的示例加载器：

- [`run/scripts/zz_ponder_json_tutorial_examples.zs.disabled`](../../run/scripts/zz_ponder_json_tutorial_examples.zs.disabled)

如果你是第一次接触这套写法，先从“最小场景”示例开始，不要上来就写 GUI 交互。
