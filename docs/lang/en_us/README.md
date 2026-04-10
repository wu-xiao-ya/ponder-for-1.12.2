# Ponder for 1.12.2

`crl_ponder` is a Minecraft 1.12.2 backport workspace for
[Ponder](https://github.com/Creators-of-Create/Ponder), targeting
Cleanroom / CRL.

This repository is not a straight recompile of upstream Ponder. It contains
substantial rewrites in UI, rendering, runtime behavior, and compatibility
layers to fit the 1.12.2 ecosystem.

## Status

This project is usable, but still under active backport and stabilization work.

Current focus:

- keep the 1.12.2 branch stable enough to use
- continue rebuilding the missing runtime pieces behind the scenes
- avoid shipping test-only content as part of the long-term public surface

## Current Capabilities

The current branch already includes:

- component, tag, scene, and localization registration
- an in-game Ponder browser and showcase UI for 1.12.2
- `/ponder`, `/ponderui`, and related client/debug entry points
- external JSON scene loading through `mods.ponder.SceneFiles`
- a custom 1.12.2 JSON scene DSL with reusable interaction definitions
- live GUI snapshot support for selected screens
- JEI / HEI compatibility handling for the Ponder UI
- runtime support for a growing subset of world, overlay, particle, and GUI operations

## Current Scope

This branch targets:

- Minecraft `1.12.2`
- Cleanroom / CRL runtime

It does **not** currently aim for stock Forge 1.12.2 compatibility.

## Main Limitations

Compared with modern upstream Ponder, the largest remaining gaps are still in:

- the full instruction system
- the element / world-section runtime model
- long-term rendering parity with newer Minecraft versions

## Build

Gradle itself requires a modern JDK in this workspace.

Typical commands:

```bat
gradlew.bat compileJava
gradlew.bat reobfJar
```

If your machine still defaults to Java 8, see:

- [Development Setup](development_setup.md)

## Documentation

- [Porting Notes](../../PORTING_NOTES.md)
- [JSON Tutorial](json_tutorial.md)
- [Wiki Source](wiki/Home.md)

## Repository Layout

- `src/` source code and bundled assets
- `run/scripts/` local scene examples and loaders
- `docs/` tutorial and wiki content

## Development Notes

Local editor setup and workflow-specific notes are kept out of the public root
README.

- [Development Setup](development_setup.md)
