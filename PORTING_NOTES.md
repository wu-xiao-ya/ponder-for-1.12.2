# Porting Notes

## Platform choice

- Runtime target: Minecraft 1.12.2 on Cleanroom / CRL
- Build template: CleanroomMC ForgeDevEnv
- Upstream reference: `Creators-of-Create/Ponder` branch `mc1.20.1/dev`

## Why `mc1.20.1/dev`

Compared with `mc1.21.1/dev`, the `mc1.20.1/dev` branch is a better backport base
because it still contains a Forge project and avoids several newer APIs that would
otherwise force extra rewrites:

- no `CustomPacketPayload`
- no `StreamCodec`
- no `RegistryFriendlyByteBuf`
- no `DataComponentType`
- no `HolderLookup`

It still uses modern Java and modern client APIs, so the backport remains large.

## Expected rewrite buckets

### High effort

- Replace `GuiGraphics` UI/render code with 1.12.2 `GuiScreen` style rendering
- Replace 1.20 render hooks with 1.12.2 event and render pipeline hooks
- Replace Forge 1.20 model-data usage with 1.12.2 equivalents or custom shims
- Rebuild the Catnip service layer where it assumes new client or registry behavior

### Moderate effort

- Convert records and `Stream.toList()` usage where needed
- Remove or rewrite `var` and switch expressions if they become tooling problems
- Port access transformers and mixins to 1.12.2 names

### Low effort / mechanical

- Basic mod metadata
- Gradle workspace setup
- package scaffolding

## Suggested order

1. Bootstrap the mod and verify Cleanroom launch.
2. Port only the core `ponder` registration/runtime path.
3. Add a minimal scene player UI with hardcoded content.
4. Port scene-building utilities.
5. Port real scenes and optional Catnip config UI last.

## Current checkpoint

Completed in this workspace:

- Cleanroom bootstrap and mod metadata
- core plugin registration and tag registration
- scene registry compilation path
- minimal `api.scene` surface for 1.12.2
- no-op scene execution runtime for storyboard validation
- sample template storyboard registration
- minimal overlay text / outline builder support
- first upstream-style debug storyboard ports for compile-time validation
- minimal client viewer for browsing compiled scenes and operation timelines
- `/ponder` server command for listing, compiling, dumping, and reloading scenes

Still missing for a user-facing Ponder experience:

- actual scene playback timeline
- client GUI screens
- rendering of overlays, sections, and effects
- input handling and tooltip integration
