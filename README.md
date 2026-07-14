# LimitedWorldEdit

LimitedWorldEdit restricts a player's WorldEdit operations to the physical WorldGuard regions that the player directly owns.

## Requirements

- Java 25
- Spigot or Paper 26.1.2 or newer in the 26.x series
- WorldEdit 7.4.4
- WorldGuard 7.0.17

The plugin uses only the Spigot, WorldEdit, and WorldGuard public APIs. It does not use NMS or Paper-only APIs, so the same JAR runs on Spigot and Paper.

## Verified compatibility

The release JAR has been startup and shutdown smoke-tested on Java 25 with WorldEdit 7.4.4 and WorldGuard 7.0.17:

| Server | Build | Result |
| --- | --- | --- |
| Paper 26.1.2 | 74 (stable) | Loaded, enabled, reached ready state, and disabled cleanly |
| Paper 26.2 | 60 (beta) | Loaded, enabled, reached ready state, and disabled cleanly |
| Spigot 26.2 | 4643 (`8db49a2` / `08de3aa`) | Loaded, enabled, reached ready state, and disabled cleanly |

Paper 26.1.2 is the stable Paper target in this matrix; Paper 26.2 was still marked beta when tested.

## Behavior

- Cuboid and polygonal WorldGuard regions retain their exact shape and vertical limits.
- A player can edit within the union of every physical region their UUID directly owns in the current world.
- Players with no owned regions cannot change blocks, biomes, or entities through WorldEdit.
- Block reads outside the allowed area are treated as air so read-driven WorldEdit operations cannot sample protected blocks.
- Existing entities outside the allowed area are hidden from WorldEdit entity operations.
- `limitedworldedit.bypass` skips all restrictions and defaults to server operators.

WorldGuard group ownership and region membership do not grant an editable area; the player must be a direct region owner.

## Installation

1. Stop the server.
2. Install matching releases of WorldEdit and WorldGuard.
3. Copy the LimitedWorldEdit JAR into the server's `plugins` directory.
4. Start the server and confirm that LimitedWorldEdit enables after WorldEdit and WorldGuard.

There are no commands or configuration files.

## Building

Install Java 25, then use the included Maven Wrapper:

```shell
./mvnw clean verify
```

On Windows:

```powershell
.\mvnw.cmd clean verify
```

The plugin JAR is written to `target/LimitedWorldEdit-2.1.0.jar`.

The test suite covers exact cuboid and polygon boundaries, vertical limits, ownership filtering, WorldEdit event stages, bypass behavior, block and biome writes, block reads, and entity filtering/creation.

## Project pages

- [BukkitDev](https://dev.bukkit.org/projects/limitedworldedit)
- [SpigotMC](https://www.spigotmc.org/resources/limitedworldedit.1163/)
