package net.mvndicraft.townyroads.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import com.palmergames.bukkit.towny.event.TitleNotificationEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.notification.TitleNotification;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.mvndicraft.townyroads.ChunkCoord;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.events.PlayerEntersIntoRoadBorderEvent;
import net.mvndicraft.townyroads.events.PlayerExitsFromRoadBorderEvent;
import net.mvndicraft.townyroads.events.TitleNotificationRoad;
import net.mvndicraft.townyroads.permissions.RoadPermissionHandler;
import net.mvndicraft.townyroads.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

            List<Road> roads = TownyRoadsPlugin.getInstance().getRoadManager().getAcceptableRoad().stream()
                    .filter(road -> RoadPermissionHandler.canAcceptTheRoad(event.getPlayer(), road)).toList();
            for (Road road : roads) {
                Messaging.sendInviteToRoadMessage(event.getPlayer(), road);
            }
        }, 1L, TimeUnit.SECONDS);
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

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMoveChunkBeforeDefault(PlayerChangePlotEvent event) {
        if (!TownyUniverse.getInstance().hasResident(event.getPlayer().getUniqueId())) {
            return;
        }
        ChunkCoord from = ChunkCoord.from(event.getFrom());
        ChunkCoord to = ChunkCoord.from(event.getTo());
        Road fromRoad = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(from);
        Road toRoad = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(to);
        if (fromRoad != null && !fromRoad.equals(toRoad)) {
            BukkitTools.fireEvent(new PlayerExitsFromRoadBorderEvent(event.getPlayer(), to, from, fromRoad));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMoveChunkAfterDefault(PlayerChangePlotEvent event) {
        if (!TownyUniverse.getInstance().hasResident(event.getPlayer().getUniqueId())) {
            return;
        }
        ChunkCoord from = ChunkCoord.from(event.getFrom());
        ChunkCoord to = ChunkCoord.from(event.getTo());
        Road fromRoad = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(from);
        Road toRoad = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(to);
        if (toRoad != null && !toRoad.equals(fromRoad)) {
            BukkitTools.fireEvent(new PlayerEntersIntoRoadBorderEvent(event.getPlayer(), to, from, toRoad));
        }
    }

    // Allow wilderness to be display as the road name, but it won't switch back to wilderness when you leave the road.
    // So we are using title notifications only for now.
    // @EventHandler(priority = EventPriority.HIGH)
    // public void onChunkNotification(ChunkNotificationEvent event) {
    // ChunkCoord to = ChunkCoord.from(event.getToCoord());
    // Road toRoad = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(to);

    // if (toRoad != null) {
    // event.setMessage(event.getMessage().replace("Wilderness", toRoad.getShortName()));
    // }
    // }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerEnterRoad(PlayerEntersIntoRoadBorderEvent event) {
        Resident resident = event.getResident();
        Road road = event.getEnteredRoad();
        if (resident == null || road == null)
            return;

        if (TownySettings.isNotificationUsingTitles() && resident.isSeeingBorderTitles()) {
            TitleNotificationEvent tne = new TitleNotificationEvent(new TitleNotificationRoad(road, event.getTo()),
                    event.getPlayer());
            BukkitTools.fireEvent(tne);
            String title = tne.getTitleNotification().getTitleNotification();
            String subtitle = tne.getTitleNotification().getSubtitleNotification();
            TownyMessaging.sendTitleMessageToResident(resident, title, subtitle,
                    TownySettings.getNotificationTitlesDurationTicks());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerExitsFromRoadBorderEvent(PlayerExitsFromRoadBorderEvent event) {
        Resident resident = event.getResident();
        WorldCoord to = event.getTo().toWorldCoord();
        if (resident == null || !TownyAPI.getInstance().isWilderness(to))
            return;

        if (TownySettings.isNotificationUsingTitles() && resident.isSeeingBorderTitles()) {
            TitleNotificationEvent tne = new TitleNotificationEvent(new TitleNotification(null, to), event.getPlayer());
            BukkitTools.fireEvent(tne);
            String title = tne.getTitleNotification().getTitleNotification();
            String subtitle = tne.getTitleNotification().getSubtitleNotification();
            TownyMessaging.sendTitleMessageToResident(resident, title, subtitle,
                    TownySettings.getNotificationTitlesDurationTicks());
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
