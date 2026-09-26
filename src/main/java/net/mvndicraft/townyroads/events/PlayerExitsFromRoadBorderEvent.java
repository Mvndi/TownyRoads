package net.mvndicraft.townyroads.events;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import net.mvndicraft.townyroads.ChunkCoord;
import net.mvndicraft.townyroads.Road;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerExitsFromRoadBorderEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
	private final Road enteredRoad;
	private final ChunkCoord from;
	private final ChunkCoord to;
	private final Player player;

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerExitsFromRoadBorderEvent(Player player, ChunkCoord to, ChunkCoord from, Road enteredRoad) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.enteredRoad = enteredRoad;
		this.player = player;
		this.from = from;
		this.to = to;
	}

    public Player getPlayer() {
        return player;
    }
    
    public Road getEnteredRoad() {
        return enteredRoad;
    }
    
    public ChunkCoord getFrom() {
        return from;
    }
    
    public ChunkCoord getTo() {
        return to;
    }

    @Nullable
	public Resident getResident() {
		return TownyAPI.getInstance().getResident(player);
	}
    
}
