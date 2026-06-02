package net.mvndicraft.townyroads.util;

import com.palmergames.bukkit.towny.TownyAPI;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class Messaging {
    public static Component translate(final String translationKey, final @NotNull List<? extends ComponentLike> args) {
        return Component.translatable("townyroads_plugin_prefix").appendSpace().append(Component.translatable(translationKey, args));
    }
    public static Component translate(final String translationKey) {
        return translate(translationKey, List.of());
    }

    public static void sendGlobalMessage(String translationKey) {
        Bukkit.getOnlinePlayers().stream()
                .filter(Objects::nonNull)
                .filter(p -> TownyAPI.getInstance().isTownyWorld(p.getLocation().getWorld()))
                .forEach(p -> p.sendMessage(translate(translationKey)));
    }
}
