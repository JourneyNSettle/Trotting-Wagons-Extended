# Log Wagon

Item and entity `trotting_wagons:log_wagon`. Class `LogWagon` extends `AbstractWagon` and implements `MenuProvider`. Names: Log Wagon. No recipe. Finished wagons are not in the creative tab. The item is created with `BASE_COLOR` white, and `getDefaultColor()` returns `null`.

Model `assets/trotting_wagons/geo/entity/log_wagon.geo.json`, geometry identifier `geometry.LogsCab`. Texture `textures/entity/log_wagon/none.png`, 256×256. The build does not read a file of that name in the repository root: the original export was moved onto these paths. Animation `animations/entity/log_wagon.animation.json` is the clip `move.forwards`, 1.5 s, on bone `cart` only, the same as the animal wagon: pitch −2.5° and side roll ±1°. Bone `Logs` is a child of `cart`, but its pitch is set to the negative of the body's, so the pile takes only the side roll. Wheels and the tongue are spun in code. Individual logs have no extra shift. The item model `models/item/log_wagon.json` has parent `minecraft:builtin/entity`, scale 0.1, a GUI translation of `[0, -2.5, 0]`, and a rotation of `[20, 45, 0]`.

The texture is always `none.png`. `getDefaultColor` returns `null`. Dye still writes the color onto the entity and onto the picked-up item. It does not change the picture.

## Parameters

| Parameter | Value |
| --- | --- |
| Hitbox | type `sized(4.5, 3.25)`. The body is `orientedBody(1.625, 0, 3.207, 2.562, 2.266)`. The white box is the shared ground plate. See [common.md](common.md) |
| Health | `log_wagon_health`, default 80 |
| Coefficient | empty 1.0, 1–1000 items 0.9, above that 0.8. Keys `log_wagon_speed_empty`, `log_wagon_speed_partial`, `log_wagon_speed_full`. Assigned when the cargo changes and on load, not in the movement tick |
| Turn | `log_wagon_turn_rate`, default 2.0, read in the constructor |
| Acceleration in config | `log_wagon_acceleration`, default 3.0. Driving leaves it unread |
| `wheelWidth`, `wheelLength` | 19.5/16 and 19/16 |
| Harness | harness slot. Empty is 0. Shafts is 1, centered at Z 77.88461/16. Tongue is 2, with slots at `horseX` ±9.5/16 and the same Z. `shaftAngle` 13 |
| Tongue | length 57/16 |
| Sag | its own, from the model. The shared angles and the shifts +3/16, −12/16, +2/16 do not apply to this wagon |

The threshold of 1000 items, the log step of 128, the vanilla capacity of 7040, the 11 bones, and the 90 kinds are not in the config. They are the constants `PARTIAL_LIMIT`, `LOG_STEP`, `CAPACITY`, `LOG_BONES`, and `TYPE_SLOTS`. The compat mod lowers the capacity to 1760 through `LogCargo.setLimit`.

### Sag

Zero wheels drop `bareDrop()` with no tilt. The default is 7/16. A diagonal mask, `0b0110` or `0b1001`, uses the shared `DiagonalSag`. Every other missing-wheel mask is solved by `LogWagonSag`: each remaining tread sits on the ground, and the frame stops on the first rigid part. Three and four wheels return a level pose. The tongue is not part of the contact. Wide, reinforced, and mixed wheels are solved from the wheel in each slot. The pose is cached until the mask and the wheel kinds change. The old degree table is not in the solver.

Live acceleration is the current coefficient, as on the other wagons: `coefficient / 400` per tick. A wide wheel multiplies the speed by 0.8 once more, and the Block Runner ground multiplier is applied after that. The coefficient is already the multiplier of the slowest draft animal, so it is not multiplied a second time.

The item count for the bones and for the speed is the sum of the counts in every cargo slot. On the client, `cargoCount` takes the larger of the handler's sum and the synced integer `DATA_CARGO`, so a stale client inventory does not under-report the number. The model looks only at `syncedCargo()`, so the logs stay visible with the menu closed. The server writes that integer in `syncCargo` and assigns the coefficient there. The client assigns it when `DATA_CARGO` arrives.

| Items | Coefficient |
| --- | --- |
| 0 | `log_wagon_speed_empty` (1.0) |
| 1–1000 inclusive | `log_wagon_speed_partial` (0.9) |
| 1001 and above | `log_wagon_speed_full` (0.8) |

## Driver seat

One seat, kind `DRIVER`. `seat(0, 17.08333, −30.83333)` stores `(0, 17.08333/16 − 0.05, 30.83333/16)`. Boarding subtracts 0.7, and the sitting hips are another 0.75 above the entity, so that Y is the hip height. An animal cannot sit here. The menu opens with the inventory key while the player is seated.

## Wheels and axles

