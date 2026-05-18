package net.mvndicraft.townyroads;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.mvndicraft.townyroads.permissions.TownyRoadsPermissionNodes;
import net.mvndicraft.townyroads.util.ChunkCoordUtil;
import org.bukkit.entity.Player;

public class Road extends TownyObject {
    private final UUID id;
    private final List<Town> towns;
    private final List<Town> townsView;
    private final List<Town> toConfirmTowns;
    private final List<Town> toConfirmTownsView;
    private final Set<ChunkCoord> chunksCoords;
    private final Set<ChunkCoord> chunksCoordsView;
    private boolean valid;

    public Road(List<Town> towns, List<Town> toConfirmTowns) {
        super(towns.stream().map(Town::getName).collect(Collectors.joining(",")));
        id = UUID.randomUUID();
        this.towns = new ArrayList<>(towns);
        this.townsView = Collections.unmodifiableList(towns);
        this.toConfirmTowns = new ArrayList<>(toConfirmTowns);
        this.toConfirmTownsView = Collections.unmodifiableList(toConfirmTowns);
        this.chunksCoords = new HashSet<>();
        this.chunksCoordsView = Collections.unmodifiableSet(chunksCoords);
        valid = false;
    }

    // Used to load from file.
    private Road(UUID id, List<Town> towns, List<Town> toConfirmTowns) {
        super(towns.stream().map(Town::getName).collect(Collectors.joining(",")));
        this.id = id;
        this.towns = towns;
        this.townsView = Collections.unmodifiableList(towns);
        this.toConfirmTowns = new ArrayList<>(toConfirmTowns);
        this.toConfirmTownsView = Collections.unmodifiableList(toConfirmTowns);
        this.chunksCoords = new HashSet<>();
        this.chunksCoordsView = Collections.unmodifiableSet(chunksCoords);
        valid = false;
    }

    public UUID getId() {
        return id;
    }
    public List<Town> getTownsView() {
        return townsView;
    }
    public List<Town> getToConfirmTownsView() {
        return toConfirmTownsView;
    }
    public Set<ChunkCoord> getChunksCoordsView() {
        return chunksCoordsView;
    }

    @Override
    public void save() {
        // TODO
    }

    @Override
    public boolean exists() {
        return towns.size() > 1 && chunksCoords.isEmpty() && toConfirmTowns.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof Road road)
            return id.equals(road.id);
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


    public ChunkCoord claim(Player player) {
        ChunkCoord chunkCoord = ChunkCoord.from(player.getLocation());
        if (TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(chunkCoord) == null) {
            chunksCoords.add(chunkCoord);
            return chunkCoord;
        }
        return null;
    }
    public boolean unclaim(Player player) {
        return chunksCoords.remove(ChunkCoord.from(player.getLocation()));
    }

    public String getDescription() {
        String description = getTownsNames(towns) + " (" + chunksCoords.size() + " chunks)";
        if (!toConfirmTowns.isEmpty()) {
            description += " (" + getTownsNames(toConfirmTowns) + " to confirm)";
        }
        return description;
    }

    // Confirm that the town want to be part of the road
    public void confirm(Town town) {
        toConfirmTowns.remove(town);
    }
    public void confirmAll() {
        toConfirmTowns.clear();
    }

    public void deny(Town town) {
        removeTown(town);
    }

    private String getTownsNames(List<Town> townList) {
        return townList.stream().map(Town::getName).collect(Collectors.joining(","));
    }

    public void removeTown(Town town) {
        towns.remove(town);
        toConfirmTowns.remove(town); // it might be, or not be in toConfirmTowns
        if (towns.size() < 2) {
            TownyRoadsPlugin.getInstance().getRoadManager().deleteRoad(this);
        }

        if (valid) {
            validate();
        }
    }

