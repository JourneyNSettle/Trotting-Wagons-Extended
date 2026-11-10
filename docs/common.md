# Shared rules

The base class is `AbstractWagon`. An inventory of ordinary stacks is `AbstractInventoryWagon`. Seats come from that wagon's `WagonSeat` array, not from model bones. Bone `Tongue` is drawn only: the hitch points are computed in code.

`EntityType.Builder.sized(width, height)` is only the registered size. After the tick, `makeBoundingBox()` returns the yaw-aligned body, `orientedBody(halfX, bottom, top, rear, front)`: local X is ±halfX, Y runs from bottom to top, and Z runs from −rear to +front. The tongue and the shafts stay outside that body. `refreshBounds()` keeps the body for culling and installs the entity box as `halfFootprint`: half the length, half the width, and only the bottom 0.25 blocks. `canBeCollidedWith()` is false, so the white box does not stop a player or an animal. The yellow bone boxes do. On a diagonal the white plate is the axis-aligned hull of that rotated body, then halved. `wheelWidth` and `wheelLength` are the reach of the ground tilt rays and the sag anchor. They are not the size of the wheel model.

Wheel slots start empty. `previewRegularWheels()` paints four regular wheels on the inventory icon only, and that dummy is never spawned. Health at creation equals the maximum from the config.

## Controls and the menu

The hitch key is `key.trotting_wagons.hitch`, default `R`, key category `Trotting Wagons`. It sends `HitchAnimalPayload` while the player is riding a mob. The wagon search wants a living draft animal from the tag `draft_animals` or from `allowed_entities`, a free harness, and a horizontal distance no farther than `HITCH_RANGE`, 1.5 blocks from the tongue anchor. The left slot is tried first, then the right.

Opening the player inventory while seated in a wagon with fixed seats (`hasFixedSeats`) cancels the inventory screen and sends `OpenWagonMenuPayload`. The server opens the menu when the entity implements `MenuProvider`. Right-clicking the wagon does not open the menu. The menu stays open while the player is alive, the wagon is alive, and the player is still its passenger.

`driverAllows` is true when the driver seat is empty, or when the player sitting there is the one acting. While a driver is seated, only that player can fit or remove wheels, the harness, lanterns, banners, and pumpkins, or unhitch. Repair, cargo, and hitching a new animal stay open. A non-driver who is holding a part the wagon would accept does not board: the click fails.

Right-click without sneaking:

- a ray through the part zones (`WagonParts`) places the part when the item fits and a slot is free;
- otherwise the player sits in the nearest suitable free seat and inherits the wagon's yaw and pitch.

Right-click while sneaking, in the order of the held item:

- the whip `trotting_wagons:horse_whip` toggles reverse;
- a dye from the map `TrottingWagons.DYE_ITEMS` dyes the wagon and is not consumed;
- otherwise, if the player is leading a tamed draft animal, the harness is fitted, and a place in it is free, the animal is hitched. The animal closest to the wagon is taken. The lead returns to the inventory. Slots fill from left to right;
- otherwise the part under the cursor is removed: a wheel, lantern, pumpkin, banner, or harness. An empty hand is not required. If the ray hits none of those, the player does not sit.

With the whip in the driver's hand, right-click raises the gear by one, up to 4 (`HorseWhipItem.use`, sound `TWSoundEvents.WHIP`). A left-click in the main hand, while the player is `getControllingPassenger`, cancels the attack and, when the attack strength is at least 0.9, sends `WhipLowerGearPayload`. On gear 0, lowering does nothing. Right-clicking the whip on an animal already hitched to this same wagon does not mount the player on it, and it raises the gear.

Only a player in the `DRIVER` seat drives (`getControllingPassenger`). The gear resets to 0 when there is no such driver, or when the draft team is incomplete. The speed bar is drawn only for that driver: sprite `hud/jump_bar_background`, width 182, filled by `speedFraction` of the current speed against the gear-4 maximum.

