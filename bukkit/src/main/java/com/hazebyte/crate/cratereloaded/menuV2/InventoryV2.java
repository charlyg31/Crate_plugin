package com.hazebyte.crate.cratereloaded.menuV2;

import static com.hazebyte.crate.cratereloaded.util.InventoryConstants.SIX_ROWS;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class InventoryV2 implements InventoryHandler {
    private Map<Integer, InventoryButtonV2> buttons;
    private Integer inventorySize;
    private String title;
    private List<InventoryDecorator> decorators;

    public InventoryV2() {
        this.buttons = new HashMap<>();
        this.decorators = new ArrayList<>();
    }

    public InventoryV2(Map<Integer, InventoryButtonV2> buttons, Integer inventorySize,
                       String title, List<InventoryDecorator> decorators) {
        this.buttons = buttons != null ? buttons : new HashMap<>();
        this.inventorySize = inventorySize;
        this.title = title;
        this.decorators = decorators != null ? decorators : new ArrayList<>();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Map<Integer, InventoryButtonV2> buttons = new HashMap<>();
        private Integer inventorySize;
        private String title;
        private List<InventoryDecorator> decorators = new ArrayList<>();

        public Builder buttons(Map<Integer, InventoryButtonV2> v) { this.buttons = v; return this; }
        public Builder inventorySize(Integer v) { this.inventorySize = v; return this; }
        public Builder title(String v) { this.title = v; return this; }
        public Builder decorators(List<InventoryDecorator> v) { this.decorators = v; return this; }
        public Builder decorator(InventoryDecorator v) { this.decorators.add(v); return this; }
        public InventoryV2 build() { return new InventoryV2(buttons, inventorySize, title, decorators); }
    }

    public Builder toBuilder() {
        return new Builder().buttons(buttons).inventorySize(inventorySize).title(title).decorators(decorators);
    }

    public Map<Integer, InventoryButtonV2> getButtons() { return buttons; }
    public Integer getInventorySize() { return inventorySize; }
    public String getTitle() { return title; }
    public List<InventoryDecorator> getDecorators() { return decorators; }
    public void setButtons(Map<Integer, InventoryButtonV2> v) { this.buttons = v; }
    public void setInventorySize(Integer v) { this.inventorySize = v; }
    public void setTitle(String v) { this.title = v; }
    public void setDecorators(List<InventoryDecorator> v) { this.decorators = v; }

    public void addButton(InventoryButtonV2 button) {
        int nextFreeSlot = 0;
        while (buttons.containsKey(nextFreeSlot) && nextFreeSlot++ < SIX_ROWS - 1) nextFreeSlot++;
        setButton(nextFreeSlot, button);
    }

    public void setButton(int slot, InventoryButtonV2 button) {
        buttons.put(slot, button);
        Optional<Inventory> optional = CorePlugin.getJavaPluginComponent().getInventoryManager().getInventory(this);
        optional.ifPresent(inv -> inv.setItem(slot, button.getItemCreator().apply(null)));
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (buttons.containsKey(event.getSlot())) buttons.get(event.getSlot()).onClick(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryV2)) return false;
        InventoryV2 v = (InventoryV2) o;
        return Objects.equals(inventorySize, v.inventorySize) && Objects.equals(title, v.title);
    }

    @Override
    public int hashCode() { return Objects.hash(inventorySize, title); }

    @Override
    public String toString() { return "InventoryV2(title=" + title + ")"; }
}
