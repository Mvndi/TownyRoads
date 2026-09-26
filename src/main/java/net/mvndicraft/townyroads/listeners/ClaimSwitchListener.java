package net.mvndicraft.townyroads.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.mvndicraft.townyroads.ChunkCoord;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townyroads.util.Messaging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ClaimSwitchListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangePlot(PlayerChangePlotEvent event) {
        Player player = event.getPlayer();
        if (!TownyRoadsPlugin.getInstance().getClaimSwitchManager().isEnabled(player.getUniqueId())) {
            return;
        }

        ChunkCoord to = ChunkCoord.from(event.getTo());

        if (TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(to) != null) {
            return;
        }

        if (TownyAPI.getInstance().getTown(to.toLocation()) != null) {
            return;
        }

        Road road = findNearestMemberRoad(player, to);
        if (road == null || road.isBlocked()) {
            disableAndNotifyNoRoad(player);
            return;
        }

        if (!road.canClaimMore()) {
            disableAndNotifyMaxed(player, road);
            return;
        }

        if (!TownyRoadsPlugin.getInstance().getRoadManager().claimRoad(road, to, player)) {
            disableAndNotifyNoRoad(player);
            return;
        }

        Messaging.sendSuccess(player, Component.translatable("success_auto_claim",
                Argument.component("road", Component.text(road.getName())),
                Argument.component("current", Component.text(road.chunksCoordsSize())),
                Argument.component("max", Component.text(road.maxChunksCoordsSize()))));

        if (!road.canClaimMore()) {
            disableAndNotifyMaxed(player, road);
        }
    }

    private Road findNearestMemberRoad(Player player, ChunkCoord chunkCoord) {
        for (ChunkCoord nearby : chunkCoord.getNearby(1)) {
            Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(nearby);
            if (road != null && road.isAPlayerOfTheRoad(player)) {
                return road;
            }
        }
        return null;
    }

    private void disableAndNotifyMaxed(Player player, Road road) {
        TownyRoadsPlugin.getInstance().getClaimSwitchManager().disable(player.getUniqueId());
        Messaging.sendError(player, Component.translatable("err_claim_switch_maxed",
                Argument.component("road", Component.text(road.getName()))));
    }

    private void disableAndNotifyNoRoad(Player player) {
        TownyRoadsPlugin.getInstance().getClaimSwitchManager().disable(player.getUniqueId());
        Messaging.sendError(player, Component.translatable("err_claim_switch_no_road"));
    }
}
