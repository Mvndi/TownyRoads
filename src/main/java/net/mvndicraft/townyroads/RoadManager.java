package net.mvndicraft.townyroads;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.mvndicraft.townyroads.settings.TownyRoadsSettings;
import net.mvndicraft.townyroads.util.Messaging;
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
                .forEach(p -> Messaging.sendInviteToRoadMessage(p, road));
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
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(road);
    }

    public void addRoads(Set<Road> roads) {
        for (Road road : roads) {
            addRoad(road);
        }
    }

    public Component listRoad(int page) {
        Component builder = Messaging.translate("roads_list").append(Component.translatable((":"))).appendNewline();
        long itemsToSkip = Math.max(page - 1L, 0L) * 10L;
        roads.stream().sorted(Comparator.comparing(Road::getName, String.CASE_INSENSITIVE_ORDER)).skip(itemsToSkip)
                .limit(10).map(Road::getDescription).forEach(name -> builder.append(name).appendNewline());
        return builder;
    }

    public Collection<Road> getRoads() {
        return roads;
    }

    public void deleteRoad(Road road) {
        roads.remove(road);
        for (ChunkCoord chunkCoord : road.getChunksCoordsView()) {
            removeFromFastAccess(chunkCoord);
        }
        updateTownBonusBlock(road);
        TownyRoadsPlugin.getInstance().getRoadStorage().delete(road);
    }

    public void unclaimRoad(Road road, Player player) {
        road.unclaim(player);
        removeFromFastAccess(ChunkCoord.from(player.getLocation()));
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(road);
    }
    public void removeFromFastAccess(ChunkCoord chunkCoord) {
        fastAccessRoads.remove(chunkCoord);
    }
    public void removeFromFastAccess(Collection<ChunkCoord> chunkCoords) {
        for (ChunkCoord chunkCoord : chunkCoords) {
            removeFromFastAccess(chunkCoord);
        }
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

    public void updateTownBonusBlock(Road road) {
        if (!TownyRoadsSettings.getBonusBlockEnabled()) {
            return;
        }
        for (Town town : road.getTownsView()) {
            Nation nation = null;
            try {
                town.getNation();
            } catch (NotRegisteredException e) {
                // no nation bonus then.
            }
            double bonusBlock = 0;
            Map<Town, Integer> townConnectedByRoads = getTownConnectedByRoads(town, Integer.MAX_VALUE);
            for (Map.Entry<Town, Integer> entry : townConnectedByRoads.entrySet()) {
                // int bonus = (10 - entry.getValue()) * town.getLevelNumber();
                double bonus = 1.0;
                if (TownyRoadsSettings.getBonusBlockMultiplyByTownLevel()) {
                    bonus *= town.getLevelNumber();
                }
                Nation townConnectedByRoadNation = null;
                try {
                    townConnectedByRoadNation = entry.getKey().getNation();
                } catch (NotRegisteredException e) {
                    // no nation bonus then.
                }

                bonus *= nationStatusMultiplierForTownClaimBonus(nation, townConnectedByRoadNation);

                bonusBlock += bonus;
            }
            if (TownyRoadsSettings.getBonusBlockMaxValue() != -1) {
                bonusBlock = Math.min(bonusBlock, TownyRoadsSettings.getBonusBlockMaxValue());
            }
            if (TownyRoadsSettings.getBonusBlockMaxMultiplyValue() != -1) {
                bonusBlock = Math.min(bonusBlock,
                        TownyRoadsSettings.getBonusBlockMaxMultiplyValue() * (town.getMaxTownBlocks()));
            }
            town.setBonusBlocks((int) bonusBlock);
        }
    }

    private double nationStatusMultiplierForTownClaimBonus(Nation nation1, Nation nation2) {
        if (nation1 != null && nation2 != null) {
            if (nation1.equals(nation2)) {
                return TownyRoadsSettings.getBonusBlockSameNationMultiplier();
            } else if (nation1.isAlliedWith(nation2)) {
                return TownyRoadsSettings.getBonusBlockAllyMultiplier();
            } else if (nation1.getEnemies().contains(nation2)) {
                return TownyRoadsSettings.getBonusBlockEnemyMultiplier();
            }
        }
        return TownyRoadsSettings.getBonusBlockNeutralMultiplier();

    }

    public Map<Town, Integer> getTownConnectedByRoads(Town startTown, int deepness) {
        if (startTown == null || deepness < 1) {
            return Map.of();
        }

        Map<Town, Integer> distances = new LinkedHashMap<>();
        Queue<Town> queue = new ArrayDeque<>();

        distances.put(startTown, 0);
        queue.add(startTown);

        while (!queue.isEmpty()) {
            Town currentTown = queue.poll();
            int currentDistance = distances.get(currentTown);
            if (currentDistance >= deepness) {
                continue;
            }

            int nextDistance = currentDistance + 1;
            for (Road road : getRoadsByTown(currentTown)) {
                for (Town connectedTown : road.getTownsView()) {
                    if (connectedTown.equals(startTown) || distances.containsKey(connectedTown)) {
                        continue;
                    }

                    distances.put(connectedTown, nextDistance);
                    queue.add(connectedTown);
                }
            }
        }

        distances.remove(startTown);
        return distances;
    }

    public boolean areConnected(Town town1, Town town2) {
        return countConnected(town1, List.of(town2)) > 0;
    }

    public int countConnected(Town town, Collection<Town> towns) {
        if (towns.isEmpty()) {
            return 0;
        }
        Collection<Town> connectedTowns = getTownConnectedByRoads(town, Integer.MAX_VALUE).keySet();
        return (int) towns.stream().filter(connectedTowns::contains).count();
    }

    public void revalidateAllValidatedRoads() {
        roads.stream().filter(Road::isValid).forEach(Road::validate);
    }
}
