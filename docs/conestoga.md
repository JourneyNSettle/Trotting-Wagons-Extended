# Conestoga Wagon

Item and entity `trotting_wagons:conestoga_wagon`. Class `ConestogaWagon` extends `AbstractInventoryWagon`. The `en_us` name is Conestoga Wagon. `ru_ru` has no key, so the Russian locale keeps the English name.

## Parameters

| Parameter | Value |
| --- | --- |
| Hitbox | type `sized(3.0, 2.9)`. The body is `orientedBody(1.382, 0, 2.815, 2.382, 2.445)`. The white box is the shared ground plate. See [common.md](common.md) |
| Health | `conestoga_wagon_health`, default 60 |
| Speed coefficient | `conestoga_wagon_speed_coefficient`, default 0.8 |
| Turn | `conestoga_wagon_turn_rate`, default 2.0 |
| Acceleration in config | `conestoga_wagon_acceleration`, default 2.0. Driving leaves it unread |
| `wheelWidth`, `wheelLength` | 1, 1 |
| Harness | harness slot. Empty is 0. Shafts is 1, centered at Z 3.6. Tongue is 2, with slots at `horseX` ±0.6 and the same Z. `shaftAngle` 13 |
| Tongue | default length 57.12/16, height 7.5/16, fallback target 0.85 |
| Sag | both rear wheels, and the diagonal, −14° |
| Cargo | two chests of 42 slots each. The second opens only while the cabin is empty, and it refuses passengers while it holds anything |
| Color | default white, texture `textures/entity/conestoga_wagon/<color>.png`. New opaque pixels of `white.png` around (244–332, 198–251) were painted onto the colored sheets only in transparent cells |

Each corner has three bones, regular, wide, and reinforced: `wheelleft` / `LeftFrontWheel2` / `wheelleft2`, `wheelright` / `RightFrontWheel2` / `wheelright2`, `bone` / `LeftRearWheel2` / `LeftRearWheel3`, `bone2` / `RightRearWheel2` / `RightRearWheel3`. Only the fitted kind is shown. Click zones are in model pixels, Y up, model Z forward (on the entity, forward is the negative of that Z):

| Corner | X | Y | Z |
| --- | --- | --- | --- |
| 0 | 16 | 7.15385 | −16 |
| 1 | −16 | 7.15385 | −16 |
| 2 | 16 | 8.5 | 17 |
| 3 | −16 | 8.5 | 17 |

## Seats

Coordinates are in blocks from the entity center. +Z is the wagon's forward. On boarding, the point is lowered another 0.7 under the sitting pose.

| Index | Position | Kind |
| --- | --- | --- |
| 0 | (0, 1.25, 2.1) | `DRIVER` |
| 1 | (−0.4375, 1.18, 1.1) | `UNIVERSAL` |
| 2 | (0.4375, 1.18, 1.1) | `UNIVERSAL` |
| 3 | (0, 1.18, 0.19) | `ANIMALBIG`, a large passenger only |

The buttons are labeled 1, 2, 3, 4 from left to right: driver, left universal, large animal, right universal. A large animal in seat 3 blocks both universal seats. A player or a small animal in seat 1 or 2 blocks seat 3. The second chest blocks all three, and it stays closed while any of them is occupied. The driver is outside that choice.

Old saves kept six seats. `assignSeat` keeps a saved index only when it is still inside the array and the seat accepts that passenger. Indices 4 and 5 are past the length of 4. A player saved on index 3 is refused by `ANIMALBIG`. Those passengers go to the nearest open seat.

## Screen and recipe

Texture `textures/gui/conestoga_wagon.png`, 320×236.

Top to bottom:

1. The row at y=6: the name on the left and the distance counter on the right. The owner is stored and is not drawn.
2. Cargo 14×3 from `(18, 22)`. On the right, two 26×26 buttons at `(277, 20)` and `(277, 50)`. The upper one is open by default and draws a chest icon. The lower one opens the second chest of 42 slots and goes dark if someone is sitting in the cabin.
3. Health bar `(13, 82)`, width 294.
4. Four seats, 26×26 buttons from `(108, 100)`, step 26: driver, universal, large animal, universal. Draft buttons sit on the left at the same height.
5. Harness label above the slot `(26, 144)`. An empty slot shows the Tongue.
6. On the right, the header `gui.trotting_wagons.repair` sits on the top edge of the seats. Open repair slots are centered under it.
7. Wheels 2×2 from `(17, 192)`. The player inventory starts at `(80, 152)`. The hotbar is at y=210.

`CargoVersion` 2 stores both chests in a row, 84 slots. Old cargo with no version keeps the first 42 slots and drops the rest beside the wagon. The harness is placed by right-click and removed by shift-right-click while nobody is hitched. A Conestoga already in the world, with no harness item, releases its loaded draft animals.

One shift-click with a repair material fills every open slot that accepts it. The leftover stays with the player. The next shift-click places the leftover into the open chest. Wheels go only into the wheel slots. Shafts and the Tongue go into the harness slot. From a wagon cell, including repair, the item goes to the player. While an animal is hitched, shift-click does not take the harness out.

There is no crafting-table recipe. On the wagon base: 2 chests, 16 wool blocks, 18 long sticks, 1 lead, 32 planks, 8 iron ingots.

On destruction, cargo drops through the ordinary `Containers.dropContents` after the wheels: each stack is its own item entity. Repair items do not drop. A Conestoga built on the base drops about half of the bill: 1 chest, 6–10 wool, 7–11 long sticks, 0–1 lead, 12–20 planks, 3–5 ingots. One placed as an item drops no body loot, neither that half bill nor the old oak planks and iron scrap.
