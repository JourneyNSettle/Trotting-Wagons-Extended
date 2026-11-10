# Kibitka

Item and entity `trotting_wagons:kibitka_wagon`. Class `KibitkaWagon` extends `AbstractInventoryWagon`. Names: Kibitka. No recipe. Finished wagons are not in the creative tab. The item is created with `BASE_COLOR` white, and `getDefaultColor()` returns `null`.

Model `assets/trotting_wagons/geo/entity/kibitka_wagon.geo.json`. The identifier in the file is `geometry.SecretShopCab`. Texture `textures/entity/kibitka_wagon/none.png`, 256×256, copied as it is. The root `Kibitka.geo.json` and `KibitkaWagon_Texture.png` were moved and deleted. Animation `animations/entity/kibitka_wagon.animation.json` is the clip `move.forwards` on bone `cart`: pitch −2.5° and roll ±1°. The item model `models/item/kibitka_wagon.json` has parent `minecraft:builtin/entity`, scale 0.1, a GUI translation of `[0, -2.5, 0]`, and a rotation of `[20, 45, 0]`.

The open-tent bone in the file is named `Tent_OPEN`, not `Tent_OPENED`. The closed one is `Tent_CLOSED`. The door is `Door`, parent `cart`. The lantern is `Lamp`, parent `Wagon`. Bone `Chest` is drawn only. There is no separate chest.

The axles are bones `FrontAxis` and `RearAxis`. They spin by the same angle as the wheel groups and stay visible. Wheel names are shared: regular, suffix `2` for wide, suffix `3` for iron. The bed is bone `Bed`. Shafts show bone `shafts`. A tongue shows bone `Tongue`.

The texture is always `none.png`. `getDefaultColor` returns `null`.

## Config

Section Kibitka Wagon Options. The coefficient is read once in the constructor, as on the passenger wagon. Driving leaves the acceleration unread.

| | |
| --- | --- |
| Health | `kibitka_wagon_health`, default 60 |
| Coefficient | `kibitka_wagon_speed_coefficient`, default 0.9 |
| Turn | `kibitka_wagon_turn_rate`, default 2.0 |
| Acceleration in config | `kibitka_wagon_acceleration`, default 3.0. Driving leaves it unread |

An existing `trotting_wagons-server.toml` keeps its old keys. New ones are picked up at their defaults until they are in the file.

## Body

The type is `sized(3.5, 3.85)`. The body is `orientedBody(1.625, 0, 3.806, 2.469, 2.438)`, and the white box is the shared ground plate. See [common.md](common.md). Ground rays: half-width `19.25/16`, half-wheelbase `17/16`. The front axle is at model Z −17, the rear at 17. Wheel clicks: `(19.25, 9.5, −17)`, `(−18.75, 9.5, −17)`, `(19.75, 9.5, 17)`, `(−19.75, 9.5, 17)`. Indices 0 and 2 are the +X side. A wide rim extends the click outward by 0.25.

The harness slot chooses the count, the same way as the other wagons: empty is 0, Shafts is 1, Tongue is 2. The constructor `horseX` is 0. One animal stands at X 0 and Z `76.88461/16`. Two animals stand at X ±0.6 and the same Z. The tongue length is `57/16` and the height is `8.5/16`. The constructor stores `shaftAngle` 13. The drawn angle is `tongueAngle`. The draft list is `allowed_entities`.

The sag is its own. Zero wheels return `(0, 0, bareDrop())` before `KibitkaWagonSag` runs. The default drop is 7/16, and the open tent is not a support in that case. One or more wheels are solved by `KibitkaWagonSag`: the remaining treads sit on the ground, and the body stops at the first contact. Three and four wheels of one tread level stay untilted. Iron wheel hubs stay at Y 9.5, the same as the regular hubs. The iron axle support is the lower one. The support uses the regular axle until all four slots are filled with iron wheels. The support includes the bottom of the frame, the floor, the step, and the axle. The tongue, the bow, the tent cloth, the door, the roof, the lantern, and the chairs do not rest.

