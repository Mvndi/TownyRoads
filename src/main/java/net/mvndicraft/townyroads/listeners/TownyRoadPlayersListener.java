package net.mvndicraft.townyroads.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsMessaging;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.permissions.TownyRoadsPermissionNodes;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TownyRoadPlayersListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getAsyncScheduler().runDelayed(TownyRoadsPlugin.getInstance(), t -> {
            Town playerTown = TownyAPI.getInstance().getTown(event.getPlayer());
            if (playerTown == null || playerTown.isRuined())
                return;
            if (!TownyUniverse.getInstance().getPermissionSource().testPermission(event.getPlayer(),
                    TownyRoadsPermissionNodes.TOWNYROADS_ACCEPT.getNode())) {
                return;
            }

            List<Road> roads = TownyRoadsPlugin.getInstance().getRoadManager().getAcceptableRoadByTown(playerTown);
            for (Road road : roads) {
                TownyRoadsMessaging.sendInviteToRoadMessage(event.getPlayer(), road);
            }
        }, 1L, TimeUnit.SECONDS);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTownDeleted(DeleteTownEvent event) {
        Town town = TownyAPI.getInstance().getTown(event.getTownUUID());
        for (Road road : TownyRoadsPlugin.getInstance().getRoadManager().getRoadsByTown(town)) {
            road.removeTown(town);
        }
    }
}
