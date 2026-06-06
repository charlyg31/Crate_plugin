package com.hazebyte.crate.cratereloaded.menuV2;

import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryButtonV2 {
    private Function<Player, ItemStack> itemCreator;
    @Nullable private Consumer<InventoryClickEvent> clickHandler;

    public InventoryButtonV2() {}

    public InventoryButtonV2(Function<Player, ItemStack> itemCreator, Consumer<InventoryClickEvent> clickHandler) {
        this.itemCreator = itemCreator;
        this.clickHandler = clickHandler;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Function<Player, ItemStack> itemCreator;
        private Consumer<InventoryClickEvent> clickHandler;

        public Builder itemCreator(Function<Player, ItemStack> v) { this.itemCreator = v; return this; }
        public Builder clickHandler(Consumer<InventoryClickEvent> v) { this.clickHandler = v; return this; }
        public InventoryButtonV2 build() { return new InventoryButtonV2(itemCreator, clickHandler); }
    }

    public Builder toBuilder() { return new Builder().itemCreator(itemCreator).clickHandler(clickHandler); }
    public Function<Player, ItemStack> getItemCreator() { return itemCreator; }
    public Consumer<InventoryClickEvent> getClickHandler() { return clickHandler; }
    public void setItemCreator(Function<Player, ItemStack> v) { this.itemCreator = v; }
    public void setClickHandler(Consumer<InventoryClickEvent> v) { this.clickHandler = v; }

    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (clickHandler != null) clickHandler.accept(event);
    }
}
