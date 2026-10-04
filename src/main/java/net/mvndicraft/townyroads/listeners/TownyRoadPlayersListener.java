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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.mvndicraft.townyroads.ChunkCoord;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.events.PlayerEntersIntoRoadBorderEvent;
import net.mvndicraft.townyroads.events.PlayerExitsFromRoadBorderEvent;
import net.mvndicraft.townyroads.events.TitleNotificationRoad;
import net.mvndicraft.townyroads.permissions.RoadPermissionHandler;
import net.mvndicraft.townyroads.settings.TownyRoadsSettings;
import net.mvndicraft.townyroads.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBuild(BlockPlaceEvent event) {
        if (!TownyAPI.getInstance().isTownyWorld(event.getPlayer().getWorld()))
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(event.getBlock().getChunk());
        // Player is not part of the road or does not have perms
        if (road != null && !RoadPermissionHandler.canBuild(event.getPlayer(), road)) {
            playNoSoundCooldown(event.getPlayer(), road);
            event.setCancelled(true);
        }
        Biome biome = event.getBlock().getBiome();
        // No road, no town and in a wilderness forbidden biome => cancel
        if (road == null && TownyAPI.getInstance().getTown(event.getBlock().getLocation()) == null
                && isInWildernessForbidenBuildBiome(event.getBlock())) {
            playNoSoundBiome(event.getPlayer(), biome);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDestroy(BlockBreakEvent event) {
        if (!TownyAPI.getInstance().isTownyWorld(event.getPlayer().getWorld()))
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(event.getBlock().getChunk());
        // Player is not part of the road or does not have perms
        if (road != null && !RoadPermissionHandler.canDestroy(event.getPlayer(), road)) {
            playNoSoundCooldown(event.getPlayer(), road);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onItemUse(PlayerInteractEvent event) {
        if (!TownyAPI.getInstance().isTownyWorld(event.getPlayer().getWorld()))
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
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
                playNoSoundCooldown(player, road);
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


    private void playNoSound(Player player, TranslatableComponent message) {
        Location location = player.getLocation();
        if (location != null) {
            location.getWorld().playSound(location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1, 1);
        }
        Messaging.sendError(player, message);
    }
    private void playNoSoundCooldown(Player player, Road road) {
        playNoSound(player, Component.translatable("err_not_part_of_road_cooldown",
                Argument.component("road", Component.text(road.getName()))));
    }
    private void playNoSoundBiome(Player player, Biome biome) {
        playNoSound(player, Component.translatable("err_biome_require_town_or_road",
                Argument.component("biome", Component.text(biome.key().asString()))));
    }

    /**
     * Do a check of the block biome and the close by biomes in a cross shape for
     * getBiomesNoBuildInWildernessPermissiveness near by biomes.
     */
    private boolean isInWildernessForbidenBuildBiome(Block block) {
        Location location = block.getLocation(); // players can always mine under roads if they are low enough
        if (location.getY() < TownyRoadsSettings.getMinY()) {
            return false;
        }

        Set<Biome> biomeToTest = new HashSet<>();
        biomeToTest.add(block.getBiome());
        int nearByTest = TownyRoadsSettings.getBiomesNoBuildInWildernessPermissiveness();
        for (int i = 1; i <= nearByTest; i++) {
            biomeToTest.add(block.getRelative(BlockFace.EAST, i * 4).getBiome());
            biomeToTest.add(block.getRelative(BlockFace.WEST, i * 4).getBiome());
            biomeToTest.add(block.getRelative(BlockFace.NORTH, i * 4).getBiome());
            biomeToTest.add(block.getRelative(BlockFace.SOUTH, i * 4).getBiome());
        }
        TownyRoadsPlugin.debug(() -> "biomeToTest: "
                + biomeToTest.stream().map(biome -> biome.key().asString()).collect(Collectors.joining(",")));
        return biomeToTest.stream().allMatch(biome -> TownyRoadsSettings.isBiomeNoBuildInWilderness(biome));
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
