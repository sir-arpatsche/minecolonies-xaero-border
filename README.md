# MineColonies Border on Xaero Map

A NeoForge mod that shows [MineColonies](https://www.curseforge.com/minecraft/mc-mods/minecolonies)
colonies on [Xaero's World Map](https://modrinth.com/mod/xaeros-world-map): their claimed area,
their buildings, and a tooltip with details.

Inspired by [KaleYang/MineColoniesXaero](https://github.com/KaleYang/MineColoniesXaero).

## Features

- **Claim areas.** Claimed chunks are drawn as coloured squares, with a thick border around the
  outer edge of each colony's area.
- **Building icons.** Every building gets an icon at its position. They fade out when you zoom
  far out, so the map stays readable. Building types without an icon show a coloured letter.
- **Tooltips.** Hover over a colony or a building to see its name, ID, dimension, number of
  claimed chunks, centre and position.
- **Remembers what it has seen.** Claims, buildings and names are saved per world or server,
  so they are still there after a restart.
- **Shows the dimension the map is displaying** (Overworld, Nether, End), not just the one you
  are standing in.
- **No need to visit every colony (optional server part).** Without help from the server, your game
  only learns about the claims and buildings of a colony once you have been near it. If the
  mod is also installed on the server, the server tells you about colonies you have never visited.
  Servers without the mod still work: the mod then behaves as before.

## Using it on a server

The server part is optional. Players without the mod can join a server that has it, and players
with the mod can join servers that do not.

On first start the server writes `config/minecoloniexaeroscompat-server.toml`:

| Setting | Default | Meaning |
|---|---|---|
| `shareMode` | `MEMBERS` | `MEMBERS`: a player only gets colonies they own or are an officer or friend in. `ALL`: every player with the mod gets every colony. `OFF`: the server sends nothing. |
| `opsSeeAll` | `true` | Server operators (permission level 2 or higher) get all colonies, even in `MEMBERS` mode. |
| `shareBuildings` | `true` | `false` sends only the claimed area, name and centre, not the buildings. |
| `checkIntervalSeconds` | `5` | How often the server checks colonies for changes (1 to 60). |

Custom ranks created inside a colony do not count as members for `MEMBERS` mode.

## Requirements

| Component          | Version tested     |
|--------------------|--------------------|
| Minecraft          | 1.21.1             |
| NeoForge           | 21.1.264           |
| Java               | 21                 |
| MineColonies       | 1.1.1403           |
| Xaero's World Map  | 1.47.0             |

MineColonies needs its own dependencies as well: Structurize, BlockUI, MultiPiston
and Domum Ornamentum. Xaero's World Map is only needed on the client.

Tested in singleplayer and with a dedicated server with two players. Other versions of
MineColonies or Xaero's World Map may not work, because this mod uses Xaero's map element API,
which can change between versions.

## Installation

Put the mod JAR into the `mods` folder of your client together with MineColonies, its
dependencies and Xaero's World Map. To use the server part, put the same JAR into the `mods`
folder of the server (together with MineColonies and its dependencies, without Xaero's World Map).

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


## Credits

The building icons are AI-generated artwork created for this mod.

## License

All rights reserved. The source code is published for viewing only, see [LICENSE](LICENSE).