package com.github.ahhuser.oxevent.listener;

import com.github.ahhuser.oxevent.OXEventPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SelectionListener implements Listener {

    private final OXEventPlugin plugin;

    public SelectionListener(OXEventPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().hasPermission("ox.admin"))
            return;
        if (event.getItem() == null || event.getItem().getType() != Material.WOODEN_SHOVEL)
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            plugin.getRegionManager().setPos1(event.getPlayer(), event.getClickedBlock().getLocation());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            plugin.getRegionManager().setPos2(event.getPlayer(), event.getClickedBlock().getLocation());
        }
    }
}
