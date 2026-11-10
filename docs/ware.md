# Ware Wagon

Item and entity `trotting_wagons:ware_wagon`. Class `WareWagon` extends `AbstractWagon` and implements `MenuProvider` itself. Names: Ware Wagon. No recipe. Finished wagons are not in the creative tab. The item is created with `BASE_COLOR` white, and `getDefaultColor()` returns `null`.

Model `assets/trotting_wagons/geo/entity/ware_wagon.geo.json`, identifier `geometry.WareCab`. Texture `textures/entity/ware_wagon/none.png`, 256×256. The root `WareWagon.geo.json` and `WareWagon_Texture.png` were moved and deleted. Animation `animations/entity/ware_wagon.animation.json` is the clip `move.forwards` on bone `cart`: pitch −2.5° and roll ±1°. Bone `Chests` was moved from `Wagon` onto `cart`, so the three chests rock with the body. `SideDoor` is always visible and has no click of its own. `Ladder` is always visible. A player outside the rear face, below the roof, and in front of the bone climbs it like a ladder: a step into the wagon or a jump raises them, sneak holds them in place, and with no key held they slide down slowly. When their feet reach the roof, they are placed on the hitbox roof, 0.6 blocks in from the rear face. On the roof the ladder no longer holds: from that side they can step away and jump off. The ladder column catches them again if they fall into it from behind. Creative flight does not start the ladder. The item model `models/item/ware_wagon.json` has parent `minecraft:builtin/entity`, scale 0.1, a GUI translation of `[0, -2.5, 0]`, and a rotation of `[20, 45, 0]`.

The axles are bones `FrontAxis` and `RearAxis`. They spin by the same angle as the wheel groups and stay visible. Wheel names are shared: regular, suffix `2` for wide, suffix `3` for iron. There are four chest slots. Bones `chest1`–`chest4` are shown by the number of placed chests.

The texture is always `none.png`. `getDefaultColor` returns `null`.

## Config

Section Ware Wagon Options. Driving leaves the acceleration unread.

| | |
| --- | --- |
| Health | `ware_wagon_health`, default 100 |
| Coefficient with no chests | `ware_wagon_speed_empty`, default 0.9, read when the chest mask changes |
| Reduction per chest | `ware_wagon_speed_per_chest`, default 0.1 |
| Turn | `ware_wagon_turn_rate`, default 2.0 |
| Acceleration in config | `ware_wagon_acceleration`, default 3.0. Driving leaves it unread |

The coefficient is the empty value minus the step for each placed chest, floored at 0. At a step of 0.1 and four chests that is 0.9, 0.8, 0.7, 0.6, and 0.5. What is counted is chests, not the items in them and not the cargo on the main tab.

## Body

The type is `sized(3.5, 2.75)`. The body is `orientedBody(1.625, 0, 2.981, 2.062, 2.078)`, and the white box is the shared ground plate. See [common.md](common.md). `bareDrop()` is 6/16. Ground rays: half-width `19.5/16`, half-wheelbase `19/16`. The front axle is at model Z −17, the rear at 21, and the half-wheelbase is the midpoint between them. Wheel clicks: `(19.25, 9.5, −17)`, `(−18.75, 9.5, −17)`, `(19.75, 9.5, 21)`, `(−19.75, 9.5, 21)`.

The tongue matches the passenger wagon: side offset `9.5/16`, reach `77.88461/16`, angle 13, length `57/16`, height `8.5/16`. Two draft animals, list `allowed_entities`. The sag is the shared one. This wagon has no angles of its own.

One seat, the driver. The hips sit on the top face of the cushion: X 0, Y 24, Z −27.75. The formula is `y/16 − 0.05`, so that after subtracting 0.7 the hips land at that height. There are no passenger seats. The screen has the driver button and as many harness buttons as `hitchSlots()`: none, one, or two. Animals stay in the harness and do not sit inside the wagon.

## Chests and cargo

Four slots. A slot accepts the tag `trotting_wagons:chests`, which contains `minecraft:chest`. A trapped chest, an ender chest, and a barrel do not fit. The wagon and each chest have 27 cells, a 9×3 grid. Everything lies in one `Cargo` list, with no hopper capability: slots 0–3 are the chests, 4–30 are the cargo, then four blocks of 27. That is 139 slots, and a new save writes `CargoVersion` 4. Version 3 was 3 chests, 27 cargo, and three blocks of 27. Those move across, and the fourth chest stays empty. Version 2 was 3 chests, 30 cargo, and three blocks of 30. The last 3 cargo stacks and the last 3 of each chest drop. A list with no version was 3 chests, 36 cargo, and three blocks of 27. The first 27 cargo cells stay, the last 9 drop, and each chest's 27 stacks move whole.

Mask `DATA_CHESTS`. Five tab buttons: the cargo and four chests. A button turns on from its own bit. Bones follow the count: one chest shows `chest1`, four show all of them. Which slot is occupied does not change the grid.

Right-clicking the wagon with a chest puts it into the first empty slot and does not seat the player, even when all four are full. Sneaking does not place it. A wheel or a lantern in hand takes its own branch. Creative mode does not spend the item. A chest in the off hand, with an empty main hand, is not placed.

Removing a chest drops its 27 stacks beside the wagon. The chest itself goes to the player. If the removed chest's tab is open, the screen returns to the cargo. Destruction drops the cargo, the chests, their contents, the lantern, and the repair before the wheels, including when `doEntityDrops` is off.

## Lantern

One slot, only `minecraft:lantern`. Bone `Lamp` is visible while the slot is filled. Light level 15 is placed at the glass center after bone `Lamp` is rotated by 90°, model `(14.53, 42.2, −22.68)`, every 10 ticks. An empty lantern catches the ray. Right-click places it. Shift-right-click removes it. A full slot does not get in the way of boarding.

## Screen

`WareMenu` / `WareScreen`, texture `textures/gui/ware_wagon.png`, window 320×250. Do not change the file size. There is no title row at the top: the name and the kilometers sit under the health bar, to the right of the driver, as on the barrel wagon. They share a line from (101, 116). The owner is stored and is not drawn. Five 26×26 buttons from (96, 7), step 32: the cargo and four chests. The cargo icon is already painted. The chest buttons draw the chest item. The selected one is outlined in yellow. A button for an empty chest cannot be pressed.

The grid is 9×3 from X 93, Y 39, step 18. The four chest slots are a 2×2 square from (49, 48). The cargo tab and each chest tab show the same 27 cells. A hidden page moves off the screen and does not take a click. The driver is at (69, 114). Draft buttons are at the same height: one at (21, 114), two at (8, 114) and (34, 114). Harness slot (26, 158). Wheels (17, 206). Lantern (278, 206). Repair (269, 130). The player inventory starts at (80, 166).

The health bar is at (13, 96), width 294. An empty chest slot shows a chest ghost. Repair frames are drawn only while the slot is active. The grid, the chest slots, the harness, the driver, the wheels, the inventory, and the lantern are already in the texture. `drawSlot` does not draw them again. No wagon screen currently adds the lock button.

The tab is stored in the menu. The button sets it on the client and sends `select_ware_tab`, so a shift fills the open page. The packet does not touch someone else's menu.

A shift from the wagon places items into the player inventory. From the player inventory, a wheel and a lantern fill only their own slots. A chest and a repair material fill their own range first. When that range is full, the next shift places them into the open tab. Any other item goes straight into the open tab.
