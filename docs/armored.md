# Armored Wagon

Item and entity `trotting_wagons:armored_wagon`. Class `ArmoredWagon` extends `AbstractWagon` and implements `MenuProvider` itself. It has no cargo inventory of its own. The `en_us` name is Armored Wagon. `ru_ru` has no key, so the Russian locale keeps the English name.

## Parameters

| Parameter | Value |
| --- | --- |
| Hitbox | type `sized(3.0, 2.9)`. The body is `orientedBody(2.279, 0, 3.479, 2.094, 2.391)`. The white box is the shared ground plate. `bareDrop()` is 6/16. See [common.md](common.md) |
| Health | `armored_wagon_health`, default 120 |
| Speed coefficient | `armored_wagon_speed_coefficient`, default 0.5 |
| Turn | `armored_wagon_turn_rate`, default 2.0 |
| Acceleration in config | `armored_wagon_acceleration`, default 4.0. Driving leaves it unread |
| `wheelWidth`, `wheelLength` | 1, 1 |
| Harness | harness slot. Empty is 0. Shafts is 1, centered at Z 3.6. Tongue is 2, with slots at `horseX` ±0.6 and Z 4.6. `shaftAngle` 13 |
| Tongue | length 61.12/16, default height 7.5/16 |
| Sag | both rear wheels, and the diagonal, −14° |
| Cargo | none |
| Color | the item is created with `BASE_COLOR` white, so a placed wagon takes `white.png`. `getDefaultColor` returns `null`. The new islands for wide and reinforced wheels were copied from `none.png` onto every colored sheet: those pixels used to be empty |

Regular wheel bones: `bone3`, `bone4`, `bone`, `bone2`, in order front left, front right, rear left, rear right. Wide and reinforced wheels stay `LeftFrontWheel2`, `RightFrontWheel2`, `LeftRearWheel2`, `RightRearWheel2`, and the same names with suffix `3`. Click zones:

| Corner | X | Y | Z |
| --- | --- | --- | --- |
| 0 | 16 | 8.5 | −17 |
| 1 | −16 | 8.5 | −17 |
| 2 | 16 | 8.5 | 17 |
| 3 | −16 | 8.5 | 17 |

## Seats

All seven seats are for players only.

| Index | Position | Kind |
| --- | --- | --- |
| 0 | (0, 1.2, 2.1) | `DRIVER` |
| 1 | (−0.5, 1.18, 1.1) | `PLAYER` |
| 2 | (0.5, 1.18, 1.1) | `PLAYER` |
| 3 | (−0.5, 1.18, 0.0) | `PLAYER` |
| 4 | (0.5, 1.18, 0.0) | `PLAYER` |
| 5 | (−0.5, 1.18, −1.1) | `PLAYER` |
| 6 | (0.5, 1.18, −1.1) | `PLAYER` |

## Screen and recipe

Texture `textures/gui/armored_wagon.png`, 320×174. The code no longer draws a frame.

Top to bottom:

1. The row at y=6: the name on the left and the distance counter on the right. The owner is stored and is not drawn.
2. Health bar `(13, 20)`, width 294.
3. Seven player seats, 26×26 buttons from `(69, 38)`, step 26. Draft buttons sit to their left: one centered at x=21, two at x=8 and x=34. The draft frame is `big_slot.png`. The seats are already painted.
4. Harness label above the slot `(26, 82)`. An empty slot shows the Tongue.
5. On the right, the header `gui.trotting_wagons.repair` sits on the top edge of the seats, y=38. Open repair slots are centered under it and are drawn only while they are open.
6. Wheels 2×2 from `(17, 130)`, with two label lines above them. The player inventory starts at `(80, 90)`. The hotbar is at y=148.

The harness is placed by right-clicking the wagon with the item and removed by shift-right-clicking the tongue while nobody is hitched. An armored wagon already in the world, with no harness item, releases its loaded draft animals on the next tick. An armored wagon built on the base drops about half of the bill: 24–40 planks, 5–7 long sticks, 1 iron block, 12–20 ingots, 3–5 string. One placed as an item drops no body loot.

There are no cargo slots. One shift-click with a repair material fills every open slot that accepts it and leaves the leftover with the player. A repeat click moves nothing. Wheels go only into the wheel slots. Shafts and the Tongue go into the harness slot. From a wagon cell, including repair, the item goes to the player. While an animal is hitched, shift-click does not take the harness out.

There is no crafting-table recipe. On the wagon base: 64 planks, 12 long sticks, 2 iron blocks, 32 iron ingots, 8 string.
