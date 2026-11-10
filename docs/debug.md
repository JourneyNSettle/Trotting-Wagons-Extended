# Debug Wagon

Item and entity `trotting_wagons:debug_wagon`. Class `DebugWagon` extends `AbstractInventoryWagon`. Names: Debug Wagon. No recipe. Finished wagons, including this one, are not in the creative tab. The texture is always `textures/entity/debug_wagon/none.png`. Dye writes the color and does not change the picture.

Geometry: `assets/trotting_wagons/geo/entity/debug_wagon.geo.json`. If a root export `DebugCab.geo.json` lands in the root again, the build does not read it.

## Parameters

| Parameter | Value |
| --- | --- |
| Hitbox | type `sized(3.5, 4.0)`. The body is `orientedBody(1.984, 0, 4.564, 2.644, 1.953)`. The white box is the shared ground plate and does not stop the player. Bone `cart` is registered in BoneHitboxLib as a hard oriented volume, and that yellow box does. The mod does not start without BoneHitboxLib 0.3.0. `bareDrop()` is 6.75/16. See [common.md](common.md) |
| Health | `debug_wagon_health`, default 67 |
| Speed coefficient | `debug_wagon_speed_coefficient`, default 0.9 |
| Turn | `debug_wagon_turn_rate`, default 2.0 |
| Acceleration in config | `debug_wagon_acceleration`, default 3.0. Driving leaves it unread |
| `wheelWidth`, `wheelLength` | 1.5, 1.5 |
| Harness | slot `Harness`: empty 0, `shafts` (Shafts, bone `shafts`) 1, `tongue` (Tongue, bone `Tongue`) 2 |
| Hitch point | one animal: X = 1/16, Z = 76.88461/16. Two: X = ±0.6, the same Z |
| Constructor fields | `horseX` 0.3125 and `horseY` 4.805. The tongue search anchor uses `horseY` |
| `shaftAngle` | 13 |
| Tongue | length 59.12/16, height 8.5/16, fallback target for a hitched tongue 1.2 |
| Sag | both rear wheels use the default −14°. A diagonal is `DiagonalSag`. The stored `diagonalPitch()` of −8° is the unused fallback |
| Cargo | 4 slots, only `minecraft:pumpkin`, limit 1 |

Each corner has three wheel meshes. Regular bones: `LeftFrontWheel`, `RightFrontWheel`, `LeftRearWheel`, `RightRearWheel`. Wide wheels use suffix `2` on the axles `FrontWheels_Wide` and `RearWheels_Wide`. Reinforced wheels use suffix `3` on `FrontWheels_Iron` and `RearWheels_Iron`. The sag and click hubs are Y 9.5 for all three kinds. An empty corner hides all three meshes. The regular axle cube stays. The parent axles spin in code, so the visible wheels spin with them.

`holdsTravel` is true while the ladder progress is above 0.001. In that state the speed maximum is 0. The gear is not reset.

## Seats

The formula `pivot(x, y, z)` is `(x/16, y/16, −z/16)`. The two rear seats add another 0.7 on Y. There is no large seat, so a horse, mule, camel, or cow does not sit here.

| Index | Model point | Kind |
| --- | --- | --- |
| 0 | (0, 27.5, −28.5), no extra +0.7 | `DRIVER` |
| 1 | (−14.5, 26, −10.25), no extra +0.7 | `PLAYER` |
| 2 | (−11, 15.65124, 9.34415) plus 0.7 Y | `ANIMAL` |
| 3 | (8, 16.65124, −13.65585) plus 0.7 Y | `UNIVERSAL` |

## Pumpkins, banner, lantern, ladder

Pumpkins are the `AbstractInventoryWagon` inventory of 4 slots. `createHandler` accepts only a pumpkin, limit 1. The pumpkin count is synced. Bones `pumpkin1`–`pumpkin4` hide when the index is not below the count.

The banner is a separate slot of 1, any `BannerItem`. An empty stack hides bones `banner` and `stand`.

The lantern is a slot of 1, only `minecraft:lantern`. Bone `Lamp` is visible while the slot is filled. On the server the light block is refreshed every 10 ticks. Lamp position in the model: pivot (−28, 21, 6).

Ladder: the menu button toggles `DATA_LADDER_OPEN`. Over 40 ticks the server brings the progress from 0 to 1 or back. Bone `Ladder` rotates by `progress * 130°` around Z. A positive angle folds the ladder outward. The client smooths the progress between ticks.

Right-clicking with an item places a pumpkin, banner, or lantern into a free slot even when the cursor hits an occupied part. Shift-right-click removes the part under the cursor. Creative mode places a copy and does not spend the stack.

## Screen

`DebugMenu` on texture `textures/gui/debug_wagon.png`, 320×197. The file size is not changed by hand.

At the top are the name and the kilometers. The owner is stored and is not drawn. Pumpkins `(125, 22)`. Health bar x=13, y=46, width 294. Passenger seats are a 26×26 frame at y=61, x=108, 134, 160, 186. On the left, 0, 1, or 2 large draft slots at the same height: one centered at x=21, two at x=8 and x=34, picture `textures/gui/icons/big_slot.png`. Under them is the line "No harness", "Tongue", or "Shafts", then the harness slot `(26, 105)`. Wheels 2×2 from `(17, 153)`, with a two-line label above them. On the right, immediately under the bar, is Repair, then repair slots from `(269, 80)`. Under those are the Custom Items header and the banner slot `(269, 153)` and lantern slot `(287, 153)`. The player inventory starts at `(80, 113)`. The ladder button is under the distance counter. This texture has no separate lock button. Gray icons of empty slots: wheel, pumpkin, white banner, lantern, and the icon of the current repair. An empty harness slot's tooltip is "Tongue or Shafts".

The items `trotting_wagons:shafts` (Shafts) and `trotting_wagons:tongue` (Tongue) are in the creative tab and can be crafted. Shafts: six long sticks, a saddle, and string. Tongue: three long sticks, two saddles, and two string. Shafts show bone `shafts` and one draft slot. The Tongue shows bone `Tongue` and two slots. An empty slot hides both bones, and its gray icon is the Tongue item. Right-clicking the wagon with the item in hand places it into the empty slot. Shift-right-click on the tongue removes the item. The harness cannot be removed or replaced while an animal is hitched. A debug wagon already in the world, with no item in this slot, releases its draft animal on the next tick if that animal is loaded.
