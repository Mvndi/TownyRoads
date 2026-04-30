package net.mvndicraft.townyroads;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.bukkit.util.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class TownyRoadsMessaging {
    private TownyRoadsMessaging() {}

    public static void sendMessage(CommandSender commandSender, Component message) {
        commandSender.sendMessage(Component.text("[TownyRoads]").append(Component.space()).append(message));
    }

    // Almost a copy of TownyMessaging.sendInvitationMessage, but running townyroads command instead.
    public static void sendInviteToRoadMessage(CommandSender commandSender, Road road) {
        final Translator translator = Translator.locale(commandSender);
        NamedTextColor acceptColour = Colors.toNamedTextColor(TownySettings.getConfirmationCommandYesColour()) != null
                ? Colors.toNamedTextColor(TownySettings.getConfirmationCommandYesColour())
                : NamedTextColor.GREEN;
        NamedTextColor denyColour = Colors.toNamedTextColor(TownySettings.getConfirmationCommandNoColour()) != null
                ? Colors.toNamedTextColor(TownySettings.getConfirmationCommandNoColour())
                : NamedTextColor.RED;

        String confirmline = "tr accept " + road.getName();
        String cancelline = "tr deny " + road.getName();

        // Create confirm button based on given params.
        TextComponent confirmComponent = Component
                .text(String.format(TownySettings.getConfirmationCommandFormat(), confirmline)).color(acceptColour)
                .hoverEvent(HoverEvent.showText(
                        translator.component("msg_confirmation_spigot_click_accept", confirmline, "/" + confirmline)))
                .clickEvent(ClickEvent.runCommand("/townyroads:" + confirmline));

        // Create cancel button based on given params.
        TextComponent cancelComponent = Component
                .text(String.format(TownySettings.getConfirmationCommandFormat(), cancelline)).color(denyColour)
                .hoverEvent(HoverEvent.showText(
                        translator.component("msg_confirmation_spigot_click_cancel", cancelline, "/" + cancelline)))
                .clickEvent(ClickEvent.runCommand("/townyroads:" + cancelline));

        sendMessage(commandSender,
                Component.text("You have been invited to the road ").append(Component.text(road.getName()))
                        .append(Component.newline()).append(confirmComponent).append(Component.space())
                        .append(cancelComponent));
    }
}
