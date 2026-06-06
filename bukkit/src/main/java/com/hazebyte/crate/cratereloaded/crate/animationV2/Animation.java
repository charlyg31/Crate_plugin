package com.hazebyte.crate.cratereloaded.crate.animationV2;

import com.hazebyte.crate.cratereloaded.model.CrateV2;
import com.hazebyte.crate.cratereloaded.model.RewardV2;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Player;

public class Animation {
    private CrateV2 crateV2;
    private Player player;
    private List<RewardV2> winningRewards;
    private List<AnimationFrame> frames;

    public Animation() {
        this.winningRewards = new ArrayList<>();
        this.frames = new ArrayList<>();
    }

    public Animation(CrateV2 crateV2, Player player, List<RewardV2> winningRewards, List<AnimationFrame> frames) {
        this.crateV2 = crateV2;
        this.player = player;
        this.winningRewards = winningRewards != null ? winningRewards : new ArrayList<>();
        this.frames = frames != null ? frames : new ArrayList<>();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private CrateV2 crateV2;
        private Player player;
        private List<RewardV2> winningRewards = new ArrayList<>();
        private List<AnimationFrame> frames = new ArrayList<>();

        public Builder crateV2(CrateV2 v) { this.crateV2 = v; return this; }
        public Builder player(Player v) { this.player = v; return this; }
        public Builder winningReward(RewardV2 v) { this.winningRewards.add(v); return this; }
        public Builder winningRewards(List<RewardV2> v) { this.winningRewards = v; return this; }
        public Builder frame(AnimationFrame v) { this.frames.add(v); return this; }
        public Builder frames(List<AnimationFrame> v) { this.frames = v; return this; }
        public Animation build() { return new Animation(crateV2, player, winningRewards, frames); }
    }

    public CrateV2 getCrateV2() { return crateV2; }
    public Player getPlayer() { return player; }
    public List<RewardV2> getWinningRewards() { return winningRewards; }
    public List<AnimationFrame> getFrames() { return frames; }
    public void setCrateV2(CrateV2 v) { this.crateV2 = v; }
    public void setPlayer(Player v) { this.player = v; }
    public void setWinningRewards(List<RewardV2> v) { this.winningRewards = v; }
    public void setFrames(List<AnimationFrame> v) { this.frames = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animation)) return false;
        Animation a = (Animation) o;
        return Objects.equals(crateV2, a.crateV2) && Objects.equals(player, a.player);
    }

    @Override
    public int hashCode() { return Objects.hash(crateV2, player); }
    @Override
    public String toString() { return "Animation(crateV2=" + crateV2 + ")"; }
}
