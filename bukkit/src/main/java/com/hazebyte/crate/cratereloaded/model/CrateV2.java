package com.hazebyte.crate.cratereloaded.model;

import com.hazebyte.crate.api.crate.AnimationType;
import com.hazebyte.crate.api.crate.CrateType;
import com.hazebyte.crate.api.crate.EndAnimationType;
import com.hazebyte.crate.api.effect.Category;
import com.hazebyte.crate.cratereloaded.crate.animation.Animation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CrateV2 {
    private String crateName;
    private String uuid;
    private Optional<String> displayName;
    private Optional<ItemStack> displayItem;
    private AnimationType animationType;
    private EndAnimationType endAnimationType;
    @Nullable private Animation animation;
    private CrateType type;
    private ItemStack item;
    private double salePrice;
    private boolean forSale;
    private List<String> openMessage;
    private List<String> broadcastMessage;
    private int previewRows;
    private boolean confirmBeforeUse;
    private ItemStack acceptButton;
    private ItemStack declineButton;
    private int minimumRewards;
    private int maximumRewards;
    private List<RewardV2> rewards;
    private List<RewardV2> constantRewards;
    private List<String> holographicText;
    private Map<Category, List<String>> effectCategoryToId;

    public CrateV2() {
        this.uuid = UUID.randomUUID().toString();
        this.displayName = Optional.empty();
        this.displayItem = Optional.empty();
        this.animationType = AnimationType.NONE;
        this.endAnimationType = EndAnimationType.BLANK;
        this.item = new ItemStack(Material.CHEST);
        this.openMessage = Collections.emptyList();
        this.broadcastMessage = Collections.emptyList();
        this.minimumRewards = 1;
        this.maximumRewards = 1;
        this.rewards = Collections.emptyList();
        this.constantRewards = Collections.emptyList();
        this.holographicText = Collections.emptyList();
        this.effectCategoryToId = new HashMap<>();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String crateName;
        private String uuid = UUID.randomUUID().toString();
        private Optional<String> displayName = Optional.empty();
        private Optional<ItemStack> displayItem = Optional.empty();
        private AnimationType animationType = AnimationType.NONE;
        private EndAnimationType endAnimationType = EndAnimationType.BLANK;
        private Animation animation;
        private CrateType type;
        private ItemStack item = new ItemStack(Material.CHEST);
        private double salePrice = 0;
        private boolean forSale = false;
        private List<String> openMessage = Collections.emptyList();
        private List<String> broadcastMessage = Collections.emptyList();
        private int previewRows;
        private boolean confirmBeforeUse;
        private ItemStack acceptButton;
        private ItemStack declineButton;
        private int minimumRewards = 1;
        private int maximumRewards = 1;
        private List<RewardV2> rewards = Collections.emptyList();
        private List<RewardV2> constantRewards = Collections.emptyList();
        private List<String> holographicText = Collections.emptyList();
        private Map<Category, List<String>> effectCategoryToId = new HashMap<>();

        public Builder crateName(String v) { this.crateName = v; return this; }
        public Builder uuid(String v) { this.uuid = v; return this; }
        public Builder displayName(Optional<String> v) { this.displayName = v; return this; }
        public Builder displayItem(Optional<ItemStack> v) { this.displayItem = v; return this; }
        public Builder animationType(AnimationType v) { this.animationType = v; return this; }
        public Builder endAnimationType(EndAnimationType v) { this.endAnimationType = v; return this; }
        public Builder animation(Animation v) { this.animation = v; return this; }
        public Builder type(CrateType v) { this.type = v; return this; }
        public Builder item(ItemStack v) { this.item = v; return this; }
        public Builder salePrice(double v) { this.salePrice = v; return this; }
        public Builder forSale(boolean v) { this.forSale = v; return this; }
        public Builder openMessage(List<String> v) { this.openMessage = v; return this; }
        public Builder broadcastMessage(List<String> v) { this.broadcastMessage = v; return this; }
        public Builder previewRows(int v) { this.previewRows = v; return this; }
        public Builder confirmBeforeUse(boolean v) { this.confirmBeforeUse = v; return this; }
        public Builder acceptButton(ItemStack v) { this.acceptButton = v; return this; }
        public Builder declineButton(ItemStack v) { this.declineButton = v; return this; }
        public Builder minimumRewards(int v) { this.minimumRewards = v; return this; }
        public Builder maximumRewards(int v) { this.maximumRewards = v; return this; }
        public Builder rewards(List<RewardV2> v) { this.rewards = v; return this; }
        public Builder constantRewards(List<RewardV2> v) { this.constantRewards = v; return this; }
        public Builder holographicText(List<String> v) { this.holographicText = v; return this; }
        public Builder effectCategoryToId(Map<Category, List<String>> v) { this.effectCategoryToId = v; return this; }

        public CrateV2 build() {
            CrateV2 c = new CrateV2();
            c.crateName = crateName; c.uuid = uuid; c.displayName = displayName;
            c.displayItem = displayItem; c.animationType = animationType;
            c.endAnimationType = endAnimationType; c.animation = animation;
            c.type = type; c.item = item; c.salePrice = salePrice; c.forSale = forSale;
            c.openMessage = openMessage; c.broadcastMessage = broadcastMessage;
            c.previewRows = previewRows; c.confirmBeforeUse = confirmBeforeUse;
            c.acceptButton = acceptButton; c.declineButton = declineButton;
            c.minimumRewards = minimumRewards; c.maximumRewards = maximumRewards;
            c.rewards = rewards; c.constantRewards = constantRewards;
            c.holographicText = holographicText; c.effectCategoryToId = effectCategoryToId;
            return c;
        }
    }

    public Builder toBuilder() {
        return new Builder().crateName(crateName).uuid(uuid).displayName(displayName)
                .displayItem(displayItem).animationType(animationType).endAnimationType(endAnimationType)
                .animation(animation).type(type).item(item).salePrice(salePrice).forSale(forSale)
                .openMessage(openMessage).broadcastMessage(broadcastMessage).previewRows(previewRows)
                .confirmBeforeUse(confirmBeforeUse).acceptButton(acceptButton).declineButton(declineButton)
                .minimumRewards(minimumRewards).maximumRewards(maximumRewards).rewards(rewards)
                .constantRewards(constantRewards).holographicText(holographicText)
                .effectCategoryToId(effectCategoryToId);
    }

    public String getCrateName() { return crateName; }
    public String getUuid() { return uuid; }
    public Optional<String> getDisplayName() { return displayName; }
    public Optional<ItemStack> getDisplayItem() { return displayItem; }
    public AnimationType getAnimationType() { return animationType; }
    public EndAnimationType getEndAnimationType() { return endAnimationType; }
    public Animation getAnimation() { return animation; }
    public CrateType getType() { return type; }
    public ItemStack getItem() { return item; }
    public double getSalePrice() { return salePrice; }
    public boolean isForSale() { return forSale; }
    public List<String> getOpenMessage() { return openMessage; }
    public List<String> getBroadcastMessage() { return broadcastMessage; }
    public int getPreviewRows() { return previewRows; }
    public boolean isConfirmBeforeUse() { return confirmBeforeUse; }
    public ItemStack getAcceptButton() { return acceptButton; }
    public ItemStack getDeclineButton() { return declineButton; }
    public int getMinimumRewards() { return minimumRewards; }
    public int getMaximumRewards() { return maximumRewards; }
    public List<RewardV2> getRewards() { return rewards; }
    public List<RewardV2> getConstantRewards() { return constantRewards; }
    public List<String> getHolographicText() { return holographicText; }
    public Map<Category, List<String>> getEffectCategoryToId() { return effectCategoryToId; }
    public void setCrateName(String v) { this.crateName = v; }
    public void setUuid(String v) { this.uuid = v; }
    public void setDisplayName(Optional<String> v) { this.displayName = v; }
    public void setDisplayItem(Optional<ItemStack> v) { this.displayItem = v; }
    public void setAnimationType(AnimationType v) { this.animationType = v; }
    public void setEndAnimationType(EndAnimationType v) { this.endAnimationType = v; }
    public void setAnimation(Animation v) { this.animation = v; }
    public void setType(CrateType v) { this.type = v; }
    public void setItem(ItemStack v) { this.item = v; }
    public void setSalePrice(double v) { this.salePrice = v; }
    public void setForSale(boolean v) { this.forSale = v; }
    public void setOpenMessage(List<String> v) { this.openMessage = v; }
    public void setBroadcastMessage(List<String> v) { this.broadcastMessage = v; }
    public void setPreviewRows(int v) { this.previewRows = v; }
    public void setConfirmBeforeUse(boolean v) { this.confirmBeforeUse = v; }
    public void setAcceptButton(ItemStack v) { this.acceptButton = v; }
    public void setDeclineButton(ItemStack v) { this.declineButton = v; }
    public void setMinimumRewards(int v) { this.minimumRewards = v; }
    public void setMaximumRewards(int v) { this.maximumRewards = v; }
    public void setRewards(List<RewardV2> v) { this.rewards = v; }
    public void setConstantRewards(List<RewardV2> v) { this.constantRewards = v; }
    public void setHolographicText(List<String> v) { this.holographicText = v; }
    public void setEffectCategoryToId(Map<Category, List<String>> v) { this.effectCategoryToId = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrateV2)) return false;
        return Objects.equals(uuid, ((CrateV2) o).uuid);
    }

    @Override
    public int hashCode() { return Objects.hash(uuid); }

    @Override
    public String toString() { return "CrateV2(crateName=" + crateName + ")"; }
}
