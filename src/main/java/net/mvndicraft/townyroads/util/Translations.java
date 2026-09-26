package net.mvndicraft.townyroads.util;

import com.google.common.collect.Maps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.codehaus.plexus.util.FileUtils;

public class Translations {
    private final TownyRoadsPlugin plugin = TownyRoadsPlugin.getInstance();
    private static final String FOLDER_NAME = "lang/reference/";
    private final Path localeFolder = plugin.getDataPath().resolve(FOLDER_NAME);
    private final Key key = Key.key(plugin, "locale");
    private MiniMessageTranslationStore storage = MiniMessageTranslationStore.create(key);

    public void reload() {
        GlobalTranslator.translator().removeSource(storage);
        storage = MiniMessageTranslationStore.create(key);
        storage.defaultLocale(Locale.ENGLISH);

        if (!Files.isDirectory(localeFolder)) {
            try {
                Files.createDirectories(localeFolder);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        // create default translation even if it exists
        Locale.availableLocales().forEach(locale -> {
            String languageKey = locale.toString();
            String outLocalePath = FOLDER_NAME + languageKey + ".yml";
            if (plugin.getResource(outLocalePath) != null) {
                plugin.saveResource(outLocalePath, true);
            }
        });

        // Load all existing locale
        try (var str = Files.list(localeFolder)) {
            str.forEach(path -> {
                String name = path.getFileName().toString();
                if (!name.endsWith(".yml")) {
                    return;
                }

                String localeName = FileUtils.basename(name, ".yml");

                Locale locale = Locale.forLanguageTag(localeName);
                if (locale != null)
                    load(locale);
                plugin.getLogger().info("Loaded locale: " + localeName);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        GlobalTranslator.translator().addSource(storage);
    }

    public Component translate(TranslatableComponent component, Locale locale) {
        Component c = storage.translate(component, locale);
        plugin.getLogger().info("Translated: " + component.toString() + " -> " + c.toString());
        return c == null ? storage.translate(component, Locale.ENGLISH) : c;
    }

    private void load(Locale locale) {
        storage.registerAll(locale, getKey(locale));
    }

    private Map<String, String> getKey(Locale locale) {
        Path localeFile = localeFolder.resolve(locale.getLanguage() + ".yml");
        if (!Files.exists(localeFile)) {
            try {
                Files.createFile(localeFile);
            } catch (Exception e) {
                e.printStackTrace();
                return Map.of();
            }
        }

        Map<String, String> map = Maps.newHashMap();
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(localeFile.toFile());

        configuration.getValues(true).forEach((k, o) -> {
            if (o instanceof MemorySection) {
                return;
            }

            map.put(k, (String) o);
        });

        return map;
    }
}