The driver camera follows Small Ships. On the tick the local player becomes the driver, the view switches to third person from behind. When they stop being the driver, the view returns to first person. In third person the mouse wheel changes the distance: up is closer, down is farther, and the hotbar does not scroll. The distance runs from 1 to 10 blocks and starts at 6. Scrolling closer than 1 block switches to first person. Scrolling outward from first person returns third person behind at that same distance. While the driver is in first person, the wheel still does not scroll the hotbar. Small Ships caps the distance at 20. Here the cap is half of that. Walls still clip the camera. A passenger who is not the driver does not change the camera. While a screen is open, the wheel stays with the screen.

## Harness

`hitchSlots()` defaults to 2. On wagons with a harness slot, the count comes from the item: empty is 0 and both bones are hidden, Shafts is 1 and bone `shafts`, Tongue is 2 and bone `Tongue`. That covers the debug, passenger, ware, kibitka, barrel, animal, log, armored, royal, and Conestoga wagons. While an animal is hitched, the item cannot be taken out of the slot. The item is placed by right-clicking the wagon and removed by shift-right-clicking the tongue.

`hasPullers` requires the left draft animal and, with two slots, the right one as well. A slot stores the mob, its UUID, and the synced id (`leftHorse`, `rightHorse`). A hitched mob receives `setNoAi(true)`. At that moment a horse stops grazing and rearing, and a camel stands up if it was sitting. While the harness holds, `canEatGrass` on a horse returns false, so a new grazing pose does not start. A living draft animal more than 0.5 blocks horizontally from its point is pulled back (`HORIZONTAL_TETHER`). If it is farther than 5 blocks from the anchor (`HITCH_BREAK_DISTANCE_SQR`, 5×5), the harness releases and the animal stays where it is. An unloaded draft animal is not released.

The draft list is the tag `trotting_wagons:draft_animals`, or an exact id match in `allowed_entities`. This mod's tag contains only `minecraft:horse`. Another mod appends its own ids to the same tag with its datapack, and without that datapack those ids do not load. The default config also contains `minecraft:horse` and stays a list of additions. The animal must also be tamed: a horse, donkey, mule, and camel through `AbstractHorse.isTamed()`, a wolf and a cat through `TamableAnimal.isTame()`. A cow, or another mob with no taming, cannot be hitched. Another mod can add a refusal through `AbstractWagon.addDraftCheck`. A baby (`isBaby`) cannot be hitched. A baby that is already hitched is released on the check. The harness code does not inspect taming beyond those calls, and it does not look at a donkey or mule chest. A player and a second wagon cannot pull.

Hitching from a lead: shift-right-click the wagon while the player is leading a tamed draft animal, the harness is fitted, and a slot is free. The lead distance is the ordinary 10 blocks. There is no separate check against the tongue anchor. The `R` key still hitches the animal the player is riding, within 1.5 blocks of the anchor.

Unhitching by hand: sneak and right-click the hitched animal in the world (boarding cooldown 60 ticks), the harness button in the menu, or a drag from the harness button onto a seat. Sneak and right-click on an animal that is sitting in the wagon does not unload it. Unloading a seat stays on the menu button. Mounting a hitched animal also releases the harness. Auto-boarding does not take a hitched animal, including into the same wagon.

The animal point is left slot `+horseX`, right slot `-horseX`, forward `horseY`, plus `draftForward`. Left is the driver's left hand when the wagon faces local +Z. An old save without `hitchSides` 1 swaps `leftHorse` and `rightHorse`, so animals that were already hitched stay on the same shafts. A single hitch (kibitka, debug) is not swapped. A camel stands on the same point as a horse: the old `draftForward` of −1 pressed its hitbox into the wagon, and a Conestoga turn stalled at about 30°. On the hitch step the wagon itself is excluded from the animal's collision. The constructor stores `shaftAngle`, and `getShaftAngle()` returns it. The drawn angle does not read that field. While the wagon is rolled or sagged, hitch XZ follows the tongue and the feet stay on the nearest ground.

If the two slots hold animals of different types, the driver who hitched the second receives the advancement `trotting_wagons:sinner` (criterion `hitch_different`; the JSON trigger is `minecraft:impossible`). Matching types do not grant it. The advancement sits under `minecraft:husbandry/root`.

