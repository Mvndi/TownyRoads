package net.mvndicraft.townyroads;

import com.palmergames.bukkit.towny.object.Town;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RoadManager {
    private Set<Road> roads;
    // save a map of all ChunkCoord -> Road for faster access to the Road from a Location
    private Map<ChunkCoord, Road> fastAccessRoads;

    public RoadManager() {
        roads = ConcurrentHashMap.newKeySet();
        fastAccessRoads = new ConcurrentHashMap<>();
    }

    public @Nullable Road getRoadAt(ChunkCoord chunkCoord) {
        return fastAccessRoads.get(chunkCoord);
    }
    public @Nullable Road getRoadAt(Chunk chunk) {
        return getRoadAt(ChunkCoord.from(chunk));
    }
    public @Nullable Road getRoadAt(Location location) {
        return getRoadAt(ChunkCoord.from(location));
    }

    public @Nullable Road getRoadByName(String roadName) {
        for (Road road : roads) {
            if (road.getName().equals(roadName)) {
                return road;
            }
        }
        return null;
    }

    public Road createRoad(List<Town> towns, List<Town> toConfirmTowns) {
        Road road = new Road(towns, toConfirmTowns);
        addRoad(road);
        Bukkit.getOnlinePlayers().stream().filter(p -> road.canAcceptTheRoad(p))
                .forEach(p -> TownyRoadsMessaging.sendInviteToRoadMessage(p, road));
        return road;
    }

    public boolean claimRoad(Road road, Player player) {
        ChunkCoord chunkCoord = road.claim(player);

        if (chunkCoord != null) {
            fastAccessRoads.put(chunkCoord, road);
            return true;
        }
        return false;
    }

    public void addRoad(Road road) {
        roads.add(road);
        for (ChunkCoord chunkCoord : road.getChunksCoordsView()) {
            fastAccessRoads.put(chunkCoord, road);
        }
    }

    public void addRoads(Set<Road> roads) {
        for (Road road : roads) {
            addRoad(road);
        }
    }

    public String listRoad(int page) {
        StringBuilder builder = new StringBuilder("List of roads:\n");
        long itemsToSkip = Math.max(page - 1L, 0L) * 10L;
        roads.stream().sorted(Comparator.comparing(Road::getName, String.CASE_INSENSITIVE_ORDER)).skip(itemsToSkip)
                .limit(10).map(Road::getDescription).forEach(name -> builder.append(name).append("\n"));
        return builder.toString();
    }

    public Collection<Road> getRoads() {
        return roads;
    }

    public void deleteRoad(Road road) {
        roads.remove(road);
        for (ChunkCoord chunkCoord : road.getChunksCoordsView()) {
            fastAccessRoads.remove(chunkCoord);
        }
    }

    public void unclaimRoad(Road road, Player player) {
        road.unclaim(player);
        fastAccessRoads.remove(ChunkCoord.from(player.getLocation()));
    }

    public List<Road> getRoadsByTown(Town town) {
        return roads.stream().filter(r -> r.getTownsView().contains(town)).toList();
    }
    public List<Road> getAcceptableRoadByTown(Town town) {
        return roads.stream().filter(r -> r.getToConfirmTownsView().contains(town)).toList();
    }
    public List<Road> getAcceptableRoad() {
        return roads.stream().filter(r -> r.getToConfirmTownsView().size() > 0).toList();
    }

    public Road getRoadWithEveryTown(List<Town> towns) {
        return roads.stream().filter(r -> r.getTownsView().containsAll(towns)).findFirst().orElse(null);
    }
}
