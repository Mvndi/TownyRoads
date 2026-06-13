# TownyRoads

Add roads to Towny.

This plugin try to push player to build roads between towns by giving them partial claim protection & other bonuses for connected towns.

Towny is required.
MapTowny, squaremap & RoadSpeedMounts are supported as optional dependencies.

## MapTowny

MapTowny allow the road to be displayed into the dynmap.

## RoadSpeedMounts

RoadSpeedMounts allow for different speed in roads and outside roads.

# Wiki

TownyRoads add roads to a Towny world.

## Creating a road

Roads need to be created between 2 town first.
`/tr create <town>` create a town between your town and the one you specify.
The other town need to accept your road invite: `/tr accept <road>`.

You then need to claim the area of the road by moving in each chunk between the 2 towns and then claim it: `/tr claim <road>`.

Once you have connected both town and the other town have accepted to join the town, you can validate the road `/tr validate <road>`.
If something is wrong with the road, you will have an explanation of what is wrong in chat and will be able to validate again once the road is fixed.

Until then the road had no effect, once the road is validated, it start having effects.

### Limits

Roads can't be more than 1000 chunk long.
Roads can't be longer than 2 times the distance of the shortest path between the 2 towns.

## Roads effects

### Claim protection

The player that are not part of the road towns won't be able to break or build much on the road chunks. Once they have placed or break 10 blocks, they need to wait 1 minutes for each new blocks they want to place or to break.
This make ambush still possible but make major grief to time consuming.
Chest or doors can be open without limitation, do not use roads as an extension of a town.

Make sure to give `comayor` or a rank with `townyroads.claimed.ownroad.*` permission to your resident that are supposed to build the roads.

### Upkeep

A town connected to it's captial pay 50% upkeep less.
A capital connected to other nation towns pay less upkeep, up to 50% if all nation towns are connected. It also make the nation pay up to 50% less upkeep.

### TownyWaypoints

TownyWaypoints can be configured to allow players to travel, only if there is roads between the towns.

### RoadSpeedMounts

If RoadSpeedMounts is on the server, horse, donkey, mule, camel and player will be faster. Default config make them 2 time faster on roads than on wilderness for the same blocks they are moving on (from +30% to +60% on suspicious gravel for horses for example).

### Bonus chunks

Each connected town provide some bonus chunk to the road.
It provide 1 chunks per town level of each town connected to the road.
Same nation towns provide 2 times more bonus chunks.
Ally nation towns provide 1.5 times more bonus chunks.
Enemy nation towns provide 0.5 times more bonus chunks.

A town can't have more than 1000 bonus chunks from all connected towns.
A town can't have more bonus chunks than regular chunks.

## Extending a road

To extend a road, you need to create a new road between the road and the town you want to be included in the road.
They need to accept to join the road.
Then you can merge both roads with `/tr merge <road1> <road2>`. To merge 2 roads, your town need to be part of both roads.
By merging several new roads, you can add several towns to the road.

The final road need to be validated once again with `/tr validate <road>`

## Town fall into ruins

If one of the town of a road fall into ruins (or get removed), the town will be removed from the road & the part of the road that was going to that town will be removed.
When a town is removed from a valid road, it try to validate it again once the town have been removed, but it might not work if the road does not match the requirements.
Claim or unclaim (`/tr unclaim`) new area to validate it again.

If one of the 2 last town of the road fall into ruins, the road will be deleted. A road going from your town to your town isn't that usefull.

## Town leaving the road

When a town leave a road, it has the same effect on the road as if the town fall into ruins.

# Translations

Translations in English & in French are done by me (Hydrolien), feel free to open a pull request to add new languages. There is a single config file to translate.

# TODO

- Require a minimal amount of suspisous gravel or gravel or suspicious sand or water to be into the road chunks to validate it.
- Fix oppening chest with an item in hand being concidered as an interact and not a switch.
- Fix breaking block being count twice because of PlayerInteractEvent.

## Statistics
[![bStats Graph Data](https://bstats.org/signatures/bukkit/TownyRoads.svg)](https://bstats.org/plugin/bukkit/TownyRoads/31859)

# Build, Test & deploy

Feature requests or pull requests are welcome. Concider creating an issue first to talk about your new feature before sending a pull request.

## Build

Clone the [repo](https://github.com/Mvndi/TownyRoads) `git clone git@github.com:Mvndi/TownyRoads.git`

Build with `./gradlew assemble`. The plugin .jar file will be in `build/libs/`.

## Test

You can test the plugin directly in a Minecraft server with `./gradlew runServer`.
You can also try it on an other server by placing the .jar in `plugins/`.
You might want to update the plugin config to fit your needs.

## Deploy

### To Github releases, Hangar & Modrinth

Push tag to git to trigger a github action release that will create a new Github release and publish to Hangar & Modrinth.
```sh
git tag 1.2.3
git push --tags
```

### to Maven central

*Require ossrhUsername & ossrhPassword in ~/.gradle/gradle.properties*

```sh
./gradlew clean publish
./gradlew jreleaserDeploy
```
The published lib will be available [on maven central](https://central.sonatype.com/artifact/net.mvndicraft.townyroads/townyroads).