package com.hazebyte.crate.cratereloaded.model.menuV2;

import com.hazebyte.crate.cratereloaded.menuV2.InventoryDecorator;
import com.hazebyte.crate.cratereloaded.menuV2.InventoryV2;
import org.bukkit.entity.Player;

public class InventoryNavigationDecorator implements InventoryDecorator {
    private InventoryV2 inventoryV2;

    public InventoryNavigationDecorator(InventoryV2 inventoryV2) {
        this.inventoryV2 = inventoryV2;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private InventoryV2 inventoryV2;
        public Builder inventoryV2(InventoryV2 v) { this.inventoryV2 = v; return this; }
        public InventoryNavigationDecorator build() { return new InventoryNavigationDecorator(inventoryV2); }
    }

    public InventoryV2 getInventoryV2() { return inventoryV2; }
    public void setInventoryV2(InventoryV2 v) { this.inventoryV2 = v; }

    @Override
    public void decorate(Player player) {
        inventoryV2.setButton(inventoryV2.getInventorySize() - 4, InventoryButtonsV2.createCloseButton());
    }
}