The harness search anchor is `(0, 0, horseY)` from the entity center. The visual tongue length and height are `tongueLength` and `tongueHeight`. `WagonModel` calls `tongueAngle(bodyPitch, roll, drop)` and applies that radian angle as `rotX` on both `Tongue` and `shafts`. With no wheels the angle is 0. Unhitched, the target height is 0. A forward body pitch lengthens the reach by `3 * wheelLength`, and a backward pitch by `1.25 * wheelLength`. Hitched, the top of the `bow` cube (model y=17) aims at the rider attachment of the taller animal. When no living animal supplies a height, the fallback is `hitchedTongueTarget()` (default 0.85; the debug wagon uses 1.2).

## Speed

Five gears, `SPEED_LEVELS` = 5, levels 0–4. The speed target is the maximum multiplied by `gear / 4`. Gear 0 is stopped. Gear 4 does not fall back to 0 on its own.

The maximum, in blocks per tick:

1. Each living draft animal contributes its `movement_speed` attribute without the Block Runner modifier `block_speed`.
2. The slowest value is multiplied by `BLOCKS_PER_SPEED_ATTRIBUTE` 43.17. That figure is blocks per second.
3. It is multiplied by the wagon coefficient and divided by 20.
4. If at least one wide wheel is fitted, it is multiplied once by 0.8 (`WIDE_WHEEL_FACTOR`). Four wide wheels stay at 0.8.
5. It is multiplied by the slowest ground multiplier among the fitted wheels.
6. It is multiplied by the harness coefficient from the table below. The Tongue (`Tongue`, two animals) is always 1. Shafts (`shafts`, one animal) depend on the wagon.
7. It is multiplied by `1 - bog`. `bog` grows by `1/240` per tick while the wagon is moving, or the gear is above zero, and soft ground is under any wheel. A wagon standing in gear 0 does not start to bog. A bog that has already started reaches 1 and holds the speed at zero. From a solid block, or with four wide wheels, `bog` falls back to zero over the same twelve seconds.

The wagon numbers are the default config values. On the log, barrel, and ware wagons the coefficient is assigned again when the cargo changes, and when a save loads. The movement tick reads the number already stored. On the others it is read in the constructor.

| Wagon | Coefficient | Shafts | Tongue |
| --- | --- | --- | --- |
| Conestoga | 0.8 | 0.7 | 1 |
| Royal | 1.0 | 0.9 | 1 |
| Armored | 0.5 | 0.5 | 1 |
| Debug | 0.9 | 0.8 | 1 |
| Animal | 0.9 | 0.8 | 1 |
| Log | 1.0 / 0.9 / 0.8 | 0.6 | 1 |
| Barrel | 1.0 / 0.9 / 0.8 | 0.6 | 1 |
| Passenger | 0.9 | 0.7 | 1 |
| Ware | 0.9 − 0.1 per chest, floored at 0 | 0.6 | 1 |
| Kibitka | 0.9 | 1.0 | 1 |
| Big cart | 1.0 | 1.0 | 1 |

The log wagon has three steps: no items, 1–1000, and 1001 or more. The barrel wagon: no barrels, 1–6, and 7–12. An empty ware wagon is 0.9, and each placed chest removes 0.1.

| Wheel | Multiplier |
| --- | --- |
| Regular | 1 |
| Wide | 0.8 once per wagon when at least one is fitted. Four wide wheels stay at 0.8 |
| Reinforced | 1 |

Without a full draft team, without all four wheels, while a fitted wheel is at zero durability (`hasSpentWheel`), or while `holdsTravel` is set, the maximum is 0 and `currentSpeed` is cleared. The default wheel-loss handler removes a wheel at zero durability, so that flag matters when another mod keeps the spent wheel. Gravity `GRAVITY` 0.04 remains. A missing wheel does not reset the gear.

Acceleration per tick is `coefficient / 400`. In blocks per second per second, that is the coefficient. The config keys `*_acceleration` are left unread by driving. Reverse is the same gear with a target of −0.25 of the forward target.

