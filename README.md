# Trotting Wagons Extended

Horse-drawn wagons for Minecraft 1.21.1. Version **0.9.6** is a public test build. The mod id is still `trotting_wagons`, so old worlds keep their wagons. This repository continues [Favouriteless/Trotting-Wagons](https://github.com/Favouriteless/Trotting-Wagons).

The test jar is on the [0.9.6 release](https://github.com/JourneyNSettle/Trotting-Wagons-Extended/releases/tag/v0.9.6).

## Install

Minecraft 1.21.1, NeoForge 21.1.248 or newer, GeckoLib 4.8.3, and BoneHitboxLib 0.3.0. Put the same jars on the server and on every client. [Block Runner](https://www.curseforge.com/minecraft/mc-mods/block-runner) is optional: wheels then follow the block under them.

## Build a wagon

Craft a long stick from two sticks and two strings. Craft a chassis from long sticks, sticks, string, and slime. Place the chassis on a block top and right-click it with planks and iron ingots. One click adds one item. Sneak adds as many as that step still needs: 16 planks and 4 iron ingots. The chassis then becomes a wagon base.

Right-click the base and choose a wagon. The screen lists what that wagon still needs. Right-click the base with those items to add them. Sneak adds a stack's worth. When the list is full, the wagon appears in place of the base. Finished wagons are not in the creative tab and not in JEI.

Break a chassis or an unfinished base with an axe. You get the chassis item and the materials back.

## Fit the harness

| Item | Animals |
| --- | --- |
| Tongue | Two, side by side |
| Shafts | One, in the middle |

Right-click the wagon with the item. Shift-right-click the pole to take it off. It stays put while an animal is hitched.

## Hitch

The animal must be a tamed adult. This mod's tag allows the horse. More animals can be added in `config/trotting_wagons-server.toml` or by another mod's datapack.

- Ride the animal up to the pole and press **R**.
- Or lead it to the wagon and shift-right-click the wagon. The lead comes back.

Shift-right-click a hitched animal to release it. If a player is in the driver seat, only that player can release animals, or fit and remove wheels, the harness, lanterns, banners, and pumpkins.

An animal that ends up more than 5 blocks from its hitch point is released where it stands.

## Drive

Right-click the wagon to sit in the nearest free seat. The driver seat is the only one that drives. The view moves behind the wagon and returns to first person when you leave. The mouse wheel sets that camera from 1 to 10 blocks and does not change the hotbar.

The wagon rolls only with a driver, a full hitch, and all four wheels.

Right-click with the horse whip to raise the gear, up to 4. Left-click with the whip in the main hand to lower it. Gear 0 is stopped. Shift-right-click with the whip toggles reverse. A speed bar shows for the driver only.

## Seats and cargo

Press the inventory key while you are seated. That opens the wagon. From there you can move passengers, change wheels, repair, and use that wagon's cargo. Click a seat to move yourself. Click an animal, or drag it, to move or unload it.

Villagers and wandering traders can take passenger seats. They cannot drive.

## Wheels

A new wagon starts with four regular wheels. Right-click with a wheel to fit one. Shift-right-click a wheel to take it off.

Wide wheels are slower. One anywhere on the wagon is enough for that. Reinforced wheels last longer and keep normal speed. On dirt, grass, sand, gravel, or snow, any full set that is not four wide wheels sinks over about twelve seconds. A dirt path stays firm. Four wide wheels do not sink.

## Damage

Hits land on the wagon's solid body, including a tall cover such as the Conestoga. A hit on a wheel damages that wheel for half as much. A hit on the harness pole does nothing. Wearing the wagon down destroys it. Wheels, the harness, and cargo drop. A wagon built on the base also drops about half of the materials that went into it.

## The wagons

| Wagon | Use |
| --- | --- |
| Conestoga | Covered wagon, two pages of cargo. The second page, a large animal, and the side seats exclude one another. |
| Royal wagon | Carriage with cargo and passenger benches. |
| Armored wagon | Slow wagon with six passenger seats. |
| Passenger wagon | Seats. Passengers step off by the rope ladder. |
| Ware wagon | Freight. Chests placed on it show on the model and slow it. |
| Log wagon | Logs, planks, and sticks. The pile grows with the load. |
| Barrel wagon | Twelve barrels. Filled ones show on the model and slow it. |
| Animal wagon | Seats for animals, including large ones. |
| Kibitka | Covered cart with a camp layout. |
| Big cart | One open bed: cargo, or one large animal, or two seats. The ladder is decoration. |
| Debug wagon | A bench for trying seats, wheels, a lantern, and the harness. |

Most wagons accept dye, and the dye is not used up. The big cart, and wagons that have only one texture, do not change color.

Server numbers for health, speed, and turning are in `config/trotting_wagons-server.toml`. The notes in [docs/](docs/) describe the current behavior in Russian.

## Building the mod

JDK 21. From the repository root run `./gradlew build`, or `gradlew.bat build` on Windows. The jar is written to `build/libs`.

## License

All rights reserved. Authors are listed in `gradle.properties`.
