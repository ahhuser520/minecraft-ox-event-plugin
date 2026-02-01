package com.github.ahhuser.oxevent.manager;

import com.github.ahhuser.oxevent.OXEventPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerManager {

    private final OXEventPlugin plugin;
    private final Set<UUID> participants = new HashSet<>();
    private final Set<UUID> spectators = new HashSet<>();

    public PlayerManager(OXEventPlugin plugin) {
        this.plugin = plugin;
    }

    public void addParticipant(Player player) {
        participants.add(player.getUniqueId());
        spectators.remove(player.getUniqueId());
        player.setGameMode(GameMode.ADVENTURE);
        clearPlayerState(player);
    }

    public void addSpectator(Player player, boolean teleport) {
        participants.remove(player.getUniqueId());
        spectators.add(player.getUniqueId());
        if (teleport) {
            Location widownia = plugin.getRegionManager().getWidownia();
            if (widownia != null) {
                player.teleport(widownia);
            }
        }
    }

    public void eliminatePlayer(Player player) {
        if (!participants.contains(player.getUniqueId()))
            return;

        participants.remove(player.getUniqueId());
        spectators.add(player.getUniqueId());

        Location widownia = plugin.getRegionManager().getWidownia();
        if (widownia != null) {
            player.teleport(widownia);
        }

        player.sendTitle("§cPRZEGRAŁEŚ!", "§7Odpadłeś z eventu.", 10, 70, 20);
    }

    public boolean isParticipant(Player player) {
        return participants.contains(player.getUniqueId());
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player.getUniqueId());
    }

    public void clearAll() {
        participants.clear();
        spectators.clear();
    }

    private void clearPlayerState(Player player) {
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
}
