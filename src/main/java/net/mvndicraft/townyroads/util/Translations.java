package net.mvndicraft.townyroads.util;

import com.google.common.collect.Maps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
    private static final String folderName = "lang/reference/";
    private final Path localeFolder = TownyRoadsPlugin.getInstance().getDataPath().resolve(folderName);
    private final Key keyName = Key.key(TownyRoadsPlugin.getInstance(), "locale");
    private MiniMessageTranslationStore storage = MiniMessageTranslationStore.create(keyName);

    public void reload() {
        GlobalTranslator.translator().removeSource(storage);
        storage = MiniMessageTranslationStore.create(keyName);

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
            String outLocalePath = folderName + languageKey + ".yml";
            if (TownyRoadsPlugin.getInstance().getResource(outLocalePath) != null) {
                TownyRoadsPlugin.getInstance().saveResource(outLocalePath, true);
            }
        });

        List<Locale> registered = new ArrayList<>(1);
        registered.add(Locale.ENGLISH);

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
                registered.add(locale);
                TownyRoadsPlugin.getInstance().getLogger().info("Loaded locale: " + localeName);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> english = getEnglishKeys();

        Locale.availableLocales().forEach(locale -> {
            if (locale.getLanguage().isEmpty()) {
                return;
            }

            if (registered.contains(locale)) {
                return;
            }

            // Do not save language for each country, just save the country less version.
            if (!locale.getLanguage().equals(locale.toString())) {
                return;
            }

            storage.registerAll(locale, english);
        });

        GlobalTranslator.translator().addSource(storage);
    }

    public Component english(TranslatableComponent component) {
        Component c = storage.translate(component, Locale.ENGLISH);
        return c == null ? component : c;
    }

    public Component translate(TranslatableComponent component, Locale locale) {
        Component c = storage.translate(component, locale);
        TownyRoadsPlugin.debug("Translated: " + component.toString() + " -> " + c.toString());
        return c == null ? storage.translate(component, Locale.ENGLISH) : c;
    }

    private void load(Locale locale) {
        Path localeFile = localeFolder.resolve(locale.getLanguage() + ".yml");
        if (!Files.exists(localeFile)) {
            try {
                Files.createFile(localeFile);
            } catch (Exception e) {
                e.printStackTrace();
                return;
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

        storage.registerAll(locale, map);
    }

    private Map<String, String> getEnglishKeys() {
        Map<String, String> map = Maps.newHashMap();
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(localeFolder.resolve("en.yml").toFile());

        configuration.getValues(true).forEach((k, o) -> {
            if (o instanceof MemorySection) {
                return;
            }

            map.put(k, (String) o);
        });

        return map;
    }
}
