# Barrel Wagon

Item and entity `trotting_wagons:barrel_wagon`. Class `BarrelWagon` extends `AbstractWagon` and implements `MenuProvider`. Names: Barrel Wagon. No recipe. Finished wagons are not in the creative tab. The item is created with `BASE_COLOR` white, and `getDefaultColor()` returns `null`.

Model `assets/trotting_wagons/geo/entity/barrel_wagon.geo.json`, geometry identifier `geometry.BarrellCab`. Texture `textures/entity/barrel_wagon/none.png`, 256×256. The build does not read files of those names in the repository root: the original export was moved onto these paths and deleted from the root. Animation `animations/entity/barrel_wagon.animation.json` is the clip `move.forwards`, 1.5 s, on bone `cart`: pitch −2.5° and side roll ±1°. Bone `Barrels` is a child of `cart`, so the barrels stay in the body and follow that rock. The barrels have no extra shift. Wheels and the tongue are spun in code. The item model `models/item/barrel_wagon.json` has parent `minecraft:builtin/entity`, scale 0.1, a GUI translation of `[0, -2.5, 0]`, and a rotation of `[20, 45, 0]`.

The texture is always `none.png`. `getDefaultColor` returns `null`. Dye still writes the color onto the entity and onto the picked-up item. It does not change the picture.

## Parameters

| Parameter | Value |
| --- | --- |
| Hitbox | type `sized(4.5, 3.25)`. The body is `orientedBody(1.625, 0, 2.710, 2.518, 2.266)`. The white box is the shared ground plate. See [common.md](common.md) |
| Health | `barrel_wagon_health`, default 80, read in the constructor |
| Coefficient with no barrels | `barrel_wagon_speed_empty`, default 1.0, read when the barrel mask changes |
| Coefficient for 1–6 barrels | `barrel_wagon_speed_partial`, default 0.9 |
| Coefficient for 7–12 barrels | `barrel_wagon_speed_full`, default 0.8 |
| Turn | `barrel_wagon_turn_rate`, default 2.0, read in the constructor |
| Acceleration in config | `barrel_wagon_acceleration`, default 3.0. Driving leaves it unread |
| `wheelWidth`, `wheelLength` | 19.5/16 and 17/16 |
| Harness | harness slot. Empty is 0. Shafts is 1, centered. Tongue is 2 at `horseX` ±9.5/16. `horseY` 77.88461/16. The constructor stores `shaftAngle` 13 |
| Tongue | length 57/16 |
| Sag | one remaining wheel is `BarrelWagonSag`. Every other mask, including a diagonal, uses the shared pose. See below and [common.md](common.md) |

The thresholds of 6 and 12 barrels are not in the config. They are the constants `PARTIAL_LIMIT` and `BARRELS`. What is counted is placed barrels, not the items inside them. The coefficient multiplies the speed of the slowest draft animal. A wide wheel multiplies the result by 0.8 once more, then the ground multiplier is applied, including Block Runner. Live acceleration per tick is `coefficient / 400`.

The config section is named `Barrel Wagon Options`. An existing `trotting_wagons-server.toml` receives the new keys on the next load. Old sections are not rewritten.

| Barrels | Coefficient |
| --- | --- |
| 0 | `barrel_wagon_speed_empty` (1.0) |
| 1–6 | `barrel_wagon_speed_partial` (0.9) |
| 7–12 | `barrel_wagon_speed_full` (0.8) |

On the client the barrel count for speed comes from the synced mask. On the server it comes from the slots.

## Driver seat

One seat, kind `DRIVER`. `seat(0, 16, −35.25)` stores `(0, 16/16 − 0.05, 35.25/16)`. The X argument is 0, so the driver stays on the center line. Boarding subtracts 0.7, and the sitting hips are another 0.75 above the entity, so that Y is the hip height. An animal does not sit in this seat. The menu opens with the inventory key while the player is seated. Right-click does not open the menu.

The draft list is the shared `allowed_entities`.

## Wheels and axles

Three meshes. Bone names are `LeftFrontWheel`, `RightFrontWheel`, `LeftRearWheel`, and `RightRearWheel`, wide wheels with suffix `2`, iron wheels with `3`. An empty corner hides all three meshes of that corner. Click zones on the pivots of the regular wheels:

| Corner | X | Y | Z |
| --- | --- | --- | --- |
| 0 | 19.25 | 9.5 | −17 |
| 1 | −18.75 | 9.5 | −17 |
| 2 | 19.75 | 9.5 | 17 |
| 3 | −19.75 | 9.5 | 17 |

Indices 0 and 2 are local +X. A wide wheel extends the outer side of the zone by 0.25. Shift-right-click removes a wheel. A wheel in hand is placed into the nearest empty corner with an ordinary right-click, and that placement takes priority over placing a barrel.

