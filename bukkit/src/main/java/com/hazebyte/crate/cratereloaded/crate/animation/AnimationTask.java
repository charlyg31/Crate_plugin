package com.hazebyte.crate.cratereloaded.crate.animation;

import com.hazebyte.crate.api.crate.reward.Reward;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class AnimationTask extends BukkitRunnable {
    protected BaseScroller parent;
    protected Inventory inventory;
    protected Player player;
    protected int speed;
    protected int totalTicks;
    protected int timeLapsed;
    protected int iterations;
    protected List<Reward> rewards;
    protected Location location;

    public AnimationTask(BaseScroller parent, Inventory inventory, Player player, int length, Location location) {
        this.parent = parent;
        this.inventory = inventory;
        this.player = player;
        this.speed = 2;
        this.totalTicks = length;
        this.timeLapsed = 0;
        this.iterations = 0;
        this.location = location;
    }

    public AnimationTask(AnimationTask previous) {
        this.parent = previous.parent;
        this.inventory = previous.inventory;
        this.player = previous.player;
        this.speed = previous.speed;
        this.totalTicks = previous.totalTicks;
        this.timeLapsed = previous.timeLapsed;
        this.iterations = previous.iterations;
        this.rewards = previous.rewards;
        this.location = previous.location;
    }

    @Override
    public abstract void run();

    public abstract void update(Player player, Inventory inventory, List<Reward> rewards);

    public void sync() {
        this.timeLapsed += speed;
        // updateInventory() removed - deprecated and broken in Paper 26.x

        //        Messenger.info(timeLapsed + "/" + totalTicks + " " + iterations);
        int index = this.parent.speed.getIndex(this.timeLapsed);
        if (index < 0) {
            this.speed = -1;
        } else {
            this.speed = this.parent.speed.getSpeed(index);
        }
    }

    public boolean shouldStop(int numberOfPrizes) {
        if (timeLapsed >= totalTicks || iterations >= numberOfPrizes) {
            return true;
        }
        this.iterations++;
        return false;
    }
}
