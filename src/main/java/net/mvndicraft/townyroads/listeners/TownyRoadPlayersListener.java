package net.mvndicraft.townyroads.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TownyRoadPlayersListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Town playerTown = TownyAPI.getInstance().getTown(event.getPlayer());
        if (playerTown == null || playerTown.isRuined())
            return;
        // TODO info message to the player if he can accept a road creation or merge
    }
}
