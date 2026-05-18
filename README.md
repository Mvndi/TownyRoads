# TownyRoads

Add roads to Towny.

A road connect 2 or more towns.

# TODO

- Limit the number of chunks of the road based on the distance between the 2 towns: minimal_distance * 1.5 (1.5 being 
configurable)
- Implement perms for from member of the 2 town.
- save roads to .yml files or .json files. 1 file per road so that it is fast to save each time a road is being edited.
- Require a minimal amount of suspisous gravel or gravel or suspicious sand to be into the road chunks to validate it.
- Add 1 to player cooldown value each time a player that does not have TOWNYROADS_CLAIMED_OWNROAD_BLOCK_BUILD permission break or build on a road chunk.
  - If the player reach max value y, then cancel build & break event & inform the player about the next time he will be able to break and how many block he already break.
  - Reduce the cooldown of x every t time.