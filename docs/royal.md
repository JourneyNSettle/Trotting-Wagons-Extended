# Royal Wagon

Item and entity `trotting_wagons:royal_wagon`. Class `RoyalWagon` extends `AbstractInventoryWagon`. The `en_us` name is Royal Wagon. `ru_ru` has no key, so the Russian locale keeps the English name.

## Parameters

| Parameter | Value |
| --- | --- |
| Hitbox | type `sized(3.0, 2.9)`. The body is `orientedBody(1.438, 0, 3.312, 2.266, 2.424)`. The white box is the shared ground plate. See [common.md](common.md) |
| Health | `royal_wagon_health`, default 80 |
| Speed coefficient | `royal_wagon_speed_coefficient`, default 1.0 |
| Turn | the constructor reads `conestoga_wagon_turn_rate`, default 2.0 |
| Acceleration in config | `royal_wagon_acceleration`, default 3.0. Driving leaves it unread |
| `wheelWidth`, `wheelLength` | 1.1, 1.3 |
| Harness | harness slot. Empty is 0. Shafts is 1, centered at Z 4.3. Tongue is 2, with slots at `horseX` ±0.6 and the same Z. `shaftAngle` 7 |
| Tongue | length 64.12/16, height 10.5/16 |
| Sag | both rear wheels, and the diagonal, −14° |
| Cargo | 40 ordinary slots, a 10×4 grid. Old cargo in slots 40–53 drops nearby on load |
| Color | default white, texture `textures/entity/royal_wagon/<color>.png`. Reinforced-wheel spokes lie on `white.png` around (147–232, 457–507). On the colored sheets those pixels were painted only where the sheet was transparent |

The key `royal_wagon_turn_rate` is declared in the same config and also defaults to 2.0, but `RoyalWagon` passes the `conestogaTurnRate` field into `super`. While both values match, the turn rate is the same. Changing only the royal key leaves this wagon on the Conestoga turn rate.

Regular wheels: `octagon2` and `octagon` in front, `octagon4` and `octagon3` at the rear. The side comes from the cube position, not from the name. Wide wheels: `LeftFrontWheel2`, `RightFrontWheel2`, and the rear pair is named `LeftFrontWheel3` and `RightFrontWheel3`. Reinforced wheels: `octagon5`, `octagon6`, `octagon9`, `octagon10` in the groups `FrontIronWheels` and `RearIronWheels`. Click zones:

| Corner | X | Y | Z |
| --- | --- | --- | --- |
| 0 | 17.75 | 10.5 | −21 |
| 1 | −17.75 | 10.5 | −21 |
| 2 | 17.75 | 10.5 | 21 |
| 3 | −17.75 | 10.5 | 21 |

## Seats

Players only. Coordinates are in blocks, +Z forward.

The cabin has two benches. Two riders sit on each, 7/16 of a block left and right of the middle. The bench nearer the driver is at Z +0.7. The far bench is at Z −0.7. A save that still has the old single passenger of the far bench (index 1) keeps that passenger on it and shifts them to the +X side.

| Index | Position | Kind |
| --- | --- | --- |
| 0 | (0, 1.3, 1.8) | `DRIVER` |
| 1 | (7/16, 1.45, −0.7) | `PLAYER`, far bench |
| 2 | (7/16, 1.45, 0.7) | `PLAYER`, near bench |
| 3 | (−7/16, 1.45, 0.7) | `PLAYER`, near bench |
| 4 | (−7/16, 1.45, −0.7) | `PLAYER`, far bench |

Animals do not sit as passengers. There is no seat for a large passenger.

## Screen and recipe

Texture `textures/gui/royal_wagon.png`, 320×250.

Top to bottom:

1. The row at y=6: the name on the left and the distance counter on the right. The owner is stored and is not drawn.
2. Cargo 10×4 from `(71, 21)`.
3. Health bar `(13, 96)`, width 294.
4. Five seats, 26×26 buttons from `(95, 114)`, step 26. Left to right: driver, near bench, far bench. Draft buttons sit to their left at the same height: one at x=21, two at x=8 and x=34. The draft frame is `big_slot.png`. The seats are already painted.
5. Harness label above the slot `(26, 158)`. An empty slot shows the Tongue.
6. On the right, the header `gui.trotting_wagons.repair` sits on the top edge of the seats. Open repair slots are centered under it and are drawn only while they are open.
7. Wheels 2×2 from `(17, 206)`. The player inventory starts at `(80, 166)`. The hotbar is at y=224.

The harness is placed by right-clicking the wagon with the item and removed by shift-right-clicking the tongue while nobody is hitched. A royal wagon already in the world, with no harness item, releases its loaded draft animals on the next tick.

One shift-click with a repair material fills every open slot that accepts it. The leftover stays with the player. The next shift-click, once repair accepts nothing more, places the leftover into cargo. Wheels go only into the wheel slots. Shafts and the Tongue go into the harness slot. From a wagon cell, including repair, the item goes to the player. While an animal is hitched, shift-click does not take the harness out.

There is no crafting-table recipe. On the wagon base: 16 wool blocks, 1 chest, 8 string, 32 planks, 12 iron ingots. A royal wagon built on the base drops about half: 6–10 wool, 0–1 chest, 3–5 string, 12–20 planks, 5–7 ingots. One placed as an item drops no body loot.
