package com.hazebyte.crate.cratereloaded.component;

import com.hazebyte.crate.cratereloaded.menuV2.InventoryV2;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public interface InventoryComponent {

    void openInventory(InventoryV2 inventoryV2, Player player);

    void closeAllInventories();

    void handleOpen(InventoryOpenEvent event);

    void handleClick(InventoryClickEvent event);

    void handleClose(InventoryCloseEvent event);
}
