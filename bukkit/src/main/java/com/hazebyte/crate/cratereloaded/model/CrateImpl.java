package com.hazebyte.crate.cratereloaded.model;

import com.google.common.base.Strings;
import com.hazebyte.crate.api.CrateAPI;
import com.hazebyte.crate.api.crate.AnimationType;
import com.hazebyte.crate.api.crate.Crate;
import com.hazebyte.crate.api.crate.CrateType;
import com.hazebyte.crate.api.crate.EndAnimationType;
import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.api.effect.Category;
import com.hazebyte.crate.api.event.CrateRewardEvent;
import com.hazebyte.crate.api.util.ItemBuilder;
import com.hazebyte.crate.api.util.Messenger;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.component.PluginSettingComponent;
import com.hazebyte.crate.cratereloaded.crate.animation.Animation;
import com.hazebyte.crate.cratereloaded.crate.animation.end.BlankEnding;
import com.hazebyte.crate.cratereloaded.crate.animation.end.BlankWheelEnding;
import com.hazebyte.crate.cratereloaded.crate.animation.end.RandomEnding;
import com.hazebyte.crate.cratereloaded.crate.animation.end.RandomWheelEnding;
import com.hazebyte.crate.cratereloaded.crate.animation.scroller.Csgo;
import com.hazebyte.crate.cratereloaded.crate.animation.scroller.Heart;
import com.hazebyte.crate.cratereloaded.crate.animation.scroller.Rectangle;
import com.hazebyte.crate.cratereloaded.crate.animation.scroller.ReverseCsgo;
import com.hazebyte.crate.cratereloaded.crate.animation.scroller.ReverseRectangle;
import com.hazebyte.crate.cratereloaded.crate.animation.scroller.Roulette;
import com.hazebyte.crate.cratereloaded.provider.effect.EffectWrapper;
import com.hazebyte.crate.cratereloaded.serialization.CrateSerialization;
import com.hazebyte.crate.cratereloaded.util.ConfigConstants;
import com.hazebyte.crate.cratereloaded.util.LocationUtil;
import com.hazebyte.crate.cratereloaded.util.PlayerUtil;
import com.hazebyte.crate.cratereloaded.util.StringUtils;
import com.hazebyte.crate.cratereloaded.util.format.CustomFormat;
import com.hazebyte.crate.cratereloaded.util.format.ItemFormatter;
import com.hazebyte.crate.cratereloaded.util.item.ItemUtil;
import com.hazebyte.util.Mat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CrateImpl implements Crate {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("CrateImpl");


    /** Maximum number of slots in an inventory (9 columns × 6 rows) */
    private static final int MAX_INVENTORY_SLOTS = 54;

    /** Delimiter for crate UUID generation */
    private static final String UUID_DELIMITER = ":";

    // The name of the crate
    protected final String name;

    // A unique identifier for the crate
    protected final String uuid;

    // The display name for the crate
    protected String displayName;

    // The item to display
    protected ItemStack displayItem;

    // TODO: Update Animation Lib
    protected AnimationType animationType;
    protected EndAnimationType endAnimationType;
    protected Animation animation;

    // The type of crate
    protected final CrateType type;

    // The crate's itemstack
    protected ItemStack item;

    // The cost of a crate
    protected double cost;

    // Whether this crate is for sale
    protected boolean purchaseable;

    private List<String> openMessage;
    private List<String> broadcastMessage;

    // Whether is crate is previewable
    private boolean previewable;

    // The number of previewable rows
    private int previewRows = 0;

    // Whether this crate should confirm before the player use
    private boolean confirmBeforeUse;

    // TODO: https://github.com/imWillX/CrateReloaded/issues/29
    private ItemStack acceptButton;

    // TODO: https://github.com/imWillX/CrateReloaded/issues/29
    private ItemStack declineButton;

    // The minimum number of rewards this crate has
    protected int minimumRewards;

    // The maximum number of rewards this crate has
    protected int maximumRewards;

    // The crate's list of rewards
    protected List<Reward> rewards;

    // The crate's list of rewards that are always given
    protected List<Reward> constantRewards;

    // The list of strings of the holographic text
    private List<String> holographicText;

    private final Map<Category, List<String>> effectCategoryToId = new HashMap<>();

    public CrateImpl(String name, CrateType type) {
        this.name = name;
        this.uuid = name + UUID_DELIMITER + type.name();
        this.type = type;
        this.minimumRewards = 1;
        this.maximumRewards = 1;
        this.rewards = new ArrayList<>();
        this.openMessage = new ArrayList<>();
        this.broadcastMessage = new ArrayList<>();
        this.constantRewards = new ArrayList<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            CrateImpl other = (CrateImpl) obj;
            return this.uuid.equals(other.uuid);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        CrateSerialization serializer = new CrateSerialization();
        return serializer.serialize(this);
    }

    @Override
    public String getCrateName() {
        return name;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getDisplayName() {
        if (displayName == null) {
            return name;
        }
        return displayName;
    }

    @Override
    public ItemStack getDisplayItem() {
        if (displayItem == null) {
            displayItem = this.getItem();
        }
        return displayItem;
    }

    @Override
    public boolean hasDisplayName() {
        return displayName != null;
    }

    @Override
    public boolean hasDisplayItem() {
        return displayItem != null;
    }

    @Override
    public CrateType getType() {
        return type;
    }

    @Override
    public ItemStack getItem() {
        return this.item;
    }

    @Override
    public double getCost() {
        if (this.cost < 0) {
            this.cost = 0;
        }
        return this.cost;
    }

    @Override
    public void setCost(double cost) throws IllegalArgumentException {
        if (cost < 0) {
            throw new IllegalArgumentException("Amount should be greater than or equal to zero.");
        }
        this.cost = cost;
    }

    @Override
    public AnimationType getAnimationType() {
        if (this.animationType == null) {
            this.animationType = AnimationType.NONE;
        }
        return this.animationType;
    }

    @Override
    public EndAnimationType getEndAnimationType() {
        if (this.endAnimationType == null) {
            this.endAnimationType = EndAnimationType.BLANK;
        }
        return this.endAnimationType;
    }

    @Override
    public void setEndAnimationType(EndAnimationType type) {
        this.endAnimationType = type;
        if (this.endAnimationType == null) {
            this.endAnimationType = EndAnimationType.BLANK;
        }
        if (animation != null) {
            PluginSettingComponent settings = CorePlugin.getJavaPluginComponent().getPluginSettingComponent();
            switch (this.animationType) {
                case RECTANGLE:
                case RECTANGLE_REVERSE:
                case HEART:
                    switch (endAnimationType) {
                        case BLANK:
                            this.getAnimation().setEnding(new BlankWheelEnding(this, settings));
                            return;
                        case RANDOM:
                            this.getAnimation().setEnding(new RandomWheelEnding(this, settings));
                            return;
                    }
                default:
                    switch (endAnimationType) {
                        case BLANK:
                            this.getAnimation().setEnding(new BlankEnding(this, settings));
                            return;
                        case RANDOM:
                            this.getAnimation().setEnding(new RandomEnding(this, settings));
                            return;
                    }
                    break;
            }
        }
    }

    @Override
    public void addReward(Reward reward) {
        reward.setParent(this);
        if (reward.isConstant()) {
            this.getConstantRewards().add(reward);
        } else {
            this.getRewards().add(reward);
        }
    }

    @Override
    public boolean removeReward(Reward reward) {
        if (this.rewards == null) {
            this.rewards = new ArrayList<>();
            return false;
        }
        return this.rewards.remove(reward);
    }

    @Override
    public void setAnimationType(AnimationType type) {
        if (type == null) {
            return;
        }

        this.animationType = type;
        Animation animation = CorePlugin.getJavaPluginComponent()
                .getAnimationFactoryComponent()
                .createAnimation(type, this);
        setAnimation(animation);
    }

    @Override
    public void setItem(ItemStack item) {
        Material chestMaterial = Mat.CHEST.toMaterial();

        // TODO: Allow AIR Type
        // If the item is invalid, direct the user to set it up.
        if (ItemUtil.isNull(item)) {
            item = new ItemStack(chestMaterial);
            ItemUtil.setName(item, String.format("&r&a&lCrate: &e%s", this.getCrateName()));
            ItemUtil.setLore(
                    item,
                    Arrays.asList(
                            "&r&fPlease set an crate item.",
                            String.format("&r&6Section: &f%s", this.getCrateName() + ".item"),
                            "",
                            "&r&4This crate is not setup correctly!"));
        }

        // If this is a supply crate and it's material is not a chest, keep attributes & set it to a
        // chest.
        if ((this.getType() == CrateType.SUPPLY) && item.getType() != chestMaterial) {
            item = ItemBuilder.of(item).type(chestMaterial).asItemStack();
        }
        this.item = item;
    }

    @Override
    public boolean is(ItemStack item) {
        return ItemUtil.compare(item, getItem());
    }

    @Override
    public boolean isBuyable() {
        if (this.getCost() < 0) {
            // Defensive fallback - primary validation happens at parse-time in YamlCrateV2ParserImpl
            this.purchaseable = false;
        }
        return purchaseable;
    }

    public void setBuyable(boolean bool) {
        this.purchaseable = bool;
    }

    @Override
    public boolean isPreviewable() {
        return previewable;
    }

    @Override
    public boolean hasConfirmationToggle() {
        return confirmBeforeUse;
    }

    /**
     * @deprecated this is used for mapstruct. Remove when we deprecate v2
     */
    public boolean isConfirmBeforeUse() {
        return confirmBeforeUse;
    }

    @Override
    public void setConfirmationToggle(boolean bool) {
        this.confirmBeforeUse = bool;
    }

    public void setPreviewable(boolean bool) {
        this.previewable = bool;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    @Override
    public void setDisplayItem(ItemStack item) {
        if (item != null) {
            ItemFormatter.format(item, this);
            this.displayItem = item;
        }
    }

    @Override
    public void setDisplayName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            name = "";
        }
        this.displayName = CustomFormat.format(name);
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public void setMinimumRewards(int min) {
        this.minimumRewards = min;
    }

    public void setPreviewRows(int previewRows) {
        this.previewRows = previewRows;
    }

    @Override
    public String toString() {
        return this.uuid;
    }

    @Override
    public List<Reward> getRewards() {
        if (this.rewards == null) {
            this.rewards = new ArrayList<>();
        }
        return this.rewards;
    }

    @Override
    public List<Reward> getConstantRewards() {
        if (this.constantRewards == null) {
            this.constantRewards = new ArrayList<>();
        }
        return this.constantRewards;
    }

    @Override
    public void setAcceptButton(ItemStack acceptButton) {
        if (acceptButton != null) {
            ItemFormatter.format(acceptButton, this);
            this.acceptButton = acceptButton;
        }
    }

    @Override
    public void setDeclineButton(ItemStack declineButton) {
        if (declineButton != null) {
            ItemFormatter.format(declineButton, this);
            this.declineButton = declineButton;
        }
    }

    @Override
    public ItemStack getAcceptButton() {
        if (this.acceptButton == null) {
            this.acceptButton = CorePlugin.getPlugin().getSettings().getClaimAcceptButton();
        }
        return this.acceptButton;
    }

    @Override
    public ItemStack getDeclineButton() {
        if (this.declineButton == null) {
            this.declineButton = CorePlugin.getPlugin().getSettings().getClaimDeclineButton();
        }
        return this.declineButton;
    }

    @Override
    public int getMinimumRewards() {
        if (minimumRewards <= 1) {
            this.minimumRewards = 1;
        }
        if (this.minimumRewards > this.maximumRewards) {
            this.minimumRewards = maximumRewards;
        }
        return minimumRewards;
    }

    public void setMaximumRewards(int max) {
        this.maximumRewards = max;
    }

    @Override
    public int getMaximumRewards() {
        return maximumRewards;
    }

    @Override
    public int getPreviewRows() {
        return getPreviewSlots() / 9;
    }

    @Override
    public int getPreviewSlots() {
        // Each row has 9 slots. slots = row * 9.
        int slots = previewRows * 9;

        // size is number of normal + constant rewards
        int size = this.getRewards().size() + this.getConstantRewards().size();

        // The number of slots should hold the number of rewards
        slots = Math.max(slots, size);

        // The number of slots should be at max the number of rewards
        slots = Math.min(slots, MAX_INVENTORY_SLOTS);

        return slots;
    }

    @Override
    public List<String> getHolographicText() {
        return CustomFormat.format(this.holographicText, this);
    }

    @Override
    public void setHolographicText(List<String> text) {
        this.holographicText = text;
    }

    public List<EffectWrapper> getEffects(Category category) {
        List<EffectWrapper> effects = new ArrayList<>();
        if (!effectCategoryToId.containsKey(category)) {
            return effects;
        }
        List<String> effectKeys = effectCategoryToId.get(category);
        for (String effectKey : effectKeys) {
            String id = ConfigConstants.generateCrateEffectKey(this, category, effectKey);
            Optional<ConfigurationSection> optional = CorePlugin.getJavaPluginComponent()
                    .getEffectServiceComponent()
                    .getEffectConfiguration(id);
            if (!optional.isPresent()) {
                continue;
            }

            Optional<? extends EffectWrapper> wrapper = CorePlugin.getJavaPluginComponent()
                    .getEffectServiceComponent()
                    .createEffect(optional.get());
            if (!wrapper.isPresent()) {
                continue;
            }

            effects.add(wrapper.get());
        }
        return effects;
    }

    public List<String> getOpenMessage() {
        return openMessage;
    }

    public List<String> getBroadcastMessage() {
        return broadcastMessage;
    }

    @Override
    public boolean isPlaceable() {
        return this.getType() == CrateType.SUPPLY;
    }

    @Override
    public void setOpenMessage(List<String> open) {
        this.openMessage = open;
    }

    @Override
    public void setBroadcastMessage(List<String> broadcast) {
        this.broadcastMessage = broadcast;
    }

    @Override
    public void runEffect(Location location, Category category) {
        runEffect(location, category, null);
    }

    @Override
    public void runEffect(Location location, Category category, Player player) {
        if (category == Category.PERSISTENT) {
            return;
        }

        List<EffectWrapper> effects = this.getEffects(category);
        for (EffectWrapper effectWrapper : effects) {
            if (effectWrapper.isPersistent()) {
                continue;
            }
            effectWrapper.setLocation(LocationUtil.getBlockCenter(location));
            effectWrapper.setTargetPlayer(player);
            effectWrapper.start();
        }
    }

    public void recordPrize(Player player, List<Reward> rewards) {
        if (rewards == null) {
            return;
        }

        StringBuilder log =
                new StringBuilder(String.format("Player: %s, Crate: %s, Rewards: [\n", player.getName(), this.name));
        for (Reward reward : rewards) {
            log.append(String.format("    %s\n", reward.toString()));
        }
        log.append("]");

        CorePlugin.getPlugin().getLogger().fine(log.toString());
    }

    @Override
    public void onRewards(Player player, List<Reward> rewards) {
        this.onRewards(player, rewards, player.getLocation());
    }

    @Override
    public void onRewards(Player player, List<Reward> rewards, Location location) {
        this.onRewards(player, rewards, player.getLocation(), null);
    }

    @Override
    public void onRewards(Player player, List<Reward> rewards, Location location, Consumer consumer) {
        // These variables keep track of messaging.
        // claimed is used for when a reward has been claimed
        // emptyInv is used if the user had an empty inventory
        // filled is used if the inventory has been filled
        boolean claimed = false, emptyInv = false, filled = false;

        CrateRewardEvent event = new CrateRewardEvent(this, player, location, rewards);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            // todo Log that it was cancelled
            return;
        }

        if (consumer != null) {
            consumer.accept(rewards);
        }
        runMessage(player, rewards);
        runEffect(location, Category.REWARD, player);

        for (Reward reward : rewards) {
            if (PlayerUtil.getSlotsLeft(player) <= 0) {
                // If we have a full inventory, this means that reward#onWin will have added a new claim
                // We want to send a message if this is true
                if (CorePlugin.getPlugin().getSettings().isHandlingClaims()) claimed = true;

                // The inventory is filled
                filled = true;
            } else {
                // There is an occurence of an empty inventory
                emptyInv = true;
            }
            if (CorePlugin.getPlugin().getSettings().isHandlingClaims() && PlayerUtil.getSlotsLeft(player) <= 0) {
                claimed = true;
            }

            CorePlugin.getJavaPluginComponent().getOpenCrateComponent().executeReward(player, reward);
        }
        // If we claim something, send the message once
        if (claimed) {
            String message = CrateAPI.getMessage("core.claim_inventory_full");
            Messenger.tell(player, message);
        } else if (filled && emptyInv) {
            // TODO: If it was previously empty but filled
            //            String message = "TODO";
            //            Messenger.tell(player, message);
        }
        try {
            this.recordPrize(player, rewards);
        } catch (Exception e) {
            CorePlugin.getPlugin()
                    .getLogger()
                    .log(
                            java.util.logging.Level.WARNING,
                            String.format("Unable to log the winning prize for %s", player.getDisplayName()),
                            e);
        }
    }

    private List<String> formatMessage(Player player, List<String> messages, List<Reward> rewardList) {
        Object[] objects = {player, rewardList, this};
        List<String> msgs = new ArrayList<>();
        List<List> lists = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        List<Reward> rewards = new ArrayList<>();

        // Keep a reference of the lists
        for (Object object : objects) {
            if (object instanceof List) {
                List list = (List) object;

                if (list.size() <= 0) continue;

                Object generic = list.get(0);
                if (generic instanceof ItemStack) {
                    items.addAll((List<? extends ItemStack>) list);
                } else if (generic instanceof Reward) {
                    items.addAll((List<? extends ItemStack>) list);
                }
            } else if (object instanceof ItemStack) {
                items.add((ItemStack) object);
            } else if (object instanceof Reward) {
                rewards.add((Reward) object);
            }
        }

        if (items.size() > 0) lists.add(items);
        if (rewards.size() > 0) lists.add(rewards);

        for (String message : messages) {
            for (List list : lists) {
                message = CustomFormat.format(message, list);
            }
            for (Object object : objects) { // Objects aren't spliced so each one must be iterated through separately.
                message = CustomFormat.format(message, player, object);
            }
            msgs.add(message);
        }
        return msgs;
    }

    public void runMessage(Player player, List<Reward> rewards) {
        List<String> openMessages = formatMessage(player, getOpenMessage(), rewards);
        List<String> broadcastMessages = formatMessage(player, getBroadcastMessage(), rewards);

        log.finer(String.format(
                "Player [%s] crate open message, [%s], and broadcast message, [%s]",
                player.getName(), openMessages, broadcastMessages));
        broadcastMessages.stream()
                .filter(s -> !Strings.isNullOrEmpty(s))
                .map(s -> StringUtils.formatString(player, s))
                .forEach(Messenger::broadcast);

        openMessages.stream()
                .filter(s -> !Strings.isNullOrEmpty(s))
                .map(s -> StringUtils.formatString(player, s))
                .forEach(s -> Messenger.tell((CommandSender) player, s));
    }

    public Map<Category, List<String>> getEffectCategoryToId() {
        return effectCategoryToId;
    }

    public CrateV2 toCrateV2() {
        return CorePlugin.CRATE_MAPPER.fromImplementation(this);
    }
}
