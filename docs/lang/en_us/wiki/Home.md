# Ponder 1.12.2 JSON Wiki

Ponder for 1.12.2 is a Cleanroom / CRL backport of the Ponder tutorial system.
It provides scene registration, an in-game browser, JSON-driven scenes, GUI overlays, and a Forge dedicated-server shim.

Recommended reading order:

1. [Quick Start](en_us-JSON-Quick-Start)
2. [Scene Reference](en_us-JSON-Scene-Reference)
3. [GUI Reference](en_us-JSON-GUI-Reference)
4. [Examples](en_us-JSON-Examples)

## Current Capabilities

- Component, tag, scene, and localization registration
- In-game Ponder browser and showcase UI
- `/ponder`, `/ponderui`, and `/ponderuidebug` entry points
- External JSON scene loading through `mods.ponder.SceneFiles`
- Static GUI textures, registered GUI snapshots, and real block/machine GUI capture
- Forge 1.12.2 dedicated-server compatibility shim

## Build Outputs

- Full client/runtime jar: `gradlew.bat reobfJar`
- Forge server shim jar: `gradlew.bat forgeServerShimJar`

Use the full jar on clients and the shim jar on stock Forge dedicated servers.