Turning: the steering impulse is multiplied by the speed fraction and by `turnRate`. If `|currentSpeed| < 1.0E-4`, the fraction is taken as 0.5, so a standing hitched wagon turns in place at half the turn speed. Without a draft animal the steering does nothing. Steering does not need every wheel, and on a two-animal harness it does not need both animals. Health and turn speed are read in the constructor. The coefficient is read there too, except on the log, barrel, and ware wagons: theirs is rewritten when the cargo changes.

The movement step is cut by the collision of the wagon itself and of each draft animal: the shortest horizontal shift is kept. Each one keeps its own vertical step, then gravity is added. The horizontal distance traveled accumulates on the server.

## Block Runner

The dependency is optional: mod id `blockrunner`, `mandatory=false`, and it is not on the compile classpath. `BlockRunnerSpeed` uses reflection to strip `block_speed` from the animal's attribute and reads `fuzs.blockrunner.world.level.block.data.BlockSpeed.getSpeedFactor` for the block under the center of a fitted wheel (`getY() - 1.0E-5`). With no mod, or no entry for the block, the multiplier is 1.

## Wheels

Four slots, limit 1, tag `trotting_wagons:wagon_wheels`:

- `trotting_wagons:wheel`
- `trotting_wagons:wide_wheel`
- `trotting_wagons:reinforced_wheel`

The regular wheel recipe is any planks and sticks around the rim, with the tag `trotting_wagons:iron_ingots` in the center. That tag contains `minecraft:iron_ingot`. The Wide Wheel is any planks, a stick, any planks / a regular wheel, the `iron_ingots` tag, a regular wheel / any planks, a stick, any planks. The Reinforced Wheel uses the `iron_ingots` tag and the `trotting_wagons:iron_bars` tag: ingot, bars, ingot / bars, ingot, bars / ingot, bars, ingot. No finished wagon has a crafting recipe that spends wheels.

Slot order: 0 front left, 1 front right, 2 rear left, 3 rear right. Left is local +X on the click zones of models with three wheel meshes (indices 0 and 2). The set label (`wheelSet`) looks at the items in the slots. Four matching items give `gui.trotting_wagons.wheels.regular`, `wheels.wide`, or `wheels.reinforced` (Regular, Wide, Reinforced). A mix is `wheels.incompatible` (Incompatible). An empty slot is `wheels.missing` (Missing). Before the menu syncs, the client can briefly show Missing.

The fitted-wheel mask is 4 bits of entity data. The wheel kind is 2 bits per corner (`DATA_WHEEL_KINDS`). The model reads the kind, not the client stack. Driving requires the mask `0x0F`.

Four wide wheels do not bog. Any other full set, when at least one wheel stands on a block in `trotting_wagons:soft_ground`, slows to zero over twelve seconds and sags toward `bareDrop()`, the same pose as with no wheels. The tag is `#minecraft:dirt`, `#minecraft:sand`, `#minecraft:snow`, `minecraft:gravel`, and `minecraft:suspicious_gravel`. A dirt path is not in the tag, and narrow wheels drive on it. One soft wheel is enough to start the bog. The bog is written to `Bog` and to entity data so the client lowers the body with the server. An incomplete wheel set does not start this bog: that pose comes from the missing wheels. The 0.8 multiplier from any wide wheel still applies when all four are wide.

A wheel click zone has half-width `WHEEL_X` 0.22 and radius `WHEEL_R` 0.75. A wide wheel extends the outer side by `WHEEL_WIDE_OUTER` 0.25. Placement goes into the nearest empty corner on an ordinary right-click. Removal is a shift-right-click on the wheel. The part ray loses to a closer block.

Regular and wide wheel durability is 100. Reinforced durability is 300 (the `MAX_DAMAGE` component on the item). An old stack without that component receives 100 when it wears (`WHEEL_DURABILITY`). Each full kilometer of total distance (`distance`, threshold `kilometerPart` 1000) removes 1 durability from each fitted wheel. Below 20 remaining durability, a moving wagon once a second (`tickCount % 20` and the flag `movedRecently`) has a 0.01 chance to remove that wheel. At 0 the wheel is removed as well. The sound is `ITEM_BREAK`. The wagon body does not lose health from distance, and it does not repair itself from distance.

