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
import net.mvndicraft.townyroads.permissions.RoadPermissionHandler;
import net.mvndicraft.townyroads.permissions.TownyRoadsPermissionNodes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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

    @EventHandler(ignoreCancelled = true)
    public void onBuild(BlockPlaceEvent event) {
        if (!TownyAPI.getInstance().isTownyWorld(event.getPlayer().getWorld()))
            return;
        Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(event.getBlock().getChunk());
        // Player is not part of the road or does not have perms
        if (road != null && !RoadPermissionHandler.canBuild(event.getPlayer(), road)) {
            playNoSound(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDestroy(BlockBreakEvent event) {
        if (!TownyAPI.getInstance().isTownyWorld(event.getPlayer().getWorld()))
            return;
        Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(event.getBlock().getChunk());
        // Player is not part of the road or does not have perms
        if (road != null && !RoadPermissionHandler.canDestroy(event.getPlayer(), road)) {
            playNoSound(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemUse(PlayerInteractEvent event) {
        if (!TownyAPI.getInstance().isTownyWorld(event.getPlayer().getWorld()))
            return;
        if (event.hasItem()) {
            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();
            Location loc = null;
            if (clickedBlock != null)
                loc = clickedBlock.getLocation();
            else
                loc = player.getLocation();

            Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(loc.getChunk());
            // Player is not part of the road or does not have perms
            if (road != null && !RoadPermissionHandler.canItemUse(event.getPlayer(), road)) {
                playNoSound(player);
                event.setCancelled(true);
            }
        }
    }

    private void playNoSound(Player player) {
        player.getLocation().getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1, 1);
        player.sendMessage("NO");
    }
}
