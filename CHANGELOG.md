# Changelog

All notable changes to LimitedWorldEdit are documented in this file.

## [2.1.0] - 2026-07-14

This release modernizes LimitedWorldEdit for current Spigot and Paper servers, updates its WorldEdit and WorldGuard integration, and closes gaps that could expose or modify blocks outside a player's owned regions.

### Compatibility

- Added verified compatibility with Paper 26.2 and Spigot 26.2.
- Retained Spigot/Paper 26.1.2 as the minimum 26.x server version so one JAR can support both 26.1.2 and 26.2.
- Updated to WorldEdit 7.4.4 and WorldGuard 7.0.17.
- Updated the build and runtime requirement to Java 25.
- Reworked the plugin around the current public Spigot, WorldEdit, and WorldGuard APIs without NMS or Paper-only APIs.

### Security and bug fixes

- Fixed WorldEdit clipboard and source operations bypassing the normal extent read restrictions.
- Blocks outside a player's owned regions are now exposed to WorldEdit as air, including direct reads used by operations such as `//copy` and `//cut`.
- Biomes outside owned regions are masked and entities outside owned regions are hidden from WorldEdit operations.
- Block, biome, and entity changes outside owned regions are rejected at both recommended WorldEdit interception stages.
- Restrictions now refresh around player commands and world changes.
- WorldEdit world overrides are preserved, and restrictions are removed cleanly for players with `limitedworldedit.bypass`.

### Region behavior

- Players may edit the union of all physical WorldGuard regions they directly own in the active world.
- Cuboid and polygonal regions retain their exact horizontal shape and vertical limits.
- Global regions are excluded because they do not represent a physical editable area.
- WorldGuard group ownership and region membership no longer grant edit access; the player's UUID must be listed as a direct owner.
- Players without an owned physical region are denied WorldEdit changes by default.

### Build and project changes

- Changed the project version to 2.1.0.
- Replaced manually bundled dependency JARs with a Maven build and Maven Wrapper.
- Added reproducible dependency and Java/Maven version checks.
- Renamed Java packages to the standard lowercase package structure.
- Updated `plugin.yml` metadata, dependency declarations, website, API version, and permission definitions.
- Added automated tests for ownership filtering, cuboid and polygonal boundaries, vertical limits, bypass handling, WorldEdit interception stages, block and biome access, clipboard-safe reads, and entity filtering.
- The release artifact is now produced as `target/LimitedWorldEdit-2.1.0.jar`.

### Removed

- Removed the obsolete bundled WorldEdit 6.1 and WorldGuard 6.1 libraries.
- Removed the legacy Towny, PlotMe, and PreciousStones hooks and bundled libraries. LimitedWorldEdit now uses WorldGuard regions as its single source of edit permissions.
- Removed the unused legacy plugin command.

### Upgrade notes

- Install Java 25, WorldEdit 7.4.4, and WorldGuard 7.0.17 before upgrading.
- Replace the old plugin JAR with `LimitedWorldEdit-2.1.0.jar` while the server is stopped.
- The bypass permission is now lowercase: `limitedworldedit.bypass`.
- Servers that depended on the removed Towny, PlotMe, or PreciousStones integrations must migrate their editable areas to WorldGuard regions.
