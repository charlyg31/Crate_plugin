package com.hazebyte.crate.cratereloaded.util.format;

import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.model.RewardImpl;
import com.hazebyte.crate.cratereloaded.util.MoreObjects;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class RewardFormat extends Format {

    private final DecimalFormat format;

    public RewardFormat(String message) {
        super(message);
        format = new DecimalFormat(CorePlugin.getPlugin().getSettings().getDecimalFormat());
    }

    @Override
    public String format(Object object) {
        if (object instanceof Reward) {
            return format((Reward) object);
        }
        if (object instanceof List) {
            return format((List) object);
        }
        return message;
    }

    public String format(Reward reward) {
        if (!(reward instanceof RewardImpl)) { // It is a legacy reward.
            return message;
        }

        RewardImpl abstractReward = (RewardImpl) reward;
        if (reward.hasDisplayItem()) {
            message = CustomFormat.format(message, abstractReward.getModel().getDisplayItem());
        } else if (reward.hasItems()) {
            message = CustomFormat.format(message, abstractReward.getItemsNonFormatted());
        }

        if (reward.getParent() != null) {
            double totalChance = reward.getParent().getRewards().stream()
                    .map(r -> r.getChance())
                    .reduce(0.0, (a, b) -> a + b);
            double chance = (reward.getChance() / totalChance) * 100;
            String chanceString = String.format("%s%%", format.format(chance));
            message = message.replace("{chance}", chanceString);
            CrateFormat crateFormat = new CrateFormat(message);
            message = crateFormat.format(reward.getParent());
        }

        message = message.replace("{raw-chance}", format.format(reward.getChance()));
        return message;
    }

    public String format(List<Reward> rewards) {
        Reward firstReward = MoreObjects.firstNonNull(rewards);

        List<ItemStack> displayItems = new ArrayList<>();
        for (Reward reward : rewards) {
            displayItems.add(reward.getDisplayItem());
        }

        message = CustomFormat.format(message, displayItems);
        message = format(firstReward);
        return message;
    }
}
