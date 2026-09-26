package net.mvndicraft.townyroads.data;

import java.util.Set;
import java.util.UUID;
import net.mvndicraft.townyroads.Road;

public interface RoadStorage {
    public Set<Road> loadAll();
    public Road load(UUID id);
    public void saveAll(Road... road);
    public void save(Road road);
    public void delete(Road road);
    public void saveSoon(Road road);
}
