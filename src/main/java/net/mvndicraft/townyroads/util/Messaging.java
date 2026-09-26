package net.mvndicraft.townyroads.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.bukkit.util.Colors;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.mvndicraft.townyroads.Road;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Messaging {
    public static Component translate(final String translationKey, final @NotNull List<? extends ComponentLike> args) {
        return Component.translatable(translationKey, args);
    }
    public static Component translate(final String translationKey) {
        return translate(translationKey, List.of());
    }

    public static void sendGlobalMessage(String translationKey) {
        Bukkit.getOnlinePlayers().stream().filter(Objects::nonNull)
                .filter(p -> TownyAPI.getInstance().isTownyWorld(p.getLocation().getWorld()))
                .forEach(p -> p.sendMessage(translate(translationKey)));
    }
    public static void sendMessage(Audience audience, Component message) {
        audience.sendMessage(Component.translatable("townyroads_plugin_prefix").appendSpace().append(message));
    }
    public static void sendError(Audience audience, Component message) {
        audience.sendMessage(Component.translatable("townyroads_plugin_prefix").appendSpace().append(message)
                .color(NamedTextColor.RED));
    }
    public static void sendError(Audience audience, String translationKey) {
        sendError(audience, translate(translationKey));
    }
    public static void sendSuccess(Audience audience, Component message) {
        audience.sendMessage(Component.translatable("townyroads_plugin_prefix").appendSpace().append(message)
                .color(NamedTextColor.GREEN));
    }
    public static void sendSuccess(Audience audience, String translationKey) {
        sendSuccess(audience, translate(translationKey));
    }

    public static void sendAccept(Audience audience, Component message) {
        audience.sendMessage(Component.translatable("townyroads_plugin_prefix").appendSpace().append(message)
                .color(NamedTextColor.GREEN));
    }
    public static void sendAccept(Audience audience, String translationKey) {
        sendAccept(audience, translate(translationKey));
    }

    public static void sendDeny(Audience audience, Component message) {
        audience.sendMessage(Component.translatable("townyroads_plugin_prefix").appendSpace().append(message)
                .color(NamedTextColor.RED));
    }
    public static void sendDeny(Audience audience, String translationKey) {
        sendDeny(audience, translate(translationKey));
    }


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
        Component confirmComponent = Component
                .text(String.format(TownySettings.getConfirmationCommandFormat(), confirmline)).color(acceptColour)
                .hoverEvent(HoverEvent.showText(
                        translator.component("msg_confirmation_spigot_click_accept", confirmline, "/" + confirmline)))
                .clickEvent(ClickEvent.runCommand("/townyroads:" + confirmline));

        // Create cancel button based on given params.
        Component cancelComponent = Component
                .text(String.format(TownySettings.getConfirmationCommandFormat(), cancelline)).color(denyColour)
                .hoverEvent(HoverEvent.showText(
                        translator.component("msg_confirmation_spigot_click_cancel", cancelline, "/" + cancelline)))
                .clickEvent(ClickEvent.runCommand("/townyroads:" + cancelline));

        sendMessage(commandSender,
                Component
                        .translatable("info_invite_to_road", Argument.component("road", Component.text(road.getName())))
                        .append(Component.newline()).append(confirmComponent).append(Component.space())
                        .append(cancelComponent));
    }
}

