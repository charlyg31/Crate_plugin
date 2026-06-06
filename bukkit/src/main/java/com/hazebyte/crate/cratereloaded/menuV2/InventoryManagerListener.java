package com.hazebyte.crate.cratereloaded.menuV2;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;

public class InventoryManagerListener implements Listener {

    private InventoryManager inventoryManager;
    private InventoryHistoryManager inventoryHistoryManager;

    public InventoryManagerListener(
            InventoryManager inventoryManager, InventoryHistoryManager inventoryHistoryManager) {
        this.inventoryManager = inventoryManager;
        this.inventoryHistoryManager = inventoryHistoryManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        inventoryManager.handleClick(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        inventoryManager.handleOpen(event);
        inventoryHistoryManager.handleOpen(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        inventoryManager.handleClose(event);
        inventoryHistoryManager.handleClose(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        inventoryManager.closeAllInventories();
        inventoryHistoryManager.handlePluginDisable(event);
    }
}