    public Optional<Component> merge(Road road, boolean force) {
        if (force) {
            // Need this road to be valid.
            if (!isValid()) {
                return Optional.of(Component.text(getName() + " is not valid. Use `/tr validate` first."));
            }
            // At least 1 town need to be common
            if (towns.stream().noneMatch(road.towns::contains)) {
                return Optional.of(Component
                        .text(road.getName() + " need to have at least 1 town in common with " + road.getName()));
            }
            // All town of road need to have accepted to join the road.
            if (!road.toConfirmTowns.isEmpty()) {
                return Optional.of(Component.text(road.getName() + " have towns that haven't confirmed yet: "
                        + getTownsNames(road.toConfirmTowns)));
            }

            // The new road need to be valid, doing a temporary road to check.
            List<Town> towns = new ArrayList<>(this.towns);
            towns.addAll(road.towns);
            Road temp = new Road(new ArrayList<>(towns), List.of());
            temp.chunksCoords.addAll(this.chunksCoords);
            temp.chunksCoords.addAll(road.chunksCoords);
            Optional<Component> error = temp.validate();
            if (error.isPresent()) {
                return error;
            }
        }

        // Merge the road into this
        this.towns.addAll(road.towns);
        this.chunksCoords.addAll(road.chunksCoords);
        // Remove the old road
        TownyRoadsPlugin.getInstance().getRoadManager().deleteRoad(road);
        return Optional.empty();
    }
    public Optional<Component> merge(Road road) {
        return merge(road, false);
    }

    public boolean isValid() {
        return valid;
    }
    public Optional<Component> validate(boolean force) {
        if (!force) {
            if (towns.size() < 2) {
                valid = false;
                return Optional.of(Component.text(getName() + " must have at least 2 towns."));
            } else if (!ChunkCoordUtil.areAllConnected(chunksCoords)) {
                valid = false;
                return Optional.of(Component.text(getName() + " must be connected."));
            } else {
                Optional<Town> firstNotConnectedTown = getFirstNotConnectedTown();
                if (firstNotConnectedTown.isPresent()) {
                    valid = false;
                    return Optional.of(Component.text("All towns of " + getName() + " must be connected including "
                            + firstNotConnectedTown.get().getName()));
                }
            }
        }

        removeUnusedChunks();

        valid = true;
        return Optional.empty();
    }
    public Optional<Component> validate() {
        return validate(false);
    }

    public Optional<Town> getFirstNotConnectedTown() {
        // for each town that should be connected to the road
        for (Town town : towns) {
            boolean isConnected = town.getTownBlocks().stream()
                    .map(tb -> new ChunkCoord(tb.getWorld().getBukkitWorld().getUID(), tb.getX(), tb.getZ()))
                    .flatMap(coord -> coord.getNearby(1).stream()).anyMatch(chunksCoords::contains);

            if (!isConnected) {
                return Optional.of(town);
            }
        }
        return Optional.empty();
    }

    // We allow up to 2 times more chunks that the shortest path between the 2 farthest towns.
    public int maxChunks() {
        return distanceBetweenTheTwoFarthestTowns() * 2;
    }
    /**
     * @return true if the road hasn't reached the maximum number of chunks
     */
    public boolean canClaimMore() {
        return chunksCoords.size() < maxChunks();
    }
    /**
     * @return true if the player town is part of the road
     */
    public boolean isAPlayerOfTheRoad(Player player) {
        Town town = TownyAPI.getInstance().getTown(player);
        return towns.contains(town);
    }
    /**
     * @return true if the player town is a town that haven't accepted the road yet and the player has the permission to
     *         confirm.
     */
    public boolean canAcceptTheRoad(Player player) {
        Town town = TownyAPI.getInstance().getTown(player);
        return toConfirmTowns.contains(town) && TownyUniverse.getInstance().getPermissionSource().testPermission(player,
                TownyRoadsPermissionNodes.TOWNYROADS_ACCEPT.getNode());
    }
    /**
     * @return true if not already claimed and at least one chunk is nearby or it's the first chunk of the road
     */
    public boolean canClaimHere(ChunkCoord chunkCoord) {
        return chunksCoords.isEmpty() || (!chunksCoords.contains(chunkCoord)
                && chunkCoord.getNearby(1).stream().anyMatch(chunksCoords::contains));
    }
    /**
     * @return true if the chunks are still connected each other without this chunk.
     */
    public boolean canUnclaimHere(ChunkCoord chunkCoord) {
        List<ChunkCoord> nearBy = chunkCoord.getNearby(1);
        if (nearBy.size() <= 1) {
            return true;
        } else {
            // All connected without this one.
            return ChunkCoordUtil.areAllConnected(
                    chunksCoords.stream().filter(c -> !c.equals(chunkCoord)).collect(Collectors.toSet()));
        }
    }