On destruction the wheels always drop, before the `doEntityDrops` check. Repair items do not drop.

Visually the axle is spun in code on the bones `FrontWheels`, `RearWheels`, and, where they exist, `FrontWheels_Wide`, `RearWheels_Wide`, `FrontWheels_Iron`, `RearWheels_Iron`. The clip `move.forwards` rocks the body when the horizontal speed is above 0.01. An animation file is optional: GeckoLib writes to the log and the model stays.

### Sag with missing wheels

This is separate from the ground tilt rays. The rays shoot down from the four corners for a length of `wheelWidth * 2`. Pitch is the difference of the front and rear heights, limited to 45°. The rays produce no roll.

Visual sag, `wheelSag`:

| Wheels | Pose |
| --- | --- |
| 3 or 4 | no sag |
| 0 | no tilt, drop `bareDrop()`. The default is 7/16. The constant `WHEEL_SINK` 0.45 is unused |
| both rear missing | pitch `missingRearPitch()`, default −14 |
| both front missing | pitch `FRONT_SAG` +10° |
| the left pair or the right pair missing | roll ±14°, then +3/16 of drop |
| a diagonal | `DiagonalSag` sets both remaining wheels and the empty rear foot on the ground. The fixed `diagonalPitch()` is only the fallback when that wagon has no spec |
| one | pitch and roll of `oneWheelSag()`, default 3.5°, then +2/16 of drop |

`WHEEL_SAG` = 14. Animal and passenger wagons override `missingRearPitch()` to −8. Their `diagonalPitch()` of −8, and the debug wagon's `diagonalPitch()` of −8, are unused while `DiagonalSag` has a spec for that wagon. Every finished wagon except the kibitka has a spec. Extra height shifts from `sagDropCorrection`: a side roll +3/16 of a block, a two-wheel diagonal −12/16, one wheel +2/16. `bareDrop()` overrides: Conestoga, armored, and ware 6/16; debug 6.75/16; passenger 4.5/16; big cart 8.75/16; royal `0.2533850438077787`; animal `0.34623842746449673`. The big cart adds 1° to the one-wheel sag and another 2/16 of drop, plus 1/16 more when the remaining wheel is a rear one. The log wagon uses this table only for a diagonal. Zero wheels drop `bareDrop()`, and every other missing-wheel mask is solved by `LogWagonSag`. See [log.md](log.md). The barrel wagon uses `BarrelWagonSag` for one remaining wheel. Every other mask, including a diagonal, uses the shared pose. See [barrel.md](barrel.md). The kibitka solves every mask in `KibitkaWagonSag`, including zero wheels at `bareDrop()`. See [kibitka.md](kibitka.md).

## Health and repair

A player hit checks the part in front of the body. A closer wheel takes the hit as wheel damage, `max(1, round(amount * 0.5))`. A closer harness, lantern, pumpkin, or banner takes no damage. Otherwise the look must meet the culling body, the same test as the yellow frame, or the hit is dropped. A hit that lands reduces health by the full amount, plays `WOOD_BREAK`, and spawns spruce-plank particles. The number on the bar is the health rounded. When that rounding reaches the maximum, the value is clamped to the maximum: the bar fills completely, the condition is `ok`, and a repair slot stays closed. Repair adds `5` for each open slot and can leave a remainder such as `99.6` against a maximum of `100`. Without the clamp the number already shows a full reserve, while the bar is one pixel short and the condition stays Scratched. At 0 and below, `onDestroyed(false)` runs: the harness is released, passengers dismount, and the wheels drop. Body loot runs only when `doEntityDrops` is on. The default, for a wagon that was not built on the base, is `0–6` oak planks and `0–2` iron ingots (`nextInt(7)` and `nextInt(3)`). A wagon built on the base drops about half of each bill line (`dropAboutHalf`, `WagonBuild.salvage`) on the log, barrel, passenger, ware, kibitka, and big cart. One item drops as 0 or 1. Two items drop as exactly one. Past that, the drop is half plus or minus `max(1, count/8)`, and at most one less than the full count: 4 → 1–3, 6 → 2–4, 8 → 3–5, 12 → 5–7, 16 → 6–10, 18 → 7–11, 24 → 9–15, 32 → 12–20, 40 → 15–25, 48 → 18–30, 64 → 24–40. The stacks that were turned in are taken first. A shortfall is filled by that line's fallback item. The animal wagon uses its own ranges when it was built on the base: 2–4 sticks, 12–20 planks, 6–10 ingots, 1–2 hay bales, and 0–1 wool. The Conestoga, the royal wagon, and the armored wagon drop that half bill only when they were built on the base, and they drop no oak planks and no iron scrap when they were placed as an item. The debug wagon has no override: a base-built one returns the paid stacks in full (`builtPlan.drop`), and an item-placed one drops the oak and iron scrap. Shift-right-click with an axe no longer breaks the wagon, and it does not return the wagon item. Open repair slots are placed by `RepairLayout` under the repair header, `header Y + 22`. A menu constant such as `REPAIR_Y` is only the slot's first coordinate.

