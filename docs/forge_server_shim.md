# Forge Server Shim

The Forge server shim is a lightweight `ponder` mod jar for stock Forge 1.12.2 dedicated servers.

Use it when the server runs Forge and clients run the full Cleanroom / CRL Ponder backport.

## Build

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
gradlew.bat forgeServerShimJar
```

The output jar is written to:

```text
build/libs/ponder-<version>-forge-server-shim.jar
```

## Install

- Put `ponder-<version>-forge-server-shim.jar` in the Forge dedicated server `mods` folder.
- Put the full Ponder jar on clients.
- Keep the shim and full jar on the same Ponder version.
- Put one Ponder jar in each `mods` folder.

## Scope

The shim provides:

- the same `modid` (`ponder`) for Forge mod-list matching
- the public Ponder API classes used by common/server-side integration code
- no-op scene, tag, language, and registry helpers
- permissive network compatibility for full clients

The shim intentionally has no Ponder UI, rendering, scene playback, client tooltip integration, JEI/HEI handling, or JSON scene runtime.

