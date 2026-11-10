# Animal Wagon

Item and entity `trotting_wagons:animal_wagon`. Class `AnimalWagon` extends `AbstractWagon`. There is no cargo inventory. Names: Animal Wagon. No recipe. The texture is always `textures/entity/animal_wagon/none.png`. Dye writes the color and does not change the texture file. `getDefaultColor` returns `null`.

Geometry: `assets/trotting_wagons/geo/entity/animal_wagon.geo.json`. The build does not read a root `AnimalWagon.geo.json`.

## Parameters

| Parameter | Value |
| --- | --- |
| Hitbox | type `sized(3.5, 2.75)`. The body is `orientedBody(1.625, 0, 3.409, 2.781, 2.266)`. The white box is the shared ground plate. See [common.md](common.md) |
| Health | `animal_wagon_health`, default 60 |
| Speed coefficient | `animal_wagon_speed_coefficient`, default 0.9 |
| Turn | `animal_wagon_turn_rate`, default 2.0 |
| Acceleration in config | `animal_wagon_acceleration`, default 3.0. Driving leaves it unread |
| `wheelWidth`, `wheelLength` | 1.5, 1.5 |
| Harness | slot `Harness`: empty 0, Shafts 1 in the center, Tongue 2 at `horseX` 9.5/16. `horseY` 77.88461/16, `shaftAngle` 13 |
| Tongue | length 55/16 |
| Sag | both rear wheels use `missingRearPitch()` −8°. A diagonal is `DiagonalSag`. The stored `diagonalPitch()` of −8° is the unused fallback |

`bareDrop()` is `0.34623842746449673`, the rotated cart step. An unloaded animal is placed at 2.85, just past the end of the floor. The rear gate closes only when every animal seat is locked, and it opens again when any of those locks is cleared.

An existing `trotting_wagons-server.toml` does not rewrite the old health. The new value 60 reaches a world that does not have this section yet.

A camel stands on the same hitch point as a horse, donkey, or mule. The old one-block shift backward is gone.

The wheels use the same three meshes as the debug wagon. Click zones on the model axes:

| Corner | X | Y | Z |
| --- | --- | --- | --- |
| 0 | 19.25 | 9.5 | −17 |
| 1 | −18.75 | 9.5 | −17 |
| 2 | 19.75 | 9.5 | 29 |
| 3 | −19.75 | 9.5 | 29 |

Indices 0 and 2 are local +X. A wide wheel extends the outer face of the zone by 0.25.

## Seats

Nine seats. The formula `seat(x, y, z)` is `(x/16, y/16, −z/16)` plus 0.7 on Y. A negative model Z points toward the entity's front. The driver is on bone `DriverSeat`. Animals stand above the floor, model Y 16.5.

| Index | Model X, Y, Z | Kind |
| --- | --- | --- |
| 0 | 0, 4, −32.25 | `DRIVER` |
| 1 | −6, 16.5, −14 | `ANIMAL` |
| 2 | 6, 16.5, −14 | `ANIMAL` |
| 3 | 0, 16.5, −4 | `ANIMALBIG` |
| 4 | −6, 16.5, 10 | `ANIMAL` |
| 5 | 6, 16.5, 10 | `ANIMAL` |
| 6 | 0, 16.5, 24 | `ANIMALBIG` |
| 7 | −6, 16.5, 34 | `ANIMAL` |
| 8 | 6, 16.5, 34 | `ANIMAL` |

`acceptsLargeSeat` lets only a large passenger onto seats 3 and 6. A small animal cannot be placed there.

The front large seat 3 is mutually closed with seats 1, 2, 4, and 5. The rear large seat 6 is mutually closed with seats 4, 5, 7, and 8. The two large seats do not close each other. An animal that is currently being dragged does not occupy a seat for itself. An empty closed cell draws the item `minecraft:barrier`. The tooltip appears at once (`TooltipDelay.MILLIS` is 0). A large seat says `gui.trotting_wagons.tooltip.large_needs_space` ("Free the neighboring seats to place a large animal"). A small seat says `gui.trotting_wagons.tooltip.small_blocked` ("Seats are taken by a large animal").

A hitched animal does not move itself onto a large seat that has opened up. The harness can be removed by sneaking and right-clicking in the world, by the harness button, or by dragging onto a seat.

An animal removed from a seat by the menu button appears at the rear center, just behind the stern of the body: 2.85 from the center plus half the animal's width. Sneaking and right-clicking a sitting animal does not unload it. The driver dismounts through the ordinary side search.

## Screen

Texture `textures/gui/animal_wagon.png`, 320×200. The file size must stay as painted.

`AnimalMenu`: wheels `(17, 156)`, player inventory from `(80, 116)`, harness slot `(26, 108)`, repair `(269, 50)`. Health bar `(13, 16)`, width 294. Immediately under it, on the right, is the Repair header. One draft button is centered at `(21, 51)`. Two sit at `(8, 51)` and `(34, 51)`. This texture has no separate lock button. An empty harness slot shows a gray Tongue. While an animal is hitched, the item stays in the slot. A wagon already in the world, with no item in the slot, releases its loaded draft animals.

Seat buttons are 26×26. The top row is the wagon's left side. Driver 0 is at `(82, 51)`. Small pairs: front 1 and 2 at x=108, middle 4 and 5 at x=160, rear 7 and 8 at x=212. The large seats are inset on the axis: front 3 at `(134, 51)`, rear 6 at `(186, 51)`.

The title row draws the wagon name and the distance counter. The owner is stored and is not drawn.
