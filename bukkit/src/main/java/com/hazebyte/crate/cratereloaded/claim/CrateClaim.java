package com.hazebyte.crate.cratereloaded.claim;

import com.hazebyte.crate.api.claim.Claim;
import com.hazebyte.crate.api.crate.reward.Reward;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.OfflinePlayer;

public class CrateClaim implements Claim, Cloneable {
    protected UUID id;
    protected List<Reward> rewards;
    protected OfflinePlayer owner;
    protected Long timestamp;
    protected Function<Claim, Boolean> executor;

    public CrateClaim(UUID id, List<Reward> rewards, OfflinePlayer owner, Long timestamp, Function<Claim, Boolean> executor) {
        this.id = id != null ? id : UUID.randomUUID();
        this.rewards = rewards;
        this.owner = owner;
        this.timestamp = timestamp != null ? timestamp : System.currentTimeMillis();
        this.executor = executor != null ? executor : claim -> true;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id = UUID.randomUUID();
        private List<Reward> rewards;
        private OfflinePlayer owner;
        private Long timestamp = System.currentTimeMillis();
        private Function<Claim, Boolean> executor = claim -> true;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder rewards(List<Reward> rewards) { this.rewards = rewards; return this; }
        public Builder owner(OfflinePlayer owner) { this.owner = owner; return this; }
        public Builder timestamp(Long timestamp) { this.timestamp = timestamp; return this; }
        public Builder executor(Function<Claim, Boolean> executor) { this.executor = executor; return this; }
        public CrateClaim build() { return new CrateClaim(id, rewards, owner, timestamp, executor); }
    }

    public Builder toBuilder() {
        return new Builder().id(id).rewards(rewards).owner(owner).timestamp(timestamp).executor(executor);
    }

    public UUID getId() { return id; }
    public List<Reward> getRewards() { return rewards; }
    public OfflinePlayer getOwner() { return owner; }
    public Long getTimestamp() { return timestamp; }
    public void setRewards(List<Reward> rewards) { this.rewards = rewards; }
    public void setExecutor(java.util.function.Function<com.hazebyte.crate.api.claim.Claim, Boolean> executor) { this.executor = executor; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean execute() { return this.executor.apply(this); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrateClaim that = (CrateClaim) o;
        return timestamp.equals(that.timestamp) && Objects.equals(id, that.id) && Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode() { return Objects.hash(id, owner, timestamp); }

    @Override
    protected CrateClaim clone() { return this.toBuilder().build(); }
}
