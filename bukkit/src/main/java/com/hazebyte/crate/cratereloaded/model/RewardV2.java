package com.hazebyte.crate.cratereloaded.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

public class RewardV2 {
    private Optional<ItemStack> displayItem;
    private double chance;
    private List<ItemStack> items;
    private List<String> commands;
    private List<String> exclusivePermissions;
    private List<String> inclusivePermissions;
    private List<String> broadcastMessage;
    private List<String> openMessage;
    private boolean constant;
    private boolean unique;

    public RewardV2() {
        this.displayItem = Optional.empty();
        this.chance = 0.0;
        this.items = Collections.emptyList();
        this.commands = Collections.emptyList();
        this.exclusivePermissions = Collections.emptyList();
        this.inclusivePermissions = Collections.emptyList();
        this.broadcastMessage = Collections.emptyList();
        this.openMessage = Collections.emptyList();
    }

    public RewardV2(Optional<ItemStack> displayItem, double chance, List<ItemStack> items,
                    List<String> commands, List<String> exclusivePermissions, List<String> inclusivePermissions,
                    List<String> broadcastMessage, List<String> openMessage, boolean constant, boolean unique) {
        this.displayItem = displayItem != null ? displayItem : Optional.empty();
        this.chance = chance;
        this.items = items != null ? items : Collections.emptyList();
        this.commands = commands != null ? commands : Collections.emptyList();
        this.exclusivePermissions = exclusivePermissions != null ? exclusivePermissions : Collections.emptyList();
        this.inclusivePermissions = inclusivePermissions != null ? inclusivePermissions : Collections.emptyList();
        this.broadcastMessage = broadcastMessage != null ? broadcastMessage : Collections.emptyList();
        this.openMessage = openMessage != null ? openMessage : Collections.emptyList();
        this.constant = constant;
        this.unique = unique;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Optional<ItemStack> displayItem = Optional.empty();
        private double chance = 0.0;
        private List<ItemStack> items = Collections.emptyList();
        private List<String> commands = Collections.emptyList();
        private List<String> exclusivePermissions = Collections.emptyList();
        private List<String> inclusivePermissions = Collections.emptyList();
        private List<String> broadcastMessage = Collections.emptyList();
        private List<String> openMessage = Collections.emptyList();
        private boolean constant = false;
        private boolean unique = false;

        public Builder displayItem(Optional<ItemStack> v) { this.displayItem = v; return this; }
        public Builder chance(double v) { this.chance = v; return this; }
        public Builder items(List<ItemStack> v) { this.items = v; return this; }
        public Builder commands(List<String> v) { this.commands = v; return this; }
        public Builder exclusivePermissions(List<String> v) { this.exclusivePermissions = v; return this; }
        public Builder inclusivePermissions(List<String> v) { this.inclusivePermissions = v; return this; }
        public Builder broadcastMessage(List<String> v) { this.broadcastMessage = v; return this; }
        public Builder openMessage(List<String> v) { this.openMessage = v; return this; }
        public Builder constant(boolean v) { this.constant = v; return this; }
        public Builder unique(boolean v) { this.unique = v; return this; }
        public RewardV2 build() {
            return new RewardV2(displayItem, chance, items, commands, exclusivePermissions,
                    inclusivePermissions, broadcastMessage, openMessage, constant, unique);
        }
    }

    public Builder toBuilder() {
        return new Builder().displayItem(displayItem).chance(chance).items(items).commands(commands)
                .exclusivePermissions(exclusivePermissions).inclusivePermissions(inclusivePermissions)
                .broadcastMessage(broadcastMessage).openMessage(openMessage).constant(constant).unique(unique);
    }

    public Optional<ItemStack> getDisplayItem() { return displayItem; }
    public double getChance() { return chance; }
    public List<ItemStack> getItems() { return items; }
    public List<String> getCommands() { return commands; }
    public List<String> getExclusivePermissions() { return exclusivePermissions; }
    public List<String> getInclusivePermissions() { return inclusivePermissions; }
    public List<String> getBroadcastMessage() { return broadcastMessage; }
    public List<String> getOpenMessage() { return openMessage; }
    public boolean isConstant() { return constant; }
    public boolean isUnique() { return unique; }
    public void setDisplayItem(Optional<ItemStack> v) { this.displayItem = v; }
    public void setChance(double v) { this.chance = v; }
    public void setItems(List<ItemStack> v) { this.items = v; }
    public void setCommands(List<String> v) { this.commands = v; }
    public void setExclusivePermissions(List<String> v) { this.exclusivePermissions = v; }
    public void setInclusivePermissions(List<String> v) { this.inclusivePermissions = v; }
    public void setBroadcastMessage(List<String> v) { this.broadcastMessage = v; }
    public void setOpenMessage(List<String> v) { this.openMessage = v; }
    public void setConstant(boolean v) { this.constant = v; }
    public void setUnique(boolean v) { this.unique = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RewardV2)) return false;
        RewardV2 r = (RewardV2) o;
        return Double.compare(r.chance, chance) == 0 && constant == r.constant && unique == r.unique;
    }

    @Override
    public int hashCode() { return Objects.hash(chance, constant, unique); }

    @Override
    public String toString() { return "RewardV2(chance=" + chance + ")"; }
}