While the tent is opening, the bottoms of the two `Tent_OPEN` poles are support as well. The pole scale is the same smoothstep as the bone: `t²(3−2t)` from pose `0..40`. At full open the pole bottoms are already on the ground, and a wagon with every wheel is not lifted. Zero wheels do not use those poles. Rotating the model around a point on the ground plane leaves those points on the slope. A separate bone shift is not needed.

While the camp pose is above 0, `holdsTravel` clears the speed, and `moveTick` clears the steering before that. The gear and the harness stay. After the tent has closed fully, the wagon accelerates from zero on the same gear.

## Camp

Two states. On the road, `Tent_CLOSED` is visible and the door is closed. In camp, `Tent_OPEN` is visible and the door stays closed as well. The transition is 40 ticks, 2 seconds. A new toggle is accepted only when the pose has reached its target: those 2 seconds are the delay. There is no separate pause after stopping.

Each tick the server steps the pose toward the target and writes it into entity data before `super.tick`, so in that same tick the wagon is already not driving. The client remembers the pose at the start of the tick and takes `Mth.lerp` between frames. On screen the progress is smoothed with `t²(3−2t)`. The tents change by scale: at 0, `Tent_OPEN` is hidden, at 1, `Tent_CLOSED` is hidden, and between them both are visible. A scale below 0.02 hides the bone and restores scale 1. The door rotation is kept: `DOOR_OPEN` is −90° inward, and `poseKibitka` applies it only when `KibitkaWagon.DOOR_OPENS` is true. The flag is currently false, so the door does not move. If it is turned on and the door opens outward, the sign in `DOOR_OPEN` changes to plus.

The camp button is a 26×26 square. A house is painted on the texture. While the target is camp, the house stays. While the target is the road, the house is covered in gray and a regular wheel, `trotting_wagons:wheel`, is drawn in its place. There is no text on the button. The tooltip has two lines: the next action, and the explanation. While the target is camp, the lines are `gui.trotting_wagons.kibitka.road` ("Take to the road") and `gui.trotting_wagons.kibitka.road.tip` ("Close the tent and drive"). While the target is the road, they are `gui.trotting_wagons.kibitka.camp` ("Make camp") and `gui.trotting_wagons.kibitka.camp.tip` ("Open the tent and stop"). While the tent is moving, the tooltip is only `gui.trotting_wagons.kibitka.wait` ("Wait for the tent"), and the square is darkened. It can be pressed when the pose has reached the target. The menu is open only for someone who is seated anyway. Packet `ToggleKibitkaCampPayload`. NBT `Camp` and `CampTarget`. A new wagon starts in the road state.

In the same row, on the right, are two 26×26 buttons. "Bed" draws a red bed in the center. The tooltip is `gui.trotting_wagons.kibitka.bed` ("Bed") and `gui.trotting_wagons.kibitka.sleep` ("Sleep"). "Set respawn point" draws a compass, with a one-line tooltip `gui.trotting_wagons.kibitka.respawn`. No text is written on the buttons. Both stay unpressable on the road and while the tent is moving, and the square is darkened. They turn on only when the pose and the target both equal 40. Packets `KibitkaSleepPayload` and `KibitkaRespawnPayload`. The server checks again that the player is seated and the tent is standing.

"Sleep" does not set the respawn point. The rules match a bed. A dimension without `natural` has its own message. By day the message is `block.minecraft.bed.no_sleep`. The check is `level.isDay()`, so a daytime thunderstorm is refused as well. Monsters nearby give `block.minecraft.bed.not_safe`. Creative mode does not check for monsters. The night is skipped through the ordinary sleeping list. `startSleepInBed` is not called: it would write the respawn point and would require a bed block. The player falls asleep through `startSleeping`, sits back down on their seat (boarding resets the pose to standing), and receives the pose `SLEEPING`. They lie on bone `Bed`, at the center of the top of the mattress: locally `(6.8/16, 19/16, −7.9/16)`. `offsetToWorld` does not subtract 0.7. The driver and the passenger lie at the same point. Sleep inside is held by `CanContinueSleepingEvent` only for `NOT_POSSIBLE_HERE`. Dawn wakes them, because `NOT_POSSIBLE_NOW` is not intercepted. `trySleep` does not set the player's rotation. No bed block is placed in the world. The chair mesh stays in the model.

