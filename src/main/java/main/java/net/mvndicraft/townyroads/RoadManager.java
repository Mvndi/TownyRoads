package main.java.net.mvndicraft.townyroads;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoadManager {
    private Set<Road> roads;
    // save a map of all ChunkCoord -> Road for faster access to the Road from a Location
    private Map<ChunkCoord, Road> fastAccessRoads;

    public RoadManager() {
        roads = new HashSet<>();
        fastAccessRoads = new HashMap<>();
    }

    public Road getRoadAt(ChunkCoord chunkCoord) {
        return fastAccessRoads.get(chunkCoord);
    }
}
