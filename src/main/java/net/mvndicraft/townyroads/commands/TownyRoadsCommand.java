package net.mvndicraft.townyroads.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import org.bukkit.command.CommandSender;

@CommandAlias("townyroads|troads|tr")
public class TownyRoadsCommand extends BaseCommand {

    @Default
    @Description("Lists the version of the plugin")
    public static void onTownyRoads(CommandSender commandSender) { commandSender.sendMessage(TownyRoadsPlugin.getInstance().toString()); }


}