There are four repair slots, limit 1. How many are open depends on the health percent:

| Health | Open slots | Condition |
| --- | --- | --- |
| 100% | 0 | `condition.ok` |
| 75–99% | 1 | `condition.scratched` |
| 50–74% | 2 | `condition.damaged` |
| 25–49% | 3 | `condition.severe` |
| below 25% | 4 | `condition.critical` |

A slot's demand is rolled once and held while the slot is open: planks 30%, iron 30%, wool 10%, leather 10%, string 10%, slime 10%. The items are `#minecraft:planks`, `minecraft:iron_ingot`, `#minecraft:wool`, `minecraft:leather`, `minecraft:string`, and `minecraft:slime_ball`. When every open slot holds the right item, they are consumed, health grows by `5 * open_slot_count`, the sound is `ANVIL_USE` at volume 0.3, and the demands are rolled again. A slot that closes because of the heal drops its item into the world. The gray icon is oak planks, an iron ingot, white wool, leather, string, or a slimeball. Plank and wool tooltips use their own keys, `gui.trotting_wagons.repair.planks` and `repair.wool`. The others use the item name. Empty-slot tooltips appear at once, with no delay.

One shift-click with a repair material places one item into every open slot that accepts it. `moveItemStackTo` itself fills only one empty slot, so the menu runs it in a loop through `RepairShift`. The leftover stack stays with the player: this gesture does not continue into the cargo. The next shift-click, once repair accepts nothing more, places the leftover into the cargo when the item fits there. On the animal, passenger, and debug wagons the cargo does not accept iron, and the armored wagon has no cargo slots, so a repeat click moves nothing. On the Conestoga and the royal wagon the next click places the leftover into the cargo. Wheels and lanterns still go only into their own slots.

The keys `repair_material` and `repair_value` are removed. The old keys `*_speed` and `*_acceleration` do not set travel.

## Seats

`WagonSeat.Kind`:

| Kind | Who sits |
| --- | --- |
| `DRIVER` | a player only. This is the driver. A villager and a wandering trader do not sit here |
| `PLAYER` | a player, a villager, or a wandering trader |
| `ANIMAL` | anyone who is not a passenger from the row above. A large passenger is refused |
| `UNIVERSAL` | a passenger from the `PLAYER` row, or a small mob. A large passenger is refused |
| `ANIMALBIG` | not a passenger. On the animal wagon, the Conestoga, and the big cart, a large passenger only. The default accepts any non-player |
| `DRAFT` | not used as a seat. These are the harness buttons |

A large passenger is `minecraft:horse`, `minecraft:mule`, `minecraft:camel`, `minecraft:cow`, or the tag `trotting_wagons:large_wagon_passenger`. The tag defaults to those four plus `minecraft:polar_bear`. The Conestoga, the big cart, and the animal wagon override `acceptsLargeSeat` to `isLargePassenger`. A donkey, llama, pig, sheep, or mooshroom is not large until it is in the tag.

The tag `trotting_wagons:cannot_mount_wagon` holds the wither, the ender dragon, the ghast, the giant, and the ravager. Entity tags live in `data/trotting_wagons/tags/entity_type/`.

