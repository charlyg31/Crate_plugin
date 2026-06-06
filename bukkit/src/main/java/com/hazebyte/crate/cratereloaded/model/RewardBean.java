package com.hazebyte.crate.cratereloaded.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public class RewardBean {
    private int slot;
    private double chance;
    private ItemStack displayItem;
    private List<ItemStack> items;
    private List<String> commands;
    private List<String> permissions;
    private boolean isUnique;
    private boolean isAlways;
    private List<String> openMessage;
    private List<String> broadcastMessage;

    public RewardBean() {
        this.items = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.permissions = new ArrayList<>();
        this.openMessage = new ArrayList<>();
        this.broadcastMessage = new ArrayList<>();
    }

    public RewardBean(int slot, double chance, ItemStack displayItem, List<ItemStack> items,
                      List<String> commands, List<String> permissions, boolean isUnique, boolean isAlways,
                      List<String> openMessage, List<String> broadcastMessage) {
        this.slot = slot;
        this.chance = chance;
        this.displayItem = displayItem;
        this.items = items != null ? items : new ArrayList<>();
        this.commands = commands != null ? commands : new ArrayList<>();
        this.permissions = permissions != null ? permissions : new ArrayList<>();
        this.isUnique = isUnique;
        this.isAlways = isAlways;
        this.openMessage = openMessage != null ? openMessage : new ArrayList<>();
        this.broadcastMessage = broadcastMessage != null ? broadcastMessage : new ArrayList<>();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int slot;
        private double chance;
        private ItemStack displayItem;
        private List<ItemStack> items = new ArrayList<>();
        private List<String> commands = new ArrayList<>();
        private List<String> permissions = new ArrayList<>();
        private boolean isUnique;
        private boolean isAlways;
        private List<String> openMessage = new ArrayList<>();
        private List<String> broadcastMessage = new ArrayList<>();

        public Builder slot(int v) { this.slot = v; return this; }
        public Builder chance(double v) { this.chance = v; return this; }
        public Builder displayItem(ItemStack v) { this.displayItem = v; return this; }
        public Builder items(List<ItemStack> v) { this.items = v; return this; }
        public Builder commands(List<String> v) { this.commands = v; return this; }
        public Builder permissions(List<String> v) { this.permissions = v; return this; }
        public Builder isUnique(boolean v) { this.isUnique = v; return this; }
        public Builder isAlways(boolean v) { this.isAlways = v; return this; }
        public Builder openMessage(List<String> v) { this.openMessage = v; return this; }
        public Builder broadcastMessage(List<String> v) { this.broadcastMessage = v; return this; }
        public RewardBean build() {
            return new RewardBean(slot, chance, displayItem, items, commands, permissions,
                    isUnique, isAlways, openMessage, broadcastMessage);
        }
    }

    public Builder toBuilder() {
        return new Builder().slot(slot).chance(chance).displayItem(displayItem).items(items)
                .commands(commands).permissions(permissions).isUnique(isUnique).isAlways(isAlways)
                .openMessage(openMessage).broadcastMessage(broadcastMessage);
    }

    public int getSlot() { return slot; }
    public double getChance() { return chance; }
    public ItemStack getDisplayItem() { return displayItem; }
    public List<ItemStack> getItems() { return items; }
    public List<String> getCommands() { return commands; }
    public List<String> getPermissions() { return permissions; }
    public boolean isUnique() { return isUnique; }
    public boolean isAlways() { return isAlways; }
    public List<String> getOpenMessage() { return openMessage; }
    public List<String> getBroadcastMessage() { return broadcastMessage; }
    public void setSlot(int v) { this.slot = v; }
    public void setChance(double v) { this.chance = v; }
    public void setDisplayItem(ItemStack v) { this.displayItem = v; }
    public void setItems(List<ItemStack> v) { this.items = v; }
    public void setCommands(List<String> v) { this.commands = v; }
    public void setPermissions(List<String> v) { this.permissions = v; }
    public void setUnique(boolean v) { this.isUnique = v; }
    public void setAlways(boolean v) { this.isAlways = v; }
    public void setOpenMessage(List<String> v) { this.openMessage = v; }
    public void setBroadcastMessage(List<String> v) { this.broadcastMessage = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RewardBean)) return false;
        RewardBean r = (RewardBean) o;
        return slot == r.slot && Double.compare(r.chance, chance) == 0;
    }

    @Override
    public int hashCode() { return Objects.hash(slot, chance); }

    @Override
    public String toString() { return "RewardBean(slot=" + slot + ", chance=" + chance + ")"; }
}
