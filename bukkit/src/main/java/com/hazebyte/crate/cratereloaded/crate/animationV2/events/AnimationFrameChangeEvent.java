package com.hazebyte.crate.cratereloaded.crate.animationV2.events;

import com.hazebyte.crate.cratereloaded.crate.animationV2.Animation;
import com.hazebyte.crate.cratereloaded.menuV2.InventoryV2;
import javax.annotation.Nonnull;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AnimationFrameChangeEvent extends Event {
    private Animation animation;
    private InventoryV2 inventoryV2;
    private int currentFrameIndex;
    private static HandlerList handlerList = new HandlerList();

    public AnimationFrameChangeEvent() {}

    public AnimationFrameChangeEvent(Animation animation, InventoryV2 inventoryV2, int currentFrameIndex) {
        this.animation = animation;
        this.inventoryV2 = inventoryV2;
        this.currentFrameIndex = currentFrameIndex;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Animation animation;
        private InventoryV2 inventoryV2;
        private int currentFrameIndex;

        public Builder animation(Animation v) { this.animation = v; return this; }
        public Builder inventoryV2(InventoryV2 v) { this.inventoryV2 = v; return this; }
        public Builder currentFrameIndex(int v) { this.currentFrameIndex = v; return this; }
        public AnimationFrameChangeEvent build() {
            return new AnimationFrameChangeEvent(animation, inventoryV2, currentFrameIndex);
        }
    }

    public Animation getAnimation() { return animation; }
    public InventoryV2 getInventoryV2() { return inventoryV2; }
    public int getCurrentFrameIndex() { return currentFrameIndex; }
    public void setAnimation(Animation v) { this.animation = v; }
    public void setInventoryV2(InventoryV2 v) { this.inventoryV2 = v; }
    public void setCurrentFrameIndex(int v) { this.currentFrameIndex = v; }

    @Nonnull
    @Override
    public HandlerList getHandlers() { return handlerList; }
    public static HandlerList getHandlerList() { return handlerList; }
}
