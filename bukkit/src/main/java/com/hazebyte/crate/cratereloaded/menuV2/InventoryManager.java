package com.hazebyte.crate.cratereloaded.menuV2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

public class InventoryManager {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("InventoryManager");


    private final Map<Inventory, InventoryV2> inventoryMap;
    private final Map<InventoryV2, Inventory> reverseInventoryMap;

    public InventoryManager() {
        this.inventoryMap = new HashMap<>();
        this.reverseInventoryMap = new HashMap<>();
    }

    public void openInventory(InventoryV2 inventoryV2, Player player) {
        final Optional<Inventory> optional = Optional.ofNullable(reverseInventoryMap.get(inventoryV2));
        if (optional.isPresent()) {
            player.openInventory(optional.get());
        } else {
            Inventory inventory = Bukkit.createInventory(null, inventoryV2.getInventorySize(), inventoryV2.getTitle() != null ? inventoryV2.getTitle() : "");
            reverseInventoryMap.put(inventoryV2, inventory);
            inventoryMap.put(inventory, inventoryV2);
            player.openInventory(inventory);
        }
    }

    public void closeAllInventories() {
        for (Inventory inventory : inventoryMap.keySet()) {
            new ArrayList<>(inventory.getViewers())
                    .stream() // copy since closeInventory removes and causes ConcurrentModification
                            .filter(viewer -> viewer instanceof Player)
                            .map(viewer -> (Player) viewer)
                            .forEach(Player::closeInventory);
        }
        inventoryMap.clear();
        reverseInventoryMap.clear();
    }

    public Optional<Inventory> getInventory(InventoryHandler inventoryHandler) {
        return Optional.ofNullable(reverseInventoryMap.get(inventoryHandler));
    }

    protected void handleOpen(InventoryOpenEvent event) {
        InventoryV2 inventoryV2 = inventoryMap.get(event.getInventory());
        Player player = (Player) event.getPlayer();
        if (inventoryV2 != null) {
            inventoryV2.getButtons().entrySet().stream().forEach(entry -> event.getInventory()
                    .setItem(entry.getKey(), entry.getValue().getItemCreator().apply(player)));
        }
    }

    protected void handleClick(InventoryClickEvent event) {
        if (inventoryMap.containsKey(event.getInventory())) {
            inventoryMap.get(event.getInventory()).onClick(event);
        }
    }

    protected void handleClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryV2 inventoryV2 = inventoryMap.get(inventory);
        if (inventoryV2 != null) {
            inventoryMap.remove(inventory);
            reverseInventoryMap.remove(inventoryV2);
        }
    }
}