The axles are the separate bones `FrontAxis` and `RearAxis`, pivots `[0, 9.5, −17]` and `[0, 9.5, 17]`. They spin by the same angle as the wheel groups. There is no separate wide axle and no separate iron axle: all three wheel kinds sit at this height.

One remaining wheel is solved in `BarrelWagonSag`: the tread sits on the ground, and the tilt grows until the lowest point of the frame touches the ground. The tongue is outside that calculation. `tongueAngle` drives it. Barrels do not affect the pose. The tread radius is the pivot height, 9.5 for all three kinds. The result is cached by the wheel mask and the wheel kind. A diagonal, and every other mask, uses the shared pose in [common.md](common.md). `BarrelWagonSag` still contains a pair solver, and the wagon does not call it.

## Barrels

Twelve slots. A slot accepts only `minecraft:barrel`, one item. TerraFirmaCraft barrels do not fit. The barrel block's inner 27 slots are unused: the four slots beside a barrel are the wagon's storage.

Each barrel has four content slots (`CONTENTS`). While a slot has no barrel, those four slots are not shown and their `mayPlace` is false, so a shift-click does not put items into them. Any item in the open slots keeps its ordinary stack limit. An old save stored six items per barrel. The extra two from each barrel drop nearby.

Removing a barrel, replacing it, or throwing the slot drops its four stacks beside the wagon. The barrel itself goes to the player, like the item of an ordinary slot. A click and a throw go through `extractItem` and drop the contents before the stack shrinks. A shift-click first sets the count to zero, and that stack already answers as air, so `BarrelMenu` calls `dropBarrelContents` itself after a successful transfer. If the transfer fails, the contents stay in the slots.

Destruction drops the barrels, the items from them, and the repair parts, then the base logic drops the wheels. This happens before the `doEntityDrops` check. Shift-right-click with an axe no longer returns the wagon item. Planks and iron, when health reaches zero, drop only when the gamerule is on.

The client receives a 12-bit mask, `DATA_BARRELS`. It still marks the occupied slots. The model shows bones `Barrel_1` through `Barrel_N`, where N is the number of placed barrels, with no tie to the slot number. Empty slots in the middle leave no holes. The parent `Barrels` is not hidden.

The save is a `Cargo` compound with an `Items` list, like an ordinary container. The slot index is a byte. There are 60 slots (12 barrels and 48 items), which is under 256. The count in a stack is ordinary, at most the item's limit and at most 99 for the item codec. The capability `ItemHandler.ENTITY` is not attached to this entity, so hoppers do not connect to it.

## Right-click in the world

A right-click without sneaking, with a stack of `minecraft:barrel`, places one barrel into the first empty slot, from slot 0 toward slot 11. On the model the next bone by count is shown, `Barrel_1` and onward, not the bone numbered like the slot. In creative mode the item is not spent. That click does not seat the player, even when all 12 slots are full. A wheel in hand is handled earlier and does not place a barrel. An ordinary right-click with an empty hand seats the player. Shift-right-click removes the part under the cursor. The whip and dye still run earlier and do not place a barrel.

## Screen

`BarrelMenu` / `BarrelScreen`, texture `textures/gui/barrel_wagon.png`, window 320×250. Do not change the file size. On the left are 12 barrel slots, a 3×4 grid from (20, 15). The texture has no cells on the right. Barrel slot `i` draws the four frames `i×4` through `i×4+3` in a 12×4 grid from (86, 15): three barrels of one row open the three groups of four in that same row. While a barrel is absent, its cells sit off the screen. An old save kept six items per barrel. The extra two from each barrel drop nearby. An empty barrel slot shows a gray icon of `minecraft:barrel`.

One shift-click from the player inventory fills only the matching special slots: barrels into the barrel slots, a repair material into the repair slots, wheels only into the wheel slots. The leftover stays with the player. The next shift-click places the leftover into the open shared item slots, once the special slots accept nothing more. Wheels do not go into the shared slots. An item with no special slot goes straight into the shared slots.

These coordinates are the default layout. `BarrelMenu.useFlatBarrelRow()` is not called by this mod. It retargets the same fields for a compat row of twelve: height 186, barrels from (53, 10), health at y=33, the driver and draft buttons at y=50, the title at y=52, the harness at y=94, the wheels at y=142, and the inventory at y=102.

The health bar is at (13, 96), width 294. Under it, as on the ware wagon: the driver at (69, 114), and on the right the name and the kilometers from (101, 116). The owner is stored and is not drawn. On the left, the harness slot is at (26, 158). On the right, Repair sits on the top edge of the driver seat, and the repair slots are centered under it. Wheels (17, 206). The player inventory starts at (80, 166). The texture has no lock button.
