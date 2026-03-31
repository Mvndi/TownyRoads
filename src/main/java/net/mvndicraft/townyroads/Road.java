package net.mvndicraft.townyroads;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class Road extends TownyObject {
    private final UUID id;
    private final List<Town> towns;
    private final List<Town> townsView;
    private final List<Town> toConfirmTowns;
    private final Set<ChunkCoord> chunksCoords;
    private final Set<ChunkCoord> chunksCoordsView;

    public Road(List<Town> towns, List<Town> toConfirmTowns) {
        super(towns.stream().map(Town::getName).collect(Collectors.joining(",")));
        id = UUID.randomUUID();
        this.towns = new ArrayList<>(towns);
        this.townsView = Collections.unmodifiableList(towns);
        this.toConfirmTowns = new ArrayList<>(toConfirmTowns);
        this.chunksCoords = new HashSet<>();
        this.chunksCoordsView = Collections.unmodifiableSet(chunksCoords);
    }

    // Used to load from file.
    private Road(UUID id, List<Town> towns, List<Town> toConfirmTowns) {
        super(towns.stream().map(Town::getName).collect(Collectors.joining(",")));
        this.id = id;
        this.towns = towns;
        this.townsView = Collections.unmodifiableList(towns);
        this.toConfirmTowns = new ArrayList<>(toConfirmTowns);
        this.chunksCoords = new HashSet<>();
        this.chunksCoordsView = Collections.unmodifiableSet(chunksCoords);
    }

    public UUID getId() { return id; }
    public List<Town> getTownsView() { return townsView; }
    public Set<ChunkCoord> getChunksCoordsView() { return chunksCoordsView; }

    @Override
    public void save() {
        // TODO
    }

    @Override
    public boolean exists() { return towns.size() > 1 && chunksCoords.isEmpty() && toConfirmTowns.isEmpty(); }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof Road road)
            return id.equals(road.id);
        return false;
    }

    @Override
    public int hashCode() { return id.hashCode(); }


    public ChunkCoord claim(Player player) {
        if (TownyAPI.getInstance().getTownBlock(player) == null) { // not in a town.
            ChunkCoord chunkCoord = ChunkCoord.from(player.getLocation());
            if (TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(chunkCoord) == null) {
                chunksCoords.add(chunkCoord);
                return chunkCoord;
            }
        }
        return null;
    }
    public boolean unclaim(Player player) { return chunksCoords.remove(ChunkCoord.from(player.getLocation())); }

    public String getDescription() {
        String description = getTownsNames(towns) + " (" + chunksCoords.size() + " chunks)";
        if (!toConfirmTowns.isEmpty()) {
            description += " (" + getTownsNames(toConfirmTowns) + " to confirm)";
        }
        return description;
    }

    // Confirm that the town want to be part of the road
    public void confirm(Town town) { toConfirmTowns.remove(town); }

    private String getTownsNames(List<Town> townList) { return townList.stream().map(Town::getName).collect(Collectors.joining(",")); }

    public void removeTown(Town town) {
        towns.remove(town);
        if (towns.size() < 2) {
            TownyRoadsPlugin.getInstance().getRoadManager().deleteRoad(this);
        }
    }
}
