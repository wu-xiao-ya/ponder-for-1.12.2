# Ponder for 1.12.2

Minecraft `1.12.2` backport of
[Ponder](https://github.com/Creators-of-Create/Ponder),
targeting **Cleanroom / CRL**.

This branch is not a straight upstream recompile. It is a practical backport
with rewritten UI, rendering, runtime, and compatibility layers for the 1.12.2
modding environment.

## At a Glance

| Item | Value |
| --- | --- |
| Minecraft | `1.12.2` |
| Runtime target | `Cleanroom/Forge` |
| Current release line | `0.1.0-beta` |
| Current state | usable, still under stabilization |

## What This Repository Already Has

- component, tag, scene, and localization registration
- in-game Ponder browser and showcase UI for 1.12.2
- `/ponder`, `/ponderui`, and related client/debug entry points
- external JSON scene loading through `mods.ponder.SceneFiles`
- custom 1.12.2 JSON scene DSL with reusable interaction definitions
- live GUI snapshot support for selected screens
- JEI / HEI compatibility handling for the Ponder UI
- a growing runtime layer for world, overlay, particle, and GUI operations

## Current Constraints

Compared with modern upstream Ponder, the biggest remaining gaps are still:

- the full instruction system
- the element / world-section runtime model
- long-term rendering parity with newer Minecraft versions

In other words: this branch is already usable, but it is still a backport in
progress rather than feature parity with current upstream.

## Documentation

### Language Entry

- [English](docs/lang/en_us/README.md)
- [简体中文](docs/lang/zh_cn/README.md)

### JSON Tutorial

- [English JSON tutorial](docs/lang/en_us/json_tutorial.md)
- [中文 JSON 教程](docs/lang/zh_cn/json_tutorial.md)

### JSON Examples

- [English example index](docs/examples/json_tutorial/lang/en_us/README.md)
- [中文示例索引](docs/examples/json_tutorial/lang/zh_cn/README.md)

### Wiki

- [GitHub Wiki Home](https://github.com/wu-xiao-ya/ponder-for-1.12.2/wiki)
- [English Wiki](https://github.com/wu-xiao-ya/ponder-for-1.12.2/wiki/en_us-Home)
- [中文 Wiki](https://github.com/wu-xiao-ya/ponder-for-1.12.2/wiki/zh_cn-Home)

## Build

This workspace expects a modern JDK for Gradle.

Typical commands:

```bat
gradlew.bat compileJava
gradlew.bat reobfJar
```

Detailed setup notes:

- [English development setup](docs/lang/en_us/development_setup.md)
- [开发环境说明](docs/lang/zh_cn/development_setup.md)

## Repository Layout

- `src/`
  - source code and bundled assets
- `docs/`
  - public documentation, wiki source, and JSON examples
- `gradle/`, `gradlew`, `gradlew.bat`
  - build environment
- `PORTING_NOTES.md`
  - migration notes and porting strategy

## Project Direction

The near-term goal is stability first:

- keep the 1.12.2 branch usable
- avoid shipping test-only internals as part of the long-term public surface
- continue rebuilding the missing runtime pieces behind the scenes

The longer-term goal is deeper runtime parity, with large rewrites accepted
where necessary.