Three meshes, the same bone names as the debug wagon and the animal wagon. Click zones on the model axes:

| Corner | X | Y | Z |
| --- | --- | --- | --- |
| 0 | 19.25 | 9.5 | −17 |
| 1 | −18.75 | 9.5 | −17 |
| 2 | 19.75 | 9.5 | 21 |
| 3 | −19.75 | 9.5 | 21 |

Indices 0 and 2 are local +X. A wide wheel extends the outer side of the zone by 0.25. Shift-right-click removes a wheel. A wheel in hand is placed into the nearest empty corner with an ordinary right-click. A wheel is not in the cargo tag, so that right-click never reaches log placement.

The axles are the separate bones `FrontAxis` and `RearAxis`. They spin by the same angle as the wheel groups and stay visible. All three wheel kinds sit at Y 9.5.

An empty corner hides all three wheel meshes on that corner. Bone `Logs` is not hidden.

## Logs on the model

`visibleLogs`: at a count of 0 or below, 0 bones. Otherwise `min(11, ceil(count / 128))`.

| Items | Visible bones |
| --- | --- |
| 0 | none |
| 1–128 | `Log_1` |
| 129–256 | `Log_1`, `Log_2` |
| … | onward by number |
| 1281–1408 | `Log_1` … `Log_11` |
| 1409 and above | the same eleven |

The order is strictly the bone number. Extra `Log_i` with `i` above the count are hidden.

While the wagon moves, the pile rolls sideways with the body and does not follow its pitch. Individual logs are not shifted along the body.

## Cargo

The item tag `trotting_wagons:log_wagon_cargo`:

- `#minecraft:logs` (logs, wood, stripped wood, stems, and hyphae, when they are in the vanilla tag);
- `#minecraft:planks` (including bamboo planks, when they are in the planks tag);
- `minecraft:stick`.

A datapack can extend the tag. A kind is `ItemStack.isSameItemSameComponents`: oak and birch are different, a log and a stripped log are different, and a renamed stack is its own kind.

Without the compat mod the limit is `CAPACITY`, 7040 items. The compat mod sets a quarter of that, 1760, at startup. The limit is for the whole wagon, not for one slot. The cargo meter divides the current count by `cargoCapacity()`. One kind takes one slot and can hold the whole current limit when no other kind is present. A new kind takes the first empty index. A 91st kind does not fit: `TYPE_SLOTS` prepares 90 slots. The vanilla tag set is about 53 kinds, so room remains for a datapack.

The handler is an `ItemStackHandler` over a `NonNullList`. `getSlotLimit` equals `cargoCapacity()`. `getStackLimit` returns `roomIn`: how many more can go into this slot given what is already there and the overall sum. An ordinary NeoForge insert then grows a pile above 64. `ItemStack.setCount` does not clamp the count.

After every slot write, `mergeSameKinds` folds matching kinds (`ItemStack.isSameItemSameComponents`) into the earliest slot and clears the duplicate. A mouse click on the empty filler no longer leaves a second slot of the same item: vanilla `set` puts the stack into the cell's index, and the merge immediately moves it onto the kind that is already there.

The vanilla `ItemStack.CODEC` accepts a count only from 1 to 99. `ContainerHelper.saveAllItems` on a pile of 6976 therefore fails the world save. The cargo is written as a `Cargo` list: each pile stores the item with a count of 1 and an integer field `pile` with the real number. The file receives the tag that `ItemStack.save` returned: the NBT encoder copies the passed components and does not fill them in. An old compound tag `Cargo` with the key `Items` is still read. The slot's network codec is a varint, so large numbers pass through an open menu.

The entity registers the capability `ItemHandler.ENTITY` (`CommonSetupEvents`, `LogWagon.getItemCapability`). A hopper inserts only an item from the tag, merges it into the same kind, and stops at `cargoCapacity()`. Hopper extraction goes through `extractItem` and is at most the item's maximum stack. For a log, a plank, and a stick that is 64.

### Right-click in the world

A right-click without sneaking, while the player is not yet sitting on the wagon and the hand holds an item from the tag, places the whole stack into a suitable slot. The leftover stays in the hand. Creative mode (`instabuild`) inserts a copy and does not spend the stack. The click counts as a success even when nothing fit, and the player does not sit. Sneaking does not take this path: the whip, dye, and shift-right-click on a part remain. An empty hand, a wheel, and the harness go through the shared `interact`.

When an empty slot receives the stack whole, the handler keeps that same stack object. The hand is then replaced with an empty stack or with a leftover copy, so the pile in the wagon is not cleared.

### Destruction

