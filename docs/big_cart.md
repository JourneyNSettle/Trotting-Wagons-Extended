# Big Cart

Entity `big_cart`, item `big_cart`. No recipe. Dye is refused: one texture, `textures/entity/big_cart/none.png`, copied from `blockbench/BigCart/BigCart_Texture.png`. Geometry `geo/entity/big_cart.geo.json`, identifier `geometry.BigCab`. Bone `cart` rocks with the clip `move.forwards`, the same way as the debug wagon. The `Ladder` is always in place and does not open.

## Size

The type is `sized(4.5, 2.5)`. The body is `orientedBody(2.062, 0, 2.413, 2.469, 1.953)`, and the white box is the shared ground plate. See [common.md](common.md). `bareDrop()` is 8.75/16. One remaining wheel adds 1° to the shared 3.5° pitch and roll, plus 2/16 of drop, and a lone rear wheel adds another 1/16.

Harness: empty is 0 slots. Shafts is one animal, centered at Z 78/16. Tongue is two animals at X ±0.6 and the same Z. Tongue length 64/16, height 8.75/16. The constructor stores angle 13. The drawn angle is `tongueAngle`.

Health, speed, and turn are read from `big_cart_health` (60), `big_cart_speed_coefficient` (1.0), and `big_cart_turn_rate` (2.0).

## Seats

`positionRider` subtracts 0.7. The driver is stored as `(0, 1.7, 1.703125)`.

The floor top is at 15. The stored Y adds 0.7 so that, after `positionRider` subtracts it, an animal's feet stay on the floor. A player on the floor is lowered another 10/16: the riding pose lifts the feet by about that much. The driver stays on the cushion, without that extra drop. A villager and a wandering trader sit as passengers, not as animals, and the driver seat is not offered to them.

| Index | Position | Kind |
| --- | --- | --- |
| 0 | (0, 1.7, 1.703125) | `DRIVER` |
| 1 | (0, 1.6375, 0.2) | `UNIVERSAL`, half a block in front of the large seat |
| 2 | (0, 1.6375, −1.3) | `UNIVERSAL`, one block behind the large seat |
| 3 | (0, 1.6375, −0.3) | `ANIMALBIG` |

The buttons are labeled 1, 2, 3, 4 from left to right: driver, front universal, large, rear universal. The large seat accepts only a large passenger and closes both universal seats. A player or a small animal on a universal seat closes the large seat. Items in the cargo close all three. Anyone on any of them closes the cargo. If one of them is an animal, the label is `gui.trotting_wagons.big_cart.animals` ("Animals are using the cargo space"). If only players are seated, it is `gui.trotting_wagons.big_cart.players` ("Passengers are using the cargo space").

## Screen

`textures/gui/big_cart.png`, 320×236. Cargo slots are not painted on the texture: 14×3 from (18, 22). While an animal occupies a seat, the slots move off and the center of that grid says "Animals are using the cargo space". The health bar is at (13, 82), width 294. Seats start at (108, 100), step 26. Draft animals are at the same height, frame `big_slot.png`. Harness (26, 144). Wheels (17, 192). The player inventory starts at (80, 152). Repair is on the right, with its header on the top edge of the seats and centered on the lantern. The lantern is already painted at (278, 192). The "Custom Items" label sits above it. There is no lock button.

The lantern is one `minecraft:lantern`, bone `Lamp`. An empty slot hides the bone. A filled slot places light level 15 in the glass-center cell every 10 ticks. Removal and destruction clear that block. Destruction drops the lantern and the harness.

Wheel bones match the debug wagon: `LeftFrontWheel`, `RightFrontWheel`, `LeftRearWheel`, `RightRearWheel`, wide wheels with `2`, reinforced wheels with `3`. Click zones in model pixels:

| Corner | X | Y | Z |
| --- | --- | --- | --- |
| 0 | 24.25 | 9.5 | −21 |
| 1 | −23.75 | 9.5 | −21 |
| 2 | 24.75 | 9.5 | 27 |
| 3 | −24.75 | 9.5 | 27 |
