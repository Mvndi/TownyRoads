package net.mvndicraft.townyroads.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import org.bukkit.command.CommandSender;

public class TownyUtil {
    private TownyUtil() {}
    public static Town getTownFromNameOrNull(CommandSender commandSender, String townName) {
        Town town = TownyAPI.getInstance().getTown(townName);
        if (town == null) {
            Messaging.sendError(commandSender, Component.translatable("err_town_does_not_exist",
                    Argument.component("town", Component.text(townName))));
        } else if (town.isRuined()) {
            Messaging.sendError(commandSender,
                    Component.translatable("err_town_is_ruined", Argument.component("town", Component.text(townName))));
            return null; // we don't want to use a ruined town
        }
        return town;
    }

    public static Road getRoadFromNameOrNull(CommandSender commandSender, String roadName) {
        return getRoad(commandSender, roadName, false);
    }

    public static Road getRoadFromNameOrUUIDOrNull(CommandSender commandSender, String roadNameOrUUID) {
        return getRoad(commandSender, roadNameOrUUID, true);
    }

    private static Road getRoad(CommandSender commandSender, String roadNameOrUUID, boolean tryUUID) {
        Road road = TownyRoadsPlugin.getInstance().getRoadManager().getRoadByName(roadNameOrUUID);
        if (road == null) {
            if (tryUUID) {
                try {
                    road = TownyRoadsPlugin.getInstance().getRoadManager()
                            .getRoadByUUID(UUID.fromString(roadNameOrUUID));
                } catch (IllegalArgumentException ignored) {
                    // No need to do anything
                }
            }
            if (road == null) {
                Messaging.sendError(commandSender, Component.translatable("err_road_does_not_exist",
                        Argument.component("road", Component.text(roadNameOrUUID))));
            }
        }
        return road;
    }

}
