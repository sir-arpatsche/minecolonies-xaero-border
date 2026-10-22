# MineColonies Border on Xaero Map

A client-side NeoForge mod that shows the claim areas of your
[MineColonies](https://www.curseforge.com/minecraft/mc-mods/minecolonies) colonies
on [Xaero's World Map](https://modrinth.com/mod/xaeros-world-map).


## Features

- Claimed chunks are drawn on the world map as coloured squares, with a thick border
  around the outer edge of each colony's area.
- Hovering over a colony shows its name, ID, dimension, number of claimed chunks and centre.
- Claims are remembered while you move away and are saved per world or server,
  so they are still there after a restart.
- Shows the dimension the map is displaying (Overworld, Nether, End), not just the one
  you are standing in.
- Client-side only: it sends no network packets and only reads data MineColonies
  already sends to your game.

## Requirements

| Component          | Version tested     |
|--------------------|--------------------|
| Minecraft          | 1.21.1             |
| NeoForge           | 21.1.264           |
| Java               | 21                 |
| MineColonies       | 1.1.1403           |
| Xaero's World Map  | 1.47.0             |

MineColonies needs its own dependencies as well: Structurize, BlockUI, MultiPiston
and Domum Ornamentum.

Only tested in singleplayer so far. Other versions of MineColonies or Xaero's World Map
may not work, because this mod uses Xaero's map element API, which can change between
versions.

## Installation

Put the mod JAR into the `mods` folder together with MineColonies, its dependencies and
Xaero's World Map.

## Building from source

The mod JARs of other authors are not part of this repository. To build, create a `libs`
folder next to `build.gradle` and put these files into it:

- `minecolonies-1.1.1403-1.21.1.jar`
- `structurize-1.0.832-1.21.1.jar`
- `blockui-1.0.209-1.21.1.jar`
- `multipiston-1.2.58-1.21.1.jar`
- `domum-ornamentum-1.0.231-main.jar`
- `towntalk-1.2.0.jar`
- `xaeroworldmap-neoforge-1.21.1-1.47.0.jar`
- `xaerolib-neoforge-1.21.1-1.7.3.jar` (found inside the Xaero's World Map JAR under
  `META-INF/jarjar`)


## License

All rights reserved. The source code is published for viewing only, see [LICENSE](LICENSE).