Animals board on their own within `ANIMAL_BOARD_RANGE`, 1 block of the hitbox, when a suitable seat exists, they are not already passengers and not being ridden, they are not hitched, and they are not on cooldown. A tamed animal boards when its owner is sitting in this wagon or standing no farther than 3 blocks (distance squared 9). Someone else's tamed animal is refused. After removal, `BOARD_COOLDOWN_TICKS` is 60.

Dismount searches for an empty volume to the left, right, front, and back. A passenger-wagon rider who is not the driver, and a kibitka rider who is not the driver, are placed at the rear center, just behind the hitbox. The passenger wagon's driver stands beside their seat, outside the side. The kibitka's driver searches for a side. An animal removed from an animal-wagon seat is placed at the rear center, just behind the stern of the body. If a block occupies the point, it shifts farther along the same axis.

A seat lock is shift plus a click with either button on `ANIMAL`, `ANIMALBIG`, or `UNIVERSAL`, even when a mob occupies the seat. The lock refuses animals and still lets a player onto `UNIVERSAL`. It is stored in the NBT `seatLocks`. A locked seat draws the item `minecraft:barrier` at the center of the button. On the animal wagon, an empty seat overlapped by a neighboring large seat, or by the small seats, draws the same item and a tooltip at once: `tooltip.large_needs_space` or `tooltip.small_blocked`.

A player changes seats by clicking a free seat that accepts a player. A click on a mob in a seat unloads it (`ejectSeat`) when the clicker is sitting in this wagon. Dragging an animal sends `MoveAnimalPayload` with no source-seat number: the server finds the mob by id.

`positionRider` always calls `offsetToWorld` for the seat: after rotation and sag, 0.7 is subtracted on Y, because the sitting animation lifts the model. Conestoga, royal, and armored coordinates are already stored as the point before that subtraction. On the debug, animal, and log wagons the formula adds the same 0.7 to some seats, and seating removes it, so the passenger ends up at the bone's model height. On the barrel wagon every seat is stored that way. On the passenger, ware, and kibitka wagons the formula uses −0.05 instead of +0.7: after the 0.7 subtraction, the hips land on the stored height.

Seat indices on wagons that already exist do not move.

## Owner, distance, wagon lock, color

Placing a wagon item writes the owner (`WagonItem`) and spends one item. The owner name is synced as a string. `WagonGui.drawHeader` draws the distance counter and does not draw the owner. The distance counter is `distance / 1000`, one digit after the decimal point, key `gui.trotting_wagons.distance`. In NBT the distance is a `double` (`distance`, `distancePart`). Entity data receives it as a `float`.

`WagonLockButton` toggles the NBT `locked` and is meant to show only for the owner, at `(6, imageHeight - 22)`. No current wagon screen adds that button. Boarding, cargo, wheels, and the harness do not read the flag.

The default color is white (`getDefaultColor`). The armored, debug, animal, log, barrel, passenger, ware, and kibitka wagons return `null`, texture `none.png`. Dye writes the color id into entity data. On the Conestoga and the royal wagon the texture is chosen by the dye name. On the debug, animal, log, barrel, passenger, ware, and kibitka wagons the texture is always `none.png`, even after dyeing. The wagon item's `BASE_COLOR` component defaults to white.

## Creative tab

`itemGroup.trotting_wagons` (EN Trotting Wagons Extended, RU Повозки). The icon is the regular wheel. Finished wagons are absent from the tab, and JEI does not show them either: the JEI list comes from the tabs, and `WagonJeiPlugin` also removes these items at runtime when JEI is installed. Order: `horse_whip`, `tongue`, `shafts`, `wheel`, `wide_wheel`, `reinforced_wheel`, `long_stick`, `chassis`.

No finished wagon has a crafting-table recipe. Recipes exist for the whip, the Tongue, the Shafts, the long stick, the chassis, the three wheels, and the saddle. The saddle is the 1.21.6 shaped recipe: three leather and one iron ingot, pattern `" X "` / `"X#X"`.
