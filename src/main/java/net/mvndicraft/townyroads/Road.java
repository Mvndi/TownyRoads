package net.mvndicraft.townyroads;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
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
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.mvndicraft.townyroads.permissions.TownyRoadsPermissionNodes;
import net.mvndicraft.townyroads.settings.TownyRoadsSettings;
import net.mvndicraft.townyroads.util.ChunkCoordUtil;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
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
        this.townsView = Collections.unmodifiableList(this.towns);
        this.toConfirmTowns = new ArrayList<>(toConfirmTowns);
        this.toConfirmTownsView = Collections.unmodifiableList(this.toConfirmTowns);
        this.chunksCoords = new HashSet<>();
        this.chunksCoordsView = Collections.unmodifiableSet(this.chunksCoords);
        valid = false;
    }

    // Used to load from file.
    private Road(UUID id, List<Town> towns, List<Town> toConfirmTowns, Set<ChunkCoord> chunksCoords, boolean valid) {
        super(towns.stream().map(Town::getName).collect(Collectors.joining(",")));
        this.id = id;
        this.towns = new ArrayList<>(towns);
        this.townsView = Collections.unmodifiableList(this.towns);
        this.toConfirmTowns = new ArrayList<>(toConfirmTowns);
        this.toConfirmTownsView = Collections.unmodifiableList(this.toConfirmTowns);
        this.chunksCoords = new HashSet<>(chunksCoords);
        this.chunksCoordsView = Collections.unmodifiableSet(this.chunksCoords);
        this.valid = valid;
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
        TownyRoadsPlugin.getInstance().getRoadStorage().save(this);
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


    public ChunkCoord claim(ChunkCoord chunkCoord) {
        if (TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(chunkCoord) == null) {
            chunksCoords.add(chunkCoord);
            TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
            return chunkCoord;
        }
        return null;
    }
    public boolean unclaim(ChunkCoord chunkCoord) {
        boolean b = chunksCoords.remove(chunkCoord);
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
        return b;
    }

    public Component getDescription(boolean isAdmin) {
        Component description = Component.text(getTownsNames(towns)).appendSpace();
        if (isValid()) {
            description = description.append(Component.text("\u2714"));
        } else {
            description = description.append(Component.text("\u2718"));
        }
        if (isAdmin) {
            description = description.appendSpace().append(Component.text(id.toString())).appendSpace();
        }
        description = description.appendSpace().append(Component.text("(")).append(Component.text(chunksCoordsSize()))
                .append(Component.text("/")).append(Component.text(maxChunksCoordsSize()))
                .append(Component.translatable("chunks")).append(Component.text(")"));
        if (!toConfirmTowns.isEmpty()) {
            description = description.appendSpace().append(Component.text("("))
                    .append(Component.text(getTownsNames(toConfirmTowns))).appendSpace()
                    .append(Component.translatable("to_confirm")).append(Component.text(")"));
        }
        return description;
    }
    public Component getDescription() {
        return getDescription(false);
    }

    // Confirm that the town want to be part of the road
    public void confirm(Town town) {
        toConfirmTowns.remove(town);
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
    }
    public void confirmAll() {
        toConfirmTowns.clear();
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
    }

    public void deny(Town town) {
        removeTown(town);
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
    }

    public String getShortName() {
        return getName();
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
        } else {
            TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
        }
    }

    public Optional<Component> merge(Road road, boolean force) {
        if (!force) {
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

            this.removeChunksInTowns();
            road.removeChunksInTowns();

            // The new road need to be valid, doing a temporary road to check.
            Road temp = new Road(this.towns, List.of());
            temp.addTowns(road.towns);
            temp.chunksCoords.addAll(this.chunksCoords);
            temp.chunksCoords.addAll(road.chunksCoords);
            Optional<Component> error = temp.wouldBeValid();
            if (error.isPresent()) {
                return error;
            }
        }

        // Remove the old road
        TownyRoadsPlugin.getInstance().getRoadManager().deleteRoad(road);

        // Merge the road into this
        this.addTowns(road.towns);
        // Use RoadManager to update fast access to.
        for (ChunkCoord chunkCoord : road.chunksCoords) {
            TownyRoadsPlugin.getInstance().getRoadManager().claimRoad(this, chunkCoord);
        }
        this.updateRoadName();
        this.validate();
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
        return Optional.empty();
    }
    public Optional<Component> merge(Road road) {
        return merge(road, false);
    }

    /**
     * Add towns without duplication
     */
    public void addTowns(Collection<Town> townsToAdd) {
        for (Town town : townsToAdd) {
            if (!this.towns.contains(town)) {
                this.towns.add(town);
            }
        }
    }

    public Optional<Component> wouldBeValid() {
        if (!toConfirmTowns.isEmpty()) {
            return Optional.of(Component.translatable("validate_not_confirmed",
                    Argument.component("road", Component.text(getName())),
                    Argument.component("to_confirm_towns", Component.text(getTownsNames(toConfirmTowns)))));
        }

        if (towns.size() < 2) {
            return Optional.of(Component.translatable("validate_at_least_2_towns",
                    Argument.component("road", Component.text(getName()))));
        }

        if (!ChunkCoordUtil.areAllConnected(chunksCoords)) {
            return Optional.of(Component.translatable("validate_connected_chunks",
                    Argument.component("road", Component.text(getName()))));
        }

        int maxChunks = maxChunksCoordsSize();
        int currentChunks = chunksCoordsSize();
        if (currentChunks > maxChunks) {
            return Optional.of(Component.translatable("validate_to_many_chunks",
                    Argument.component("road", Component.text(getName())),
                    Argument.component("current_chunks", Component.text(currentChunks)),
                    Argument.component("max_chunks", Component.text(maxChunks))));
        }


        Optional<Town> firstNotConnectedTown = getFirstNotConnectedTown();
        if (firstNotConnectedTown.isPresent()) {
            return Optional.of(Component.translatable("validate_connected_towns",
                    Argument.component("road", Component.text(getName())),
                    Argument.component("town", Component.text(firstNotConnectedTown.get().getName()))));
        }
        return Optional.empty();
    }

    public boolean isValid() {
        return valid;
    }
    public Optional<Component> validate(boolean force) {
        removeChunksInTowns();

        if (!force) {
            Optional<Component> error = wouldBeValid();
            if (error.isPresent()) {
                valid = false;
                TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
                return error;
            }
        }

        removeUnusedChunks();

        valid = true;
        TownyRoadsPlugin.getInstance().getRoadManager().updateTownBonusBlock(this);
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
        return Optional.empty();
    }
    public Optional<Component> validate() {
        return validate(false);
    }

    public void unvalidate() {
        valid = false;
        TownyRoadsPlugin.getInstance().getRoadManager().updateTownBonusBlock(this);
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
    }

    public int maxChunksCoordsSize() {
        int max = Integer.MAX_VALUE;
        if (TownyRoadsSettings.getMaxClaimsMultiplier() != -1) {
            max = (int) (TownyRoadsSettings.getMaxClaimsMultiplier() * distanceBetweenTheTwoFarthestTowns());
        }
        if (TownyRoadsSettings.getMaxclaims() != -1) {
            max = Math.min(max, TownyRoadsSettings.getMaxclaims());
        }
        return max;
    }
    public int chunksCoordsSize() {
        return chunksCoords.size();
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

    /**
     * @return true if the road hasn't reached the maximum number of chunks
     */
    public boolean canClaimMore() {
        return chunksCoordsSize() < maxChunksCoordsSize();
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
     * @return distance in chunks
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
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
    }

    public void removeChunksInTowns() {
        Set<ChunkCoord> toRemove = chunksCoords.stream()
                .filter(chunkCoord -> TownyAPI.getInstance().getTown(chunkCoord.toLocation()) != null)
                .collect(Collectors.toSet());
        chunksCoords.removeAll(toRemove);
        TownyRoadsPlugin.getInstance().getRoadManager().removeFromFastAccess(toRemove);
        TownyRoadsPlugin.getInstance().getRoadStorage().saveSoon(this);
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

    public void updateRoadName() {
        setName(getTownsNames(towns));
    }


    public static Road fromYml(File file) {
        Configuration config = YamlConfiguration.loadConfiguration(file);
        try {
            return new Road(UUID.fromString(config.getString("id")),
                    config.getStringList("towns").stream().map(UUID::fromString)
                            .map(uuid -> TownyAPI.getInstance().getTown(uuid)).filter(t -> t != null).toList(),
                    config.getStringList("toConfirmTowns").stream().map(UUID::fromString)
                            .map(uuid -> TownyAPI.getInstance().getTown(uuid)).filter(t -> t != null).toList(),
                    config.getStringList("chunksCoords").stream().map(ChunkCoord::fromString)
                            .collect(Collectors.toSet()),
                    config.getBoolean("valid"));
        } catch (Exception e) {
            TownyRoadsPlugin.error("Error while loading road " + file.getName(), e);
            return null;
        }
    }

    public void toYml(File file) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("id", this.getId().toString());
        config.set("towns", this.towns.stream().map(Town::getUUID).map(UUID::toString).toList());
        config.set("toConfirmTowns", this.toConfirmTowns.stream().map(Town::getUUID).map(UUID::toString).toList());
        config.set("chunksCoords", this.chunksCoords.stream().sorted().map(ChunkCoord::toString).toList());
        config.set("valid", this.valid);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
