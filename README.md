# TeleportBow

A Minecraft plugin that teleports you where your arrow lands — with safety checks.

## Features

- Shoot an arrow, teleport to where it lands
- Searches a 3×3×3 area around the landing spot for a safe position
- Requires 30 contiguous passable blocks in at least one direction (filters out landing inside structures)
- Configurable safe-blocks / unsafe-blocks / pattern-based passable materials
- Per-player cooldown with decimal support (e.g. 0.5s)
- Optional max distance limit
- `/teleportbow reload` command
- Debug mode for troubleshooting

## Requirements

- Paper, Purpur, or Spigot 1.21.8+
- Java 21

## Installation

1. Download `TeleportBow.jar` from Releases
2. Drop it in your server's `plugins/` folder
3. Restart the server
4. Edit `plugins/TeleportBow/config.yml` as needed

## Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/teleportbow reload` | `teleportbow.reload` | Reload the config |

## Permissions

| Permission | Default | Description |
|-----------|---------|-------------|
| `teleportbow.use` | true | Allows using the teleport bow |
| `teleportbow.reload` | op | Allows reloading the config |

## Configuration
see src/main/resources/config.yml for the full commented reference

## How it works

When an arrow hits a block, the plugin checks the landing location.
If it's safe (solid ground, no hazards, enough vertical/horizontal space, 
and 30+ passable blocks in at least one direction), you teleport there.
If not, it searches the surrounding 3×3×3 cube for the first safe spot.
If nothing passes, the teleport is cancelled.

The "30 blocks in one direction" rule prevents teleporting inside a 
building or under a low ceiling.

## Requirements

- Paper/Purpur/Spigot 1.21.8+
- Java 21

## Building

`mvn clean package`
Output: `target/TeleportBow.jar`
