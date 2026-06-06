package com.hazebyte.crate.cratereloaded.crate.animationV2;

import static com.hazebyte.crate.cratereloaded.util.InventoryConstants.getValidatedTitle;

import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.crate.animationV2.events.AnimationCloseEvent;
import com.hazebyte.crate.cratereloaded.crate.animationV2.events.AnimationFrameChangeEvent;
import com.hazebyte.crate.cratereloaded.menuV2.InventoryButtonV2;
import com.hazebyte.crate.cratereloaded.menuV2.InventoryV2;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

class AnimationExecution extends BukkitRunnable {

    
    private final Animation animation;

    private final long RUNNABLE_PERIOD = 1L;

    private InventoryV2 inventoryV2;
    private int currentFrameIndex = 0;
    private long elapsedTimeInTicks = 0L;
    private long lastFrameChangeInTick = 0L;

    public AnimationExecution(Animation animation) {
        this.animation = animation;

        String name = animation.getCrateV2().getDisplayName().orElse("Crate Animation");
        this.inventoryV2 = InventoryV2.builder()
                .title(getValidatedTitle(name))
                .inventorySize(27)
                .build();

        CorePlugin.getJavaPluginComponent()
                .getInventoryManager()
                .openInventory(this.inventoryV2, animation.getPlayer());
    }

    @Override
    public void run() {
        syncElapsedTime();
        cancelIfFinished();
        updateFramesAndSyncTiming();
    }

    @Override
    public void cancel() {
        throw new RuntimeException("Incorrect call to cancel animation");
    }

    public void cancelAnimation(boolean isCompleted) {
        super.cancel();
        callAnimationCloseEvent(isCompleted);
        if (animation.getPlayer().isOnline()
                && CorePlugin.getJavaPluginComponent()
                        .getInventoryManager()
                        .getInventory(inventoryV2)
                        .isPresent()
                && CorePlugin.getJavaPluginComponent()
                                .getInventoryManager()
                                .getInventory(inventoryV2)
                                .get()
                                .getViewers()
                                .size()
                        > 0) {
            animation.getPlayer().closeInventory();
        }
    }

    private void cancelIfFinished() {
        if (isFinished() || !animation.getPlayer().isOnline()) {
            cancelAnimation(true);
        }
    }

    private boolean isFinished() {
        return currentFrameIndex >= animation.getFrames().size() && isReadyForFrameChange();
    }

    private boolean isReadyForFrameChange() {
        if (currentFrameIndex == 0) { // display first frame immediately.
            return true;
        }

        AnimationFrame animationFrame = animation.getFrames().get(currentFrameIndex - 1);
        return elapsedTimeInTicks - animationFrame.getFrameLength() >= lastFrameChangeInTick;
    }

    private void syncElapsedTime() {
        elapsedTimeInTicks += RUNNABLE_PERIOD;
    }

    private void updateFramesAndSyncTiming() {
        if (isFinished() || !isReadyForFrameChange()) {
            return;
        }

        AnimationFrame animationFrame = animation.getFrames().get(currentFrameIndex);
        for (int keyIndex : animationFrame.getItemMappings().keySet()) {
            ItemStack itemStack = animationFrame.getItemMappings().get(keyIndex);
            inventoryV2.setButton(
                    keyIndex,
                    InventoryButtonV2.builder().itemCreator(player -> itemStack).build());
        }
        currentFrameIndex++;
        lastFrameChangeInTick = elapsedTimeInTicks;
        callAnimationFrameChangeEvent();
    }

    private void callAnimationCloseEvent(boolean isCompleted) {
        AnimationCloseEvent closeEvent = AnimationCloseEvent.builder()
                .animation(animation)
                .inventoryV2(inventoryV2)
                .isCompleted(isCompleted)
                .build();
        Bukkit.getPluginManager().callEvent(closeEvent);
    }

    private void callAnimationFrameChangeEvent() {
        AnimationFrameChangeEvent frameChangeEvent = AnimationFrameChangeEvent.builder()
                .animation(animation)
                .inventoryV2(inventoryV2)
                .currentFrameIndex(currentFrameIndex)
                .build();
        Bukkit.getPluginManager().callEvent(frameChangeEvent);
    }

    public InventoryV2 getInventoryV2() {
        return inventoryV2;
    }
}
