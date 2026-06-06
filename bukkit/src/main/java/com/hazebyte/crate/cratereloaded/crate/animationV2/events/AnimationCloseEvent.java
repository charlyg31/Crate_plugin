package com.hazebyte.crate.cratereloaded.crate.animationV2.events;

import com.hazebyte.crate.cratereloaded.crate.animationV2.Animation;
import com.hazebyte.crate.cratereloaded.menuV2.InventoryV2;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AnimationCloseEvent extends Event {
    private Animation animation;
    private InventoryV2 inventoryV2;
    private Boolean isCompleted;
    private static HandlerList handlerList = new HandlerList();

    public AnimationCloseEvent() {}

    public AnimationCloseEvent(Animation animation, InventoryV2 inventoryV2, Boolean isCompleted) {
        this.animation = animation;
        this.inventoryV2 = inventoryV2;
        this.isCompleted = isCompleted;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Animation animation;
        private InventoryV2 inventoryV2;
        private Boolean isCompleted;

        public Builder animation(Animation v) { this.animation = v; return this; }
        public Builder inventoryV2(InventoryV2 v) { this.inventoryV2 = v; return this; }
        public Builder isCompleted(Boolean v) { this.isCompleted = v; return this; }
        public AnimationCloseEvent build() {
            return new AnimationCloseEvent(animation, inventoryV2, isCompleted);
        }
    }

    public Animation getAnimation() { return animation; }
    public InventoryV2 getInventoryV2() { return inventoryV2; }
    public Boolean getIsCompleted() { return isCompleted; }
    public void setAnimation(Animation v) { this.animation = v; }
    public void setInventoryV2(InventoryV2 v) { this.inventoryV2 = v; }
    public void setIsCompleted(Boolean v) { this.isCompleted = v; }

    @Override
    public HandlerList getHandlers() { return handlerList; }
    public static HandlerList getHandlerList() { return handlerList; }
}