"Set respawn point" does not put the player to sleep. The point is forced, `setRespawnPosition` with `forced`, beside the kibitka: the feet cell and the cell above it are passable for respawn, a solid face is under the feet, and the search starts outside the 3.5 hitbox. The message is the vanilla `block.minecraft.set_spawn`. Several players share this kibitka's one point. The point is cleared at the moment of the change to the road, and when the kibitka is destroyed, including when `doEntityDrops` is off. Only a point that still matches the recorded one is cleared: a later bed of the player's remains. An offline player waits until they log in. The record lives in the overworld `SavedData` `trotting_wagons_kibitka_spawns`. Unloading the chunk does not clear the point.

## Seats, lantern, cargo

Two seats, with the same formula as the passenger wagon: `(x/16, y/16 − 0.05, −z/16)`. Y is the top of the cushion. The driver is `(0, 18, −30.25)` on `DriverSeat`. The passenger is on `Chair` at `(−8.39723, 22, −11.50666)`. `Bed` is not a seat: it is the bed. A saved index 2 is past the array length on load and is unused. The player sits in a free suitable seat. Shift from the passenger seat places the player at the rear center, just behind the body. The driver stands beside the driver seat, outside the body, on the ground.

One lantern slot, only `minecraft:lantern`. A soul lantern does not fit. An empty slot hides bone `Lamp`. Light level 15 is at `(0, 39.6, −36)`. Right-clicking the wagon with a lantern places it and does not seat the player. Shift-right-click on the glass removes the lantern. Sneaking does not place it. Creative mode does not spend the item. A lantern in the off hand, with an empty main hand, is not placed. A click with an empty hand that misses the glass seats the player. The NBT is a nested compound `Lantern`, so it does not collide with the cargo's root `Items`.

Forty-five cargo cells, 3 rows of 15, through `AbstractInventoryWagon`, key `Items`. An old list of 36 items is read into the same indices. The new cells are empty. There are no hoppers: no capability is attached to the kibitka. An item from the world is not placed into these cells. Four wheel slots, three kinds. A new kibitka has no wheels fitted. Repair is the ordinary four slots.

Shift-click: a wheel goes only into the wheel slots, a lantern only into the lantern slot. One gesture with a repair material fills every open slot that accepts it, one item per slot. The leftover stays with the player. The next shift-click, once repair accepts nothing more, places the leftover into the cargo. An ordinary item goes straight into the cargo. From a wagon cell the item goes into the player inventory. A direct click also does not put a wheel or a lantern into the cargo.

On breaking, the kibitka drops the lantern and copies of the repair items before `super`, even when `doEntityDrops` is off. `AbstractInventoryWagon` drops the cargo after `super`, also regardless of that gamerule. The base drops the wheels. The repair slots are not cleared through `setStackInSlot`, so that does not start a heal.

The screen texture is `textures/gui/kibitka_wagon.png`. The window is 320×233. Slots already painted on the texture are not outlined in code. Repair frames are drawn only while the slot is active. Empty wheel, lantern, and active-repair slots show an item ghost. The health bar is the shared red one, under the cargo: x=13, y=79, width 294, height 10. The texture leaves no room under it. The bar is drawn in code.

The window is 320×233. Cargo is 15×3 from (26, 22). The health bar is at (13, 79). Seats are 26×26: the driver at (74, 97), the passenger at (100, 97). The camp, sleep, and respawn buttons are at (150, 97), (185, 97), and (220, 97). The harness slot is at (26, 141). Wheels at (17, 189). The inventory starts at (80, 149). The lantern is at (278, 189). Repair sits on the top edge of the seats. The texture has no lock button. An empty harness slot shows a gray Tongue.
