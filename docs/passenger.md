# Passenger Wagon

Item and entity `trotting_wagons:passenger_wagon`. Class `PassengerWagon` extends `AbstractWagon` and implements `MenuProvider` itself. There is no cargo inventory. Names: Passenger Wagon. No recipe. The texture is always `textures/entity/passenger_wagon/none.png` (256×256). Dye writes the color and does not change the texture file. `getDefaultColor` returns `null`.

Geometry: `assets/trotting_wagons/geo/entity/passenger_wagon.geo.json`, identifier `geometry.PassagireCab`. A later export brought back cubes that had export turned off: roof +11, body +4. The root `PassengerWagon.geo.json` and `PassengerWagon_Texture.png` were moved and deleted. The axles are bones `FrontAxis` and `RearAxis`. They spin with the wheels. `Lamps` and `rope_ladder` are children of `cart`. The clip `move.forwards` pitches the body by −2.5° and rolls it by ±1°.

## Parameters

| Parameter | Value |
| --- | --- |
| Hitbox | type `sized(3.0, 3.5)`. The body is `orientedBody(1.625, 0, 4.198, 3.312, 2.266)`. The white box is the shared ground plate. `bareDrop()` is 4.5/16. See [common.md](common.md) |
| Health | `passenger_wagon_health`, default 100 |
| Speed coefficient | `passenger_wagon_speed_coefficient`, default 0.9 |
| Turn | `passenger_wagon_turn_rate`, default 2.0 |
| Acceleration in config | `passenger_wagon_acceleration`, default 3.0. Driving leaves it unread |
| `wheelWidth`, `wheelLength` | 19.5/16, 1.5 |
| Harness | harness slot. Empty is 0. Shafts is 1, centered. Tongue is 2 at `horseX` ±9.5/16. `horseY` 77.88461/16. The constructor stores `shaftAngle` 13 |
| Tongue | length 57/16, height 8.5/16 |
| Sag | both rear wheels use `missingRearPitch()` −8°. A diagonal is `DiagonalSag`. The stored `diagonalPitch()` of −8° is the unused fallback |

On every wagon a camel stands on the same hitch point as a horse.

Three wheel kinds. Click zones on the model pivots:

| Corner | X | Y | Z |
| --- | --- | --- | --- |
| 0 | 19.25 | 9.5 | −17 |
| 1 | −18.75 | 9.5 | −17 |
| 2 | 19.75 | 9.5 | 29 |
| 3 | −19.75 | 9.5 | 29 |

Indices 0 and 2 are local +X. A wide wheel extends the outer face of the zone by 0.25. An empty slot, a mix, and an incomplete set leave the regular axle.

## Seats

Seven seats, players only. `positionRider` subtracts 0.7, and the sitting model's hips are 0.75 above the entity, so `seat(x, y, z)` is `(x/16, y/16 − 0.05, −z/16)`. The stored Y is the hip height. For passengers that is 24, two pixels above the cushion top. For the driver it is 17, one pixel below the cushion top of 18. The cabin roof starts at 45. The `Lamp1` glass starts at 36.1.

| Index | Bone | Model X, Y, Z | Kind |
| --- | --- | --- | --- |
| 0 | `DriverSeat` | 0, 17, −30.75 | `DRIVER` |
| 1 | `PassengerSeat_1` | 8.5, 24, −10 | `PLAYER` |
| 2 | `PassengerSeat_2` | −8.5, 24, −10 | `PLAYER` |
| 3 | `PassengerSeat_3` | 8.5, 24, 10 | `PLAYER` |
| 4 | `PassengerSeat_4` | −8.5, 24, 11 | `PLAYER` |
| 5 | `PassengerSeat_5` | 8.5, 24, 31 | `PLAYER` |
| 6 | `PassengerSeat_6` | −8.5, 24, 31 | `PLAYER` |

Shift from seats 1–6 places the player at the rear center, flush behind the hitbox, at the ladder. The driver, seat 0, stands at the same depth as their seat, outside the side: local +X first, then −X, and farther from the side if the point is occupied.

## Lanterns

Two slots, only `minecraft:lantern`, one each. Slot 0 is bone `Lamp1`. Slot 1 is `Lamp2`. An empty slot hides its bone. The client receives a 2-bit mask. The save is the list `Lantern`.

Right-clicking with a lantern places it in the nearest free bone and does not seat the player. If both slots are free, the bone whose center is closer to the aim point is chosen. If one is free, the lantern goes there. If both are full, a lantern in hand does not block boarding. Shift-right-click on a lantern cube removes that slot's lantern. A wheel still goes into the nearest empty corner and takes priority over removal.

Click zones are the union of the bone's cubes. `Lamp1`: origin `(−3, 36.1, −33)`, size `(6, 9.25, 6)`. `Lamp2`: origin `(−3, 34.35, 47)`, size `(6, 11, 6)`.

A filled slot places a light block of level 15 in the glass-center cell every 10 ticks. The front model center is `(0, 39.6, −30)`. The rear is `(0, 37.85, 50)`. Removal and destruction clear that slot's light block. Destruction drops both lanterns before the `doEntityDrops` check.

`rope_ladder` is always visible and has no click zone of its own. Passengers from seats 1–6 stand at the center of that ladder, just behind the rear face of the hitbox. The driver is not placed there.

## Screen

`PassengerMenu` / `PassengerScreen`, texture `textures/gui/passenger_wagon.png`, window 320×174. Do not change the file size. On the title row the wagon name is on the left and the distance counter is on the right. The owner is stored and is not drawn. The health bar is at (13, 20), width 294. Seats are 26×26 from (69, 38), step 26: the driver and six passengers. Draft buttons are at the same height: one at (21, 38), two at (8, 38) and (34, 38). Harness slot (26, 82). Wheels 2×2 at (17, 130). The player inventory starts at (80, 90). On the right, under the bar, is Repair. Repair slots start at (269, 54). Below them are the Custom Items label and the lanterns (269, 130) and (287, 130). The texture has no lock button. An empty harness slot shows a gray Tongue. While an animal is hitched, the item stays in the slot.

A shift-click from the player inventory places a wheel only into the wheels, a lantern only into the lanterns, and a repair material only into repair. The extra stays with the player.
