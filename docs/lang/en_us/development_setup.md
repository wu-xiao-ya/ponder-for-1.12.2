# Development Setup

This document contains local development notes that do not belong in the public
repository homepage.

## JDK

This workspace expects a modern JDK for Gradle itself.

If the system default still points to Java 8, use the helper scripts already
included in the repository:

```bat
scripts\gradle-jdk25.cmd classes
scripts\gradle-jdk25.cmd runClient
```

## VSCode

The repository includes `.vscode` files for local development convenience.

Typical shortcuts in the current setup:

- `F5` launch the local Ponder client profile
- `Ctrl+Shift+B` run the configured build task
- `Terminal > Run Task` run client or server tasks from the workspace

These files are optional editor configuration, not part of the public API or
runtime behavior of the mod itself.

## Notes

- `run/` is local development state and examples
- `build/` and `.gradle/` are build artifacts / caches
- temporary verification folders should not be treated as part of the project surface