    /**
     * Calculates the distance between the 2 farthest towns
     * 
     * @return distance
     */
    public int distanceBetweenTheTwoFarthestTowns() {
        if (towns.size() < 2) {
            return 0;
        }

        int maxDistance = 0;
        int x1, z1, x2, z2;
        for (Town town1 : towns) {
            try {
                x1 = town1.getHomeBlock().getX();
                z1 = town1.getHomeBlock().getZ();
            } catch (TownyException e) {
                continue;
            }
            for (Town town2 : towns) {
                if (town1.equals(town2)) {
                    continue;
                }
                try {
                    x2 = town2.getHomeBlock().getX();
                    z2 = town2.getHomeBlock().getZ();
                } catch (TownyException e) {
                    continue;
                }
                int distance = Math.abs(x1 - x2) + Math.abs(z1 - z2);
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }

        return maxDistance;
    }


    // List all ChunkCoord are connecting 2 towns + 1 extra chunk that is allowed
    public Set<ChunkCoord> listUsefulChunks() {
        Set<ChunkCoord> usefulChunks = new HashSet<>();
        for (Town town1 : towns) {
            for (Town town2 : towns) {
                if (!town1.equals(town2)) {
                    usefulChunks.addAll(getPath(town1, town2));
                }
            }
        }

        Set<ChunkCoord> usefulWithMargin = new HashSet<>(usefulChunks);
        for (ChunkCoord chunkCoord : chunksCoords) {
            if (chunkCoord.getNearby(1).stream().anyMatch(usefulChunks::contains)) {
                usefulWithMargin.add(chunkCoord);
            }
        }

        return usefulWithMargin;
    }

    public void removeUnusedChunks() {
        Set<ChunkCoord> usefullChunkCoords = listUsefulChunks();
        Set<ChunkCoord> toRemove = chunksCoords.stream().filter(c -> !usefullChunkCoords.contains(c))
                .collect(Collectors.toSet());
        chunksCoords.removeAll(toRemove);
        // chunksCoords.retainAll(listUsefulChunks());
        TownyRoadsPlugin.getInstance().getRoadManager().removeFromFastAccess(toRemove);
    }


    // Returns the shortest path between 2 towns
    public List<ChunkCoord> getPath(Town from, Town to) {
        Set<ChunkCoord> startChunks = getChunksTouchingTown(from);
        Set<ChunkCoord> endChunks = getChunksTouchingTown(to);

        if (startChunks.isEmpty() || endChunks.isEmpty()) {
            return List.of();
        }

        for (ChunkCoord startChunk : startChunks) {
            if (endChunks.contains(startChunk)) {
                return List.of(startChunk);
            }
        }

        Queue<ChunkCoord> queue = new ArrayDeque<>(startChunks);
        Set<ChunkCoord> visited = new HashSet<>(startChunks);
        Map<ChunkCoord, ChunkCoord> previous = new HashMap<>();

        while (!queue.isEmpty()) {
            ChunkCoord current = queue.poll();

            for (ChunkCoord neighbor : current.getNearby(1)) {
                if (!chunksCoords.contains(neighbor) || !visited.add(neighbor)) {
                    continue;
                }

                previous.put(neighbor, current);
                if (endChunks.contains(neighbor)) {
                    return buildPath(previous, neighbor);
                }

                queue.add(neighbor);
            }
        }

        return List.of();
    }

    private Set<ChunkCoord> getChunksTouchingTown(Town town) {
        return town.getTownBlocks().stream()
                .map(tb -> new ChunkCoord(tb.getWorld().getBukkitWorld().getUID(), tb.getX(), tb.getZ()))
                .flatMap(coord -> coord.getNearby(1).stream()).filter(chunksCoords::contains)
                .collect(Collectors.toSet());
    }

    private List<ChunkCoord> buildPath(Map<ChunkCoord, ChunkCoord> previous, ChunkCoord end) {
        LinkedList<ChunkCoord> path = new LinkedList<>();
        ChunkCoord current = end;

        while (current != null) {
            path.addFirst(current);
            current = previous.get(current);
        }

        return path;
    }

}
