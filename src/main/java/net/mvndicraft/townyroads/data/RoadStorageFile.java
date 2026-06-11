package net.mvndicraft.townyroads.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;

public class RoadStorageFile implements RoadStorage {
    private File dataDir;
    private File roadDataDir;
    public RoadStorageFile() {
        dataDir = new File(TownyRoadsPlugin.getInstance().getDataFolder(), "data");
        dataDir.mkdirs();
        roadDataDir = new File(dataDir, "roads");
        roadDataDir.mkdirs();
    }
    @Override
    public Set<Road> loadAll() {
        Set<Road> roads = ConcurrentHashMap.newKeySet();
        for (File file : roadDataDir.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                Road road = load(file);
                if (road != null) {
                    roads.add(road);
                }
            }
        }
        return roads;
    }
    @Override
    public Road load(UUID id) {
        File file = new File(roadDataDir, id + ".yml");
        if (file.exists()) {
            return load(file);
        }
        return null;
    }

    private Road load(File existingFile) {
        TownyRoadsPlugin.debug("Loaded road " + existingFile.getName());
        return Road.fromYml(existingFile);
    }

    @Override
    public void saveAll(Road... roads) {
        for (Road road : roads) {
            save(road);
        }
    }

    @Override
    public void save(Road road) {
        File docFile = new File(roadDataDir, road.getId() + ".yml");
        road.toYml(docFile);
        TownyRoadsPlugin.debug("Saved road " + road.getId());
    }

    @Override
    public void delete(Road road) {
        try {
            if (!Files.deleteIfExists(Path.of(roadDataDir.getAbsolutePath(), road.getId() + ".yml"))) {
                TownyRoadsPlugin.error("Fail to delete road " + road.getId() + " because file can't be deleted.");
            }
        } catch (IOException e) {
            TownyRoadsPlugin.error("Fail to delete road " + road.getId() + " because " + e.getMessage());
        }
    }
    @Override
    public void saveSoon(Road road) {
        save(road);
    }
}
