package com.github.ahhuser.oxevent.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import com.github.ahhuser.oxevent.OXEventPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RegionManager {

    private final OXEventPlugin plugin;
    private final File locationsFile;
    private FileConfiguration locationsConfig;

    private Location spawnLocation;
    private Location widowniaLocation;

    private Cuboid trueZone;
    private Cuboid falseZone;
    private Cuboid teleportZone; // Fail-safe zone

    // Temporary selections for players
    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    // Stored blocks for restoration
    private final Map<Cuboid, List<BlockState>> storedBlocks = new HashMap<>();

    public RegionManager(OXEventPlugin plugin) {
        this.plugin = plugin;
        this.locationsFile = new File(plugin.getDataFolder(), "locations.yml");
        loadLocations();
    }

    public void setPos1(Player player, Location loc) {
        pos1.put(player.getUniqueId(), loc);
        player.sendMessage("§aPosition 1 set.");
    }

    public void setPos2(Player player, Location loc) {
        pos2.put(player.getUniqueId(), loc);
        player.sendMessage("§aPosition 2 set.");
    }

    public Cuboid getSelection(Player player) {
        Location p1 = pos1.get(player.getUniqueId());
        Location p2 = pos2.get(player.getUniqueId());
        if (p1 == null || p2 == null)
            return null;
        if (!p1.getWorld().equals(p2.getWorld()))
            return null;
        return new Cuboid(p1, p2);
    }

    public void setSpawn(Location loc) {
        this.spawnLocation = loc;
        saveLocation("spawn", loc);
    }

    public Location getSpawn() {
        return spawnLocation;
    }

    public void setWidownia(Location loc) {
        this.widowniaLocation = loc;
        saveLocation("widownia", loc);
    }

    public Location getWidownia() {
        return widowniaLocation;
    }

    public void setTrueZone(Cuboid cuboid) {
        this.trueZone = cuboid;
        saveCuboid("trueZone", cuboid);
    }

    public Cuboid getTrueZone() {
        return trueZone;
    }

    public void setFalseZone(Cuboid cuboid) {
        this.falseZone = cuboid;
        saveCuboid("falseZone", cuboid);
    }

    public Cuboid getFalseZone() {
        return falseZone;
    }

    public void setTeleportZone(Cuboid cuboid) {
        this.teleportZone = cuboid;
        saveCuboid("teleportZone", cuboid);
    }

    public Cuboid getTeleportZone() {
        return teleportZone;
    }

    public void clearZone(Cuboid zone) {
        if (zone == null)
            return;
        List<BlockState> states = new ArrayList<>();

        for (Block block : zone.getBlocks()) {
            if (block.getType() != Material.AIR) {
                states.add(block.getState());
                block.setType(Material.AIR);
            }
        }
        storedBlocks.put(zone, states);
    }

    public void restoreZone(Cuboid zone) {
        if (zone == null || !storedBlocks.containsKey(zone))
            return;
        List<BlockState> states = storedBlocks.remove(zone);
        for (BlockState state : states) {
            state.update(true, false);
        }
    }

    private void loadLocations() {
        if (!locationsFile.exists())
            return;
        locationsConfig = YamlConfiguration.loadConfiguration(locationsFile);

        spawnLocation = locationsConfig.getLocation("spawn");
        widowniaLocation = locationsConfig.getLocation("widownia");

        if (locationsConfig.contains("trueZone"))
            trueZone = loadCuboid("trueZone");
        if (locationsConfig.contains("falseZone"))
            falseZone = loadCuboid("falseZone");
        if (locationsConfig.contains("teleportZone"))
            teleportZone = loadCuboid("teleportZone");
    }

    private void saveLocation(String path, Location loc) {
        if (locationsConfig == null)
            locationsConfig = new YamlConfiguration();
        locationsConfig.set(path, loc);
        saveConfig();
    }

    private void saveCuboid(String path, Cuboid cuboid) {
        if (locationsConfig == null)
            locationsConfig = new YamlConfiguration();
        locationsConfig.set(path + ".world", cuboid.getWorld().getName());
        locationsConfig.set(path + ".x1", cuboid.getX1());
        locationsConfig.set(path + ".y1", cuboid.getY1());
        locationsConfig.set(path + ".z1", cuboid.getZ1());
        locationsConfig.set(path + ".x2", cuboid.getX2());
        locationsConfig.set(path + ".y2", cuboid.getY2());
        locationsConfig.set(path + ".z2", cuboid.getZ2());
        saveConfig();
    }

    private Cuboid loadCuboid(String path) {
        String worldName = locationsConfig.getString(path + ".world");
        World world = Bukkit.getWorld(worldName);
        if (world == null)
            return null;
        int x1 = locationsConfig.getInt(path + ".x1");
        int y1 = locationsConfig.getInt(path + ".y1");
        int z1 = locationsConfig.getInt(path + ".z1");
        int x2 = locationsConfig.getInt(path + ".x2");
        int y2 = locationsConfig.getInt(path + ".y2");
        int z2 = locationsConfig.getInt(path + ".z2");
        return new Cuboid(world, x1, y1, z1, x2, y2, z2);
    }

    private void saveConfig() {
        try {
            locationsConfig.save(locationsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Cuboid {
        private final World world;
        private final int x1, y1, z1;
        private final int x2, y2, z2;

        public Cuboid(Location l1, Location l2) {
            this.world = l1.getWorld();
            this.x1 = Math.min(l1.getBlockX(), l2.getBlockX());
            this.y1 = Math.min(l1.getBlockY(), l2.getBlockY());
            this.z1 = Math.min(l1.getBlockZ(), l2.getBlockZ());
            this.x2 = Math.max(l1.getBlockX(), l2.getBlockX());
            this.y2 = Math.max(l1.getBlockY(), l2.getBlockY());
            this.z2 = Math.max(l1.getBlockZ(), l2.getBlockZ());
        }

        public Cuboid(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
            this.world = world;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
        }

        public boolean contains(Location loc) {
            if (!loc.getWorld().equals(world))
                return false;
            return loc.getX() >= x1 && loc.getX() <= x2 + 1 &&
                    loc.getY() >= y1 && loc.getY() <= y2 + 1 && // +1 for loose check or exact block? usually block
                                                                // coords need care.
                    // Let's stick to block coords. If a player is at 10.5, block is 10.
                    // If x1=10, x2=12. Player 10.5 is inside. Player 12.9 is inside. Player 13.0 is
                    // outside.
                    loc.getZ() >= z1 && loc.getZ() <= z2 + 1;
        }

        // Better contains for players (checking if their feet are in the zone bounds)
        public boolean contains(Player p) {
            return contains(p.getLocation());
        }

        public List<Block> getBlocks() {
            List<Block> blocks = new ArrayList<>();
            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    for (int z = z1; z <= z2; z++) {
                        blocks.add(world.getBlockAt(x, y, z));
                    }
                }
            }
            return blocks;
        }

        public World getWorld() {
            return world;
        }

        public int getX1() {
            return x1;
        }

        public int getY1() {
            return y1;
        }

        public int getZ1() {
            return z1;
        }

        public int getX2() {
            return x2;
        }

        public int getY2() {
            return y2;
        }

        public int getZ2() {
            return z2;
        }
    }
}
