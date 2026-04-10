# Ponder Cleanroom Backport

This directory is a Cleanroom/CRL workspace for a Minecraft 1.12.2 backport of
[Ponder](https://github.com/Creators-of-Create/Ponder).

## Current direction

- Target platform: Cleanroom / CRL on Minecraft 1.12.2
- Build base: CleanroomMC ForgeDevEnv
- Upstream code baseline: `Creators-of-Create/Ponder` branch `mc1.20.1/dev`

`mc1.20.1/dev` was chosen on purpose. It is still far from 1.12.2, but it keeps a
Forge subproject and avoids some of the newer 1.20.5+/1.21 packet and registry APIs
that would add extra rewrite work.

## Why this workspace exists

Using Cleanroom removes the need to stay on a pure Java 8-style workflow while still
targeting the 1.12.2 ecosystem. This helps with parts of the upstream codebase that
already use newer Java features such as records, `var`, switch expressions, and
`Stream.toList()`.

That said, this is still a real backport, not a straight recompile. The largest
rewrite areas remain:

- GUI code built around `GuiGraphics`
- modern rendering hooks and model data usage
- 1.20 block/entity/client APIs
- the Catnip support layer that assumes newer Minecraft internals

## Working assumptions

- We are targeting Cleanroom-only 1.12.2, not stock Forge 1.12.2 compatibility.
- Mixin and access transformer support are enabled up front because upstream Ponder
  already depends on both concepts.
- This workspace is the bootstrap point; upstream source has not been bulk-imported
  yet.

## Local build note

This template currently expects a modern JDK for Gradle itself. On this machine the
system `JAVA_HOME` still points at Java 8, so use:

```bat
scripts\gradle-jdk25.cmd classes
```

The helper script forces a Java 25 installation before invoking `gradlew`.

To launch a dev client with the same JDK override, use:

```bat
scripts\gradle-jdk25.cmd runClient
```

## VSCode workflow

This workspace now includes a `.vscode` setup for local testing:

- `F5` -> launch `Ponder Client`
- `Ctrl+Shift+B` -> run `Ponder: classes`
- `Terminal > Run Task` -> run `Ponder: runClient` or `Ponder: runServer`

The workspace settings pin VSCode's Java/Gradle integration to the same Java 25
installation used by `scripts\gradle-jdk25.cmd`, so the editor and terminal should
behave consistently on this machine.

## Current prototype status

The backport now includes:

- core plugin / tag / scene registration
- a minimal scene compile path that executes storyboard code
- no-op scene builder and utility layers for 1.12.2-compatible scripts
- a sample `minecraft:crafting_table` storyboard
- minimal overlay text / outline builder support for storyboard compilation
- an upstream-style debug storyboard pack registered on `minecraft:compass`
- a minimal client-side Ponder viewer screen opened with `/ponderui`
- an in-game `/ponder` debug command for validating registry state

The actual Ponder UI and rendering stack are still not backported yet.

## In-game verification

After launching the dev environment, use these commands:

```text
/ponder list
/ponder compile minecraft:crafting_table
/ponder dump minecraft:crafting_table
/ponder compile minecraft:compass
/ponder dump minecraft:compass 0
/ponder reload
/ponderui
/ponderui minecraft:compass 0
```

This is the current validation loop for the backport before the real UI/player is
ported.

## Next milestones

1. Expand `overlay`, `effects`, and `world` instructions until real upstream scenes compile.
2. Port a first gameplay-relevant storyboard beyond template/debug coverage.
3. Rebuild a minimal Ponder scene player UI on 1.12.2 client APIs.
4. Reconnect rendering, input, and scene playback timing.

See [PORTING_NOTES.md](PORTING_NOTES.md) for the current migration map.
