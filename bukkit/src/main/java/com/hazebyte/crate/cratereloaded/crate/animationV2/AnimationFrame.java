package com.hazebyte.crate.cratereloaded.crate.animationV2;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public class AnimationFrame {
    private Map<Integer, ItemStack> itemMappings;
    private long frameLength;

    public AnimationFrame() { this.itemMappings = new HashMap<>(); }

    public AnimationFrame(Map<Integer, ItemStack> itemMappings, long frameLength) {
        this.itemMappings = itemMappings != null ? itemMappings : new HashMap<>();
        this.frameLength = frameLength;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Map<Integer, ItemStack> itemMappings = new HashMap<>();
        private long frameLength;

        public Builder itemMappings(Map<Integer, ItemStack> v) { this.itemMappings = v; return this; }
        public Builder itemMapping(int slot, ItemStack item) { this.itemMappings.put(slot, item); return this; }
        public Builder frameLength(long v) { this.frameLength = v; return this; }
        public AnimationFrame build() { return new AnimationFrame(itemMappings, frameLength); }
    }

    public Map<Integer, ItemStack> getItemMappings() { return itemMappings; }
    public long getFrameLength() { return frameLength; }
    public void setItemMappings(Map<Integer, ItemStack> v) { this.itemMappings = v; }
    public void setFrameLength(long v) { this.frameLength = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnimationFrame)) return false;
        AnimationFrame f = (AnimationFrame) o;
        return frameLength == f.frameLength && Objects.equals(itemMappings, f.itemMappings);
    }

    @Override
    public int hashCode() { return Objects.hash(frameLength); }
    @Override
    public String toString() { return "AnimationFrame(frameLength=" + frameLength + ")"; }
}