Before `super.onDestroyed`, each kind drops in batches no larger than the item's maximum stack and no larger than 99. One entity holding the full count cannot be saved by that same codec, and it crashes the world when the dropped item is written into the chunk. The wheels then drop as they do on every wagon. Repair items do not drop. The `doEntityDrops` flag does not cut off these piles or the wheels: the gamerule check sits below the wheel drop and applies to the wagon item or to the scraps.

## Screen

Texture `textures/gui/log_wagon.png`, 320×190. That is the height of one cargo row. Cargo cells are not painted on the texture: a frame appears only on a visible slot. The name and the kilometers sit to the right of the driver, as on the ware wagon: the name at `(101, 56)`, the counter 27 pixels on. The owner is stored and is not drawn.

Top to bottom, while there is one row:

1. Cargo from `(25, 14)`, 15 columns, step 18.
2. The health bar 4 pixels below the last row, x=13, width 294, height 10. Eight pixels remain before the driver.
3. The driver, a 26×26 button at `(69, 54)`. Draft buttons sit to the left at the same height: one at x=21, two at x=8 and x=34. The draft frame is `big_slot.png`.
4. The harness label and the slot `(26, 98)`. An empty slot shows the Tongue.
5. On the right, the header `gui.trotting_wagons.repair` sits on the top edge of the driver seat. Open repair slots are centered under it. Below the space of four repair slots, even when fewer are open, is the vertical meter `gui.trotting_wagons.cargo`: a 12×48 column grows from the bottom, and the fill is `cargoCount / cargoCapacity()`. Up to 40% the fill is green, up to 80% yellow, and from 80% red. The percent sits under the column.
6. Wheels 2×2 from `(17, 146)`. The player inventory starts at `(80, 106)`. The hotbar is at y=164.

Each further cargo row inserts 18 pixels at y=54 and lowers the painted lower half together with the driver, the harness, the wheels, the repair, and the inventory. The top edge of the texture stays put. The window recenters. Six rows give a height of 280. Empty spare kind-slots are not drawn and do not take a click.

The harness is placed by right-clicking the wagon with the item and removed by shift-right-clicking the tongue while nobody is hitched. A wagon already in the world, with no harness item, releases its loaded draft animals on the next tick.

The order of visible cargo cells is the filled slots by rising index, then one trailing empty filler slot. The filler is the first empty index, even when that is a hole between occupied slots. Its gray icon is an oak log (`GraySlotIcons`, `Items.OAK_LOG`). While the item count is under the current limit and a free index exists, there is one filler. At the limit, or at 90 occupied kinds, there is no filler. The gray icons for wheels and repair stay as they were.

`Slot.x` and `Slot.y` are declared `public final` in 1.21.1. The client screen writes them every frame through `sun.misc.Unsafe`, because the search for the cell under the cursor reads those fields and cannot be overridden from outside. The server clicks by slot index and does not need the coordinates.

`LogCargoSlot.isActive` is true when the slot holds an item or when it is the filler index. `mayPlace` requires an active slot, so a hidden hole accepts neither a direct click, nor a drag, nor a shift-click. A filled slot is active.

The count is drawn in full. When the string is wider than 16 pixels, the vanilla digit is covered and the same string is drawn at scale `16 / width`, in white, with a shadow.

The menu contains 90 cargo slots, 4 wheels, 36 player slots (the hotbar first, then three rows), the harness slot, and 4 repair slots from the start. `stillValid` means the player is sitting in a living wagon.

### Cargo clicks

A left-click takes items through `extractItem`, and that cuts the output at the item's maximum stack. For a log, a plank, and a stick that is 64, even when the cell holds thousands. A right-click on a cell takes half the count, but the output is still at most 64: a pile above 128 also yields 64 on right-click. The drop-one key gives 1. Dropping the whole stack gives at most 64. Middle-click in creative mode copies the maximum stack, 64, not the whole pile.

A shift-click out of the cargo takes another path: `moveItemStackTo` shrinks the live stack and spreads it across the player's ordinary slots in batches of 64, until the inventory runs out. One such click can take the whole pile.

A shift-click from the player inventory:

1. A wheel from the tag `wagon_wheels` goes into the wheel slots.
2. Shafts and the Tongue go into the harness slot when no animals are hitched.
3. One gesture with a repair material fills every open repair slot that accepts it. The leftover stays with the player.
4. The next shift-click, once repair accepts nothing more, and any cargo-tag item that repair does not want, merges into its kind or into the filler.

Planks are both cargo and a repair material. The first shift-click fills the repair slots and does not put the leftover into the pile. The next shift-click puts the leftover into the cargo. A manual click on a repair slot still puts planks there. Wheels do not enter the cargo.

A manual click on the filler, or on the same kind, puts the item into that pile up to the overall capacity. Vanilla can swap a different kind into an occupied cell when both items are accepted as cargo: the cells are then drawn again by index.

The hopper and the menu share one handler. The server stays the source of truth for the sum, the bones, and the speed.
