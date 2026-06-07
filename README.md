# TownyRoads

Add roads to Towny.

A road connect 2 or more towns.

Towny is required.
MapTowny, squaremap & RoadSpeedMounts are supported as optional dependencies.

## MapTowny

MapTowny allow the road to be displayed into the dynmap.

## RoadSpeedMounts

RoadSpeedMounts allow for different speed in roads and outside roads.

# TODO

<!-- - Limit the number of chunks of the road based on the distance between the 2 towns: minimal_distance * 1.5 (1.5 being configurable) -->
<!-- - Implement perms for members of the 2 town. -->
<!-- - save roads to .yml files or .json files. 1 file per road so that it is fast to save each time a road is being edited. -->
<!-- - Add 1 to player cooldown value each time a player that does not have TOWNYROADS_CLAIMED_OWNROAD_BLOCK_BUILD permission break or build on a road chunk.
  - If the player reach max value y, then cancel build & break event & inform the player about the next time he will be able to break and how many block he already break.
  - Reduce the cooldown of x every t time. -->
<!-- - give bonus chunks for connected road to a town. -->
<!-- - translation -->
<!-- - max the number of bonus block to 1.0 * the default max plot & to brut value. -->
<!-- - make sure players know when they enter a road claim as when they enter a town claim. -->
<!-- - display roads into the dynmap in light grey -->


- Require a minimal amount of suspisous gravel or gravel or suspicious sand or water to be into the road chunks to validate it.
- Fix oppening chest with an item in hand being concidered as an interact and not a switch.
- Fix breaking block being count twice because of PlayerInteractEvent.
<!-- - reduce town taxes if connected to the capital. -->
- make speed higher on road in RoadSpeedMounts.

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