package net.mvndicraft.townyroads.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.mvndicraft.townyroads.ChunkCoord;

public class ChunkCoordUtil {
    private ChunkCoordUtil() {}

    public static boolean areAllConnected(Collection<ChunkCoord> chunks) {
        if (chunks.isEmpty())
            return true;

        UUID world = chunks.iterator().next().worldUuid();
        if (chunks.stream().anyMatch(c -> !c.worldUuid().equals(world)))
            return false;

        return getComponents(new HashSet<>(chunks)).size() == 1;
    }

    public static Optional<Set<ChunkCoord>> findChunksToConnect(Collection<ChunkCoord> chunks, int maxChunksToClaim) {
        Set<ChunkCoord> connected = new HashSet<>(chunks);

        if (connected.isEmpty() || areAllConnected(connected))
            return Optional.of(Collections.emptySet());

        if (maxChunksToClaim < 0 || hasMultipleWorlds(connected))
            return Optional.empty();

        UUID world = connected.iterator().next().worldUuid();
        Set<ChunkCoord> added = new HashSet<>();

        while (true) {
            List<Set<ChunkCoord>> components = getComponents(connected);

            if (components.size() <= 1)
                return Optional.of(added);

            Optional<Set<ChunkCoord>> path = findBestPath(components, world, connected);

            if (path.isEmpty())
                return Optional.empty();

            added.addAll(path.get());
            connected.addAll(path.get());

            if (added.size() > maxChunksToClaim)
                return Optional.empty();
        }
    }

    private static boolean hasMultipleWorlds(Set<ChunkCoord> chunks) {
        UUID world = chunks.iterator().next().worldUuid();
        return chunks.stream().anyMatch(c -> !c.worldUuid().equals(world));
    }

    private static Optional<Set<ChunkCoord>> findBestPath(List<Set<ChunkCoord>> components, UUID world,
            Set<ChunkCoord> connected) {
        Set<ChunkCoord> bestPath = null;

        for (int i = 0; i < components.size(); i++) {
            for (int j = i + 1; j < components.size(); j++) {
                Optional<Set<ChunkCoord>> candidate = findPath(components.get(i), components.get(j), world, connected);

                if (candidate.isEmpty())
                    continue;

                if (bestPath == null || candidate.get().size() < bestPath.size())
                    bestPath = candidate.get();

                if (bestPath.isEmpty())
                    return Optional.of(bestPath);
            }
        }

        return Optional.ofNullable(bestPath);
    }

    private static List<Set<ChunkCoord>> getComponents(Set<ChunkCoord> chunks) {
        List<Set<ChunkCoord>> components = new ArrayList<>();
        Set<ChunkCoord> visited = new HashSet<>();

        for (ChunkCoord start : chunks) {
            if (!visited.add(start))
                continue;

            components.add(getComponent(start, chunks, visited));
        }

        return components;
    }

    private static Set<ChunkCoord> getComponent(ChunkCoord start, Set<ChunkCoord> chunks, Set<ChunkCoord> visited) {
        Set<ChunkCoord> component = new HashSet<>();
        Queue<ChunkCoord> queue = new ArrayDeque<>();

        queue.add(start);
        component.add(start);

        while (!queue.isEmpty()) {
            ChunkCoord current = queue.poll();

            for (ChunkCoord neighbor : current.getNearby(1)) {
                if (chunks.contains(neighbor) && visited.add(neighbor)) {
                    component.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return component;
    }

    private static Optional<Set<ChunkCoord>> findPath(Set<ChunkCoord> startComponent, Set<ChunkCoord> targetComponent,
            UUID world, Set<ChunkCoord> connected) {
        Queue<ChunkCoord> queue = new ArrayDeque<>(startComponent);
        Map<ChunkCoord, ChunkCoord> previous = new HashMap<>();
        Set<ChunkCoord> visited = new HashSet<>(startComponent);

        while (!queue.isEmpty()) {
            ChunkCoord current = queue.poll();

            Optional<ChunkCoord> target = findTarget(current, targetComponent, world, connected, visited, previous,
                    queue);

            if (target.isPresent())
                return reconstructPath(target.get(), startComponent, connected, previous);
        }

        return Optional.empty();
    }

    private static Optional<ChunkCoord> findTarget(ChunkCoord current, Set<ChunkCoord> targetComponent, UUID world,
            Set<ChunkCoord> connected, Set<ChunkCoord> visited, Map<ChunkCoord, ChunkCoord> previous,
            Queue<ChunkCoord> queue) {
        for (ChunkCoord neighbor : current.getNearby(1)) {
            if (!neighbor.worldUuid().equals(world) || !visited.add(neighbor))
                continue;

            if (targetComponent.contains(neighbor)) {
                previous.put(neighbor, current);
                return Optional.of(neighbor);
            }

            if (connected.contains(neighbor) || TownyUtil.hasTown(neighbor))
                continue;

            previous.put(neighbor, current);
            queue.add(neighbor);
        }

        return Optional.empty();
    }

    private static Optional<Set<ChunkCoord>> reconstructPath(ChunkCoord target, Set<ChunkCoord> startComponent,
            Set<ChunkCoord> connected, Map<ChunkCoord, ChunkCoord> previous) {
        Set<ChunkCoord> path = new HashSet<>();
        ChunkCoord current = target;

        while (!startComponent.contains(current)) {
            if (!connected.contains(current))
                path.add(current);

            current = previous.get(current);

            if (current == null)
                return Optional.empty();
        }

        return Optional.of(path);
    }
}
