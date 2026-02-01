package com.github.ahhuser.oxevent.listener;

import com.github.ahhuser.oxevent.OXEventPlugin;
import com.github.ahhuser.oxevent.manager.GameManager;
import com.github.ahhuser.oxevent.manager.RegionManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

import java.time.Duration;

public class GameListener implements Listener {

    private final OXEventPlugin plugin;

    public GameListener(OXEventPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        GameManager gm = plugin.getGameManager();

        if (gm.getGameState() == GameManager.GameState.IN_GAME || gm.getGameState() == GameManager.GameState.STARTING) {
            plugin.getPlayerManager().addSpectator(p, true);
            p.sendMessage("§eEvent OX trwa, zostałeś przeteleportowany na widownię.");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        GameManager gm = plugin.getGameManager();
        if (gm.getGameState() == GameManager.GameState.WAITING || gm.getGameState() == GameManager.GameState.ENDING)
            return;

        Player p = event.getPlayer();
        if (plugin.getPlayerManager().isSpectator(p))
            return;

        RegionManager rm = plugin.getRegionManager();
        RegionManager.Cuboid teleportZone = rm.getTeleportZone();

        // Fail-safe teleport zone check
        if (teleportZone != null && teleportZone.contains(p)) {
            plugin.getPlayerManager().eliminatePlayer(p);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p))
            return;
        GameManager gm = plugin.getGameManager();
        if (gm.getGameState() == GameManager.GameState.WAITING)
            return; // Allow dmg in waiting? Maybe not.

        // Disable all damage for everyone in event area usually, but logic specifically
        // asked for fail-safe fall.
        // "zadbaj o to aby gracze nie byli w stanie dostawać żadnego damage podczas
        // eventu w żaden sposób."

        if (gm.getGameState() != GameManager.GameState.ENDING) {
            event.setCancelled(true);
        }

        if (plugin.getPlayerManager().isSpectator(p))
            return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            plugin.getPlayerManager().eliminatePlayer(p);
        }

        // Also check if they fell 20 blocks down?
        // Relying on damage event is simpler if they hit ground.
        // Additional fail-safe: check Y level using move event or scheduler, but damage
        // event handles "hit ground".
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (plugin.getGameManager().getGameState() != GameManager.GameState.WAITING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().hasPermission("ox.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().hasPermission("ox.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (plugin.getGameManager().getGameState() == GameManager.GameState.WAITING)
            return;
        if (event.getPlayer().hasPermission("ox.admin"))
            return;

        String[] args = event.getMessage().split(" ");
        String cmd = args[0].toLowerCase();

        // Whitelist: /helpop, /report, /msg, /enderchest
        if (cmd.equals("/helpop") || cmd.equals("/report") || cmd.equals("/msg") || cmd.equals("/enderchest")) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage("§cKomendy są zablokowane podczas eventu.");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Prevent pearls/chorus
        if (plugin.getGameManager().getGameState() == GameManager.GameState.WAITING)
            return;
        if (event.getItem() == null)
            return;

        Material mat = event.getItem().getType();
        if (mat == Material.ENDER_PEARL || mat == Material.CHORUS_FRUIT) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cPrzedmiot zablokowany.");
        }
    }
}
