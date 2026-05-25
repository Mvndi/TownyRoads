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
    public RoadStorageFile() {
        dataDir = new File(TownyRoadsPlugin.getInstance().getDataFolder(), "data");
        dataDir.mkdirs();
    }
    @Override
    public Set<Road> loadAll() {
        Set<Road> roads = ConcurrentHashMap.newKeySet();
        for (File file : dataDir.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                roads.add(load(file));
            }
        }
        return roads;
    }
    @Override
    public Road load(UUID id) {
        File file = new File(dataDir, id + ".yml");
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
        File docFile = new File(dataDir, road.getId() + ".yml");
        road.toYml(docFile);
        TownyRoadsPlugin.debug("Saved road " + road.getId());
    }

    @Override
    public void delete(Road road) {
        try {
            if (!Files.deleteIfExists(Path.of(dataDir.getAbsolutePath(), road.getId() + ".yml"))) {
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
