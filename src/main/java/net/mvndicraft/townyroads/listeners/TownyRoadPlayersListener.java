package net.mvndicraft.townyroads.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
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
import org.bukkit.event.block.Action;
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

        Action action = event.getAction();
        if (actionIsNotRightClickOrPhysical(action) && actionIsNotLeftClickThatCountsAsSwitch(event, action)) {
            return;
        }

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


    // Private methods copied from TownyPlayerListener.java
    /**
     * Is the action one that involves left-clicking on a Switch block? This is
     * useful for protecting (usually) modded blocks that can be used via left
     * clicks.
     * 
     * @param event  PlayerInteractEvent causing a switch test.
     * @param action Action that has to be LEFT_CLICK_BLOCK for this to count.
     * @return true if the player is left clicking a block that is technically a
     *         switch_id in Towny.
     */
    private boolean actionIsNotLeftClickThatCountsAsSwitch(PlayerInteractEvent event, Action action) {
        return action != Action.LEFT_CLICK_BLOCK || !event.hasBlock() || !TownySettings
                .isSwitchMaterial(event.getClickedBlock().getType(), event.getClickedBlock().getLocation());
    }

    /**
     * Is the action something we don't want to worry about when we're dealing with something like honey comb and a
     * sign, or candles and cake when testing PlayerInteractEvents.
     * 
     * @param action Action that player is making for this to matter.
     * @return true if the action is a right click or physical Action.
     */
    private boolean actionIsNotRightClickOrPhysical(Action action) {
        return action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR && action != Action.PHYSICAL;
    }
}
